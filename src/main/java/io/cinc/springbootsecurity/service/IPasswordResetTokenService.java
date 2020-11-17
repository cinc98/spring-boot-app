package io.cinc.springbootsecurity.service;

import io.cinc.springbootsecurity.model.PasswordResetToken;
import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.model.VerificationToken;

public interface IPasswordResetTokenService {

    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(User user);
    void save(User user, String token);
}
