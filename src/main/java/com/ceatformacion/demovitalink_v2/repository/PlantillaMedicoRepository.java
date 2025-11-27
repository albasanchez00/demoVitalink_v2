package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.ConfigMedico;
import com.ceatformacion.demovitalink_v2.model.PlantillaMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PlantillaMedicoRepository extends JpaRepository<PlantillaMedico, Long> {

    List<PlantillaMedico> findByConfig(ConfigMedico config);

    List<PlantillaMedico> findByConfigOrderByNombreAsc(ConfigMedico config);

    @Query("SELECT p FROM PlantillaMedico p WHERE p.config.idConfig = :configId ORDER BY p.nombre ASC")
    List<PlantillaMedico> findByConfigId(int configId);

    void deleteByConfigAndId(ConfigMedico config, Long id);
}