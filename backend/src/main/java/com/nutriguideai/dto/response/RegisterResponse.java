package com.nutriguideai.dto.response;

import com.nutriguideai.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RegisterResponse {
    private final String message;
    private final UserResponse user;
    public static RegisterResponse of(User user, String message){
        return RegisterResponse.builder()
                .message(message)
                .user(UserResponse.fromEntity(user))
                .build();
    }
}
