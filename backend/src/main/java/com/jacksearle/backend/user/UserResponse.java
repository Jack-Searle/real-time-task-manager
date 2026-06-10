package com.jacksearle.backend.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UserResponse {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean emailVerified;
}
