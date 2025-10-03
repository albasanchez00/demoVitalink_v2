package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
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
    @Autowired private CitasRepository citasRepository;
    @Autowired private PasswordEncoder encoder;

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

    // GET página de configuración (carga usuario + cliente con fetch join)
    // === GET página de configuración ===
    @GetMapping("/configUsuario")
    public String configuracionUsuario(Model model,
                                       @AuthenticationPrincipal UsuariosDetails principal) {
        Usuarios u = usuariosRepository.findByIdWithCliente(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        Clientes c = u.getCliente(); // puede ser null
        model.addAttribute("usuario", u);
        model.addAttribute("cliente", c);

        // Prefs demo (si aún no las persistes)
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("notificaciones", Boolean.TRUE);
        prefs.put("tema", "auto");
        model.addAttribute("prefs", prefs);

        return "configUsuario";
    }

    // === POST Datos personales (crea Cliente si no existe) ===
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

            // 1) Si NO hay cliente, lo creamos y enlazamos
            Clientes c = u.getCliente();
            if (c == null) {
                c = new Clientes();
                // Inicializa lo mínimo (puedes copiar nombre/email si quieres)
                clientesRepository.save(c);     // obtiene id_cliente
                u.setCliente(c);                // enlaza
                usuariosRepository.save(u);     // persiste el enlace (id_cliente en usuarios)
            }

            // 2) Normaliza y guarda campos del cliente
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
                    // Evita duplicados de username
                    Optional<Usuarios> dupe = usuariosRepository.findByUsernameIgnoreCase(nuevo);
                    if (dupe.isPresent() && dupe.get().getId_usuario() != u.getId_usuario()) {
                        ra.addFlashAttribute("msgDatosErr", "Ese correo ya está en uso.");
                        return "redirect:/usuarios/configUsuario";
                    }
                    c.setCorreoElectronico(nuevo);
                    cambioCorreo = true;
                }
            }

            // 3) Guarda cliente
            clientesRepository.saveAndFlush(c);

            // 4) Si cambió el correo, sincroniza username
            if (cambioCorreo) {
                u.setUsername(c.getCorreoElectronico());
                usuariosRepository.saveAndFlush(u);
            }

            ra.addFlashAttribute("msgDatosOk", "Datos actualizados correctamente.");
        } catch (org.springframework.dao.DataIntegrityViolationException dive) {
            ra.addFlashAttribute("msgDatosErr", "Restricción de datos: " + dive.getMostSpecificCause().getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            ra.addFlashAttribute("msgDatosErr", "No se pudieron guardar los datos.");
        }
        return "redirect:/usuarios/configUsuario";
    }

    // === POST Cambiar contraseña ===
    @PostMapping("/configUsuario/password")
    @org.springframework.transaction.annotation.Transactional
    public String cambiarPassword(@AuthenticationPrincipal UsuariosDetails principal,
                                  @RequestParam String actual,
                                  @RequestParam String nueva,
                                  @RequestParam String confirmacion,
                                  HttpServletRequest request,           // jakarta.*
                                  HttpServletResponse response) {       // jakarta.*
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

        // 1) Guardar nueva contraseña (BCrypt via CompositePasswordEncoder)
        u.setPassword(encoder.encode(nueva));
        usuariosRepository.saveAndFlush(u);

        // 2) Cerrar sesión actual para exigir login con la nueva clave
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        new SecurityContextLogoutHandler().logout(request, response, auth);

        // 3) Redirigir al login (GET) con un flag para mostrar aviso
        return "redirect:/inicioSesion?changed=1";
    }


    // === POST Preferencias (si aún no las persistes) ===
    @PostMapping("/configUsuario/preferencias")
    public String guardarPreferencias(@RequestParam(defaultValue = "false") boolean notificaciones,
                                      @RequestParam(defaultValue = "auto") String tema,
                                      RedirectAttributes ra) {
        ra.addFlashAttribute("msgPrefsOk", "Preferencias guardadas (tema: " + tema + ").");
        return "redirect:/usuarios/configUsuario";
    }
}