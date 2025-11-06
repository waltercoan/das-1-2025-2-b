package br.univille.microkernel_plugin.service;

import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import br.univille.microkernel_interface.service.DefaultService;

@Service
public class ServiceOneImpl implements DefaultService{

    @Override
    public HttpStatus doWork(HashMap<String, String> params) {
        System.out.println("Service one" + params);
        return HttpStatus.OK;
    }

}
