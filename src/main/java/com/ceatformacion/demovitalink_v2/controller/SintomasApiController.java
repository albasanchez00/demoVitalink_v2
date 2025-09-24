package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.SintomasDTO;
import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.SintomasRepository;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sintomas")
public class SintomasApiController {
    private final SintomasRepository repo;

    public SintomasApiController(SintomasRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/mios")
    public List<SintomasDTO> misSintomas(@AuthenticationPrincipal UsuariosDetails auth) {
        return repo.findByUsuarioOrderByFechaRegistroDesc(auth.getUsuario())
                .stream()
                .map(SintomasDTO::from)
                .toList();
    }


    @PostMapping
    public SintomasDTO crear(@AuthenticationPrincipal UsuariosDetails auth, @RequestBody Sintomas body) {
        body.setUsuario(auth.getUsuario());
        if (body.getFechaRegistro() == null) body.setFechaRegistro(java.time.LocalDateTime.now());
        return SintomasDTO.from(repo.save(body));
    }

    @PutMapping("/{id}")
    public SintomasDTO actualizar(@PathVariable int id,
                                 @AuthenticationPrincipal UsuariosDetails auth,
                                 @RequestBody Sintomas body) {
        Sintomas db = repo.findById(id).orElseThrow();
        // (opcional) valida que pertenece al usuario autenticado
        db.setTipo(body.getTipo());
        db.setZona(body.getZona());
        db.setDescripcion(body.getDescripcion());
        db.setFechaRegistro(body.getFechaRegistro());
        return SintomasDTO.from(repo.save(db));
    }

    @DeleteMapping("/{id}")
    public void borrar(@PathVariable int id, @AuthenticationPrincipal UsuariosDetails auth) {
        // opcional: validar propiedad antes de borrar
        repo.deleteById(id);
    }
}


