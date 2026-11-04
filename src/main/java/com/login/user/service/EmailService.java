package com.login.user.service;

import java.util.UUID;

import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.login.user.domain.exception.EmailMessagingException;
import com.login.user.util.ActivateUserEmailBody;
import com.login.user.util.RedefinePasswordEmailBody;

import jakarta.mail.MessagingException;

@AllArgsConstructor
@Service
public class EmailService {

    private JavaMailSender mailSender;

    private ActivateUserEmailBody activateUserEmailBody;

    private RedefinePasswordEmailBody redefinePasswordEmailBody;

    public void sendHtmlEmail(String to, String subject, String htmlBody) throws MessagingException {
        var message = mailSender.createMimeMessage();
        var helper = new MimeMessageHelper(message, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlBody, true);
        helper.setFrom("no-reply@pedroscarelli.com");

        mailSender.send(message);
    }

    public void sendSignUpEmail(String to, UUID userId) {
        try {
            sendHtmlEmail(to, "User activation", activateUserEmailBody.of(userId.toString()));
        } catch (MessagingException e) {
            throw new EmailMessagingException("Error sending user activation e-mail");
        }
    }

    public void sendRedefinePasswordEmail(String to, String otpCode) {
        try {
            sendHtmlEmail(to, "Password reset", redefinePasswordEmailBody.of(otpCode));
        } catch (MessagingException e) {
            throw new EmailMessagingException("Error sending password reset e-mail");
        }
    }
}

