package com.practica2.gestionexcepciones.repository;

import com.practica2.gestionexcepciones.model.MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MensajeChatRepository extends JpaRepository<MensajeChat, Long> {

    // Obtener historial global ordenado por fecha
    List<MensajeChat> findByDestinatarioOrderByFechaHoraAsc(String destinatario);

    // Obtener historial privado entre dos usuarios (yo le hablo a él, o él me habla a mí)
    @Query("SELECT m FROM MensajeChat m WHERE (m.remitente = :user1 AND m.destinatario = :user2) OR (m.remitente = :user2 AND m.destinatario = :user1) ORDER BY m.fechaHora ASC")
    List<MensajeChat> obtenerHistorialPrivado(String user1, String user2);
}