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

    // Actualizamos el método para recibir los nuevos parámetros
    public void registrarUsuario(String username, String password, String securityQuestion, String securityAnswer) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password)); // Contraseña encriptada
        user.setSecurityQuestion(securityQuestion);         // Guardamos la pregunta
        user.setSecurityAnswer(securityAnswer);             // Guardamos la respuesta secreta

        userRepository.save(user);
    }

    public User buscarPorUsuario(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public boolean resetearPassword(String username, String respuesta, String nuevaPassword) {
        // Usamos el método de arriba
        User user = buscarPorUsuario(username);

        // Si el usuario existe, tiene respuesta guardada, y coincide
        if (user != null && user.getSecurityAnswer() != null && user.getSecurityAnswer().equalsIgnoreCase(respuesta)) {
            user.setPassword(passwordEncoder.encode(nuevaPassword));
            userRepository.save(user); // Guardamos la nueva clave
            return true;
        }
        return false; // Si falla, devolvemos falso
    }
}