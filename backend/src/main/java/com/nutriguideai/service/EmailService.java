package com.nutriguideai.service;

public interface EmailService {

    void send(String to, String subject, String body);
}