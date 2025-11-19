package com.ceatformacion.demovitalink_v2.services;
import com.ceatformacion.demovitalink_v2.dto.admin.ClienteCreateDTO;
import com.ceatformacion.demovitalink_v2.dto.admin.ClienteListDTO;
import com.ceatformacion.demovitalink_v2.dto.admin.UsuarioCreateResponseDTO;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
@Service
public class AdminClientesService {

    private final ClientesRepository clientesRepository;
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminClientesService(ClientesRepository clientesRepository,
                                UsuariosRepository usuariosRepository,
                                PasswordEncoder passwordEncoder) {
        this.clientesRepository = clientesRepository;
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Listar clientes con paginación y búsqueda opcional
     */
    @Transactional(readOnly = true)
    public Page<ClienteListDTO> listarClientesDTO(String query, Pageable pageable) {
        Page<Clientes> page;

        if (query != null && !query.trim().isEmpty()) {
            // Búsqueda por nombre, apellidos, email o teléfono
            page = clientesRepository.buscarPorNombreEmailOTelefono(query.trim(), pageable);
        } else {
            // Listar todos
            page = clientesRepository.findAll(pageable);
        }

        return page.map(this::toListDTO);
    }

    /**
     * Obtener un cliente por ID
     */
    @Transactional(readOnly = true)
    public ClienteListDTO obtenerClientePorId(Integer id) {
        Clientes cliente = clientesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + id));
        return toDetailDTO(cliente);
    }

    /**
     * Crear un nuevo cliente (sin usuario asociado)
     */
    @Transactional
    public ClienteListDTO crearCliente(ClienteCreateDTO dto) {
        // Validar que el email no exista ya
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
        nuevoCliente.setCiudad_id(dto.getCiudad_id());

        Clientes guardado = clientesRepository.save(nuevoCliente);
        return toListDTO(guardado);
    }

    /**
     * Actualizar un cliente existente
     */
    @Transactional
    public void actualizarCliente(Integer id, Map<String, Object> datos) {
        Clientes cliente = clientesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Actualizar solo los campos presentes en el Map
        if (datos.containsKey("nombre")) {
            cliente.setNombre((String) datos.get("nombre"));
        }
        if (datos.containsKey("apellidos")) {
            cliente.setApellidos((String) datos.get("apellidos"));
        }
        if (datos.containsKey("correoElectronico")) {
            String nuevoEmail = ((String) datos.get("correoElectronico")).trim().toLowerCase();

            // Validar que el nuevo email no esté en uso por otro usuario
            Optional<Usuarios> usuarioConEmail = usuariosRepository.findByUsernameIgnoreCase(nuevoEmail);
            if (usuarioConEmail.isPresent()) {
                Usuarios usuarioExistente = usuarioConEmail.get();
                // Permitir si es el mismo cliente
                if (usuarioExistente.getCliente() == null ||
                        !Objects.equals(usuarioExistente.getCliente().getIdCliente(), id)) {
                    throw new IllegalArgumentException("El correo ya está en uso por otro usuario");
                }
            }

            cliente.setCorreoElectronico(nuevoEmail);

            // Si tiene usuario asociado, actualizar también su username
            if (cliente.getUsuario() != null) {
                Usuarios usuario = cliente.getUsuario();
                usuario.setUsername(nuevoEmail);
                usuariosRepository.save(usuario);
            }
        }
        if (datos.containsKey("telefono")) {
            cliente.setTelefono((String) datos.get("telefono"));
        }
        if (datos.containsKey("direccion")) {
            cliente.setDireccion((String) datos.get("direccion"));
        }
        if (datos.containsKey("cp_id")) {
            cliente.setCp_id((String) datos.get("cp_id"));
        }
        if (datos.containsKey("ciudad")) {
            cliente.setCiudad_id((String) datos.get("ciudad"));
        }
        clientesRepository.save(cliente);
    }

    /**
     * Eliminar un cliente y su usuario asociado
     */
    @Transactional
    public void eliminarCliente(Integer id) {
        Clientes cliente = clientesRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Eliminar usuario asociado si existe
        if (cliente.getUsuario() != null) {
            Usuarios usuario = cliente.getUsuario();
            cliente.setUsuario(null);
            clientesRepository.save(cliente);
            usuariosRepository.delete(usuario);
        }

        // Eliminar cliente
        clientesRepository.delete(cliente);
    }

    /**
     * Crear usuario para un cliente existente
     */
    @Transactional
    public UsuarioCreateResponseDTO crearUsuarioParaCliente(Integer clienteId,
                                                            String username,
                                                            String password,
                                                            String rolStr) {
        // Buscar cliente
        Clientes cliente = clientesRepository.findById(clienteId)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));

        // Validar que el cliente no tenga ya un usuario
        if (cliente.getUsuario() != null) {
            throw new IllegalStateException("Este cliente ya tiene un usuario asociado");
        }

        // Validar que el username no exista
        if (usuariosRepository.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("El username ya está en uso");
        }

        // Parsear rol
        Rol rol;
        try {
            rol = Rol.valueOf(rolStr.replace("ROLE_", ""));
        } catch (IllegalArgumentException e) {
            rol = Rol.USER; // Por defecto
        }

        // Crear usuario
        Usuarios nuevoUsuario = new Usuarios();
        nuevoUsuario.setUsername(username);
        nuevoUsuario.setPassword(passwordEncoder.encode(password));
        nuevoUsuario.setRol(rol);
        nuevoUsuario.setCliente(cliente);

        Usuarios guardado = usuariosRepository.save(nuevoUsuario);

        // Actualizar cliente con el usuario
        cliente.setUsuario(guardado);
        clientesRepository.save(cliente);

        // Construir DTO de respuesta
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
        dto.setCp_id(cliente.getCp_id());
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
