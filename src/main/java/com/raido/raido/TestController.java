package com.raido.raido;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @RequestMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Hello");
    }
}
