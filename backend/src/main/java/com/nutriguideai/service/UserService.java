package com.nutriguideai.service;

import com.nutriguideai.dto.request.UpdateUserRequest;
import com.nutriguideai.dto.response.UserResponse;
import com.nutriguideai.enums.Role;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getUserByEmail(String email);

    UserResponse getCurrentUser();

    UserResponse updateProfile(UpdateUserRequest request);

    void deleteUser(Long id);

    /** Admin: every account. */
    List<UserResponse> listUsers();

    /** Admin: promote/demote a user. An admin cannot change their own role. */
    UserResponse updateUserRole(Long id, Role role);
}