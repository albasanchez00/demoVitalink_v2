package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="mensajes",
        indexes = @Index(name="idx_mensajes_conv_time", columnList="conv_id, creado_en DESC"))
public class Mensaje {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;

    // relación
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conv_id", nullable=false)
    private Conversacion conversacion;

    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="remitente_id", nullable=false)
    private Usuarios remitente;

    @Lob @Column(columnDefinition="MEDIUMTEXT")
    private String contenido;

    @Column(nullable=false, length=10) // TEXT | FILE
    private String tipo = "TEXT";

    private String urlAdjunto;

    @Column(name="creado_en", nullable=false)
    private LocalDateTime creadoEn = LocalDateTime.now();

    // getters/setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Conversacion getConversacion() {
        return conversacion;
    }

    public void setConversacion(Conversacion conversacion) {
        this.conversacion = conversacion;
    }

    public Usuarios getRemitente() {
        return remitente;
    }

    public void setRemitente(Usuarios remitente) {
        this.remitente = remitente;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getUrlAdjunto() {
        return urlAdjunto;
    }

    public void setUrlAdjunto(String urlAdjunto) {
        this.urlAdjunto = urlAdjunto;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
}
