package com.nutriguideai.service;

import com.nutriguideai.dto.request.UpdateUserRequest;
import com.nutriguideai.dto.response.UserResponse;

public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateUserRequest request);

    void deleteUser(Long id);
}