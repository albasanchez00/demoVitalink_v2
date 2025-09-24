package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.services.SintomasService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/sintomas") // <- cambia la base path
public class SintomasController {

    private final SintomasService sintomasService;

    public SintomasController(SintomasService sintomasService) {
        this.sintomasService = sintomasService;
    }

    /* ===================== Crear ===================== */
    @PostMapping
    public ResponseEntity<Sintomas> crear(@Valid @RequestBody Sintomas body) {
        if (body.getFechaRegistro() == null) body.setFechaRegistro(LocalDateTime.now());
        Sintomas creado = sintomasService.crear(body);
        return ResponseEntity
                .created(URI.create("/api/admin/sintomas/" + creado.getId_sintoma()))
                .body(creado);
    }

    /* ===================== Obtener por id ===================== */
    @GetMapping("/{id_sintoma}")
    public ResponseEntity<Sintomas> obtenerPorId(@PathVariable int id_sintoma) {
        return sintomasService.obtenerPorId(id_sintoma)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ===================== Listar por usuario ===================== */
    @GetMapping("/usuario/{id_usuario}") // <- evita colisión con /{id_sintoma}
    public ResponseEntity<List<Sintomas>> listarPorUsuario(@PathVariable int id_usuario) {
        List<Sintomas> lista = sintomasService.listarPorUsuario(id_usuario);
        return ResponseEntity.ok(lista);
    }

    /* ====== Listar por usuario y tipo (opcional) ====== */
    @GetMapping("/usuario/{id_usuario}/tipo/{tipo}")
    public ResponseEntity<List<Sintomas>> listarPorUsuarioYTipo(@PathVariable int id_usuario,
                                                                @PathVariable String tipo) {
        List<Sintomas> lista = sintomasService.listarPorUsuarioYTipo(id_usuario, tipo);
        return ResponseEntity.ok(lista);
    }

    /* ===================== Actualizar ===================== */
    @PutMapping("/{id_sintoma}")
    public ResponseEntity<Sintomas> actualizar(@PathVariable int id_sintoma,
                                               @Valid @RequestBody Sintomas body) {
        return sintomasService.actualizar(id_sintoma, body)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* ===================== Eliminar ===================== */
    @DeleteMapping("/{id_sintoma}")
    public ResponseEntity<Void> eliminar(@PathVariable int id_sintoma) {
        try {
            sintomasService.eliminar(id_sintoma);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

