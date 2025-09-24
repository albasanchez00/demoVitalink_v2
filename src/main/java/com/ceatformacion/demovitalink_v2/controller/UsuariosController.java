package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/usuarios")
public class UsuariosController {

    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private CitasRepository citasRepository;
    @Autowired private PasswordEncoder encoder;

    // === LOGIN ===
    @GetMapping("/inicioSesion")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null)  model.addAttribute("error", "Credenciales inválidas.");
        if (logout != null) model.addAttribute("msg", "Sesión cerrada correctamente.");
        return "usuarios/inicioSesion"; // -> templates/usuarios/inicioSesion.html
    }

    // (opcional) página de acceso denegado
    @GetMapping("/accesoDenegado")
    public String accesoDenegado(Model model) {
        model.addAttribute("error", "No tienes permisos para acceder a esta sección.");
        return "usuarios/inicioSesion";
    }

    // === TU CÓDIGO EXISTENTE ===

    //mostrar panelUsuario.html
    @GetMapping("/panelUsuario")
    public String mostrarPanelUsuario() {
        return "panelUsuario";
    }

    // POST: procesa algo y vuelve al panel
    @PostMapping("/panelUsuario")
    public String procesarAlgo(@ModelAttribute Usuarios usuariosForm) {
        usuariosRepository.save(usuariosForm);
        return "redirect:/usuarios/panelUsuario";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listaUsuarios")
    public String mostrarListaUsuarios(Model model) {
        model.addAttribute("usuarios", usuariosRepository.findAll());
        return "listaUsuarios";
    }

    @PostMapping("/listaUsuarios")
    public String leerCliente(@ModelAttribute Usuarios usuariosCrud, Model model){
        usuariosRepository.save(usuariosCrud);
        return "redirect:/usuarios/listaUsuarios"; // <-- corrige el path
    }

    @GetMapping("/mensajesUsuario")
    public String mensajesUsuario() { return "mensajesUsuario"; }

    @GetMapping("/configUsuario")
    public String configuracionUsuario() { return "configUsuario"; }

    @GetMapping("/registroSintomas")
    public String registroSintomas() { return "registroSintomas"; }

    @GetMapping("/registroTratamiento")
    public String registroTratamientos() { return "tratamientos"; }
}