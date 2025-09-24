package com.ceatformacion.demovitalink_v2.repository;


import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

}

