package io.cinc.springbootsecurity.service;

import io.cinc.springbootsecurity.model.Authority;

import java.util.List;

public interface IAuthorityService {
    List<Authority> findById(Long id);
    List<Authority> findByName(String name);
}
