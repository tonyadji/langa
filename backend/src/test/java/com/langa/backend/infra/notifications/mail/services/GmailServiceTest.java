package com.langa.backend.infra.notifications.mail.services;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Test
    void ping_shouldSendSimpleMailMessage() {
        GmailService service = new GmailService(mailSender);

        service.ping("to@example.com", "Subject", "Body");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void html_shouldSendMimeMessage_toEachRecipient() {
        GmailService service = new GmailService(mailSender);
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertDoesNotThrow(() -> service.html(List.of("to1@example.com", "to2@example.com"), "Subject", "<p>Body</p>"));

        verify(mailSender, org.mockito.Mockito.times(2)).send(any(MimeMessage.class));
    }
}
