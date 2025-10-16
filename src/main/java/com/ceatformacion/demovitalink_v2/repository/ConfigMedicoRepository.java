package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigMedicoRepository extends JpaRepository<ConfigMedico, Long> {
    Optional<ConfigMedico> findByMedico(Usuarios medico);
}
