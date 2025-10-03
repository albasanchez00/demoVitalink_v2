package com.ceatformacion.demovitalink_v2.repository;

import com.ceatformacion.demovitalink_v2.model.Clientes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClientesRepository extends JpaRepository<Clientes, Integer> {
    Optional<Clientes> findClientesByIdCliente(int idCliente);
    boolean existsByCorreoElectronico(String correoElectronico); // opcional
}
