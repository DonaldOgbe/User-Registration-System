package com.deodev.User_Registration_System.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @PostMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.status(200).body(null);
    }
}
