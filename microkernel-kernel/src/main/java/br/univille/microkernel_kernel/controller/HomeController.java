package br.univille.microkernel_kernel.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.univille.microkernel_interface.service.DefaultService;

@RestController
@RequestMapping("/")
public class HomeController {
    
    @Autowired
    private Map<String, DefaultService> services;

    @GetMapping
    public ResponseEntity<String> index(){

        for(var umService : services.values()){
            umService.doWork(null);
        }

        return ResponseEntity.ok().build();
    }
}
