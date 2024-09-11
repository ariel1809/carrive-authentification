package com.manage.carriveauth.service.api;

import com.manage.carrive.dto.UserRegister;
import com.manage.carrive.response.UserCarriveResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public interface CarriceAuthServiceApi {
    ResponseEntity<UserCarriveResponse> register(UserRegister userRegister);
    ResponseEntity<UserCarriveResponse> validateRegistration(Integer code);
}
