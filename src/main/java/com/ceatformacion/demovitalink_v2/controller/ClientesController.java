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
import org.springframework.web.bind.annotation.*;

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
        try {
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

            // Intenta enviar el correo con credenciales
            boolean emailEnviado = emailService.enviarCredenciales(
                    cliente.getCorreoElectronico(),
                    cliente.getNombre(),
                    passwordPlano
            );

            if (!emailEnviado) {
                // El cliente y usuario se crearon, pero el correo falló
                model.addAttribute("warning", "Usuario registrado correctamente pero hubo un problema al enviar el correo con las credenciales. " +
                        "Por favor, contacte con el administrador.");
                return "registroCliente";
            }

            return "redirect:/listaClientes?success=true";

        } catch (Exception e) {
            model.addAttribute("error", "Error al procesar el registro. Por favor, inténtelo de nuevo.");
            return "registroCliente";
        }
    }

    // 3. Lista de clientes
    @GetMapping("/listaClientes")
    public String mostrarListaClientes(Model model) {
        model.addAttribute("clientes", clientesRepository.findAll()); // <- debe devolver una lista no nula
        return "listaClientes";
    }






    // 5. Buscar usuario por correo de cliente
    @GetMapping("/buscar-usuario")
    public String buscarUsuarioPorEmail(@RequestParam("email") String correoElectronico, Model model) {
        Optional<Usuarios> usuario = usuariosRepository.findByCliente_CorreoElectronico(correoElectronico);
        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "listaClientes";
        } else {
            model.addAttribute("error", "No se encontró ningún usuario con ese correo.");
            return "errorUsuario";
        }
    }

    @GetMapping("/clientes/editar/{id}")
    public String mostrarFormularioEdicion(@PathVariable int id, Model model) {
        Optional<Clientes> cliente = clientesRepository.findById(id);
        if (cliente.isPresent()) {
            model.addAttribute("cliente", cliente.get()); // <--- Esto es lo importante
            return "listaClientes"; // <--- Debe coincidir con tu archivo HTML
        } else {
            return "redirect:/listaClientes?error=notfound";
        }
    }

    @PostMapping("/clientes/actualizar")
    public String actualizarCliente(@ModelAttribute Clientes cliente) {
        clientesRepository.save(cliente);
        return "redirect:/listaClientes";
    }
    @GetMapping("/clientes/eliminar/{id}")
    public String eliminarCliente(@PathVariable int id) {
        clientesRepository.deleteById(id);
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
}
