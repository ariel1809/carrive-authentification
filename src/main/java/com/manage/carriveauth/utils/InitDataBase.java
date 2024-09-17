package com.manage.carriveauth.utils;

import com.manage.carrive.entity.Driver;
import com.manage.carriveutility.repository.DriverRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitDataBase {

    @Autowired
    private DriverRepository driverRepository;

    @PostConstruct
    @Transactional
    public void init() {
        Driver driver = driverRepository.findByEmail("stevekamga18@gmail.com").orElse(null);
        if (driver != null) {
            if (driver.getIsActive() == null){
                driver.setIsActive(true);
                driverRepository.save(driver);
            }
        }
    }
}
