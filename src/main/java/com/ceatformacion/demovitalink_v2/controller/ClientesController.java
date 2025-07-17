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

    // Mostrar formulario de registro de cliente
    @GetMapping("/registroCliente")
    public String mostrarFormularioCliente(Model model) {
        model.addAttribute("cliente", new Clientes());
        return "registroCliente";
    }

    // Guardar cliente en base de datos
    @PostMapping("/guardarCliente")
    public String guardarCliente(@ModelAttribute("cliente") Clientes cliente, Model model) {

        if (clientesRepository.findClientesByIdCliente(cliente.getIdCliente()).isEmpty()) {

            // Guardar el cliente
            Clientes clienteGuardado = clientesRepository.save(cliente);

            // Generar contraseña aleatoria
            String passwordPlano = PasswordGenerator.generar(10);

            // Crear usuario asociado
            Usuarios nuevoUsuario = new Usuarios();
            nuevoUsuario.setUsername(cliente.getCorreo_electronico());
            nuevoUsuario.setPassword(passwordEncoder.encode(passwordPlano));
            nuevoUsuario.setRol("USER");
            nuevoUsuario.setCliente(clienteGuardado);

            usuariosRepository.save(nuevoUsuario);

            // Enviar credenciales por correo
            emailService.enviarCredenciales(
                    cliente.getCorreo_electronico(),
                    cliente.getNombre(),
                    passwordPlano
            );

            return "redirect:/listaClientes";

        } else {
            model.addAttribute("error", "El cliente ya existe, indique uno nuevo");
            return "registroCliente";
        }
    }

    // Mostrar lista de clientes
    @GetMapping("/listaClientes")
    public String mostrarListaClientes(Model model) {
        model.addAttribute("clientes", clientesRepository.findAll());
        return "listaClientes";
    }

    // Guardar cliente desde lista (edición rápida)
    @PostMapping("/listaClientes")
    public String leerCliente(@ModelAttribute Clientes clientesForm, Model model) {
        clientesRepository.save(clientesForm);
        return "redirect:/listaClientes";
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
