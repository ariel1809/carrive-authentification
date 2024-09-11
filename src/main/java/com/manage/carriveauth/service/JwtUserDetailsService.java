package com.manage.carriveauth.service;

import com.manage.carrive.entity.Driver;
import com.manage.carrive.entity.Passenger;

import com.manage.carriveutility.repository.DriverRepository;
import com.manage.carriveutility.repository.PassengerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    PassengerRepository passengerRepository;

    @Autowired
    DriverRepository driverRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (driverRepository.existsByEmail(username)) {
            Driver driver = driverRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
            return new User(driver.getEmail(), driver.getPassword(), new ArrayList<>());
        } else if (passengerRepository.existsByEmail(username)) {
            Passenger passenger = passengerRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User " + username + " not found"));
            return new User(passenger.getEmail(), passenger.getPassword(), new ArrayList<>());
        } else {
            throw new UsernameNotFoundException("User " + username + " not found");
        }
    }
}
