package com.practica2.gestionexcepciones.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.practica2.gestionexcepciones.service.ApiService;
import com.practica2.gestionexcepciones.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;

@Controller
public class ErrorTestController {

    @Autowired
    private ApiService apiService;

    @Autowired
    private UserService userService;

    // Herramienta para leer JSON
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @GetMapping("/")
    public String mostrarInicio() {
        return "index";
    }

    @GetMapping("/pruebas-api")
    public String mostrarPantallaPruebas() {
        return "pruebas_api";
    }

    @GetMapping("/register")
    public String mostrarRegistro() {
        return "register";
    }

    @PostMapping("/register")
    public String registrarUsuario(@RequestParam String username,
                                   @RequestParam String password,
                                   @RequestParam String confirmPassword,
                                   @RequestParam String securityQuestion,
                                   @RequestParam String securityAnswer,
                                   Model model) {

        // Comprobamos si las contraseñas coinciden
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden. Inténtalo de nuevo.");
            return "register"; // Devolvemos a la página de registro con el error
        }

        // Si coinciden, lo guardamos en la base de datos
        userService.registrarUsuario(username, password, securityQuestion, securityAnswer);

        // Redirigimos al login con mensaje de éxito
        return "redirect:/login?success";
    }
    @GetMapping("/funcional/archivo")
    public String probarArchivo(@RequestParam String nombreArchivo, Model model) throws Exception {
        String resultado = apiService.leerArchivo(nombreArchivo);
        Map<String, Object> map = objectMapper.readValue(resultado, Map.class);
        // Ahora Python nos manda una lista de Pokemons bajo la clave "equipo"
        model.addAttribute("equipoPokemon", map.get("equipo"));
        model.addAttribute("nombreArchivo", nombreArchivo);
        return "pruebas_api";
    }
    @GetMapping("/funcional/basedatos")
    public String probarBD(@RequestParam String nombreTabla, Model model) throws Exception {
        String resultado = apiService.consultarBD(nombreTabla);
        Map<String, Object> map = objectMapper.readValue(resultado, Map.class);
        model.addAttribute("datosBD", map.get("datos")); // Enviamos solo la lista de datos
        model.addAttribute("nombreTabla", nombreTabla);
        return "pruebas_api";
    }

    // NUEVO: Procesar el formulario de añadir entrenador
    @PostMapping("/funcional/basedatos/añadir")
    public String añadirEntrenador(@RequestParam String nombre, @RequestParam int medallas, Model model) {
        apiService.añadirEntrenador(nombre, medallas);
        model.addAttribute("exitoGeneral", "¡Entrenador " + nombre + " registrado correctamente en la Liga!");
        return "pruebas_api";
    }

    @GetMapping("/funcional/pokemon")
    public String probarPokemon(@RequestParam String nombrePokemon, Model model) throws Exception {
        String resultado = apiService.buscarPokemon(nombrePokemon.toLowerCase());
        Map<String, Object> map = objectMapper.readValue(resultado, Map.class);
        model.addAttribute("datosPokemon", map); // Enviamos el Pokemon al HTML
        return "pruebas_api";
    }

    @GetMapping("/forgot-password")
    public String mostrarOlvidoPassword() {
        return "forgot_password";
    }

    @PostMapping("/forgot-password")
    public String buscarUsuarioParaReseteo(@RequestParam String username, Model model) {
        var user = userService.buscarPorUsuario(username);
        if (user == null) {
            model.addAttribute("error", "No existe ningún entrenador con ese nombre en la base de datos.");
            return "forgot_password";
        }
        // Si existe, le pasamos su pregunta a la siguiente pantalla
        model.addAttribute("username", user.getUsername());

        // Formateamos la pregunta para que se vea bonita
        String preguntaBonita = switch (user.getSecurityQuestion()) {
            case "pokemon_inicial" -> "¿Cuál fue tu primer Pokémon?";
            case "ciudad_natal" -> "¿En qué ciudad naciste?";
            case "mascota_infancia" -> "¿Nombre de tu primera mascota?";
            case "lider_favorito" -> "¿Quién es tu líder de gimnasio favorito?";
            default -> "Pregunta secreta";
        };

        model.addAttribute("pregunta", preguntaBonita);
        return "reset_password";
    }

    @PostMapping("/reset-password")
    public String resetearPassword(@RequestParam String username,
                                   @RequestParam String respuesta,
                                   @RequestParam String newPassword,
                                   @RequestParam String confirmNewPassword,
                                   Model model) {

        // Verificamos si las contraseñas coinciden
        if (!newPassword.equals(confirmNewPassword)) {
            model.addAttribute("error", "Las contraseñas nuevas no coinciden.");
            model.addAttribute("username", username);
            model.addAttribute("pregunta", "Vuelve a intentarlo, fallaste al confirmar la contraseña.");
            return "reset_password";
        }

        // Verificamos la respuesta secreta
        boolean exito = userService.resetearPassword(username, respuesta, newPassword);

        if (exito) {
            return "redirect:/login?resetSuccess"; // Al login con mensaje de éxito
        } else {
            model.addAttribute("error", "La respuesta secreta es INCORRECTA.");
            model.addAttribute("username", username);
            model.addAttribute("pregunta", "¿Intentando hackear? La respuesta no es válida.");
            return "reset_password";
        }
    }

    @GetMapping("/funcional/simular-error")
    public String probarExcepciones(@RequestParam String codigo, Model model) {
        try {
            String resultado = apiService.simularError(codigo);
            model.addAttribute("exitoGeneral", "La API respondió correctamente: " + resultado);

        } catch (HttpClientErrorException e) {
            // EXCEPCIONES NO CRÍTICAS (400, 401, 404) -> Se traducen y se muestran en la misma pantalla
            String mensajeTraducido = switch (e.getStatusCode().value()) {
                case 400 -> "Atención (400): La petición está mal formulada. Por favor, revisa los datos.";
                case 401 -> "Acceso Denegado (401): No tienes permisos suficientes para ver esta información.";
                case 404 -> "Extraviado (404): No hemos podido encontrar lo que buscas en nuestros registros.";
                default -> "Advertencia: Ocurrió un error no crítico inesperado.";
            };
            model.addAttribute("error_no_critico", mensajeTraducido);
        }
        // Nota: Los errores 5xx (HttpServerErrorException) no los capturamos aquí.
        // Dejamos que suban al GlobalExceptionHandler para mostrar la pantalla crítica de caída de sistema.

        return "pruebas_api";
    }
}