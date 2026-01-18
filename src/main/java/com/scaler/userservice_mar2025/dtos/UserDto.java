package com.scaler.userservice_mar2025.dtos;

import com.scaler.userservice_mar2025.models.Role;
import com.scaler.userservice_mar2025.models.User;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserDto {
    private Long userId;
    private String email;
    private List<Role> roles;



    public static UserDto from(User user) {
        if(user == null) {
            return null;
        }
        UserDto userDto = new UserDto();
        userDto.setUserId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setRoles(user.getRoles());
        return userDto;
    }
}
