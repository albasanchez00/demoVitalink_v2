package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

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
}