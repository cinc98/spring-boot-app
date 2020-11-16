package io.cinc.springbootsecurity.service;

import io.cinc.springbootsecurity.model.User;

import javax.mail.MessagingException;

public interface IEmailService {
    void sendMail(User user) throws MessagingException;
}
