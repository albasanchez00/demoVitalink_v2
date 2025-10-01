package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Recordatorios;
import com.ceatformacion.demovitalink_v2.model.TipoRecordatorio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface RecordatoriosRepository extends JpaRepository<Recordatorios, Integer> {
    @Query("""
         select r
         from Recordatorios r
         where r.usuario.id_usuario = :id_usuario
         order by r.fechaHora asc
         """)
    List<Recordatorios> findAllByUsuarioId(@Param("id_usuario") int id_usuario);

    @Query("""
         select r
         from Recordatorios r
         where r.usuario.id_usuario = :id_usuario
           and r.fechaHora between :desde and :hasta
         order by r.fechaHora asc
         """)
    List<Recordatorios> findAllByUsuarioIdAndFechaBetween(
            @Param("id_usuario") int id_usuario,
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);

    @Query("""
         select r
         from Recordatorios r
         where r.usuario.id_usuario = :id_usuario
           and r.tipo = :tipo
         order by r.fechaHora asc
         """)
    List<Recordatorios> findAllByUsuarioIdAndTipo(
            @Param("id_usuario") int id_usuario,
            @Param("tipo") TipoRecordatorio tipo);
}
