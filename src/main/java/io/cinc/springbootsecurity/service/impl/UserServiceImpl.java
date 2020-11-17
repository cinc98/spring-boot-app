package io.cinc.springbootsecurity.service.impl;

import io.cinc.springbootsecurity.dto.UserRequestDTO;
import io.cinc.springbootsecurity.model.Authority;
import io.cinc.springbootsecurity.model.User;
import io.cinc.springbootsecurity.repository.UserRepository;
import io.cinc.springbootsecurity.service.IAuthorityService;
import io.cinc.springbootsecurity.service.IEmailService;
import io.cinc.springbootsecurity.service.IUserService;
import io.cinc.springbootsecurity.service.IVerificationTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IAuthorityService authService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private IVerificationTokenService verificationTokenService;

    @Override
    public User findByUsername(String username) throws UsernameNotFoundException {
        User u = userRepository.findByUsername(username);
        return u;
    }

    public User findById(Long id) throws AccessDeniedException {
        User u = userRepository.findById(id).orElseGet(null);
        return u;
    }

    public List<User> findAll() throws AccessDeniedException {
        List<User> result = userRepository.findAll();
        return result;
    }

    @Override
    public User save(UserRequestDTO userRequestDTO) {
        User u = new User();
        u.setUsername(userRequestDTO.getUsername());
        // pre nego sto postavimo lozinku u atribut hesiramo je
        u.setPassword(passwordEncoder.encode(userRequestDTO.getPassword()));
        u.setFirstName(userRequestDTO.getFirstname());
        u.setLastName(userRequestDTO.getLastname());
        u.setEmail(userRequestDTO.getEmail());
        u.setEnabled(false);

        List<Authority> auth = authService.findByName("ROLE_USER");
        // u primeru se registruju samo obicni korisnici i u skladu sa tim im se i dodeljuje samo rola USER
        u.setAuthorities(auth);

        Optional<User> saved = Optional.of(userRepository.save(u));

        saved.ifPresent(user -> {
            try {
                String token = UUID.randomUUID().toString();
                verificationTokenService.save(saved.get(), token);

                emailService.sendVerificationMail(user);

            }catch (Exception e){
                e.printStackTrace();
            }
        });

        return saved.get();
    }

    @Override
    public User update(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
