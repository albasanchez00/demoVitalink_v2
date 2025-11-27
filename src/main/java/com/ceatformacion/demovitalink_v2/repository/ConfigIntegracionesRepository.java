package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.ConfigIntegraciones;
import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigIntegracionesRepository extends JpaRepository<ConfigIntegraciones, Long> {
    Optional<ConfigIntegraciones> findByConfig(ConfigMedico config);
}