package com.manage.carriveauth.mailer.service.api;


import com.manage.carrive.entity.Mailer;
import org.springframework.stereotype.Service;

@Service
public interface MailServiceApi {
    void sendMail(Mailer mailer);
}
