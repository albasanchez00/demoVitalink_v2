package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.repository.SintomasRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class SintomasServiceImpl implements SintomasService {

    private final SintomasRepository sintomasRepository;

    public SintomasServiceImpl(SintomasRepository sintomasRepository) {
        this.sintomasRepository = sintomasRepository;
    }

    @Override
    public Sintomas crear(Sintomas sintoma) {
        // Si no te llega fechaRegistro desde el controlador, puedes setearla aquí.
        // if (sintoma.getFechaRegistro() == null) sintoma.setFechaRegistro(LocalDateTime.now());
        return sintomasRepository.save(sintoma);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Sintomas> obtenerPorId(int id_sintoma) {
        return sintomasRepository.findById(id_sintoma);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sintomas> listarPorUsuario(int id_usuario) {
        return sintomasRepository.findSintomasByUsuario_Id_usuario(id_usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sintomas> listarPorUsuarioYTipo(int id_usuario, String tipo) {
        return sintomasRepository.findByUsuario_Id_usuarioAndTipo(id_usuario, tipo);
    }

    @Override
    public Optional<Sintomas> actualizar(int id_sintoma, Sintomas datos) {
        return sintomasRepository.findById(id_sintoma).map(s -> {
            // Actualiza SOLO los campos que te interese permitir cambiar
            if (datos.getTipo() != null) s.setTipo(datos.getTipo());
            if (datos.getDescripcion() != null) s.setDescripcion(datos.getDescripcion());
            if (datos.getFechaRegistro() != null) s.setFechaRegistro(datos.getFechaRegistro());
            if (datos.getUsuario() != null) s.setUsuario(datos.getUsuario()); // opcional cambiar usuario
            return s; // JPA hará flush al final de la transacción
        });
    }

    @Override
    public void eliminar(int id_sintoma) {
        if (!sintomasRepository.existsById(id_sintoma)) {
            throw new EntityNotFoundException("Síntoma no encontrado con id_sintoma=" + id_sintoma);
        }
        sintomasRepository.deleteById(id_sintoma);
    }
}
