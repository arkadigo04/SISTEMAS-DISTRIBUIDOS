package com.trabajo.gestionexcepciones.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/tienda")
public class TiendaController {

    // Inicializa el estado de la tienda en sesión
    @GetMapping
    public String mostrarTienda(HttpSession session, Model model) {
        if (session.getAttribute("pokemonedas") == null) {
            session.setAttribute("pokemonedas", 0);
            session.setAttribute("mochila", new HashMap<String, String>()); // Mapa de objetos en la mochila
        }
        return "tienda";
    }

    // Endpoint para procesar recargas con Stripe
    @PostMapping("/recargar")
    public String procesarPagoStripe(@RequestParam String numeroTarjeta,
                                     @RequestParam String titular,
                                     HttpSession session,
                                     RedirectAttributes redirectAttributes) {

        String tarjetaLimpia = numeroTarjeta.replaceAll("\\s+", "");

        if (tarjetaLimpia.startsWith("4242") && tarjetaLimpia.length() >= 12) {
            int saldoActual = (int) session.getAttribute("pokemonedas");
            session.setAttribute("pokemonedas", saldoActual + 1000);
            redirectAttributes.addFlashAttribute("pagoExito", "¡Pago procesado por Stripe! +1000 Pokémonedas para " + titular + ".");
        } else {
            redirectAttributes.addFlashAttribute("pagoError", "Transacción denegada. Tarjeta inválida (Usa la de test 4242...).");
        }
        return "redirect:/tienda";
    }

    // Gestiona la compra de un Pokémon y lo añade a la mochila
    @PostMapping("/comprar-pokemon")
    public String comprarPokemon(@RequestParam String nombrePokemon,
                                 @RequestParam String imagenUrl,
                                 @RequestParam int precio,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        int saldoActual = (int) session.getAttribute("pokemonedas");
        @SuppressWarnings("unchecked")
        Map<String, String> mochila = (Map<String, String>) session.getAttribute("mochila");

        // Validar que el Pokémon no esté ya en la mochila
        if (mochila.containsKey(nombrePokemon.toLowerCase())) {
            redirectAttributes.addFlashAttribute("compraError", "¡Ya tienes a " + nombrePokemon + " en tu mochila! No puedes comprar repetidos.");
            return "redirect:/tienda";
        }

        // Verificar que el usuario tenga saldo suficiente
        if (saldoActual >= precio) {
            session.setAttribute("pokemonedas", saldoActual - precio);
            mochila.put(nombrePokemon.toLowerCase(), imagenUrl); // Añade el Pokémon comprado a la mochila
            redirectAttributes.addFlashAttribute("compraExito", "¡Transacción completada! Has adquirido a " + nombrePokemon.toUpperCase() + ".");
        } else {
            redirectAttributes.addFlashAttribute("compraError", "Saldo insuficiente. Necesitas " + (precio - saldoActual) + " Pokémonedas más.");
        }
        return "redirect:/tienda";
    }

    // Gestiona la venta de un Pokémon de la mochila
    @PostMapping("/vender-pokemon")
    public String venderPokemon(@RequestParam String nombrePokemon,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        @SuppressWarnings("unchecked")
        Map<String, String> mochila = (Map<String, String>) session.getAttribute("mochila");

        if (mochila.containsKey(nombrePokemon.toLowerCase())) {
            mochila.remove(nombrePokemon.toLowerCase()); // Elimina el objeto de la mochila
            int saldoActual = (int) session.getAttribute("pokemonedas");
            session.setAttribute("pokemonedas", saldoActual + 300); // Abona 300 Pokémonedas por la venta

            redirectAttributes.addFlashAttribute("pagoExito", "Has vendido a " + nombrePokemon.toUpperCase() + " por 300 Pokémonedas.");
        }
        return "redirect:/tienda";
    }
}