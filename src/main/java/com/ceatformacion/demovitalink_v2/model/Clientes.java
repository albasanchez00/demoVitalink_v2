package com.ceatformacion.demovitalink_v2.model;

import jakarta.persistence.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Entity
public class Clientes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private int idCliente;
    private String nombre;
    private String apellidos;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate nacimiento;
    private String correo_electronico;
    private String telefono;
    private String tipo_documento;
    private String numero_identificacion;
    private String numero_tarjeta_sanitaria;
    private String genero;
    private String direccion;
    private String ciudad_id;
    private String cp_id;

    @OneToOne(mappedBy = "cliente")
    private Usuarios usuario;


    public int getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(int idCliente) {
        this.idCliente = idCliente;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public LocalDate getNacimiento() {
        return nacimiento;
    }

    public void setNacimiento(LocalDate nacimiento) {
        this.nacimiento = nacimiento;
    }

    public String getCorreo_electronico() {
        return correo_electronico;
    }

    public void setCorreo_electronico(String correo_electronico) {
        this.correo_electronico = correo_electronico;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getTipo_documento() {
        return tipo_documento;
    }

    public void setTipo_documento(String tipo_documento) {
        this.tipo_documento = tipo_documento;
    }

    public String getNumero_identificacion() {
        return numero_identificacion;
    }

    public void setNumero_identificacion(String numero_identificacion) {
        this.numero_identificacion = numero_identificacion;
    }

    public String getNumero_tarjeta_sanitaria() {
        return numero_tarjeta_sanitaria;
    }

    public void setNumero_tarjeta_sanitaria(String numero_tarjeta_sanitaria) {
        this.numero_tarjeta_sanitaria = numero_tarjeta_sanitaria;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getCiudad_id() {
        return ciudad_id;
    }

    public void setCiudad_id(String ciudad_id) {
        this.ciudad_id = ciudad_id;
    }

    public String getCp_id() {
        return cp_id;
    }

    public void setCp_id(String cp_id) {
        this.cp_id = cp_id;
    }

    public Usuarios getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuarios usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "Clientes{Id:"+ idCliente + "}"+
                "\nUsuario → " + usuario +
                "\nNombre → " + nombre +
                "\nApellidos → " + apellidos +
                "\nNacimiento → " + nacimiento +
                "\nCorreo electrónico → " + correo_electronico +
                "\nTeléfono → " + telefono +
                "\nTipoDocumento → " + tipo_documento +
                "\nNúmero Identificación → " + numero_identificacion +
                "\nNúmero tarjeta sanitaria → " + numero_tarjeta_sanitaria +
                "\nGenero → " + genero +
                "\nDirección → " + direccion +
                "\nCiudad → " + ciudad_id +
                "\nCódigo postal → " + cp_id;
    }
}
