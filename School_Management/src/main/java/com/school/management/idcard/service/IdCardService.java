package com.school.management.idcard.service;

import com.itextpdf.barcodes.Barcode128;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import com.school.management.academic.entity.ClassGrade;
import com.school.management.academic.repository.ClassGradeRepository;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Generates student ID cards as colourful portrait PDFs using iText 8.
 * Card size: 85mm × 135mm (standard credit card portrait).
 * Includes: school header, student photo placeholder, details, roll number barcode.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IdCardService {

    private final StudentRepository studentRepo;
    private final ClassGradeRepository classGradeRepo;

    // Card dimensions (A6 portrait ~85×135mm in points)
    private static final PageSize CARD_SIZE = new PageSize(241, 382);   // ~85×135mm

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(142, 68, 173);   // Purple
    private static final DeviceRgb HEADER_2     = new DeviceRgb(52, 152, 219);   // Blue accent
    private static final DeviceRgb DARK         = new DeviceRgb(44, 62, 80);     // Dark text
    private static final DeviceRgb LIGHT_BG     = new DeviceRgb(245, 245, 250);  // Light card bg
    private static final DeviceRgb ACCENT       = new DeviceRgb(231, 76, 60);    // Red blood group

    // ── Single Student ID Card ────────────────────────────────────────────────

    public byte[] generateStudentIdCard(Long studentId) throws IOException {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        buildIdCardPdf(student, baos);
        return baos.toByteArray();
    }

    // ── Bulk ID Cards as ZIP ──────────────────────────────────────────────────

    public byte[] generateBulkIdCards(Long classGradeId) throws IOException {
        ClassGrade classGrade = classGradeRepo.findById(classGradeId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassGrade", classGradeId));

        List<Student> students = studentRepo.findAll().stream()
                .filter(s -> s.getClassGrade() != null && s.getClassGrade().getId().equals(classGradeId)
                        && Boolean.TRUE.equals(s.getIsActive()))
                .toList();

        if (students.isEmpty()) {
            throw new IllegalStateException("No active students found in class " + classGrade.getGradeName());
        }

        ByteArrayOutputStream zipBaos = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(zipBaos)) {
            for (Student student : students) {
                ByteArrayOutputStream pdfBaos = new ByteArrayOutputStream();
                buildIdCardPdf(student, pdfBaos);
                String filename = "IDCard_" + student.getRollNumber() + ".pdf";
                zip.putNextEntry(new ZipEntry(filename));
                zip.write(pdfBaos.toByteArray());
                zip.closeEntry();
            }
        }
        return zipBaos.toByteArray();
    }

    // ── PDF Builder ───────────────────────────────────────────────────────────

    private void buildIdCardPdf(Student student, ByteArrayOutputStream out) throws IOException {
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        pdf.setDefaultPageSize(CARD_SIZE);
        Document doc = new Document(pdf);
        doc.setMargins(0, 0, 0, 0);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        String fullName = student.getUser() != null ? student.getUser().getFullName() : student.getRollNumber();
        String className = student.getClassGrade() != null
                ? student.getClassGrade().getGradeName() + " - " + student.getClassGrade().getSection() : "N/A";
        // Use yearLabel from academicYear if available, fallback to current year range
        String academicYear;
        try {
            academicYear = (student.getClassGrade() != null && student.getClassGrade().getAcademicYear() != null)
                    ? student.getClassGrade().getAcademicYear().getYearLabel() : "2024-2025";
        } catch (Exception e) {
            academicYear = "2024-2025";
        }

        // ── Colourful Header Band ────────────────────────────────────────────
        Table header = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
        header.addCell(new Cell()
                .add(new Paragraph("🏫 SCHOOL MANAGEMENT").setFont(bold).setFontSize(11)
                        .setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER))
                .add(new Paragraph("STUDENT IDENTITY CARD").setFont(regular).setFontSize(8)
                        .setFontColor(new DeviceRgb(200, 200, 255)).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(HEADER_COLOR).setBorder(null).setPadding(10));
        doc.add(header);

        // ── Photo + Details Side by Side ─────────────────────────────────────
        Table body = new Table(UnitValue.createPercentArray(new float[]{35, 65}))
                .useAllAvailableWidth().setMarginTop(0).setBackgroundColor(LIGHT_BG);

        // Photo placeholder column
        String photoText = "PHOTO";
        Cell photoCell = new Cell()
                .add(new Paragraph("[ " + photoText + " ]")
                        .setFont(bold).setFontSize(9)
                        .setFontColor(new DeviceRgb(150, 150, 150))
                        .setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(new DeviceRgb(220, 220, 230))
                .setHeight(130).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setBorderRight(new SolidBorder(HEADER_COLOR, 2))
                .setBorder(Border.NO_BORDER)
                .setPaddingLeft(4).setPaddingRight(4);
        body.addCell(photoCell);

        // Details column
        Cell detailsCell = new Cell()
                .add(new Paragraph(fullName).setFont(bold).setFontSize(11)
                        .setFontColor(DARK).setMarginBottom(4))
                .add(detailRow("Class: ", className, bold, regular))
                .add(detailRow("Roll No: ", student.getRollNumber(), bold, regular))
                .add(student.getDateOfBirth() != null
                        ? detailRow("DOB: ", student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), bold, regular)
                        : new Paragraph(""))
                .add(student.getBloodGroup() != null
                        ? new Paragraph("🩸 Blood: ").setFont(bold).setFontSize(9)
                        .setFontColor(ACCENT)
                        .add(new com.itextpdf.layout.element.Text(student.getBloodGroup()).setFont(regular))
                        : new Paragraph(""))
                .setBorder(Border.NO_BORDER).setPadding(8)
                .setBackgroundColor(LIGHT_BG);
        body.addCell(detailsCell);
        doc.add(body);

        // ── Parent Info ───────────────────────────────────────────────────────
        if (student.getParentName() != null) {
            Table parentTable = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
            parentTable.addCell(new Cell()
                    .add(new Paragraph("Parent/Guardian: " + student.getParentName())
                            .setFont(regular).setFontSize(8).setFontColor(DARK))
                    .add(student.getParentPhone() != null
                            ? new Paragraph("Contact: " + student.getParentPhone())
                            .setFont(regular).setFontSize(8).setFontColor(DARK) : new Paragraph(""))
                    .setBorder(Border.NO_BORDER)
                    .setBackgroundColor(new DeviceRgb(230, 220, 250)).setPadding(6));
            doc.add(parentTable);
        }

        // ── Barcode of Roll Number ────────────────────────────────────────────
        Barcode128 barcode = new Barcode128(pdf);
        barcode.setCode(student.getRollNumber());
        barcode.setBarHeight(28f);
        barcode.setX(1.2f);
        PdfFormXObject barcodeObj = barcode.createFormXObject(DARK, DARK, pdf);
        Image barcodeImage = new Image(barcodeObj)
                .setAutoScale(true)
                .setHorizontalAlignment(HorizontalAlignment.CENTER);

        Table barcodeTable = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
        barcodeTable.addCell(new Cell()
                .add(barcodeImage)
                .add(new Paragraph(student.getRollNumber()).setFont(bold).setFontSize(8)
                        .setFontColor(DARK).setTextAlignment(TextAlignment.CENTER))
                .setBorder(Border.NO_BORDER).setBackgroundColor(ColorConstants.WHITE)
                .setPadding(6).setTextAlignment(TextAlignment.CENTER));
        doc.add(barcodeTable);

        // ── Footer Band ───────────────────────────────────────────────────────
        Table footer = new Table(UnitValue.createPercentArray(new float[]{1})).useAllAvailableWidth();
        footer.addCell(new Cell()
                .add(new Paragraph("Valid: " + academicYear + "  •  If found, please return to school")
                        .setFont(regular).setFontSize(7)
                        .setFontColor(ColorConstants.WHITE).setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(HEADER_2).setBorder(null).setPadding(6));
        doc.add(footer);

        doc.close();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Paragraph detailRow(String label, String value, PdfFont bold, PdfFont regular) {
        return new Paragraph()
                .add(new com.itextpdf.layout.element.Text(label).setFont(bold).setFontColor(HEADER_COLOR))
                .add(new com.itextpdf.layout.element.Text(value).setFont(regular).setFontColor(DARK))
                .setFontSize(9).setMarginBottom(2);
    }
}
