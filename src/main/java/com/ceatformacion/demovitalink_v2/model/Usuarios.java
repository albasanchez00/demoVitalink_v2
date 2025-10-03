package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;
import jdk.jfr.Enabled;

import java.util.List;


@Entity
@Table(
        name = "usuarios",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuarios_username", columnNames = {"username"})
        },
        indexes = {
                @Index(name = "idx_usuarios_username", columnList = "username")
        }
)
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private int id_usuario;
    @Column(name = "username", nullable = false, length = 150)
    private String username;
    @Column(name = "password", nullable = false, length = 100 /* >=60 */)
    private String password;
    private String rol;
    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "id_cliente", referencedColumnName = "id_cliente") // <— usa id_cliente
    private Clientes cliente;

    //Getters and Setters
    public Clientes getCliente() { return cliente; }
    public void setCliente(Clientes cliente) { this.cliente = cliente; }

    public int getId_usuario() {return id_usuario;}
    public void setId_usuario(int id_usuario) {this.id_usuario = id_usuario;}

    public String getUsername() {return username;}
    public void setUsername(String username) {this.username = username;}

    public String getPassword() {return password;}
    public void setPassword(String password) {this.password = password;}

    public String getRol() {return rol;}
    public void setRol(String rol) {this.rol = rol;}

    @Override
    public String toString() {
        return "Usuarios{Id:"+ id_usuario +"}" +
                "\nUsuario → " + username +
                "\nPassword → " + password +
                "\nRol → " + rol;
    }
}
