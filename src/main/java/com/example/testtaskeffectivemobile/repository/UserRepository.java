package com.example.testtaskeffectivemobile.repository;

import com.example.testtaskeffectivemobile.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository  extends JpaRepository<User, String> {
    Optional<User> findByEmailIgnoreCase(String email);
}
