package com.manage.carriveauth.mailer.service.impl;


import com.manage.carrive.entity.Mailer;
import com.manage.carriveauth.mailer.service.api.MailServiceApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
public class MailServiceImpl implements MailServiceApi {

    @Autowired
    private JavaMailSender javaMailSender;

    private final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);

    @Value("${spring.mail.username}")
    private String sender;

    @Override
    public void sendMail(Mailer mailer) {
        // Try block to check for exceptions
        try {
            MimeMessagePreparator preparator = message -> {
                MimeMessageHelper helper = new MimeMessageHelper(message, true);
                helper.setFrom("stevekamga18@gmail.com");
                helper.setTo(mailer.getReceiver());
                helper.setSubject(mailer.getSubject());
                helper.setText(mailer.getContent(), true); // Deuxième paramètre à vrai pour le contenu HTML
            };

            // Sending the mail
            javaMailSender.send(preparator);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
