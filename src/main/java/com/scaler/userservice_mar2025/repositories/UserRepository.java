package com.scaler.userservice_mar2025.repositories;

import com.scaler.userservice_mar2025.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User save(User user);


}
