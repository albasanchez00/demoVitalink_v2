package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.DisponibilidadMedica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface DisponibilidadMedicaRepository extends JpaRepository<DisponibilidadMedica, Long> {
    @Query(value = """
        SELECT *
        FROM disponibilidad_medica
        WHERE id_medico = :idMedico
          AND dia_semana = :dia
        ORDER BY hora_inicio
    """, nativeQuery = true)
    List<DisponibilidadMedica> findByMedicoAndDia(
            @Param("idMedico") int idMedico,
            @Param("dia") String dia  // OJO: pasaremos dia.name()
    );
}