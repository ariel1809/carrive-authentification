package com.manage.carriveauth.controller;

import com.manage.carrive.dto.UserRegister;
import com.manage.carrive.response.UserCarriveResponse;
import com.manage.carriveauth.service.impl.CarriceAuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@CrossOrigin("*")
public class CarriveAuthController {

    @Autowired
    private CarriceAuthServiceImpl service;

    @PostMapping("sign-up")
    public ResponseEntity<UserCarriveResponse> register(@RequestBody UserRegister userRegister) {
        return service.register(userRegister);
    }

    @PostMapping("valid-register")
    public ResponseEntity<UserCarriveResponse> validRegister(@RequestParam("code") Integer code) {
        return service.validateRegistration(code);
    }
}
