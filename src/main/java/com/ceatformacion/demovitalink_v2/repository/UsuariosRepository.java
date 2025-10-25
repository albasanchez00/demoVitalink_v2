package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuariosRepository extends JpaRepository<Usuarios, Integer> {
    Optional<Usuarios> findByUsername(String username);
    Optional<Usuarios> findByCliente_CorreoElectronico(String correoElectronico);
    @Query("select u from Usuarios u left join fetch u.cliente where u.id_usuario = :id")
    Optional<Usuarios> findByIdWithCliente(@Param("id") int id);


    boolean existsByUsernameIgnoreCase(String username);
    Optional<Usuarios> findByUsernameIgnoreCase(String username);
    Optional<Usuarios> findByCliente_IdCliente(int idCliente); // si usas el borrado en cascada manual
    List<Usuarios> findByRol(Rol rol);

    //Panel Admin
    // ya lo tendrás:
    Page<Usuarios> findByRol(Rol rol, Pageable pageable);
    Page<Usuarios> findByRolAndUsernameContainingIgnoreCase(Rol rol, String username, Pageable pageable);

    // NUEVO: listado de médicos con ORDER BY fijo por la propiedad "id_usuario"
    @Query("""
           select u
           from Usuarios u
           where u.rol = :rol
           order by u.id_usuario desc
           """)
    Page<Usuarios> findMedicosOrdenFijo(@Param("rol") Rol rol, Pageable pageable);

    // 👇 NUEVO: búsqueda ligera para autocompletado (nombre, apellidos o username)
    @Query("""
        SELECT u FROM Usuarios u
        LEFT JOIN u.cliente c
        WHERE (:q IS NULL OR :q = ''
               OR LOWER(u.username)  LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.nombre)    LIKE LOWER(CONCAT('%', :q, '%'))
               OR LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :q, '%')))
    """)
    Page<Usuarios> buscarLigeroAdmin(@Param("q") String q, Pageable pageable);

    @Query("""
   SELECT u FROM Usuarios u
   WHERE u.rol = :nombreRol
   ORDER BY u.id_usuario ASC
""")
    List<Usuarios> findByRolNombre(@Param("nombreRol") String nombreRol);

}