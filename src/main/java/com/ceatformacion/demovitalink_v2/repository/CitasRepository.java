package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

}
