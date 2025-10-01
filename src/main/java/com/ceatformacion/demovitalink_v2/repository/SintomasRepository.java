package com.ceatformacion.demovitalink_v2.repository;


import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SintomasRepository extends JpaRepository<Sintomas, Integer> {
    // Obtener todos los síntomas de un usuario
    @Query("SELECT s FROM Sintomas s WHERE s.usuario.id_usuario = :id_usuario")
    List<Sintomas> findSintomasByUsuario_Id_usuario(@Param("id_usuario") int id_usuario);

    // ... existing code ...
    // Opcional: buscar por tipo de síntoma dentro de un usuario
    @Query("SELECT s FROM Sintomas s WHERE s.usuario.id_usuario = :id_usuario AND s.tipo = :tipo")
    List<Sintomas> findByUsuario_Id_usuarioAndTipo(@Param("id_usuario") int id_usuario, @Param("tipo") String tipo);

    // SintomasRepository.java
    List<Sintomas> findByUsuarioOrderByFechaRegistroDesc(Usuarios usuario);

    @Query(value = """
  SELECT DATE(s.fecha_registro) AS fecha, COUNT(*) AS total
  FROM sintomas s
  WHERE s.id_usuario = :id_usuario
    AND DATE(s.fecha_registro) BETWEEN :desde AND :hasta
    AND (:tipo = 'todos' OR LOWER(s.tipo) = LOWER(:tipo))
  GROUP BY DATE(s.fecha_registro)
  ORDER BY DATE(s.fecha_registro)
""", nativeQuery = true)
    List<Object[]> contarPorDia(@Param("id_usuario") int id_usuario,
                                @Param("desde") LocalDate desde,
                                @Param("hasta") LocalDate hasta,
                                @Param("tipo") String tipo);

    @Query(value = """
  SELECT s.tipo AS categoria, COUNT(*) AS total
  FROM sintomas s
  WHERE s.id_usuario = :id_usuario
    AND DATE(s.fecha_registro) BETWEEN :desde AND :hasta
  GROUP BY s.tipo
  ORDER BY total DESC
""", nativeQuery = true)
    List<Object[]> contarPorTipo(@Param("id_usuario") int id_usuario,
                                 @Param("desde") LocalDate desde,
                                 @Param("hasta") LocalDate hasta);

    @Query(value = "SELECT COUNT(*) FROM sintomas WHERE id_usuario = :id_usuario", nativeQuery = true)
    long countByUsuario(@Param("id_usuario") int id_usuario);

    @Query(value = "SELECT MIN(DATE(fecha_registro)) FROM sintomas WHERE id_usuario = :id_usuario", nativeQuery = true)
    LocalDate minFecha(@Param("id_usuario") int id_usuario);

    @Query(value = "SELECT MAX(DATE(fecha_registro)) FROM sintomas WHERE id_usuario = :id_usuario", nativeQuery = true)
    LocalDate maxFecha(@Param("id_usuario") int id_usuario);

}

