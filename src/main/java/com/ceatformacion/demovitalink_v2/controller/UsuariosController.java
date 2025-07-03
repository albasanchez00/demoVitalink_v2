package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class UsuariosController {
    @Autowired
    private UsuariosRepository usuariosRepository;

    @Autowired
    private PasswordEncoder encoder;



    @GetMapping("/registroUsuario")
    public String mostrarFormulario(Model model){
        //Le enviamos un objeto tipo Cliente para que lo reciba ek formulafio, y a partir de alli asi
        model.addAttribute("usuario", new Usuarios());
        return "registroUsuario";
    }


    @PostMapping("/guardarUsuario")
    public String guardarUsuario(@ModelAttribute Usuarios usuario,Model model) {
        if (usuariosRepository.findByUsername(usuario.getUsername()).isEmpty()) {
            Usuarios user = new Usuarios();
            user.setUsername(usuario.getUsername());
            user.setPassword(encoder.encode(usuario.getPassword()));
            user.setRol(usuario.getRol());
            usuariosRepository.save(user);
            return "redirect:/";
        }else{
            model.addAttribute("error", "El usuario ya existe, indique uno nuevo");
            return "registroUsuario";
        }
    }

    //mostrar panelUsuario.html
    @GetMapping("/panelUsuario")
    public String mostrarPanelAdmin(){
        return "panelUsuario";
    }
    @PostMapping("/panelUsuario")
    public String listadoAdmin(@ModelAttribute Usuarios usuariosForm, Model model){
        usuariosRepository.save(usuariosForm);//Se guarda en la BBDD.
        return "redirect:/panelUsuario";
    }

    //mostrar listaUsuarios.html
    @GetMapping("/listaUsuarios")
    public String mostrarListaUsuarios(Model model){
        model.addAttribute("usuarios", usuariosRepository.findAll());
        return "listaUsuarios";
    }
    @PostMapping("/listaUsuarios")
    public String leerCliente(@ModelAttribute Usuarios usuariosCrud, Model model){
        usuariosRepository.save(usuariosCrud); //Lo guarda en la BBDD
        return "redirect:/listaUsuarios";
    }
    @GetMapping("/logout")
    public String logout(){
        return "redirect:/inicioSesion";
    }




}
