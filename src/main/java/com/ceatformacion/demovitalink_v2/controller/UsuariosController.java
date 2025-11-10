package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.*;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.PanelUsuarioService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import com.ceatformacion.demovitalink_v2.utils.PasswordGenerator;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;


@Controller
@RequestMapping("/usuarios")
public class UsuariosController {

    @Autowired private ClientesRepository clientesRepository;
    @Autowired private UsuariosRepository usuariosRepository;
    @Autowired private PasswordEncoder encoder;
    @Autowired private final PanelUsuarioService panelUsuarioService;

    public UsuariosController(ClientesRepository clientesRepository, UsuariosRepository usuariosRepository, PasswordEncoder encoder, PanelUsuarioService panelUsuarioService) {
        this.clientesRepository = clientesRepository;
        this.usuariosRepository = usuariosRepository;
        this.encoder = encoder;
        this.panelUsuarioService = panelUsuarioService;
    }

    // Asegura que SIEMPRE exista `${usuario}` en el Model para las vistas de este controller
    @ModelAttribute("usuario")
    public Usuarios initUsuario() {
        return new Usuarios();
    }

    // === LOGIN ===
    @GetMapping("/inicioSesion")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            @RequestParam(value = "changed", required = false) String changed,
                            Model model) {
        if (error != null)  model.addAttribute("error", "Credenciales inválidas.");
        if (logout != null) model.addAttribute("msg", "Sesión cerrada correctamente.");
        if (changed != null) model.addAttribute("msg", "Contraseña actualizada. Inicia sesión con la nueva contraseña.");
        return "usuarios/inicioSesion";
    }

    @GetMapping("/accesoDenegado")
    public String accesoDenegado(Model model) {
        model.addAttribute("error", "No tienes permisos para acceder a esta sección.");
        return "usuarios/inicioSesion";
    }

    // === VISTAS BASE ===
    @GetMapping("/panelUsuario")
    public String mostrarPanelUsuario() {
        return "panelUsuario";
    }

    @PostMapping("/panelUsuario")
    public String procesarAlgo(@ModelAttribute Usuarios usuariosForm) {
        usuariosRepository.save(usuariosForm);
        return "redirect:/usuarios/panelUsuario";
    }

    @ModelAttribute("panel")
    public PanelUsuarioVM supplyPanel(Authentication auth) {
        String user = (auth != null) ? auth.getName() : "anon";
        return panelUsuarioService.cargarPanel(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/listaUsuarios")
    public String mostrarListaUsuarios(Model model) {
        model.addAttribute("usuarios", usuariosRepository.findAll());
        return "listaUsuarios";
    }

    @PostMapping("/listaUsuarios")
    public String leerCliente(@ModelAttribute Usuarios usuariosCrud) {
        usuariosRepository.save(usuariosCrud);
        return "redirect:/usuarios/listaUsuarios";
    }

    @GetMapping("/mensajesUsuario")
    public String mensajesUsuario() { return "mensajesUsuario"; }

    @GetMapping("/registroSintomas")
    public String registroSintomas() { return "registroSintomas"; }

    @GetMapping("/registroTratamiento")
    public String registroTratamientos() { return "tratamientos"; }

    // === CONFIGURACIÓN ===
    @GetMapping("/configUsuario")
    public String configuracionUsuario(Model model,
                                       @AuthenticationPrincipal UsuariosDetails principal) {
        Usuarios u = usuariosRepository.findByIdWithCliente(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        Clientes c = u.getCliente(); // puede ser null
        model.addAttribute("usuario", u);
        model.addAttribute("cliente", c);

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("notificaciones", Boolean.TRUE);
        prefs.put("tema", "auto");
        model.addAttribute("prefs", prefs);

        return "configUsuario";
    }

    @PostMapping("/configUsuario/datos")
    @Transactional
    public String actualizarDatos(@AuthenticationPrincipal UsuariosDetails principal,
                                  @RequestParam(required = false) String nombre,
                                  @RequestParam(required = false) String apellidos,
                                  @RequestParam(required = false) String correoElectronico,
                                  @RequestParam(required = false) String telefono,
                                  RedirectAttributes ra) {

        try {
            Usuarios u = usuariosRepository.findByIdWithCliente(principal.getUsuario().getId_usuario())
                    .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

            Clientes c = u.getCliente();
            if (c == null) {
                c = new Clientes();
                clientesRepository.save(c);     // obtiene id_cliente
                u.setCliente(c);                // enlaza
                usuariosRepository.save(u);     // persiste el enlace
            }

            if (nombre != null)    c.setNombre(nombre.trim());
            if (apellidos != null) c.setApellidos(apellidos.trim());
            if (telefono != null)  c.setTelefono(telefono.trim());

            boolean cambioCorreo = false;
            if (correoElectronico != null) {
                String nuevo = correoElectronico.trim().toLowerCase();
                if (nuevo.isBlank()) {
                    ra.addFlashAttribute("msgDatosErr", "El correo no puede estar vacío.");
                    return "redirect:/usuarios/configUsuario";
                }
                String actual = c.getCorreoElectronico();
                if (actual == null || !actual.equalsIgnoreCase(nuevo)) {
                    Optional<Usuarios> dupe = usuariosRepository.findByUsernameIgnoreCase(nuevo);
                    if (dupe.isPresent() && dupe.get().getId_usuario() != u.getId_usuario()) {
                        ra.addFlashAttribute("msgDatosErr", "Ese correo ya está en uso.");
                        return "redirect:/usuarios/configUsuario";
                    }
                    c.setCorreoElectronico(nuevo);
                    cambioCorreo = true;
                }
            }

            clientesRepository.saveAndFlush(c);

            if (cambioCorreo) {
                u.setUsername(c.getCorreoElectronico());
                usuariosRepository.saveAndFlush(u);
            }

            ra.addFlashAttribute("msgDatosOk", "Datos actualizados correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("msgDatosErr", "No se pudieron guardar los datos.");
        }
        return "redirect:/usuarios/configUsuario";
    }

    @PostMapping("/configUsuario/password")
    @Transactional
    public String cambiarPassword(@AuthenticationPrincipal UsuariosDetails principal,
                                  @RequestParam String actual,
                                  @RequestParam String nueva,
                                  @RequestParam String confirmacion,
                                  HttpServletRequest request,
                                  HttpServletResponse response) {
        Usuarios u = usuariosRepository.findById(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (nueva == null || nueva.length() < 8) {
            return "redirect:/usuarios/configUsuario?perror=weak";
        }
        if (!nueva.equals(confirmacion)) {
            return "redirect:/usuarios/configUsuario?perror=nomatch";
        }

        String hashActual = u.getPassword();
        boolean esBcrypt = (hashActual != null && hashActual.length() >= 60);
        boolean okActual = esBcrypt ? encoder.matches(actual, hashActual) : actual.equals(hashActual);
        if (!okActual) {
            return "redirect:/usuarios/configUsuario?perror=badcurrent";
        }

        u.setPassword(encoder.encode(nueva));
        usuariosRepository.saveAndFlush(u);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);

        return "redirect:/usuarios/inicioSesion?changed=1";
    }

    @PostMapping("/configUsuario/preferencias")
    public String guardarPreferencias(@RequestParam(defaultValue = "false") boolean notificaciones,
                                      @RequestParam(defaultValue = "auto") String tema,
                                      RedirectAttributes ra) {
        ra.addFlashAttribute("msgPrefsOk", "Preferencias guardadas (tema: " + tema + ").");
        return "redirect:/usuarios/configUsuario";
    }

    // === ALTA DE PERSONAL SANITARIO (SOLO ADMIN) ===
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/registroUsuario")
    public String mostrarRegistroUsuario(Model model) {
        // Solo médicos; si quieres permitir ADMIN añade Rol.ADMIN aquí.
        model.addAttribute("roles", new Rol[]{ Rol.MEDICO });
        // Si prefieres permitir ambos:
        // model.addAttribute("roles", new Rol[]{ Rol.MEDICO, Rol.ADMIN });
        return "registroUsuario";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/guardarUsuario")
    @Transactional
    public String guardarUsuario(@ModelAttribute("usuario") Usuarios usuario, Model model) {
        String email = usuario.getUsername() == null ? null : usuario.getUsername().trim().toLowerCase();

        if (email == null || email.isBlank()) {
            model.addAttribute("roles", new Rol[]{ Rol.MEDICO });
            model.addAttribute("error", "El correo es obligatorio.");
            return "registroUsuario";
        }
        if (usuariosRepository.existsByUsernameIgnoreCase(email)) {
            model.addAttribute("roles", new Rol[]{ Rol.MEDICO });
            model.addAttribute("error", "Ya existe un usuario con ese correo.");
            return "registroUsuario";
        }

        usuario.setUsername(email);

        String passPlano = (usuario.getPassword() == null || usuario.getPassword().isBlank())
                ? PasswordGenerator.generar(10)
                : usuario.getPassword().trim();
        usuario.setPassword(encoder.encode(passPlano));

        if (usuario.getRol() == null) {
            usuario.setRol(Rol.MEDICO);
        }

        // Personal sanitario: no enlazamos Cliente
        usuariosRepository.save(usuario);

        // Si deseas, envía las credenciales aquí con tu EmailService
        // emailService.enviarCredenciales(email, "Personal Sanitario", passPlano);

        return "redirect:/usuarios/listaUsuarios?success=true";
    }

}