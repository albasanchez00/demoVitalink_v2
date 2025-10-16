package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import com.ceatformacion.demovitalink_v2.model.ConfigNotificaciones;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfigNotificacionesRepository extends JpaRepository<ConfigNotificaciones, Long> {
    Optional<ConfigNotificaciones> findByConfig(ConfigMedico config);
}
