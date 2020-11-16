package io.cinc.springbootsecurity.service.impl;

import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.model.VerificationToken;
import io.cinc.springbootsecurity.repository.VerificationTokenRepository;
import io.cinc.springbootsecurity.service.IVerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;

@Service
public class VerificationTokenServiceImpl implements IVerificationTokenService {

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Override
    public VerificationToken findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Override
    public VerificationToken findByUser(User user) {
        return verificationTokenRepository.findByUser(user);
    }

    @Override
    public void save(User user, String token) {
        VerificationToken verificationToken = new VerificationToken(token, user);

        verificationToken.setExpiryDate(calculateExpiryDate(60*24));
        verificationTokenRepository.save(verificationToken);
    }


    private Timestamp calculateExpiryDate(int expiryTimeInMinutes){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Timestamp(cal.getTime().getTime());
    }
}
