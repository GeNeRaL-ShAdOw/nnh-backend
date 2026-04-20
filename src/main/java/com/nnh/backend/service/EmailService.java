package com.nnh.backend.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.mail.enabled:false}")
    private boolean enabled;

    public record Attachment(String filename, byte[] content, String mimeType) {}

    public void send(String to, String cc, String subject, String htmlBody, Attachment... attachments) {
        if (!enabled) {
            log.info("[MAIL DISABLED] Would send '{}' to {} (cc: {})", subject, to, cc);
            return;
        }
        try {
            MimeMessage msg = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(msg, attachments.length > 0, "UTF-8");
            helper.setFrom(from, "Nanda Nursing Home");
            helper.setTo(to);
            if (cc != null && !cc.isBlank()) helper.setCc(cc);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            for (Attachment a : attachments) {
                helper.addAttachment(a.filename(), new ByteArrayResource(a.content()), a.mimeType());
            }
            mailSender.send(msg);
            log.info("Email '{}' sent to {}", subject, to);
        } catch (Exception e) {
            log.error("Failed to send email '{}' to {}: {}", subject, to, e.getMessage());
        }
    }
}
