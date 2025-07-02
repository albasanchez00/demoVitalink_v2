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

    @GetMapping("/registroCliente")
    public String mostrarFormulario(Model model){
        //Le enviamos un objeto tipo Cliente para que lo reciba ek formulafio, y a partir de alli asi
        model.addAttribute("cliente", new Clientes());
        return "registroCliente";
    }

    @PostMapping("/registroCliente")
    public String guardarCliente(@ModelAttribute Clientes cliente, Model model) {
        if (clientesRepository.findClientesByIdCliente(cliente.getIdCliente()).isEmpty()) {
            Clientes client = new Clientes();
            client.setIdCliente(client.getIdCliente());
            client.setNombre(client.getNombre());
            client.setApellidos(client.getApellidos ());
            client.setNacimiento(client.getNacimiento());
            client.setGenero(client.getGenero());
            client.setCorreo_electronico(client.getCorreo_electronico());
            client.setTelefono(client.getTelefono());
            client.setTipo_documento(client.getTipo_documento());
            client.setNumero_identificacion(client.getNumero_identificacion());
            client.setNumero_tarjeta_sanitaria(client.getNumero_tarjeta_sanitaria ());
            client.setDireccion(client.getDireccion());
            client.setCiudad_id(client.getCiudad_id());
            client.setCp_id(client.getCp_id());
            clientesRepository.save(client);
            return "redirect:/";
        }else{
            model.addAttribute("error", "El usuario ya existe, indique uno nuevo");
            return "registroCliente";
        }
    }

    @PostMapping("/listaClientes")
    public String leerCliente(@ModelAttribute Clientes clientesForm, Model model){
        clientesRepository.save(clientesForm); //Lo guarda en la BBDD
        return "redirect:/listaClientes";
    }

}
