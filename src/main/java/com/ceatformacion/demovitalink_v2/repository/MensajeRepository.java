package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {
    Page<Mensaje> findByConversacion_Id(Integer convId, Pageable pageable);
}