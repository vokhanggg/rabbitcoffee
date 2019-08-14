package com.rabbitcoffee.service;

import java.util.List;

import com.rabbitcoffee.model.User;

public interface UserService {
    User findById(Long id);
    User findByUsername(String username);
    List<User> findAll ();
}
