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

    public void registrarUsuario(String username, String password, String securityQuestion, String securityAnswer) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setSecurityQuestion(securityQuestion);
        user.setSecurityAnswer(securityAnswer);

        userRepository.save(user);
    }

    public User buscarPorUsuario(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean resetearPassword(String username, String respuesta, String nuevaPassword) {
        User user = buscarPorUsuario(username);

        if (user != null && user.getSecurityAnswer() != null && user.getSecurityAnswer().equalsIgnoreCase(respuesta)) {
            user.setPassword(passwordEncoder.encode(nuevaPassword));
            userRepository.save(user);
            return true;
        }
        return false;
    }
}