package com.example.cicd;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class CicdController {
    private final CicdService cicdService;

    @Value("${app.version:dev}")
    private String version;

    public CicdController(CicdService cicdService) {
        this.cicdService = cicdService;
    }

    @GetMapping("/hello")
    public Map<String, String> hello() throws Exception{
        String hostname = InetAddress.getLocalHost().getHostName();
        return cicdService.hello(version, hostname);
    }
}
