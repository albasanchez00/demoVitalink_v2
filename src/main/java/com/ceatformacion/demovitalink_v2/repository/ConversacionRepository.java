package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Conversacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversacionRepository extends JpaRepository<Conversacion, Integer> {

    // ========== QUERIES EXISTENTES ==========

    /**
     * Conversaciones donde participa el usuario (más recientes primero)
     */
    @Query("""
           select distinct c
           from Conversacion c
           join c.miembros m
           where m.id_usuario = :userId
           order by c.creadoEn desc
           """)
    List<Conversacion> findConversacionesDeMiembro(@Param("userId") Integer userId);

    /**
     * Verifica que la conversación pertenece al usuario (membership check)
     */
    @Query("""
           select (count(c) > 0)
           from Conversacion c
           join c.miembros m
           where c.id = :convId
             and m.id_usuario = :userId
           """)
    boolean pertenece(@Param("convId") Integer convId, @Param("userId") Integer userId);

    /**
     * Busca si ya existe una DIRECT entre dos usuarios (A y B)
     */
    @Query("""
           select distinct c
           from Conversacion c
           join c.miembros m1
           join c.miembros m2
           where c.tipo = 'DIRECT'
             and m1.id_usuario = :a
             and m2.id_usuario = :b
           """)
    Optional<Conversacion> findDirectaEntre(@Param("a") Integer a, @Param("b") Integer b);

    /**
     * Búsqueda global (ADMIN) con filtros de query y tipo
     */
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

    /**
     * Busca conversación DIRECT por su clave única
     */
    Optional<Conversacion> findByTipoAndDirectKey(String tipo, String directKey);

    /**
     * Verifica membresía (alternativa al método pertenece)
     */
    @Query("""
       select count(c) > 0
       from Conversacion c
       join c.miembros m
       where c.id = :convId
         and m.id_usuario = :userId
       """)
    boolean existsByIdAndMiembro(@Param("convId") Integer convId,
                                 @Param("userId") Integer userId);

    /**
     * Elimina conversación (hard delete con Modifying)
     */
    @Transactional
    @Modifying
    @Query("DELETE FROM Conversacion c WHERE c.id = :id")
    void deleteByIdHard(@Param("id") Integer id);

    // ========== NUEVAS QUERIES PARA SISTEMA DE LECTURAS ==========

    /**
     * 🔢 Cuenta mensajes NO LEÍDOS en una conversación específica para un usuario
     *
     * Lógica: Mensajes de la conversación donde:
     * - El remitente NO es el usuario actual (no cuento mis propios mensajes)
     * - NO existe registro en tabla lecturas para ese mensaje + usuario
     */
    @Query("""
       select count(m)
       from Mensaje m
       where m.conversacion.id = :convId
         and m.remitente.id_usuario <> :userId
         and not exists (
           select 1 from Lectura l
           where l.mensaje.id = m.id
             and l.usuario.id_usuario = :userId
         )
       """)
    long contarNoLeidosEnConversacion(@Param("convId") Integer convId,
                                      @Param("userId") Integer userId);

    /**
     * 🔢 Cuenta TODOS los mensajes no leídos del usuario en TODAS sus conversaciones
     *
     * Útil para badge global en header/navbar
     */
    @Query("""
       select count(m)
       from Mensaje m
       join m.conversacion c
       join c.miembros miembro
       where miembro.id_usuario = :userId
         and m.remitente.id_usuario <> :userId
         and not exists (
           select 1 from Lectura l
           where l.mensaje.id = m.id
             and l.usuario.id_usuario = :userId
         )
       """)
    long contarNoLeidosTotales(@Param("userId") Integer userId);

    /**
     * 📋 Lista conversaciones con contador de no leídos (alternativa optimizada)
     *
     * Nota: Esta query es más eficiente que hacer múltiples llamadas individuales.
     * Retorna tuplas [Conversacion, unreadCount]
     */
    @Query("""
       select c, 
              (select count(m2)
               from Mensaje m2
               where m2.conversacion.id = c.id
                 and m2.remitente.id_usuario <> :userId
                 and not exists (
                   select 1 from Lectura l
                   where l.mensaje.id = m2.id
                     and l.usuario.id_usuario = :userId
                 )
              ) as unreadCount
       from Conversacion c
       join c.miembros m
       where m.id_usuario = :userId
       order by c.creadoEn desc
       """)
    List<Object[]> findConversacionesConNoLeidos(@Param("userId") Integer userId);
}