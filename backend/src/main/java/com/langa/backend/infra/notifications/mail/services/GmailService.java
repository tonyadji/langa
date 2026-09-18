package com.langa.backend.infra.notifications.mail.services;

import com.langa.backend.common.model.errors.Errors;
import com.langa.backend.infra.notifications.exceptions.NotificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnProperty(havingValue = "gmail", name = "application.mail.provider")
public class GmailService implements MailSendService {

    private final JavaMailSender mailSender;

    @Override
    public void ping(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    @Override
    public void html(List<String> to, String subject, String body) {
        to.forEach(recipient -> {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper;
            try {
                helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(recipient);
                helper.setSubject(subject);
                helper.setText(body, true);
                mailSender.send(mimeMessage);
            } catch (MessagingException e) {
                log.error(e.getMessage(), e);
                throw new NotificationException("Error occured sending mail : "+subject,
                        e, Errors.NOTIFICATION_MAIL_ERROR);
            }
        });
    }

}
