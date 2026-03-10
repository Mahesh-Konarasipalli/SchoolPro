package com.organization.school_api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;


    public void sendOtpEmail(String toEmail, String otp){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("schoolproadmin@gmail.com");
        message.setTo(toEmail);
        message.setText("Welcome to SchoolPro!\n\n"
                + "To complete your Faculty registration, please enter the following 6-digit verification code:\n\n"
                + "👉 " + otp + " 👈\n\n"
                + "If you did not request this, please ignore this email.\n\n"
                + "Best,\nThe SchoolPro IT Team");
        mailSender.send(message);
        System.out.println("OTP Email successfully sent to "+toEmail);;
    }
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        
        // Make sure this matches the email in your application.properties!
        message.setFrom("konarasimahesh@gmail.com"); 
        message.setTo(toEmail);
        message.setSubject("Reset Your SchoolPro Password 🔒");
        
        message.setText("Hello,\n\n"
                + "You requested a password reset. Click the secure link below to choose a new password:\n\n"
                + resetLink + "\n\n"
                + "If you did not request this, please ignore this email. Your password will remain unchanged.\n\n"
                + "Best,\nThe SchoolPro IT Team");
        
        mailSender.send(message);
        System.out.println("Password reset link sent to: " + toEmail);
    }
}
