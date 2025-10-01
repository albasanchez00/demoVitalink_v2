package com.ceatformacion.demovitalink_v2.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

@Entity
public class Recordatorios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recordatorio")
    private int id_recordatorio;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuarios usuario;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String titulo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoRecordatorio tipo; // MEDICAMENTO, CITA, TRATAMIENTO, OTRO

    @NotNull
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    // usamos la TZ del usuario en capa de presentación

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Repeticion repeticion; // NONE, DAILY, WEEKLY, MONTHLY

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Canal canal; // INAPP, EMAIL, SMS

    @Size(max = 100)
    @Column(name = "vinculo_tipo", length = 100) // TRATAMIENTO, CITA
    private String vinculoTipo;

    @Column(name = "vinculo_id")
    private Integer vinculoId;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(nullable = false)
    private boolean completado = false;


    public Recordatorios() {}

    public int getId_recordatorio() { return id_recordatorio; }

    public void setId_recordatorio(int id_recordatorio) {
        this.id_recordatorio = id_recordatorio;
    }

    public Usuarios getUsuario() { return usuario; }
    public void setUsuario(Usuarios usuario) { this.usuario = usuario; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public TipoRecordatorio getTipo() { return tipo; }
    public void setTipo(TipoRecordatorio tipo) { this.tipo = tipo; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public Repeticion getRepeticion() { return repeticion; }
    public void setRepeticion(Repeticion repeticion) { this.repeticion = repeticion; }

    public Canal getCanal() { return canal; }
    public void setCanal(Canal canal) { this.canal = canal; }

    public String getVinculoTipo() { return vinculoTipo; }
    public void setVinculoTipo(String vinculoTipo) { this.vinculoTipo = vinculoTipo; }

    public Integer getVinculoId() { return vinculoId; }
    public void setVinculoId(Integer vinculoId) { this.vinculoId = vinculoId; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public boolean isCompletado() { return completado; }
    public void setCompletado(boolean completado) { this.completado = completado; }
}
