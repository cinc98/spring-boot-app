package io.cinc.springbootsecurity.service.impl;

import io.cinc.springbootsecurity.model.PasswordResetToken;
import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.model.VerificationToken;
import io.cinc.springbootsecurity.service.IEmailService;
import io.cinc.springbootsecurity.service.IPasswordResetTokenService;
import io.cinc.springbootsecurity.service.IVerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


@Service
public class EmailServiceImpl implements IEmailService {

    @Autowired
    private IVerificationTokenService verificationTokenService;

    @Autowired
    private IPasswordResetTokenService passwordResetTokenService;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Override
    public void sendVerificationMail(User user) throws MessagingException {
        VerificationToken verificationToken = verificationTokenService.findByUser(user);
        if(verificationToken != null){
            String token = verificationToken.getToken();
            Context context = new Context();
            context.setVariable("title","Verify your email address");
            context.setVariable("link", "http://localhost:8080/auth/activation?token=" + token);

            String body = templateEngine.process("verification", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Email address verification");
            helper.setText(body, true);
            javaMailSender.send(message);
        }
    }

    @Override
    public void sendPasswordMail(User user) throws MessagingException {
        PasswordResetToken passwordResetToken = passwordResetTokenService.findByUser(user);
        if(passwordResetToken != null){
            String token = passwordResetToken.getToken();
            Context context = new Context();
            context.setVariable("title","Forgotten password");
            context.setVariable("link", "http://localhost:8080/auth/forgotten?token=" + token);

            String body = templateEngine.process("forgotten_password", context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(user.getEmail());
            helper.setSubject("Forgotten password");
            helper.setText(body, true);
            javaMailSender.send(message);
        }
    }
}
