package com.scaler.userservice_mar2025.dtos;

import com.scaler.userservice_mar2025.models.Role;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long userId;
    private String email;
    private List<Role> roles;
}
