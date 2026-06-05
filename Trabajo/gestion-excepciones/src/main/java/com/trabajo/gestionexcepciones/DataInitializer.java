package com.trabajo.gestionexcepciones;

import com.trabajo.gestionexcepciones.model.User;
import com.trabajo.gestionexcepciones.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.admin.password:1234}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");

            // Usa la contraseña configurable en lugar de hardcode
            admin.setPassword(passwordEncoder.encode(adminPassword));
            userRepository.save(admin);

            // Registro simple de creación de usuario por defecto
            System.out.println("Usuario 'admin' creado con contraseña por defecto.");
        }
    }
}