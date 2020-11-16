package io.cinc.springbootsecurity.service;

import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.model.VerificationToken;

public interface IVerificationTokenService {

    VerificationToken findByToken(String token);
    VerificationToken findByUser(User user);
    void save(User user, String token);

}
