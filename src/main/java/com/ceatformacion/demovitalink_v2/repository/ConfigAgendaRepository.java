package com.ceatformacion.demovitalink_v2.repository;


import com.ceatformacion.demovitalink_v2.model.ConfigAgenda;
import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConfigAgendaRepository extends JpaRepository<ConfigAgenda, Long> {
    Optional<ConfigAgenda> findByConfig(ConfigMedico config);

    // Buscar la agenda por el id del médico (Usuarios.id_usuario)
    @Query("select a from ConfigAgenda a where a.config.medico.id_usuario = :idUsuario")
    Optional<ConfigAgenda> findByMedicoId(int idUsuario);

}