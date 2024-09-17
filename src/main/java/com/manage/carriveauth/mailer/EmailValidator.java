package com.manage.carriveauth.mailer;

import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
public class EmailValidator {

    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@(gmail\\.com|yahoo\\.com|[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    public boolean isValidEmail(String email) {
        if (email == null) {
            return true;
        }
        return !EMAIL_PATTERN.matcher(email).matches();
    }
}
