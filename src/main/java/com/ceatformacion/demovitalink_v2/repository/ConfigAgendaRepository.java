package com.ceatformacion.demovitalink_v2.repository;


import com.ceatformacion.demovitalink_v2.model.ConfigAgenda;
import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigAgendaRepository extends JpaRepository<ConfigAgenda, Long> {
    Optional<ConfigAgenda> findByConfig(ConfigMedico config);
}