package com.scaler.userservice_mar2025.controllers;


import com.scaler.userservice_mar2025.dtos.LoginRequestDto;
import com.scaler.userservice_mar2025.dtos.SignUpRequestDto;
import com.scaler.userservice_mar2025.dtos.TokenDto;
import com.scaler.userservice_mar2025.dtos.UserDto;
import com.scaler.userservice_mar2025.exceptions.InvalidTokenException;
import com.scaler.userservice_mar2025.exceptions.PasswordMismatchException;
import com.scaler.userservice_mar2025.models.Token;
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
    public TokenDto login(@RequestBody LoginRequestDto requestDto) throws PasswordMismatchException {

        Token token=userService.login(requestDto.getEmail(),requestDto.getPassword());
        return TokenDto.from(token);
    }

    @GetMapping("/validate/{tokenValue}")
    public UserDto validateToken(@PathVariable("tokenValue") String tokenValue) throws InvalidTokenException {

        System.out.println("Validating token: " + tokenValue);

        User user=userService.validateToken(tokenValue);
        return UserDto.from(user);
    }

    @PostMapping("/logout/{tokenValue}")
    public void logOut(@PathVariable("tokenValue") String tokenValue){
       // userService.logout(tokenValue);
    }
}
