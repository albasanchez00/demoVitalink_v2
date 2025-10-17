package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="lecturas")
@IdClass(LecturaKey.class)
public class Lectura {
    @Id
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="mensaje_id")
    private Mensaje mensaje;

    @Id @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="usuario_id")
    private Usuarios usuario;

    @Column(name="leido_en", nullable=false)
    private LocalDateTime leidoEn = LocalDateTime.now();

    public Mensaje getMensaje() {
        return mensaje;
    }

    public void setMensaje(Mensaje mensaje) {
        this.mensaje = mensaje;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    public LocalDateTime getLeidoEn() {
        return leidoEn;
    }

    public void setLeidoEn(LocalDateTime leidoEn) {
        this.leidoEn = leidoEn;
    }
}
