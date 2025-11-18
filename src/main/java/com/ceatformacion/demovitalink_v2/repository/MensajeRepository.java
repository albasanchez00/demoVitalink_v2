package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Mensaje;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MensajeRepository extends JpaRepository<Mensaje, Integer> {

    /**
     * Mensajes de una conversación (paginados)
     */
    Page<Mensaje> findByConversacion_Id(Integer convId, Pageable pageable);

    /**
     * Cuenta total de mensajes en una conversación
     * (Útil para estadísticas en AdminChatRestController)
     */
    @Query("select count(m) from Mensaje m where m.conversacion.id = :convId")
    long countByConversacion_Id(@Param("convId") Integer convId);

    /**
     * Último mensaje de una conversación (opcional, para previews)
     */
    @Query("""
        select m from Mensaje m
        where m.conversacion.id = :convId
        order by m.creadoEn desc
        limit 1
        """)
    Mensaje findUltimoMensaje(@Param("convId") Integer convId);
}