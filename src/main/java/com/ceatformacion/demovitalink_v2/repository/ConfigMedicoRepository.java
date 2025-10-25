package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ConfigMedicoRepository extends JpaRepository<ConfigMedico, Long> {
    Optional<ConfigMedico> findByMedico(Usuarios medico);

    @Query("SELECT c FROM ConfigMedico c WHERE c.medico.id_usuario = :idUsuario")
    Optional<ConfigMedico> findByIdUsuario(int idUsuario);

    @Query("SELECT (COUNT(c) > 0) FROM ConfigMedico c WHERE c.medico.id_usuario = :idUsuario")
    boolean existsByIdUsuario(int idUsuario);
}
