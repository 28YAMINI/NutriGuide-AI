package com.nutriguideai.service.impl;

import com.nutriguideai.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from:nutriguide@localhost}")
    private String from;

    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    /** DEV ONLY: when SMTP is disabled, print the email (including the link) to the console. */
    @Value("${app.mail.log-tokens-in-dev:true}")
    private boolean logTokensInDev;

    @Override
    public void send(String to, String subject, String body) {
        if (!mailEnabled) {
            if (logTokensInDev) {
                log.info("[DEV MAIL] to={} subject={}\n{}", to, subject, body);
            } else {
                log.info("[DEV MAIL] to={} subject={} (body suppressed)", to, subject);
            }
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }
}