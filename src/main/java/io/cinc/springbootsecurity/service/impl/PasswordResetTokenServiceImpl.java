package io.cinc.springbootsecurity.service.impl;

import io.cinc.springbootsecurity.model.PasswordResetToken;
import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.repository.PasswordResetTokenRepository;
import io.cinc.springbootsecurity.service.IPasswordResetTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Calendar;

@Service
public class PasswordResetTokenServiceImpl implements IPasswordResetTokenService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Override
    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    public PasswordResetToken findByUser(User user) {
        return passwordResetTokenRepository.findByUser(user);
    }

    @Override
    public void save(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
        passwordResetToken.setExpiryDate(calculateExpiryDate(60*24));

        passwordResetTokenRepository.save(passwordResetToken);
    }

    private Timestamp calculateExpiryDate(int expiryTimeInMinutes){
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Timestamp(cal.getTime().getTime());
    }
}
