package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.EmailService;
import com.ceatformacion.demovitalink_v2.utils.PasswordGenerator;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class ClientesController {

    @Autowired private ClientesRepository clientesRepository;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private EmailService emailService;
    @Autowired private PasswordEncoder passwordEncoder;

    // 1) Formulario de registro
    @GetMapping("/registroCliente")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Clientes());
        return "registroCliente";
    }

    // 2) Guardar cliente y crear usuario asociado (username = email)
    @PostMapping("/guardarCliente")
    @Transactional
    public String guardarCliente(@ModelAttribute("cliente") Clientes cliente, Model model) {
        try {
            // normalizar email
            String email = (cliente.getCorreoElectronico() == null) ? null
                    : cliente.getCorreoElectronico().trim().toLowerCase();

            if (email == null || email.isBlank()) {
                model.addAttribute("error", "El correo electrónico es obligatorio.");
                return "registroCliente";
            }

            // ¿existe ya un usuario con ese username?
            if (usuariosRepository.existsByUsernameIgnoreCase(email)) {
                model.addAttribute("error", "Ya existe un usuario con ese correo.");
                return "registroCliente";
            }

            // guardar cliente
            cliente.setCorreoElectronico(email);
            Clientes clienteGuardado = clientesRepository.save(cliente);

            // crear usuario
            String passwordPlano = PasswordGenerator.generar(10);
            Usuarios nuevoUsuario = new Usuarios();
            nuevoUsuario.setUsername(email);
            nuevoUsuario.setPassword(passwordEncoder.encode(passwordPlano));
            nuevoUsuario.setRol("USER");
            nuevoUsuario.setCliente(clienteGuardado);
            usuariosRepository.save(nuevoUsuario);

            // enviar credenciales
            boolean emailEnviado = emailService.enviarCredenciales(
                    email,
                    clienteGuardado.getNombre(),
                    passwordPlano
            );

            if (!emailEnviado) {
                model.addAttribute("warning",
                        "Usuario registrado correctamente pero no se pudo enviar el correo con las credenciales.");
                return "registroCliente";
            }

            return "redirect:/listaClientes?success=true";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error al procesar el registro. Por favor, inténtelo de nuevo.");
            return "registroCliente";
        }
    }

    // 3) Listado
    @GetMapping("/listaClientes")
    public String mostrarListaClientes(Model model) {
        model.addAttribute("clientes", clientesRepository.findAll());
        return "listaClientes";
    }

    // 4) Buscar usuario por correo de cliente (usa username = email)
    @GetMapping("/buscar-usuario")
    public String buscarUsuarioPorEmail(@RequestParam("email") String correoElectronico, Model model) {
        String email = (correoElectronico == null) ? null : correoElectronico.trim().toLowerCase();
        Optional<Usuarios> usuario = (email == null || email.isBlank())
                ? Optional.empty()
                : usuariosRepository.findByUsernameIgnoreCase(email);

        if (usuario.isPresent()) {
            model.addAttribute("usuario", usuario.get());
            return "listaClientes";
        } else {
            model.addAttribute("error", "No se encontró ningún usuario con ese correo.");
            return "errorUsuario";
        }
    }

    // 5) Editar (corrige el nombre del path variable)
    @GetMapping("/clientes/editar/{idCliente}")
    public String mostrarFormularioEdicion(@PathVariable("idCliente") int idCliente, Model model) {
        Optional<Clientes> cliente = clientesRepository.findById(idCliente);
        if (cliente.isPresent()) {
            model.addAttribute("cliente", cliente.get());
            return "listaClientes"; // o "editarCliente" si tienes vista específica
        } else {
            return "redirect:/listaClientes?error=notfound";
        }
    }

    @PostMapping("/clientes/actualizar")
    public String actualizarCliente(@ModelAttribute Clientes cliente) {
        // Asegúrate de que el form incluya el campo hidden id_cliente para hacer update
        clientesRepository.save(cliente);
        return "redirect:/listaClientes?updated=true";
    }

    // 6) Eliminar (corrige el nombre del path variable)
    @GetMapping("/clientes/eliminar/{idCliente}")
    public String eliminarCliente(@PathVariable("idCliente") int idCliente) {
        // Si quieres además eliminar el usuario asociado, descomenta:
        // usuariosRepository.findByCliente_IdCliente(idCliente).ifPresent(usuariosRepository::delete);

        clientesRepository.deleteById(idCliente); // con FK ON DELETE SET NULL no fallará
        return "redirect:/listaClientes?deleted=true";
    }
}
