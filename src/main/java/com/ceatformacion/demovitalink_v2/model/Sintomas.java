package com.ceatformacion.demovitalink_v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sintomas")
public class Sintomas {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_sintoma")
    private int id_sintoma;

    // Si devuelves DTOs, esta anotación no es necesaria, pero no molesta.
    @JsonIgnoreProperties({"hibernateLazyInitializer","handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuarios usuario;

    @Column(name = "tipo", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private TipoSintoma tipo;

    @Column(name = "zona", length = 50)
    @Enumerated(EnumType.STRING)
    private ZonaCorporal zona;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "fecha_registro", nullable = false)
    private LocalDateTime fechaRegistro;

    // --- Constructores ---
    public Sintomas() {}

    public Sintomas(int id_sintoma, Usuarios usuario, TipoSintoma tipo, ZonaCorporal zona, String descripcion, LocalDateTime fechaRegistro) {
        this.id_sintoma = id_sintoma;
        this.usuario = usuario;
        this.tipo = tipo;
        this.zona = zona;
        this.descripcion = descripcion;
        this.fechaRegistro = fechaRegistro;
    }

    // --- Getters/Setters ---
    public int getId_sintoma() { return id_sintoma; }
    public void setId_sintoma(int id_sintoma) { this.id_sintoma = id_sintoma; }

    public Usuarios getUsuario() { return usuario; }
    public void setUsuario(Usuarios usuario) { this.usuario = usuario; }

    public TipoSintoma getTipo() { return tipo; }
    public void setTipo(TipoSintoma tipo) { this.tipo = tipo; }

    public ZonaCorporal getZona() { return zona; }
    public void setZona(ZonaCorporal zona) { this.zona = zona; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
