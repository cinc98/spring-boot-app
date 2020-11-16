package io.cinc.springbootsecurity.repository;

import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.model.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

        VerificationToken findByToken(String token);
        VerificationToken findByUser(User user);

}
