package com.ceatformacion.demovitalink_v2.controller;


import com.ceatformacion.demovitalink_v2.dto.admin.ClienteCreateDTO;
import com.ceatformacion.demovitalink_v2.dto.admin.ClienteListDTO;
import com.ceatformacion.demovitalink_v2.dto.admin.UsuarioCreateResponseDTO;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.AdminClientesService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller REST para gestión de clientes desde el panel de administración
 * Maneja las operaciones CRUD para la pestaña "Clientes" en usuariosAdmin.html
 */
@RestController
@RequestMapping("/api/admin/clientes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminClientesRestController {

    private final ClientesRepository clientesRepository;
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminClientesRestController(ClientesRepository clientesRepository,
                                UsuariosRepository usuariosRepository,
                                PasswordEncoder passwordEncoder) {
        this.clientesRepository = clientesRepository;
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ===============================
// LISTAR CLIENTES
// ===============================
    @GetMapping
    public Page<ClienteListDTO> listarClientesDTO(
            @RequestParam(value = "q", required = false) String query,
            Pageable pageable
    ) {
        Page<Clientes> page;

        if (query != null && !query.trim().isEmpty()) {
            page = clientesRepository.buscarPorNombreEmailOTelefono(query.trim(), pageable);
        } else {
            page = clientesRepository.findAll(pageable);
        }

        return page.map(this::toListDTO);
    }

    // ===============================
// OBTENER CLIENTE POR ID
// ===============================
    @GetMapping("/{id}")
    public ClienteListDTO obtenerClientePorId(@PathVariable Integer id) {
        Clientes cliente = clientesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        return toDetailDTO(cliente);
    }

    // ===============================
// CREAR CLIENTE
// ===============================
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteListDTO crearCliente(@Valid @RequestBody ClienteCreateDTO dto) {
        String email = dto.getCorreoElectronico().trim().toLowerCase();

        if (usuariosRepository.existsByUsernameIgnoreCase(email)) {
            throw new IllegalArgumentException("Ya existe un usuario con ese correo electrónico");
        }

        Clientes nuevoCliente = new Clientes();
        nuevoCliente.setNombre(dto.getNombre());
        nuevoCliente.setApellidos(dto.getApellidos());
        nuevoCliente.setCorreoElectronico(email);
        nuevoCliente.setTelefono(dto.getTelefono());
        nuevoCliente.setDireccion(dto.getDireccion());
        nuevoCliente.setCp_id(dto.getCp_id());

        Clientes guardado = clientesRepository.save(nuevoCliente);
        return toListDTO(guardado);
    }

    // ===============================
// ACTUALIZAR CLIENTE
// ===============================
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void actualizarCliente(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> datos
    ) {
        Clientes cliente = clientesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        if (datos.containsKey("nombre")) cliente.setNombre((String) datos.get("nombre"));
        if (datos.containsKey("apellidos")) cliente.setApellidos((String) datos.get("apellidos"));

        if (datos.containsKey("correoElectronico")) {
            String nuevoEmail = ((String) datos.get("correoElectronico")).trim().toLowerCase();
            Optional<Usuarios> usuarioConEmail = usuariosRepository.findByUsernameIgnoreCase(nuevoEmail);
            if (usuarioConEmail.isPresent()) {
                Usuarios usuarioExistente = usuarioConEmail.get();
                if (usuarioExistente.getCliente() == null ||
                        usuarioExistente.getCliente().getIdCliente() != id) {
                    throw new IllegalArgumentException("El correo ya está en uso por otro usuario");
                }
            }
            cliente.setCorreoElectronico(nuevoEmail);

            if (cliente.getUsuario() != null) {
                Usuarios usuario = cliente.getUsuario();
                usuario.setUsername(nuevoEmail);
                usuariosRepository.save(usuario);
            }
        }

        if (datos.containsKey("telefono")) cliente.setTelefono((String) datos.get("telefono"));
        if (datos.containsKey("direccion")) cliente.setDireccion((String) datos.get("direccion"));
        if (datos.containsKey("cp_id")) cliente.setCp_id((String) datos.get("cp_id"));

        clientesRepository.save(cliente);
    }

    // ===============================
// ELIMINAR CLIENTE
// ===============================
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarCliente(@PathVariable Integer id) {
        Clientes cliente = clientesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        if (cliente.getUsuario() != null) {
            Usuarios usuario = cliente.getUsuario();
            cliente.setUsuario(null);
            clientesRepository.save(cliente);
            usuariosRepository.delete(usuario);
        }

        clientesRepository.delete(cliente);
    }

    // ===============================
// CREAR USUARIO PARA CLIENTE
// ===============================
    @PostMapping("/{clienteId}/crear-usuario")
    public UsuarioCreateResponseDTO crearUsuarioParaCliente(
            @PathVariable Integer clienteId,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam("rol") String rolStr
    ) {
        Clientes cliente = clientesRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        if (cliente.getUsuario() != null) {
            throw new IllegalStateException("Este cliente ya tiene un usuario asociado");
        }

        if (usuariosRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("El username ya está en uso");
        }

        Rol rol;
        try {
            rol = Rol.valueOf(rolStr.replace("ROLE_", ""));
        } catch (IllegalArgumentException e) {
            rol = Rol.USER;
        }

        Usuarios nuevoUsuario = new Usuarios();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setCliente(cliente);

        Usuarios guardado = usuariosRepository.save(nuevoUsuario);

        cliente.setUsuario(guardado);
        clientesRepository.save(cliente);

        UsuarioCreateResponseDTO dto = new UsuarioCreateResponseDTO();
        dto.setId(guardado.getId_usuario());
        dto.setUsername(guardado.getUsername());
        dto.setRol("ROLE_" + guardado.getRol().name());
        dto.setClienteId(cliente.getIdCliente());

        return dto;
    }

    /**
     * Convertir entidad a DTO para listado
     */
    private ClienteListDTO toListDTO(Clientes cliente) {
        ClienteListDTO dto = new ClienteListDTO();
        dto.setId(cliente.getIdCliente());
        dto.setIdCliente(cliente.getIdCliente());
        dto.setNombre(cliente.getNombre());
        dto.setApellidos(cliente.getApellidos());
        dto.setCorreoElectronico(cliente.getCorreoElectronico());
        dto.setTelefono(cliente.getTelefono());
        dto.setDireccion(cliente.getDireccion());
        dto.setCiudad_id(cliente.getCiudad_id());
        dto.setCp_id(cliente.getCp_id());

        // Información del usuario asociado
        Usuarios usuario = cliente.getUsuario();
        if (usuario != null) {
            dto.setUsuarioId(usuario.getId_usuario());
            dto.setUsuarioUsername(usuario.getUsername());
        }

        return dto;
    }

    /**
     * Convertir entidad a DTO con todos los detalles
     */
    private ClienteListDTO toDetailDTO(Clientes cliente) {
        // Por ahora es igual al listDTO, pero puedes agregar más campos si necesitas
        return toListDTO(cliente);
    }
}
