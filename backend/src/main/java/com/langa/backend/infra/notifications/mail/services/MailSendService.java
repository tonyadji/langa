package com.langa.backend.infra.notifications.mail.services;


import java.util.List;

public interface MailSendService {

    void ping(String to, String subject, String body);

    void html(List<String> to, String subject, String body);

}
