package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Conversacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {

    // Conversaciones donde participa el usuario (más recientes primero)
    @Query("""
           select distinct c
           from Conversacion c
           join c.miembros m
           where m.id_usuario = :userId
           order by c.creadoEn desc
           """)
    List<Conversacion> findConversacionesDeMiembro(@Param("userId") Integer userId);

    // Verifica que la conversación pertenece al usuario (membership check)
    @Query("""
           select (count(c) > 0)
           from Conversacion c
           join c.miembros m
           where c.id = :convId
             and m.id_usuario = :userId
           """)
    boolean pertenece(@Param("convId") Integer convId, @Param("userId") Integer userId);

    // Busca si ya existe una DIRECT entre dos usuarios (A y B)
    @Query("""
           select distinct c
           from Conversacion c
           join c.miembros m1
           join c.miembros m2
           where c.tipo = 'DIRECT'
             and m1.id_usuario = :a
             and m2.id_usuario = :b
           """)
    Optional<Conversacion> findDirectaEntre(@Param("a") Integer a,
                                            @Param("b") Integer b);

    @Query("""
       select distinct c
       from Conversacion c
       left join c.miembros m
       left join m.cliente cli
       where (:tipo is null or :tipo = '' or c.tipo = :tipo)
         and (
            :q is null or :q = '' or
            lower(coalesce(c.servicio, '')) like lower(concat('%', :q, '%')) or
            lower(coalesce(m.username, '')) like lower(concat('%', :q, '%')) or
            lower(concat(coalesce(cli.nombre,''), ' ', coalesce(cli.apellidos,''))) like lower(concat('%', :q, '%'))
         )
       order by c.creadoEn desc
       """)
    Page<Conversacion> buscarGlobal(@Param("q") String q,
                                    @Param("tipo") String tipo,
                                    Pageable pageable);
}