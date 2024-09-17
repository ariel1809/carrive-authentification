package com.manage.carriveauth.utils;

import com.manage.carrive.entity.Driver;
import com.manage.carrive.entity.Passenger;
import com.manage.carriveutility.repository.DriverRepository;
import com.manage.carriveutility.repository.PassengerRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class InitDataBase {

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private PassengerRepository passengerRepository;

//    @PostConstruct
    @Transactional
    public void init() {
        Driver driver = driverRepository.findByEmail("stevekamga18@gmail.com").orElse(null);
        assert driver != null;
        if (driver.getIsRegister() == Boolean.FALSE) {
            driver.setIsRegister(Boolean.TRUE);
            driverRepository.save(driver);
        }
    }

//    @PostConstruct
    @Transactional
    public void init2() {
        List<Passenger> passengers = passengerRepository.findAll();
        for (Passenger passenger : passengers) {
            if (passenger.getIsRegister() == Boolean.FALSE){
                passenger.setIsRegister(true);
                passengerRepository.save(passenger);
            }
        }
    }
}
