package io.cinc.springbootsecurity.repository;

import io.cinc.springbootsecurity.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username );
    Optional<User> findByEmail(String email);
}
