package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.EstadoCita;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitasRepository extends JpaRepository<Citas, Integer> {
    List<Citas> findCitasByUsuario(Usuarios usuario);
    // 1) Citas por día (rango)
    // Serie temporal: citas por día en rango
    @Query(value = """
  SELECT c.fecha AS fecha, COUNT(*) AS total
  FROM citas c
  WHERE c.id_usuario = :id_usuario
    AND c.fecha BETWEEN :desde AND :hasta
  GROUP BY c.fecha
  ORDER BY c.fecha
""", nativeQuery = true)
    List<Object[]> contarPorDia(@Param("id_usuario") int id_usuario,
                                @Param("desde") LocalDate desde,
                                @Param("hasta") LocalDate hasta);

    @Query(value = """
  SELECT CASE WHEN TIMESTAMP(c.fecha, c.hora) >= NOW() THEN 'PROXIMA' ELSE 'PASADA' END AS categoria,
         COUNT(*) AS total
  FROM citas c
  WHERE c.id_usuario = :id_usuario
    AND c.fecha BETWEEN :desde AND :hasta
  GROUP BY categoria
  ORDER BY categoria
""", nativeQuery = true)
    List<Object[]> contarProximasVsPasadas(@Param("id_usuario") int id_usuario,
                                           @Param("desde") LocalDate desde,
                                           @Param("hasta") LocalDate hasta);

    @Query(value = "SELECT COUNT(*) FROM citas WHERE id_usuario = :id_usuario", nativeQuery = true)
    long countByUsuario(@Param("id_usuario") int id_usuario);

    @Query(value = "SELECT MIN(fecha) FROM citas WHERE id_usuario = :id_usuario", nativeQuery = true)
    LocalDate minFecha(@Param("id_usuario") int id_usuario);

    @Query(value = "SELECT MAX(fecha) FROM citas WHERE id_usuario = :id_usuario", nativeQuery = true)
    LocalDate maxFecha(@Param("id_usuario") int id_usuario);

    // Lista por médico y rango
    @Query("""
       SELECT c
       FROM Citas c
       WHERE c.medico.id_usuario = :idMedico
         AND c.fecha BETWEEN :desde AND :hasta
       ORDER BY c.fecha ASC, c.hora ASC
       """)
    List<Citas> findAgendaMedicoBetween(int idMedico, LocalDate desde, LocalDate hasta);

    // Solo por médico (sin rango)
    @Query("""
       SELECT c
       FROM Citas c
       WHERE c.medico.id_usuario = :idMedico
       ORDER BY c.fecha ASC, c.hora ASC
       """)
    List<Citas> findAgendaMedico(int idMedico);

    // (Opcional) con estado
    @Query("""
       SELECT c
       FROM Citas c
       WHERE c.medico.id_usuario = :idMedico
         AND c.estado = :estado
         AND c.fecha BETWEEN :desde AND :hasta
       ORDER BY c.fecha ASC, c.hora ASC
       """)
    List<Citas> findAgendaMedicoByEstado(int idMedico, EstadoCita estado, LocalDate desde, LocalDate hasta);

    // (Métricas para cards)
    @Query("SELECT COUNT(c) FROM Citas c WHERE c.medico.id_usuario = :idMedico AND c.fecha = :hoy")
    long countHoyPorMedico(int idMedico, LocalDate hoy);

    @Query("SELECT COUNT(c) FROM Citas c WHERE c.medico.id_usuario = :idMedico AND c.fecha BETWEEN :desde AND :hasta")
    long countEntreFechasPorMedico(int idMedico, LocalDate desde, LocalDate hasta);
    // CitasRepository
    @Query("""
   select c
   from Citas c
   join c.usuario u
   join u.cliente cli
   where c.medico.id_usuario = :idMedico
     and cli.medicoReferencia.id_usuario = :idMedico
     and c.fecha between :desde and :hasta
   order by c.fecha asc, c.hora asc
""")
    List<Citas> findAgendaMedicoSoloDeSusPacientes(int idMedico, LocalDate desde, LocalDate hasta);

    @Query("""
  SELECT MAX(ci.fecha)
  FROM Citas ci
  WHERE ci.usuario.id_usuario = :usuarioId
""")
    java.time.LocalDateTime findUltimaConsultaByUsuarioId(Integer usuarioId);
    @Query("""
    SELECT ci
    FROM Citas ci
    WHERE ci.usuario.id_usuario = :usuarioId
    ORDER BY ci.fecha DESC, ci.hora DESC
""")
    List<Citas> findTopByUsuarioOrderByFechaHoraDesc(Integer usuarioId);

    // ADMIN
    @Query("""
   SELECT c FROM Citas c
   WHERE (:q IS NULL OR :q = '' OR
          LOWER(c.titulo) LIKE LOWER(CONCAT('%', :q, '%')) OR
          LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :q, '%')))
     AND (:estado IS NULL OR c.estado = :estado)
     AND (:desde IS NULL OR c.fecha >= :desde)
     AND (:hasta IS NULL OR c.fecha <= :hasta)
     AND (:idPaciente IS NULL OR c.usuario.id_usuario = :idPaciente)
     AND (:idMedico  IS NULL OR c.medico.id_usuario  = :idMedico)
""")
    Page<Citas> buscarAdmin(
            @Param("q") String q,
            @Param("estado") EstadoCita estado,
            @Param("desde") java.time.LocalDate desde,
            @Param("hasta") java.time.LocalDate hasta,
            @Param("idPaciente") Integer idPaciente,
            @Param("idMedico") Integer idMedico,
            Pageable pageable
    );

    // horas ocupadas de un médico en una fecha:
    @Query("""
           select c
             from Citas c
            where c.medico.id_usuario = :medicoId
              and c.fecha = :fecha
           """)
    List<Citas> findByMedicoAndFecha(int medicoId, LocalDate fecha);
}
