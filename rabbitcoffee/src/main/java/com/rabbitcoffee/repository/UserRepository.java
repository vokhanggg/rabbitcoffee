package com.rabbitcoffee.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rabbitcoffee.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername( String username );
}

