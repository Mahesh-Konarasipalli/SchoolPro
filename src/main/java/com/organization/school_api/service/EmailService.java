package com.organization.school_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    // --- 1. HTML OTP EMAIL ---
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            Context context = new Context();
            context.setVariable("otp", otp);

            String process = templateEngine.process("otp-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setSubject("Your SchoolPro Verification Code 🔐");
            helper.setTo(toEmail);
            helper.setText(process, true); 
            helper.setFrom("schoolproadmin@gmail.com");

            mailSender.send(mimeMessage);
            System.out.println("HTML OTP Email successfully sent to " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
        }
    }

    // --- 2. HTML PASSWORD RESET EMAIL ---
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("resetLink", resetLink);

            String process = templateEngine.process("reset-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setSubject("Reset Your SchoolPro Password 🔒");
            helper.setTo(toEmail);
            helper.setText(process, true);
            helper.setFrom("konarasimahesh@gmail.com"); // Your configured email

            mailSender.send(mimeMessage);
            System.out.println("HTML Password reset link sent to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send reset email: " + e.getMessage());
        }
    }

    // --- 3. ADMIN WELCOME EMAIL ---
    public void sendWelcomeEmail(String toEmail, String fullName) {
        try {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("loginUrl", "http://localhost:8080/login");

            // Make sure the template name matches your file exactly!
            String process = templateEngine.process("admin-welcome-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setSubject("Welcome to SchoolPro! 🏫");
            helper.setTo(toEmail);
            helper.setText(process, true); 
            helper.setFrom("schoolproadmin@gmail.com");

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            System.err.println("Failed to send welcome email: " + e.getMessage());
        }
    }

    // --- 4. STUDENT WELCOME EMAIL ---
    public void sendStudentWelcomeEmail(String toEmail, String fullName, String password, String studentId) {
        try {
            Context context = new Context();
            context.setVariable("name", fullName);
            context.setVariable("studentId", studentId);
            context.setVariable("password", password);
            context.setVariable("loginUrl", "http://localhost:8080/login");

            String process = templateEngine.process("student-welcome-email", context);

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setSubject("Welcome to SchoolPro! Here are your login details 🎒");
            helper.setTo(toEmail);
            helper.setText(process, true);
            helper.setFrom("schoolproadmin@gmail.com");

            mailSender.send(mimeMessage);

        } catch (MessagingException e) {
            System.err.println("Failed to send student email: " + e.getMessage());
        }
    }
}