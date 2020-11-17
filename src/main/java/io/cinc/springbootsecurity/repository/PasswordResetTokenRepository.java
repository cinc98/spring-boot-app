package io.cinc.springbootsecurity.repository;

import io.cinc.springbootsecurity.model.PasswordResetToken;
import io.cinc.springbootsecurity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    PasswordResetToken findByToken(String token);
    PasswordResetToken findByUser(User user);
}
