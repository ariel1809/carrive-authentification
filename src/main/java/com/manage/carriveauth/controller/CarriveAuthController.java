package com.manage.carriveauth.controller;

import com.manage.carrive.dto.UserLogin;
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

    @PostMapping("login")
    public ResponseEntity<UserCarriveResponse> login(@RequestBody UserLogin userLogin) {
        return service.login(userLogin);
    }

    @PostMapping("forgot-password")
    public ResponseEntity<UserCarriveResponse> forgotPassword(@RequestParam("email") String email) {
        return service.forgetPassword(email);
    }

    @PostMapping("validate-reset")
    public ResponseEntity<UserCarriveResponse> validateReset(@RequestParam("code") Integer code) {
        return service.validateEmail(code);
    }

    @PostMapping("reset-password")
    public ResponseEntity<UserCarriveResponse> resetPassword(@RequestParam("id") String id, @RequestParam("password") String password, @RequestParam("confirm_password") String confirmPassword) {
        return service.changePassword(id, password, confirmPassword);
    }

    @PostMapping("resend-register-code")
    public ResponseEntity<UserCarriveResponse> resendRegisterCode(@RequestParam("email") String email) {
        return service.resendCodeRegister(email);
    }

    @PostMapping("resend-reset-code")
    public ResponseEntity<UserCarriveResponse> resendResetCode(@RequestParam("email") String email) {
        return service.resendCodeReset(email);
    }
}
