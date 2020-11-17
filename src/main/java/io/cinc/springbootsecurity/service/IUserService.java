package io.cinc.springbootsecurity.service;

import io.cinc.springbootsecurity.dto.UserRequestDTO;
import io.cinc.springbootsecurity.model.User;

import java.util.List;
import java.util.Optional;

public interface IUserService {
    User findById(Long id);
    User findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll ();
    User save(UserRequestDTO userRequestDTO);
    User update(User user);


}
