package com.jacksearle.backend.auth.dto;

import com.jacksearle.backend.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RegisterResponse {
    private String message;
    private UserResponse user;
}
