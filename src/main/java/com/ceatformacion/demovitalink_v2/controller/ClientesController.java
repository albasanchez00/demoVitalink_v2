package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.EmailService;
import com.ceatformacion.demovitalink_v2.utils.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
public class ClientesController {
    @Autowired
    private ClientesRepository clientesRepository;
    @Autowired
    private UsuariosRepository usuariosRepository;
    @Autowired
    EmailService emailService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. Mostrar el formulario de registro
    @GetMapping("/registroCliente")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Clientes());
        return "registroCliente"; // <-- tu plantilla HTML
    }

    // 2. Guardar cliente y crear usuario
    @PostMapping("/guardarCliente")
    public String guardarCliente(@ModelAttribute("cliente") Clientes cliente, Model model) {
        // Verifica si ya existe ese correo electrónico en algún usuario
        Optional<Usuarios> usuarioExistente = usuariosRepository.findByCliente_CorreoElectronico(cliente.getCorreoElectronico());

        if (usuarioExistente.isPresent()) {
            model.addAttribute("error", "Ya existe un usuario con ese correo.");
            return "registroCliente";
        }

        // Guarda cliente
        Clientes clienteGuardado = clientesRepository.save(cliente);

        // Crea usuario
        String passwordPlano = PasswordGenerator.generar(10);
        Usuarios nuevoUsuario = new Usuarios();
        nuevoUsuario.setUsername(cliente.getCorreoElectronico());
        nuevoUsuario.setPassword(passwordEncoder.encode(passwordPlano));
        nuevoUsuario.setRol("USER");
        nuevoUsuario.setCliente(clienteGuardado);

        usuariosRepository.save(nuevoUsuario);

        // Envía correo con credenciales
        emailService.enviarCredenciales(
                cliente.getCorreoElectronico(),
                cliente.getNombre(),
                passwordPlano
        );

        return "redirect:/registroCliente?success=true";
    }

    // 3. Lista de clientes
    @GetMapping("/lista")
    public String mostrarListaClientes(Model model) {
        model.addAttribute("clientes", clientesRepository.findAll());
        return "listaClientes";
    }

    // 4. Edición desde la lista (opcional)
    @PostMapping("/lista")
    public String editarCliente(@ModelAttribute Clientes clientesForm) {
        clientesRepository.save(clientesForm);
        return "redirect:/clientes/lista";
    }

    // 5. Buscar usuario por correo de cliente
    @GetMapping("/buscar-usuario")
    public String buscarUsuarioPorEmail(@RequestParam("email") String correoElectronico, Model model) {
        Optional<Usuarios> usuario = usuariosRepository.findByCliente_CorreoElectronico(correoElectronico);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "datosUsuario";
        } else {
            model.addAttribute("error", "No se encontró ningún usuario con ese correo.");
            return "errorUsuario";
        }
    }

    @GetMapping("/estadisticasUsuario")
    public String mostrarEstadisticas() {
        return "estadisticasUsuario"; // <-- nombre del archivo HTML en /templates
    }
    @GetMapping("/recordatorios")
    public String mostrarRecordatorios() {
        return "recordatorios"; // <-- nombre del archivo HTML en /templates
    }
    @GetMapping("/historialMedico")
    public String mostrarHistorial() {
        return "historialMedico"; // <-- nombre del archivo HTML en /templates
    }
    @GetMapping("/configUsuario")
    public String mostrarConfiguracion() {
        return "configUsuario"; // <-- nombre del archivo HTML en /templates
    }
}
