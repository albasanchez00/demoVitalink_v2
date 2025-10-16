package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.TipoSintoma;
import com.ceatformacion.demovitalink_v2.model.ZonaCorporal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SintomasService {
    Sintomas crear(Sintomas sintoma);

    Optional<Sintomas> obtenerPorId(int id_sintoma);

    List<Sintomas> listarPorUsuario(int id_usuario);

    List<Sintomas> listarPorUsuarioYTipo(int id_usuario, String tipo);

    /**
     * Actualiza campos del síntoma indicado.
     * Devuelve el registro actualizado o empty si no existe.
     */
    Optional<Sintomas> actualizar(int id_sintoma, Sintomas datos);

    /**
     * Eliminación física (DELETE) del síntoma.
     * Si prefieres borrado lógico, te preparo la variante.
     */
    void eliminar(int id_sintoma);

    /**SINTOMAS MEDICOS*/
    Page<Sintomas> listarPaginadoConFiltros(Integer idUsuario,
                                            TipoSintoma tipo,
                                            ZonaCorporal zona,
                                            LocalDateTime desde,
                                            LocalDateTime hasta,
                                            Pageable pageable);
}

