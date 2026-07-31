package com.nutriguideai.service;



import com.nutriguideai.dto.request.UpdateUserRequest;
import com.nutriguideai.dto.response.UserResponse;

/**
 * Contract for user profile operations.
 * Authorization (who may access which account) is enforced at the
 * controller / security layer — this interface only defines capability.
 */
public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse getCurrentUser();

    UserResponse updateProfile(Long id, UpdateUserRequest request);

    void deleteUser(Long id);
}