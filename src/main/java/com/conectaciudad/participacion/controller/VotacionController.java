package com.conectaciudad.participacion.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/votaciones")
public class VotacionController {

    @GetMapping("/hola")
    public String hola(){
        return "hola";
    }

}
