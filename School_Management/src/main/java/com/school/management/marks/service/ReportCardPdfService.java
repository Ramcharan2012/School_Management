package com.school.management.marks.service;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.school.management.common.exception.ResourceNotFoundException;
import com.school.management.marks.entity.Mark;
import com.school.management.student.entity.Student;
import com.school.management.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Generates a printable Report Card PDF using iText 8.
 * Layout: School header → Student info → Marks table → Summary footer.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportCardPdfService {

    private final StudentRepository studentRepo;
    private final MarksService marksService;

    private static final DeviceRgb HEADER_COLOR = new DeviceRgb(41, 128, 185);   // Blue
    private static final DeviceRgb TABLE_HEADER  = new DeviceRgb(52, 73, 94);    // Dark
    private static final DeviceRgb ROW_ALT       = new DeviceRgb(236, 240, 241); // Light grey
    private static final DeviceRgb PASS_COLOR    = new DeviceRgb(39, 174, 96);   // Green
    private static final DeviceRgb FAIL_COLOR    = new DeviceRgb(192, 57, 43);   // Red

    public byte[] generate(Long studentId, Long classGradeId) throws IOException {
        Student student = studentRepo.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student", studentId));

        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> reportData = marksService.getStudentReportCard(studentId, classGradeId);
        List<Mark> marks = (List<Mark>) reportData.get("marks");
        double totalObtained = (double) reportData.get("totalMarksObtained");
        double totalMax = (double) reportData.get("totalMaxMarks");
        double percentage = (double) reportData.get("percentage");
        String overallGrade = (String) reportData.get("overallGrade");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document doc = new Document(pdf);
        doc.setMargins(36, 36, 36, 36);

        PdfFont bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        PdfFont regular = PdfFontFactory.createFont(StandardFonts.HELVETICA);

        // ── School Header ─────────────────────────────────────────────────────
        Paragraph schoolName = new Paragraph("🏫 SCHOOL MANAGEMENT SYSTEM")
                .setFont(bold).setFontSize(20).setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER);
        Table headerTable = new Table(UnitValue.createPercentArray(new float[]{1}))
                .useAllAvailableWidth()
                .addCell(new Cell().add(schoolName)
                        .setBackgroundColor(HEADER_COLOR).setBorder(null).setPadding(18));
        doc.add(headerTable);

        // ── Subtitle ──────────────────────────────────────────────────────────
        doc.add(new Paragraph("STUDENT REPORT CARD")
                .setFont(bold).setFontSize(14).setFontColor(HEADER_COLOR)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(10));

        doc.add(new Paragraph("Academic Year: 2024-2025    |    Date: "
                + LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")))
                .setFont(regular).setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER).setFontColor(new DeviceRgb(100, 100, 100)));

        // ── Student Info Table ────────────────────────────────────────────────
        String fullName = student.getUser() != null ? student.getUser().getFullName() : student.getRollNumber();
        String className = student.getClassGrade() != null
                ? student.getClassGrade().getGradeName() + " - " + student.getClassGrade().getSection() : "N/A";

        Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                .useAllAvailableWidth().setMarginTop(14);
        addInfoRow(infoTable, "Student Name", fullName, bold, regular);
        addInfoRow(infoTable, "Roll Number", student.getRollNumber(), bold, regular);
        addInfoRow(infoTable, "Class", className, bold, regular);
        if (student.getDateOfBirth() != null)
            addInfoRow(infoTable, "Date of Birth",
                    student.getDateOfBirth().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), bold, regular);
        doc.add(infoTable);

        // ── Marks Table ───────────────────────────────────────────────────────
        doc.add(new Paragraph("Marks Summary").setFont(bold).setFontSize(13)
                .setFontColor(HEADER_COLOR).setMarginTop(16).setMarginBottom(4));

        float[] cols = {3, 3, 1, 1, 1, 1, 2};
        Table marksTable = new Table(UnitValue.createPercentArray(cols)).useAllAvailableWidth();
        String[] headers = {"Exam", "Subject", "Type", "Max", "Obtained", "Grade", "Status"};
        for (String h : headers) {
            marksTable.addHeaderCell(new Cell()
                    .add(new Paragraph(h).setFont(bold).setFontSize(9).setFontColor(ColorConstants.WHITE))
                    .setBackgroundColor(TABLE_HEADER).setBorder(null).setPadding(6));
        }

        boolean alt = false;
        for (Mark m : marks) {
            DeviceRgb bg = alt ? ROW_ALT : new DeviceRgb(255, 255, 255);
            DeviceRgb statusColor = (m.getIsAbsent() || m.getGrade().equals("F")) ? FAIL_COLOR : PASS_COLOR;
            String status = m.getIsAbsent() ? "ABSENT" : (m.getGrade().equals("F") ? "FAIL" : "PASS");

            marksTable.addCell(cell(m.getExam().getTitle(), regular, 9, TextAlignment.LEFT, bg));
            marksTable.addCell(cell(m.getExam().getSubject().getName(), regular, 9, TextAlignment.LEFT, bg));
            marksTable.addCell(cell(m.getExam().getExamType().name(), regular, 8, TextAlignment.CENTER, bg));
            marksTable.addCell(cell(String.valueOf(m.getExam().getTotalMarks()), regular, 9, TextAlignment.CENTER, bg));
            marksTable.addCell(cell(m.getIsAbsent() ? "AB" : String.valueOf(m.getMarksObtained().intValue()), regular, 9, TextAlignment.CENTER, bg));
            marksTable.addCell(cell(m.getGrade(), bold, 9, TextAlignment.CENTER, bg));
            marksTable.addCell(new Cell()
                    .add(new Paragraph(status).setFont(bold).setFontSize(8).setFontColor(statusColor))
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBackgroundColor(bg).setBorder(null).setPadding(4));
            alt = !alt;
        }

        // Totals row
        marksTable.addCell(spanCell("TOTALS", bold, 3, TABLE_HEADER));
        marksTable.addCell(cell(String.valueOf((int) totalMax), bold, 9, TextAlignment.CENTER, TABLE_HEADER)
                .setFontColor(ColorConstants.WHITE));
        marksTable.addCell(cell(String.valueOf((int) totalObtained), bold, 9, TextAlignment.CENTER, TABLE_HEADER)
                .setFontColor(ColorConstants.WHITE));
        marksTable.addCell(cell(overallGrade, bold, 9, TextAlignment.CENTER, TABLE_HEADER)
                .setFontColor(ColorConstants.WHITE));
        marksTable.addCell(cell("", regular, 9, TextAlignment.CENTER, TABLE_HEADER));

        doc.add(marksTable);

        // ── Summary ───────────────────────────────────────────────────────────
        boolean passed = percentage >= 35;
        doc.add(new Paragraph(String.format("Overall Percentage: %.1f%%    Overall Grade: %s    Result: %s",
                percentage, overallGrade, passed ? "✅ PASS" : "❌ FAIL"))
                .setFont(bold).setFontSize(12)
                .setFontColor(passed ? PASS_COLOR : FAIL_COLOR)
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(14));

        doc.add(new Paragraph("This is a computer-generated document. No signature required.")
                .setFont(regular).setFontSize(8)
                .setFontColor(new DeviceRgb(150, 150, 150))
                .setTextAlignment(TextAlignment.CENTER).setMarginTop(20));

        doc.close();
        return baos.toByteArray();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void addInfoRow(Table table, String label, String value,
            PdfFont bold, PdfFont regular) {
        table.addCell(new Cell().add(new Paragraph(label + ":").setFont(bold).setFontSize(9))
                .setBorder(null).setPadding(4).setBackgroundColor(ROW_ALT));
        table.addCell(new Cell().add(new Paragraph(value).setFont(regular).setFontSize(9))
                .setBorder(null).setPadding(4));
    }

    private Cell cell(String text, PdfFont font, int size,
            TextAlignment align, DeviceRgb bg) {
        return new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(size))
                .setTextAlignment(align).setBackgroundColor(bg)
                .setBorder(null).setPadding(4);
    }

    private Cell spanCell(String text, PdfFont font, int span, DeviceRgb bg) {
        return new Cell(1, span)
                .add(new Paragraph(text).setFont(font).setFontSize(9).setFontColor(ColorConstants.WHITE))
                .setBackgroundColor(bg).setBorder(null).setPadding(4);
    }
}
