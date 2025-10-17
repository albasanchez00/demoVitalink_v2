package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Lectura;
import com.ceatformacion.demovitalink_v2.model.LecturaKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LecturaRepository extends JpaRepository<Lectura, LecturaKey> {

    @Query("""
           select (count(l) > 0)
           from Lectura l
           where l.mensaje.id = :mensajeId
             and l.usuario.id_usuario = :usuarioId
           """)
    boolean existsLectura(@Param("mensajeId") Integer mensajeId,
                          @Param("usuarioId") Integer usuarioId);
}