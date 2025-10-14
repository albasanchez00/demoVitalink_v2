package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.EmailService;
import com.ceatformacion.demovitalink_v2.utils.PasswordGenerator;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
public class ClientesController {
    private static final Logger log = LoggerFactory.getLogger(ClientesController.class);

    private final ClientesRepository clientesRepository;
    private final UsuariosRepository usuariosRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public ClientesController(ClientesRepository clientesRepository,
                              UsuariosRepository usuariosRepository,
                              EmailService emailService,
                              PasswordEncoder passwordEncoder) {
        this.clientesRepository = clientesRepository;
        this.usuariosRepository = usuariosRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    // 1) Formulario de registro de Cliente
    @GetMapping("/registroCliente")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("cliente", new Clientes());
        model.addAttribute("roles", Rol.values()); // para <select name="rol">
        return "registroCliente";
    }


    // 2) Guardar cliente y crear usuario asociado (username = email)
    @PostMapping("/guardarCliente")
    @Transactional
    public String guardarCliente(@ModelAttribute("cliente") Clientes cliente,
                                 @RequestParam(value = "rol", required = false) String rolParam,
                                 Model model) {
        try {
            // normalizar email
            String email = (cliente.getCorreoElectronico() == null)
                    ? null
                    : cliente.getCorreoElectronico().trim().toLowerCase();

            if (email == null || email.isBlank()) {
                model.addAttribute("error", "El correo electrónico es obligatorio.");
                prepararRoles(model);
                return "registroCliente";
            }

            // ¿ya existe usuario con ese username?
            if (usuariosRepository.existsByUsernameIgnoreCase(email)) {
                model.addAttribute("error", "Ya existe un usuario con ese correo.");
                prepararRoles(model);
                return "registroCliente";
            }

            // guardar Cliente
            cliente.setCorreoElectronico(email);
            Clientes clienteGuardado = clientesRepository.save(cliente);

            Rol rol = Rol.USER; // <-- todos los clientes serán USER

            String passwordPlano = PasswordGenerator.generar(10);
            Usuarios nuevoUsuario = new Usuarios();
            nuevoUsuario.setUsername(email); // username = correo
            nuevoUsuario.setPassword(passwordEncoder.encode(passwordPlano));
            nuevoUsuario.setRol(rol);
            nuevoUsuario.setCliente(clienteGuardado);
            usuariosRepository.save(nuevoUsuario);

            // enviar credenciales
            boolean emailEnviado = emailService.enviarCredenciales(
                    email, clienteGuardado.getNombre(), passwordPlano
            );

            if (!emailEnviado) {
                model.addAttribute("warning",
                        "Usuario registrado correctamente pero no se pudo enviar el correo con las credenciales.");
                prepararRoles(model);
                return "registroCliente";
            }

            return "redirect:/listaClientes?success=true";

        } catch (Exception e) {
            log.error("Error al registrar cliente/usuario", e);
            model.addAttribute("error", "Error al procesar el registro. Por favor, inténtelo de nuevo.");
            prepararRoles(model);
            return "registroCliente";
        }
    }

    // 3) Listado
    @GetMapping("/listaClientes")
    public String mostrarListaClientes(Model model) {
        model.addAttribute("clientes", clientesRepository.findAll());
        return "listaClientes";
    }

    // 4) Buscar usuario por correo (username = email)
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
    private void cargarMedicos(Model model) {
        model.addAttribute("medicos", usuariosRepository.findByRol(Rol.MEDICO));
    }
    // 5) Editar
    // ====== EDICIÓN ======
    @GetMapping("/clientes/editar/{idCliente}")
    public String mostrarFormularioEdicion(@PathVariable int idCliente, Model model) {
        return clientesRepository.findById(idCliente)
                .map(c -> {
                    model.addAttribute("cliente", c);
                    cargarMedicos(model);
                    Integer sel = (c.getMedicoReferencia()!=null) ? c.getMedicoReferencia().getId_usuario() : null;
                    model.addAttribute("medicoSeleccionadoId", sel);
                    return "editarCliente"; // <-- debe existir como editarCliente.html
                })
                .orElse("redirect:/listaClientes?error=notfound");
    }

    @PostMapping("/clientes/actualizar")
    @Transactional
    public String actualizarCliente(@ModelAttribute Clientes form,
                                    @RequestParam(value = "medicoId", required = false) Integer medicoId) {

        // OJO: ajusta el getter del ID según tu entidad (getIdCliente vs getId_cliente)
        Optional<Clientes> opt = clientesRepository.findById(form.getIdCliente());
        if (opt.isPresent()) {
            Clientes cdb = opt.get();
            cdb.setNombre(form.getNombre());
            cdb.setApellidos(form.getApellidos());
            cdb.setTelefono(form.getTelefono());
            cdb.setCorreoElectronico(
                    form.getCorreoElectronico() == null ? null : form.getCorreoElectronico().trim().toLowerCase()
            );

            if (medicoId != null) {
                usuariosRepository.findById(medicoId).ifPresentOrElse(m -> {
                    if (m.getRol() == Rol.MEDICO) cdb.setMedicoReferencia(m);
                    else cdb.setMedicoReferencia(null);
                }, () -> cdb.setMedicoReferencia(null));
            } else {
                cdb.setMedicoReferencia(null);
            }

            clientesRepository.save(cdb);
        }
        return "redirect:/listaClientes?updated=true";
    }

    // 6) Eliminar
    @GetMapping("/clientes/eliminar/{idCliente}")
    public String eliminarCliente(@PathVariable("idCliente") int idCliente) {
        // Si quieres eliminar también el usuario asociado, añade aquí la lógica
        clientesRepository.deleteById(idCliente);
        return "redirect:/listaClientes?deleted=true";
    }

    // ---------- helpers ----------
    private Rol parseRolOrDefault(String rolParam, Rol def) {
        try {
            return Rol.valueOf(rolParam == null ? def.name() : rolParam.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return def;
        }
    }

    private void prepararRoles(Model model) {
        if (!model.containsAttribute("roles")) {
            model.addAttribute("roles", Rol.values());
        }
    }
}
