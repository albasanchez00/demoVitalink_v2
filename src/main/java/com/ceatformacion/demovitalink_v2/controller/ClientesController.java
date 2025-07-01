package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
public class ClientesController {
    @Autowired
    private ClientesRepository clientesRepository;


}
