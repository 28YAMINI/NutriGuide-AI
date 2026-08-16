package com.nutriguideai.service;

public interface AccountService {

    void sendVerificationEmail(String email);

    void verifyEmail(String token);

    void requestPasswordReset(String email);

    void resetPassword(String token, String newPassword);
}