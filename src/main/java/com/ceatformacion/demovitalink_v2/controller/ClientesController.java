package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ClientesController {
    @Autowired
    private ClientesRepository clientesRepository;

    // Mostrar formulario de registro de cliente
    @GetMapping("/registroCliente")
    public String mostrarFormularioCliente(Model model) {
        model.addAttribute("cliente", new Clientes());
        return "registroCliente";
    }

    // Guardar cliente en base de datos
    @PostMapping("/guardarCliente")
    public String guardarCliente(@ModelAttribute("cliente") Clientes cliente, Model model) {

        // Solo guardar si no existe ya un cliente con ese ID
        if (clientesRepository.findClientesByIdCliente(cliente.getIdCliente()).isEmpty()) {

            // Debug opcional: imprimir en consola los datos recibidos
            System.out.println("Nombre recibido: " + cliente.getNombre());
            System.out.println("Correo recibido: " + cliente.getCorreo_electronico());

            clientesRepository.save(cliente);
            return "redirect:/listaClientes";

        } else {
            model.addAttribute("error", "El usuario ya existe, indique uno nuevo");
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
    @GetMapping("/pedirCita")
    public String mostrarCitas() {
        return "pedirCita"; // <-- nombre del archivo HTML en /templates
    }
    @GetMapping("/recordatorios")
    public String mostrarRecordatorios() {
        return "recordatorios"; // <-- nombre del archivo HTML en /templates
    }
    @GetMapping("/historialMedico")
    public String mostrarHistorial() {
        return "historialMedico"; // <-- nombre del archivo HTML en /templates
    }
    @GetMapping("/registroTratamiento")
    public String mostrarTratamientos() {
        return "registroTratamiento"; // <-- nombre del archivo HTML en /templates
    }
    @GetMapping("/configUsuario")
    public String mostrarConfiguracion() {
        return "configUsuario"; // <-- nombre del archivo HTML en /templates
    }
}
