package com.raja.salessavvy.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.raja.salessavvy.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
   Optional<User> findByEmail(String email);
   Optional<User> findByUsername(String username);
}