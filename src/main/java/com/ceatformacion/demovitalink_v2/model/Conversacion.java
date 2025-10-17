package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name="conversaciones")
public class Conversacion {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable=false, length=10) // DIRECT | GROUP
    private String tipo;

    @Column(length=80)
    private String servicio; // opcional (UCI, Urgencias...)

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="creado_por", nullable=false)
    private Usuarios creadoPor;

    @Column(name="creado_en", nullable=false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    @ManyToMany
    @JoinTable(
            name="conversacion_miembros",
            joinColumns=@JoinColumn(name="conv_id"),
            inverseJoinColumns=@JoinColumn(name="usuario_id")
    )
    private Set<Usuarios> miembros;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getServicio() {
        return servicio;
    }

    public void setServicio(String servicio) {
        this.servicio = servicio;
    }

    public Usuarios getCreadoPor() {
        return creadoPor;
    }

    public void setCreadoPor(Usuarios creadoPor) {
        this.creadoPor = creadoPor;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public Set<Usuarios> getMiembros() {
        return miembros;
    }

    public void setMiembros(Set<Usuarios> miembros) {
        this.miembros = miembros;
    }
}
