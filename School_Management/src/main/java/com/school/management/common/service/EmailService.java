package com.school.management.common.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Async email service — all methods use @Async so they never block the HTTP
 * response.
 * Covers: admission approval/rejection, leave decision, fee receipt, password
 * reset OTP.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

  private final JavaMailSender mailSender;

  // ── Admission ─────────────────────────────────────────────────────────────

  @Async
  public void sendAdmissionApprovalEmail(String toEmail, String studentName,
      String username, String tempPassword, String className) {
    sendHtmlEmail(toEmail, "Welcome - Admission Approved!",
        buildApprovalEmailHtml(studentName, username, tempPassword, className));
  }

  @Async
  public void sendAdmissionRejectionEmail(String toEmail, String studentName, String reason) {
    sendHtmlEmail(toEmail, "Admission Status Update",
        buildRejectionEmailHtml(studentName, reason));
  }

  // ── Leave ─────────────────────────────────────────────────────────────────

  @Async
  public void sendLeaveApprovalEmail(String toEmail, String name, String status, String remarks) {
    String content = String.format(
        "<h2>Hello %s,</h2><p>Your leave request has been <strong>%s</strong>.</p>"
            + "<p><strong>Remarks:</strong> %s</p>",
        name, status, remarks != null ? remarks : "No remarks provided.");
    sendHtmlEmail(toEmail, "Leave Request Update", content);
  }

  // ── Absence Notification to Parent ──────────────────────────────────

  /**
   * Sends an alert email to parent/guardian when student is marked ABSENT.
   * Called asynchronously from AttendanceService - no latency impact on API.
   */
  @Async
  public void sendAbsenceNotificationEmail(String parentEmail, String parentName,
      String studentName, String subjectName, LocalDate date) {
    sendHtmlEmail(parentEmail,
        "\u26a0\ufe0f Attendance Alert: " + studentName + " was absent today",
        buildAbsenceEmailHtml(parentName, studentName, subjectName, date));
  }

  // \u2500\u2500 Staff Welcome \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500

  /**
   * Sends welcome email to newly created non-teaching staff with their login credentials.
   */
  @Async
  public void sendStaffWelcomeEmail(String toEmail, String fullName,
      String username, String tempPassword, String category) {
    sendHtmlEmail(toEmail, "\ud83c\udf93 Welcome to the School Management System!",
        """
            <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
              <div style="background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;border-radius:10px 10px 0 0;text-align:center">
                <h1 style="color:white;margin:0">\ud83c\udf93 Welcome Aboard!</h1>
              </div>
              <div style="background:#f8f9fa;padding:30px;border-radius:0 0 10px 10px">
                <p>Dear <strong>%s</strong>,</p>
                <p>Your staff account has been created. You can now log in to the School Management System.</p>
                <div style="background:white;border-left:4px solid #667eea;padding:20px;margin:20px 0;border-radius:5px">
                  <p style="margin:5px 0"><strong>\ud83d\udc64 Role:</strong> %s</p>
                  <p style="margin:5px 0"><strong>\ud83d\udcbb Username:</strong> %s</p>
                  <p style="margin:5px 0"><strong>\ud83d\udd12 Temporary Password:</strong> %s</p>
                </div>
                <p style="color:#e74c3c"><strong>Please change your password after first login.</strong></p>
                <p>Best regards,<br><strong>School Administration</strong></p>
              </div>
            </div>""".formatted(fullName, category, username, tempPassword));
  }

  // \u2500\u2500 Fee Receipt \u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500

  @Async
  public void sendFeeReceiptEmail(String toEmail, String studentName, String receiptNumber,
      String feeType, BigDecimal amountPaid, String paymentMethod,
      String transactionRef, LocalDate paymentDate) {
    sendHtmlEmail(toEmail, "Fee Payment Receipt - " + receiptNumber,
        buildFeeReceiptHtml(studentName, receiptNumber, feeType,
            amountPaid, paymentMethod, transactionRef, paymentDate));
  }

  // ── Password Reset OTP ────────────────────────────────────────────────────

  @Async
  public void sendPasswordResetOtpEmail(String toEmail, String name, String otp) {
    sendHtmlEmail(toEmail, "Password Reset OTP - School Management System",
        buildOtpEmailHtml(name, otp));
  }

  // ── Core ──────────────────────────────────────────────────────────────────

  private void sendHtmlEmail(String to, String subject, String htmlContent) {
    try {
      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(htmlContent, true);
      mailSender.send(message);
      log.info("Email sent to: {}", to);
    } catch (MessagingException e) {
      log.error("Failed to send email to {}: {}", to, e.getMessage());
      // Don't rethrow — email failure must not fail the main operation
    }
  }

  // ── HTML Templates ────────────────────────────────────────────────────────

  private String buildApprovalEmailHtml(String name, String username, String password, String className) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
          <div style="background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;border-radius:10px 10px 0 0;text-align:center">
            <h1 style="color:white;margin:0">🎓 Admission Approved!</h1>
          </div>
          <div style="background:#f8f9fa;padding:30px;border-radius:0 0 10px 10px">
            <p>Dear <strong>%s</strong>,</p>
            <p>Your admission is approved. Login credentials:</p>
            <div style="background:white;border-left:4px solid #667eea;padding:20px;margin:20px 0;border-radius:5px">
              <p><strong>Class:</strong> %s</p>
              <p><strong>Username:</strong> <code>%s</code></p>
              <p><strong>Temp Password:</strong> <code>%s</code></p>
            </div>
            <p style="color:#e74c3c"><strong>⚠️ Change your password on first login.</strong></p>
          </div>
        </div>"""
        .formatted(name, className, username, password);
  }

  private String buildRejectionEmailHtml(String name, String reason) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
          <div style="background:#e74c3c;padding:30px;border-radius:10px 10px 0 0;text-align:center">
            <h1 style="color:white;margin:0">Admission Status Update</h1>
          </div>
          <div style="background:#f8f9fa;padding:30px;border-radius:0 0 10px 10px">
            <p>Dear <strong>%s</strong>,</p>
            <p>Your admission could not be approved. <strong>Reason:</strong> %s</p>
            <p>Please contact us for more information.</p>
          </div>
        </div>""".formatted(name, reason != null ? reason : "Not specified.");
  }

  private String buildFeeReceiptHtml(String studentName, String receiptNumber, String feeType,
      BigDecimal amountPaid, String paymentMethod, String transactionRef, LocalDate paymentDate) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
          <div style="background:linear-gradient(135deg,#11998e,#38ef7d);padding:30px;border-radius:10px 10px 0 0;text-align:center">
            <h1 style="color:white;margin:0">✅ Fee Payment Receipt</h1>
          </div>
          <div style="background:#f8f9fa;padding:30px;border-radius:0 0 10px 10px">
            <p>Dear <strong>%s</strong>,</p>
            <p>Your payment has been recorded:</p>
            <div style="background:white;border-left:4px solid #11998e;padding:20px;margin:20px 0;border-radius:5px">
              <p><strong>Receipt No:</strong> %s</p>
              <p><strong>Fee Type:</strong> %s</p>
              <p><strong>Amount Paid:</strong> ₹%s</p>
              <p><strong>Method:</strong> %s</p>
              <p><strong>Transaction Ref:</strong> %s</p>
              <p><strong>Date:</strong> %s</p>
            </div>
            <p>Please keep this receipt for your records.</p>
          </div>
        </div>"""
        .formatted(studentName, receiptNumber, feeType,
            amountPaid.toPlainString(), paymentMethod,
            transactionRef != null ? transactionRef : "N/A", paymentDate);
  }

  private String buildOtpEmailHtml(String name, String otp) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
          <div style="background:linear-gradient(135deg,#f093fb,#f5576c);padding:30px;border-radius:10px 10px 0 0;text-align:center">
            <h1 style="color:white;margin:0">🔐 Password Reset OTP</h1>
          </div>
          <div style="background:#f8f9fa;padding:30px;border-radius:0 0 10px 10px">
            <p>Dear <strong>%s</strong>,</p>
            <p>Use the OTP below to reset your password:</p>
            <div style="text-align:center;margin:30px 0">
              <div style="background:white;border:2px dashed #f5576c;display:inline-block;padding:20px 40px;border-radius:10px">
                <span style="font-size:40px;font-weight:bold;letter-spacing:10px;color:#f5576c">%s</span>
              </div>
            </div>
            <p style="color:#e74c3c"><strong>⚠️ Expires in 15 minutes.</strong></p>
            <p>If you did not request this, please ignore this email.</p>
          </div>
        </div>"""
        .formatted(name, otp);
  }

  private String buildAbsenceEmailHtml(String parentName, String studentName,
      String subjectName, LocalDate date) {
    return """
        <div style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:20px">
          <div style="background:linear-gradient(135deg,#f7971e,#ffd200);padding:30px;border-radius:10px 10px 0 0;text-align:center">
            <h1 style="color:white;margin:0;text-shadow:1px 1px 3px rgba(0,0,0,0.3)">⚠️ Attendance Alert</h1>
          </div>
          <div style="background:#f8f9fa;padding:30px;border-radius:0 0 10px 10px">
            <p>Dear <strong>%s</strong>,</p>
            <p>We wish to inform you that your ward <strong>%s</strong> was marked <strong style="color:#e74c3c">ABSENT</strong> in today's class.</p>
            <div style="background:white;border-left:4px solid #f7971e;padding:20px;margin:20px 0;border-radius:5px">
              <p style="margin:5px 0"><strong>📚 Subject:</strong> %s</p>
              <p style="margin:5px 0"><strong>📅 Date:</strong> %s</p>
            </div>
            <p>If this absence was planned, please submit a leave application through the school portal or contact the class teacher at the earliest.</p>
            <p style="color:#7f8c8d;font-size:13px">This is an automated attendance notification. Please do not reply to this email.</p>
            <p>Best regards,<br><strong>School Management Team</strong></p>
          </div>
        </div>"""
        .formatted(parentName, studentName, subjectName, date.toString());
  }
}
