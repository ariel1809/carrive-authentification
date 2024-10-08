package com.manage.carriveauth.service.impl;

import com.manage.carrive.dto.UserLogin;
import com.manage.carrive.dto.UserRegister;
import com.manage.carrive.entity.*;
import com.manage.carrive.enumeration.CodeResponseEnum;
import com.manage.carrive.enumeration.UserTypeEnum;
import com.manage.carrive.response.UserCarriveResponse;
import com.manage.carriveauth.configuration.JwtTokenUtil;
import com.manage.carriveauth.mailer.EmailValidator;
import com.manage.carriveauth.mailer.service.impl.MailServiceImpl;
import com.manage.carriveauth.service.JwtUserDetailsService;
import com.manage.carriveauth.service.api.CarriceAuthServiceApi;
import com.manage.carriveutility.repository.*;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class CarriceAuthServiceImpl implements CarriceAuthServiceApi {

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private JwtUserDetailsService userDetailsService;

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private RegisterCodeRepository registerCodeRepository;

    @Autowired
    private MailServiceImpl mailService;

    @Autowired
    private MailerRepository mailerRepository;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private ResetPasswordRepository resetPasswordRepository;

    @Autowired
    private EmailValidator emailValidator;

    private final Logger logger = LoggerFactory.getLogger(CarriceAuthServiceImpl.class);
    private final ZoneId zoneId = ZoneId.systemDefault();

    @Override
    public ResponseEntity<UserCarriveResponse> register(UserRegister userRegister) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();

        try {

            if (userRegister == null) {
                userCarriveResponse.setMessage("user register is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (userRegister.getUserType() == null){
                userCarriveResponse.setMessage("user type is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (userRegister.getName() == null){
                userCarriveResponse.setMessage("name is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (userRegister.getEmail() == null){
                userCarriveResponse.setMessage("email is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
//            if (emailValidator.isValidEmail(userRegister.getEmail())){
//                userCarriveResponse.setMessage("email is not valid");
//                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
//                userCarriveResponse.setData(null);
//                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
//            }
            if (driverRepository.existsByEmail(userRegister.getEmail()) || passengerRepository.existsByEmail(userRegister.getEmail())){
                userCarriveResponse.setMessage("email already exist");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (userRegister.getPassword() == null || userRegister.getPassword().length() < 8){
                userCarriveResponse.setMessage("password is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (userRegister.getConfirmPassword() == null || userRegister.getConfirmPassword().length() < 8){
                userCarriveResponse.setMessage("confirm password is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (!userRegister.getPassword().equals(userRegister.getConfirmPassword())){
                userCarriveResponse.setMessage("password does not match");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            RegisterCode registerCode = new RegisterCode();
            registerCode.setExpirationDate(LocalDateTime.now(zoneId).plusMinutes(10));
            int code = generateRegisterCode();
            String emailContent = mailRegister(code);
            registerCode.setCode(code);
            registerCode.setIsOk(false);

            Mailer mailer = new Mailer();
            mailer.setReceiver(userRegister.getEmail());
            mailer.setSubject("VALIDEZ VOTRE INSCRIPTION : CARRIVE");
            mailer.setSender("stevekamga18@gmail.com");
            mailer.setContent(emailContent);
            if (userRegister.getUserType().equals(UserTypeEnum.DRIVER)){
                if (userRegister.getIdCar() == null){
                    userCarriveResponse.setMessage("idCar is null");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                if (userRegister.getMatriculation() == null){
                    userCarriveResponse.setMessage("matriculation is null");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                if (userRegister.getColor() == null){
                    userCarriveResponse.setMessage("color is null");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                if (userRegister.getCarModel() == null){
                    userCarriveResponse.setMessage("carModel is null");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                Driver driver = new Driver();
                driver.setName(userRegister.getName());
                driver.setEmail(userRegister.getEmail());
                driver.setPassword(passwordEncoder.encode(userRegister.getPassword()));
                driver.setUserType(userRegister.getUserType());
                driver.setIsActive(false);
                driver.setIsConnected(false);
                Car car = new Car();
                car.setColor(userRegister.getColor());
                car.setCarBrand(userRegister.getCarBrand());
                car.setIdCar(userRegister.getIdCar());
                car.setMatriculation(userRegister.getMatriculation());
                car.setCarModel(userRegister.getCarModel());
                driver = driverRepository.save(driver);
                car.setDriver(driver);
                carRepository.save(car);

                registerCode.setUser(driver);
                try {
                    mailService.sendMail(mailer);
                    mailer.setIsGo(true);
                    mailerRepository.save(mailer);
                    registerCode = registerCodeRepository.save(registerCode);
                } catch (Exception ex) {
                    mailer.setIsGo(false);
                    mailerRepository.save(mailer);
                    logger.error(ex.getMessage());
                    userCarriveResponse.setMessage("mail does not send");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                    userCarriveResponse.setData(null);
                }
                userCarriveResponse.setMessage("success");
                userCarriveResponse.setData(registerCode);
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else if (userRegister.getUserType().equals(UserTypeEnum.PASSENGER)){
                Passenger passenger = new Passenger();
                passenger.setName(userRegister.getName());
                passenger.setEmail(userRegister.getEmail());
                passenger.setPassword(passwordEncoder.encode(userRegister.getPassword()));
                passenger.setUserType(userRegister.getUserType());
                passenger.setIsActive(false);
                passenger.setIsConnected(false);
                passenger = passengerRepository.save(passenger);

                registerCode.setUser(passenger);
                registerCode.setIsOk(false);
                registerCodeRepository.save(registerCode);
                try {
                    mailService.sendMail(mailer);
                    mailer.setIsGo(true);
                    mailerRepository.save(mailer);
                    registerCode = registerCodeRepository.save(registerCode);
                } catch (Exception ex) {
                    mailer.setIsGo(false);
                    mailerRepository.save(mailer);
                    logger.error(ex.getMessage());
                    userCarriveResponse.setMessage("mail does not send");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                    userCarriveResponse.setData(null);
                }
                userCarriveResponse.setMessage("success");
                userCarriveResponse.setData(registerCode);
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else {
                userCarriveResponse.setMessage("user type invalide");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserCarriveResponse> resendCodeRegister(String email) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();
        try {

            if (email == null || email.isEmpty()) {
                userCarriveResponse.setMessage("email is null or empty");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            Mailer mailer = new Mailer();
            mailer.setReceiver(email);
            mailer.setSubject("VALIDEZ VOTRE INSCRIPTION : CARRIVE");
            mailer.setSender("stevekamga18@gmail.com");
            if (driverRepository.existsByEmail(email)) {
                Driver driver = driverRepository.findByEmail(email).orElse(null);
                if (driver == null) {
                    userCarriveResponse.setMessage("driver not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                RegisterCode registerCode = registerCodeRepository.findByUser(driver).orElse(null);
                if (registerCode == null) {
                    userCarriveResponse.setMessage("registerCode not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                int code = generateRegisterCode();
                registerCode.setCode(code);
                registerCode.setUser(driver);
                registerCode.setExpirationDate(LocalDateTime.now(zoneId).plusMinutes(10));
                if (!registerCode.getIsOk()){
                    registerCode.setIsOk(false);
                    String emailContent = mailRegister(code);
                    mailer.setContent(emailContent);
                    try {
                        mailService.sendMail(mailer);
                        mailer.setIsGo(true);
                        mailerRepository.save(mailer);
                        registerCode = registerCodeRepository.save(registerCode);
                    } catch (Exception ex) {
                        mailer.setIsGo(false);
                        mailerRepository.save(mailer);
                        logger.error(ex.getMessage());
                        userCarriveResponse.setMessage("mail does not send");
                        userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                        userCarriveResponse.setData(null);
                    }
                }else {
                    userCarriveResponse.setMessage("registerCode already registered");
                    userCarriveResponse.setData(null);
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                }
                userCarriveResponse.setMessage("success");
                userCarriveResponse.setData(registerCode);
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else if (passengerRepository.existsByEmail(email)) {
                Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
                if (passenger == null) {
                    userCarriveResponse.setMessage("driver not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                RegisterCode registerCode = registerCodeRepository.findByUser(passenger).orElse(null);
                if (registerCode == null) {
                    userCarriveResponse.setMessage("registerCode not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                int code = generateRegisterCode();
                registerCode.setCode(code);
                registerCode.setUser(passenger);
                registerCode.setExpirationDate(LocalDateTime.now(zoneId).plusMinutes(10));
                if (!registerCode.getIsOk()){
                    registerCode.setIsOk(false);
                    String emailContent = mailRegister(code);
                    mailer.setContent(emailContent);
                    try {
                        mailService.sendMail(mailer);
                        mailer.setIsGo(true);
                        mailerRepository.save(mailer);
                        registerCode = registerCodeRepository.save(registerCode);
                    } catch (Exception ex) {
                        mailer.setIsGo(false);
                        mailerRepository.save(mailer);
                        logger.error(ex.getMessage());
                        userCarriveResponse.setMessage("mail does not send");
                        userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                        userCarriveResponse.setData(null);
                    }
                }else {
                    userCarriveResponse.setMessage("registration already exist");
                    userCarriveResponse.setData(null);
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                }
                userCarriveResponse.setMessage("success");
                userCarriveResponse.setData(registerCode);
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else {
                userCarriveResponse.setMessage("user type invalide");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }

        }catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserCarriveResponse> validateRegistration(Integer code) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();
        try {

            if (code == null) {
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setMessage("Invalid code");
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            RegisterCode registerCode = registerCodeRepository.findByCode(code).orElse(null);
            if (registerCode == null) {
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setMessage("Invalid code");
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            boolean check = isValidCode(code);
            if (check){
                if (registerCode.getIsOk()){
                    userCarriveResponse.setMessage("code is already valid");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                }
                registerCode.setIsOk(true);
                if (driverRepository.existsByEmail(registerCode.getUser().getEmail())){
                    Driver driver = driverRepository.findByEmail(registerCode.getUser().getEmail()).orElse(null);
                    if (driver == null){
                        userCarriveResponse.setMessage("driver already exist");
                        userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                        userCarriveResponse.setData(null);
                        return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                    }
                    driver.setIsActive(true);
                    driver.setIsRegister(true);
                    driver = driverRepository.save(driver);
                    registerCode.setUser(driver);
                    registerCode = registerCodeRepository.save(registerCode);
                }else if (passengerRepository.existsByEmail(registerCode.getUser().getEmail())){
                    Passenger passenger = passengerRepository.findByEmail(registerCode.getUser().getEmail()).orElse(null);
                    if (passenger == null){
                        userCarriveResponse.setMessage("passenger already exist");
                        userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                        userCarriveResponse.setData(null);
                        return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                    }
                    passenger.setIsActive(true);
                    passenger.setIsRegister(true);
                    passenger = passengerRepository.save(passenger);
                    registerCode.setUser(passenger);
                    registerCode = registerCodeRepository.save(registerCode);
                }
                userCarriveResponse.setMessage("code valid");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                userCarriveResponse.setData(registerCode);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else {
                userCarriveResponse.setMessage("code invalid");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }

        }catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserCarriveResponse> login(UserLogin userLogin) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();
        try {

            if (userLogin == null){
                userCarriveResponse.setMessage("user login is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (userLogin.getEmail() == null || userLogin.getPassword() == null){
                userCarriveResponse.setMessage("email or password is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
//            if (emailValidator.isValidEmail(userLogin.getEmail())){
//                userCarriveResponse.setMessage("email invalid");
//                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
//                userCarriveResponse.setData(null);
//                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
//            }
            if (driverRepository.existsByEmail(userLogin.getEmail())){
                Driver driver = driverRepository.findByEmail(userLogin.getEmail()).orElse(null);
                if (driver == null){
                    userCarriveResponse.setMessage("driver already exist");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                if (!driver.getIsActive()){
                    userCarriveResponse.setMessage("driver is inactive");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                RegisterCode registerCode = registerCodeRepository.findByUser(driver).orElse(null);
                if (registerCode == null){
                    userCarriveResponse.setMessage("registerCode is null");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                if (!registerCode.getIsOk()){
                    userCarriveResponse.setMessage("registerCode is invalid");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                boolean checkPassword = passwordEncoder.matches(userLogin.getPassword(), driver.getPassword());
                if (!checkPassword){
                    userCarriveResponse.setMessage("password invalid");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }else {
                    authenticate(userLogin.getEmail(), userLogin.getPassword());
                }
                final UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getEmail());
                final String token = jwtTokenUtil.generateToken(userDetails);
                driver.setToken(token);
                driver.setIsConnected(true);
                driver = driverRepository.save(driver);

                userCarriveResponse.setMessage("login success");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                userCarriveResponse.setData(driver);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else if (passengerRepository.existsByEmail(userLogin.getEmail())){
                Passenger passenger = passengerRepository.findByEmail(userLogin.getEmail()).orElse(null);
                if (passenger == null){
                    userCarriveResponse.setMessage("passenger already exist");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                if (!passenger.getIsActive()){
                    userCarriveResponse.setMessage("passenger is inactive");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                RegisterCode registerCode = registerCodeRepository.findByUser(passenger).orElse(null);
                if (registerCode == null){
                    userCarriveResponse.setMessage("registerCode is null");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                if (!registerCode.getIsOk()){
                    userCarriveResponse.setMessage("registerCode is invalid");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                boolean checkPassword = passwordEncoder.matches(userLogin.getPassword(), passenger.getPassword());
                if (!checkPassword){
                    userCarriveResponse.setMessage("password invalid");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }else {
                    authenticate(userLogin.getEmail(), userLogin.getPassword());
                }
                final UserDetails userDetails = userDetailsService.loadUserByUsername(userLogin.getEmail());
                final String token = jwtTokenUtil.generateToken(userDetails);
                passenger.setToken(token);
                passenger.setIsConnected(true);
                passenger = passengerRepository.save(passenger);

                userCarriveResponse.setMessage("login success");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                userCarriveResponse.setData(passenger);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else {
                userCarriveResponse.setMessage("Username or password is invalid");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }

        }catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserCarriveResponse> forgetPassword(String email) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();
        try {

            if (email == null || email.isEmpty()){
                userCarriveResponse.setMessage("email is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }

            Integer code = generateResetCode();

            String emailContent = mailResetPassword(code);

            ResetPassword resetPassword = new ResetPassword();
            resetPassword.setCode(code);
            resetPassword.setResetDate(LocalDateTime.now(zoneId).plusMinutes(30));
            resetPassword.setUserMail(email);
            resetPassword.setIsReset(false);
            resetPassword.setCheckCode(false);

            Mailer mailer = new Mailer();
            mailer.setReceiver(email);
            mailer.setSubject("MOT DE PASSE OUBLIE?");
            mailer.setSender("stevekamga18@gmail.com");

            if (passengerRepository.existsByEmail(email)){
                Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
                if (passenger == null){
                    userCarriveResponse.setMessage("Investor not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                resetPassword.setUser(passenger);

                mailer.setContent(emailContent);
                resetPassword = resetPasswordRepository.save(resetPassword);
                try {
                    mailService.sendMail(mailer);
                    mailer.setIsGo(true);
                    mailerRepository.save(mailer);
                }catch (Exception ex) {
                    mailer.setIsGo(false);
                    mailerRepository.save(mailer);
                    logger.error(ex.getMessage());
                    userCarriveResponse.setMessage("mail does not send");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                    userCarriveResponse.setData(null);
                }
                userCarriveResponse.setData(resetPassword);
                userCarriveResponse.setMessage("send mail forgot password");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);

            }else {
                Driver driver = driverRepository.findByEmail(email).orElse(null);
                if (driver == null){
                    userCarriveResponse.setMessage("Startup not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }

                resetPassword.setUser(driver);

                mailer.setContent(emailContent);
                resetPasswordRepository.save(resetPassword);
                try {
                    mailService.sendMail(mailer);
                    mailer.setIsGo(true);
                    mailerRepository.save(mailer);
                }catch (Exception ex) {
                    mailer.setIsGo(false);
                    mailerRepository.save(mailer);
                    logger.error(ex.getMessage());
                    userCarriveResponse.setMessage("mail does not send");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                    userCarriveResponse.setData(null);
                }
                userCarriveResponse.setData(resetPassword);
                userCarriveResponse.setMessage("send mail forgot password");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }

        }catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserCarriveResponse> resendCodeReset(String email) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();
        try {

            if (email == null || email.isEmpty()) {
                userCarriveResponse.setMessage("email is null or empty");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            Mailer mailer = new Mailer();
            mailer.setReceiver(email);
            mailer.setSubject("MOT DE PASSE OUBLIE?");
            mailer.setSender("stevekamga18@gmail.com");
            if (driverRepository.existsByEmail(email)) {
                Driver driver = driverRepository.findByEmail(email).orElse(null);
                if (driver == null) {
                    userCarriveResponse.setMessage("driver not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                ResetPassword resetPassword = resetPasswordRepository.findByUser(driver).orElse(null);
                if (resetPassword == null) {
                    userCarriveResponse.setMessage("registerCode not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                int code = generateResetCode();
                resetPassword.setCode(code);
                resetPassword.setUser(driver);
                resetPassword.setResetDate(LocalDateTime.now(zoneId).plusMinutes(10));
                if (!resetPassword.getIsReset()){
                    resetPassword.setIsReset(false);
                    String emailContent = mailRegister(code);
                    mailer.setContent(emailContent);
                    try {
                        mailService.sendMail(mailer);
                        mailer.setIsGo(true);
                        mailerRepository.save(mailer);
                        resetPassword = resetPasswordRepository.save(resetPassword);
                    } catch (Exception ex) {
                        mailer.setIsGo(false);
                        mailerRepository.save(mailer);
                        logger.error(ex.getMessage());
                        userCarriveResponse.setMessage("mail does not send");
                        userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                        userCarriveResponse.setData(null);
                    }
                }else {
                    userCarriveResponse.setMessage("registerCode already registered");
                    userCarriveResponse.setData(null);
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                }
                userCarriveResponse.setMessage("success");
                userCarriveResponse.setData(resetPassword);
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else if (passengerRepository.existsByEmail(email)) {
                Passenger passenger = passengerRepository.findByEmail(email).orElse(null);
                if (passenger == null) {
                    userCarriveResponse.setMessage("driver not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                ResetPassword resetPassword = resetPasswordRepository.findByUser(passenger).orElse(null);
                if (resetPassword == null) {
                    userCarriveResponse.setMessage("registerCode not found");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
                }
                int code = generateRegisterCode();
                resetPassword.setCode(code);
                resetPassword.setUser(passenger);
                resetPassword.setResetDate(LocalDateTime.now(zoneId).plusMinutes(10));
                if (!resetPassword.getIsReset()){
                    resetPassword.setIsReset(false);
                    String emailContent = mailRegister(code);
                    mailer.setContent(emailContent);
                    try {
                        mailService.sendMail(mailer);
                        mailer.setIsGo(true);
                        mailerRepository.save(mailer);
                        resetPassword = resetPasswordRepository.save(resetPassword);
                    } catch (Exception ex) {
                        mailer.setIsGo(false);
                        mailerRepository.save(mailer);
                        logger.error(ex.getMessage());
                        userCarriveResponse.setMessage("mail does not send");
                        userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                        userCarriveResponse.setData(null);
                    }
                }else {
                    userCarriveResponse.setMessage("registration already exist");
                    userCarriveResponse.setData(null);
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                }
                userCarriveResponse.setMessage("success");
                userCarriveResponse.setData(resetPassword);
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else {
                userCarriveResponse.setMessage("user type invalide");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }

        }catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserCarriveResponse> validateEmail(Integer code) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();
        try {

            if (code == null){
                userCarriveResponse.setMessage("code is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            ResetPassword resetPassword = resetPasswordRepository.findByCode(code).orElse(null);
            if (resetPassword == null){
                userCarriveResponse.setMessage("reset password not found");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            boolean check = isValidCodeReset(code);
            if (check){
                if (resetPassword.getIsReset()){
                    userCarriveResponse.setMessage("code is already valid");
                    userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                    userCarriveResponse.setData(null);
                    return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
                }
                resetPassword.setCheckCode(true);
                resetPassword = resetPasswordRepository.save(resetPassword);
                userCarriveResponse.setMessage("code valid");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
                userCarriveResponse.setData(resetPassword);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            }else {
                userCarriveResponse.setMessage("code invalid");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }

        }catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public ResponseEntity<UserCarriveResponse> changePassword(String id, String password, String confirmPassword) {
        UserCarriveResponse userCarriveResponse = new UserCarriveResponse();
        try {

            if (id == null){
                userCarriveResponse.setMessage("email is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (password == null){
                userCarriveResponse.setMessage("password is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (confirmPassword == null){
                userCarriveResponse.setMessage("confirmPassword is null");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (!password.equals(confirmPassword)){
                userCarriveResponse.setMessage("passwords do not match");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            ResetPassword resetPassword = resetPasswordRepository.findById(id).orElse(null);
            if (resetPassword == null){
                userCarriveResponse.setMessage("reset password not found");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_NULL.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            if (!resetPassword.getCheckCode()){
                userCarriveResponse.setMessage("check code invalid");
                userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
                userCarriveResponse.setData(null);
                return new ResponseEntity<>(userCarriveResponse, HttpStatus.BAD_REQUEST);
            }
            resetPassword.setIsReset(true);
            resetPassword.setNewPassword(passwordEncoder.encode(password));
            UserCarrive userCarrive = resetPassword.getUser();
            if (driverRepository.existsByEmail(userCarrive.getEmail())){
                Driver driver = driverRepository.findByEmail(userCarrive.getEmail()).orElse(null);
                if (driver != null){
                    driver.setPassword(passwordEncoder.encode(password));
                    driver = driverRepository.save(driver);
                    resetPassword.setUser(driver);
                }
            }else {
                Passenger passenger = passengerRepository.findByEmail(userCarrive.getEmail()).orElse(null);
                if (passenger != null){
                    passenger.setPassword(passwordEncoder.encode(password));
                    passenger = passengerRepository.save(passenger);
                    resetPassword.setUser(passenger);
                }
            }

            resetPassword.setResetDate(LocalDateTime.now(zoneId));
            resetPassword = resetPasswordRepository.save(resetPassword);
            userCarriveResponse.setMessage("reset password success");
            userCarriveResponse.setCode(CodeResponseEnum.CODE_SUCCESS.getCode());
            userCarriveResponse.setData(resetPassword);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.OK);
            
        }catch (Exception e) {
            logger.error(e.getMessage());
            userCarriveResponse.setCode(CodeResponseEnum.CODE_ERROR.getCode());
            userCarriveResponse.setMessage(e.getMessage());
            userCarriveResponse.setData(null);
            return new ResponseEntity<>(userCarriveResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private Boolean isValidCode(Integer code) {
        return registerCodeRepository.findByCode(code)
                .map(registerCode -> registerCode.getExpirationDate().isAfter(LocalDateTime.now(zoneId)))
                .orElse(false);
    }

    private Boolean isValidCodeReset(Integer code) {
        return resetPasswordRepository.findByCode(code)
                .map(resetPassword -> resetPassword.getResetDate().isAfter(LocalDateTime.now(zoneId)))
                .orElse(false);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }

    private String mailResetPassword(Integer code){
        return  "<!DOCTYPE html>" +
                "<html lang=\"fr\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>REINITIALISATION DU MOT DE PASSE</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
                "        .container { background-color: white; border-radius: 5px; padding: 20px; max-width: 600px; margin: auto; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
                "        h1 { color: #333; }" +
                "        p { font-size: 16px; line-height: 1.5; color: #555; }" +
                "        .code { font-size: 24px; font-weight: bold; color: #007bff; background-color: #e9ecef; padding: 10px; border-radius: 5px; display: inline-block; margin-top: 10px; }" +
                "        .footer { margin-top: 20px; font-size: 12px; color: #777; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <h1>REINITIALISATION DU MOT DE PASSE</h1>" +
                "        <p>Veuillez entrer ce code pour changer votre mot de passe :</p>" +
                "        <div class=\"code\">" + code + "</div>" +
                "        <p class=\"footer\">Ce code est valable pour 60 minutes.</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private String mailRegister(Integer code){
        return "<!DOCTYPE html>" +
                "<html lang=\"fr\">" +
                "<head>" +
                "    <meta charset=\"UTF-8\">" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "    <title>Validation de l'inscription</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 20px; }" +
                "        .container { background-color: white; border-radius: 5px; padding: 20px; max-width: 600px; margin: auto; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }" +
                "        h1 { color: #333; }" +
                "        p { font-size: 16px; line-height: 1.5; color: #555; }" +
                "        .code { font-size: 24px; font-weight: bold; color: #007bff; background-color: #e9ecef; padding: 10px; border-radius: 5px; display: inline-block; margin-top: 10px; }" +
                "        .footer { margin-top: 20px; font-size: 12px; color: #777; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class=\"container\">" +
                "        <h1>Validation de votre inscription</h1>" +
                "        <p>Veuillez entrer ce code pour valider votre inscription :</p>" +
                "        <div class=\"code\">" + code + "</div>" +
                "        <p class=\"footer\">Ce code est valable pour 10 minutes.</p>" +
                "    </div>" +
                "</body>" +
                "</html>";
    }

    private Integer generateRegisterCode(){
        int code;
        do {
            code = Integer.parseInt(RandomStringUtils.random(5, false, true));
        } while (registerCodeRepository.existsByCode(code));
        return code;
    }

    private Integer generateResetCode(){
        int code;
        do {
            code = Integer.parseInt(RandomStringUtils.random(5, false, true));
        } while (resetPasswordRepository.existsByCode(code));
        return code;
    }
}
