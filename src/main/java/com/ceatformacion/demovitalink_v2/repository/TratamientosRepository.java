package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TratamientosRepository extends JpaRepository <Tratamientos, Integer>{
    List<Tratamientos> findTratamientosByUsuario(Usuarios usuario);

    @Query("SELECT t FROM Tratamientos t WHERE t.usuario.id_usuario = :idUsuario ORDER BY t.fecha_inicio DESC")
    List<Tratamientos> findByUsuarioId(@Param("idUsuario") int idUsuario);

    // 🔎 Nuevo: búsqueda flexible para ADMIN con paginación
    @Query("""
    SELECT t FROM Tratamientos t
    WHERE (:q IS NULL OR LOWER(t.nombre_tratamiento) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:estado IS NULL OR t.estado_tratamiento = :estado)
      AND (:idUsuario IS NULL OR t.usuario.id_usuario = :idUsuario)
""")
    Page<Tratamientos> buscarAdmin(
            @Param("q") String q,
            @Param("estado") String estado,
            @Param("idUsuario") Integer idUsuario,
            Pageable pageable
    );

    // (Tus nativas para estadísticas siguen intactas)
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
