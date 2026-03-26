package com.school.management.idcard.controller;

import com.school.management.idcard.service.IdCardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints for generating student ID card PDFs.
 * Downloads a single PDF or a ZIP of all students in a class.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "ID Card", description = "Generate student ID card PDFs")
@SecurityRequirement(name = "bearerAuth")
public class IdCardController {

    private final IdCardService idCardService;

    @GetMapping("/admin/students/{studentId}/idcard")
    @Operation(summary = "Download ID card PDF for a single student")
    public ResponseEntity<byte[]> getStudentIdCard(@PathVariable Long studentId) throws Exception {
        byte[] pdf = idCardService.generateStudentIdCard(studentId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=IDCard_student_" + studentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/admin/classes/{classGradeId}/idcards")
    @Operation(summary = "Download ZIP of all student ID cards for a class")
    public ResponseEntity<byte[]> getBulkIdCards(@PathVariable Long classGradeId) throws Exception {
        byte[] zip = idCardService.generateBulkIdCards(classGradeId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=IDCards_class_" + classGradeId + ".zip")
                .contentType(MediaType.parseMediaType("application/zip"))
                .body(zip);
    }
}
