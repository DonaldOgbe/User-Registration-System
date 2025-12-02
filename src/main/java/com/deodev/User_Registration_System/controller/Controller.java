package com.deodev.User_Registration_System.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class Controller {

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.status(200).body(null);
    }
}
