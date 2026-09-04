package org.example.userauthservice_july2026.controllers;

import org.example.userauthservice_july2026.dtos.LoginRequestDto;
import org.example.userauthservice_july2026.dtos.SignupRequestDto;
import org.example.userauthservice_july2026.dtos.UserDto;
import org.example.userauthservice_july2026.models.Role;
import org.example.userauthservice_july2026.models.User;
import org.example.userauthservice_july2026.services.IAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private IAuthService authService;

    //signup or register
    @PostMapping("/signup")
    public ResponseEntity<UserDto> signup(@RequestBody
                                              SignupRequestDto signupRequestDto) {
       User user = authService.signup(signupRequestDto.getEmail(),
               signupRequestDto.getPassword(),
               signupRequestDto.getName(),
               signupRequestDto.getPhoneNumber());

       return new ResponseEntity<>(from(user), HttpStatus.CREATED);
    }


    //login
    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        User user = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return new ResponseEntity<>(from(user),HttpStatus.OK);

    }

    private UserDto from(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        List<String> roleValues = new ArrayList<>();
        for (Role role : user.getRoles()) {
            roleValues.add(role.getValue());
        }
        userDto.setRoles(roleValues);
        return userDto;
    }
}
