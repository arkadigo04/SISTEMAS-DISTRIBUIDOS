package com.practica2.gestionexcepciones.repository;

import com.practica2.gestionexcepciones.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Necesitamos este método para buscar al usuario
    Optional<User> findByUsername(String username);
}