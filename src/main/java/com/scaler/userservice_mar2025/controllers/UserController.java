package com.scaler.userservice_mar2025.controllers;


import com.scaler.userservice_mar2025.dtos.LoginRequestDto;
import com.scaler.userservice_mar2025.dtos.SignUpRequestDto;
import com.scaler.userservice_mar2025.dtos.TokenDto;
import com.scaler.userservice_mar2025.dtos.UserDto;
import com.scaler.userservice_mar2025.models.User;
import com.scaler.userservice_mar2025.services.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/signup")
    public UserDto signUp(@RequestBody SignUpRequestDto requestDto) {
       User user = userService.signUp(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPassword()
        );


        return UserDto.from(user);
    }

    @PostMapping("/login")
    public TokenDto login(@RequestBody LoginRequestDto requestDto){
        return null;
    }

    @GetMapping("/validate/{tokenValaue}")
    public UserDto validateToken(@PathVariable("tokenValue") String tokenValue){
        return null;
    }

    @PostMapping("/logout/{tokenValaue}")
    public void logOut(@PathVariable("tokenValue") String tokenValue){
       // userService.logout(tokenValue);
    }
}
