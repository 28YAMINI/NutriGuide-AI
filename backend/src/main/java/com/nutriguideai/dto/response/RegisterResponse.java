package com.nutriguideai.dto.response;

import com.nutriguideai.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "Response returned after successful user registration.")
public class RegisterResponse {

    @Schema(
            description = "Confirmation message",
            example = "User registered successfully"
    )
    private final String message;

    @Schema(
            description = "The newly registered user's profile",
            implementation = UserResponse.class
    )
    private final UserResponse user;

    /**
     * Builds the registration response from the created user.
     */
    public static RegisterResponse of(User user, String message) {
        return RegisterResponse.builder()
                .message(message)
                .user(UserResponse.fromEntity(user))
                .build();
    }
}