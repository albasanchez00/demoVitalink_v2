package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TratamientosRepository extends JpaRepository <Tratamientos, Integer>{
    List<Tratamientos> findTratamientosByUsuario(Usuarios usuario);

    @Query(value = """
  SELECT COALESCE(NULLIF(t.estado_tratamiento, ''), 'Sin estado') AS categoria,
         COUNT(*) AS total
  FROM tratamientos t
  WHERE t.id_usuario = :id_usuario
  GROUP BY categoria
  ORDER BY total DESC
""", nativeQuery = true)
    List<Object[]> contarPorEstado(@Param("id_usuario") int id_usuario);

    @Query(value = "SELECT COUNT(*) FROM tratamientos WHERE id_usuario = :id_usuario", nativeQuery = true)
    long countByUsuario(@Param("id_usuario") int id_usuario);

}
