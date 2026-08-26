package com.nutriguideai.controller;

import com.nutriguideai.dto.request.ForgotPasswordRequest;
import com.nutriguideai.dto.request.ResendVerificationRequest;
import com.nutriguideai.dto.request.ResetPasswordRequest;
import com.nutriguideai.dto.response.MessageResponse;
import com.nutriguideai.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /** Opens when the user clicks the link in the verification email. */
    @GetMapping("/verify-email")
    public MessageResponse verifyEmail(@RequestParam("token") String token) {
        accountService.verifyEmail(token);
        return new MessageResponse("Email verified successfully. You can now log in.");
    }

    @PostMapping("/resend-verification")
    public MessageResponse resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        accountService.sendVerificationEmail(request.getEmail());
        return new MessageResponse("If an account exists for this email, a verification email has been sent.");
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        accountService.requestPasswordReset(request.getEmail());
        return new MessageResponse("If an account exists for this email, a password reset email has been sent.");
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        accountService.resetPassword(request.getToken(), request.getNewPassword());
        return new MessageResponse("Password reset successfully. You can now log in with your new password.");
    }
}