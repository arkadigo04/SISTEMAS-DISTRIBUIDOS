package com.practica2.gestionexcepciones.service;

import com.practica2.gestionexcepciones.model.User;
import com.practica2.gestionexcepciones.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registrarUsuario(String username, String password) {
        User user = new User();
        user.setUsername(username);
        // Encriptamos la contraseña antes de guardar
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }
}