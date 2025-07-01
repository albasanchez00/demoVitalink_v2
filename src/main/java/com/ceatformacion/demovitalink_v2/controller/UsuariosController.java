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
            return "altaUsuario";
        }
    }

    @PostMapping("/listaUsuario")
    public String leerCliente(@ModelAttribute Usuarios usuariosForm, Model model){
        usuariosRepository.save(usuariosForm); //Lo guarda en la BBDD
        return "redirect:/listaUsuario";
    }

}
