package com.manage.carriveauth.service.api;

import com.manage.carrive.dto.UserLogin;
import com.manage.carrive.dto.UserRegister;
import com.manage.carrive.response.UserCarriveResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface CarriceAuthServiceApi {
    ResponseEntity<UserCarriveResponse> register(UserRegister userRegister);
    ResponseEntity<UserCarriveResponse> resendCodeRegister(String email);
    ResponseEntity<UserCarriveResponse> validateRegistration(Integer code);
    ResponseEntity<UserCarriveResponse> login(UserLogin userLogin);
    ResponseEntity<UserCarriveResponse> forgetPassword(String email);
    ResponseEntity<UserCarriveResponse> resendCodeReset(String email);
    ResponseEntity<UserCarriveResponse> validateEmail(Integer code);
    ResponseEntity<UserCarriveResponse> changePassword(String id, String password, String confirmPassword);
}
