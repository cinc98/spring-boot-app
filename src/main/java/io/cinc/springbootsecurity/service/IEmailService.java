package io.cinc.springbootsecurity.service;

import io.cinc.springbootsecurity.model.User;

import javax.mail.MessagingException;

public interface IEmailService {
    void sendVerificationMail(User user) throws MessagingException;
    void sendPasswordMail(User user) throws MessagingException;
}
