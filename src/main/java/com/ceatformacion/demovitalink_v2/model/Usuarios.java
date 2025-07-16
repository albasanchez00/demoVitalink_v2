package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jdk.jfr.Enabled;

import java.util.List;

@Entity
public class Usuarios {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id_usuario;
    private String username;
    private String password;
    private String rol;
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
