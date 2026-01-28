package com.example.cicd;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class CicdController {

    @Value("${app.version:dev}")
    private String version;

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "hello",
                "version", version
        );
    }
}
