package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Clientes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientesRepository extends JpaRepository<Clientes, Integer> {
    Optional<Clientes> findClientesByIdCliente(int idCliente);
    boolean existsByCorreoElectronico(String correoElectronico); // opcional
    @Query("""
    SELECT c
    FROM Clientes c
    LEFT JOIN c.usuario u
    WHERE c.medicoReferencia.id_usuario = :medicoId
      AND (:q IS NULL OR :q = '' OR
           LOWER(
             CONCAT(
               COALESCE(c.nombre,''), ' ', COALESCE(c.apellidos,''), ' ',
               COALESCE(c.correoElectronico,''), ' ', COALESCE(c.telefono,''), ' ',
               COALESCE(c.numero_tarjeta_sanitaria ,''), ' ', COALESCE(c.numero_identificacion,''), ' ',
               COALESCE(u.username,'')
             )
           ) LIKE LOWER(CONCAT('%', :q, '%')))
      AND (:estado IS NULL OR :estado = '' OR
           (:estado = 'ACTIVO'   AND u IS NOT NULL) OR
           (:estado = 'INACTIVO' AND u IS NULL))
    """)
    Page<Clientes> buscarVinculados(@Param("medicoId") Integer medicoId,
                                    @Param("q") String q,
                                    @Param("estado") String estado,
                                    Pageable pageable);

    //Panel Admin
    Page<Clientes> findAll(Pageable pageable);
    Page<Clientes> findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(String n, String a, Pageable pageable);

    /**
     * Busca clientes por nombre, apellidos, email o teléfono
     * Utilizado en el panel de administración para búsqueda dinámica
     *
     * @param q término de búsqueda
     * @param pageable configuración de paginación
     * @return página de clientes que coinciden con la búsqueda
     */
    @Query("SELECT c FROM Clientes c WHERE " +
            "LOWER(c.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "LOWER(c.correoElectronico) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
            "c.telefono LIKE CONCAT('%', :q, '%')")
    Page<Clientes> buscarPorNombreEmailOTelefono(@Param("q") String q, Pageable pageable);
}
