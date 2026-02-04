package com.example.cicd;

import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CicdService {

    public Map<String, String> hello(String version, String hostname){
        return Map.of(
                "message", "hello",
                "version", version == null ? "unknown" : version,
                "hostname", hostname == null ? "unknown" : hostname
        );
    }
}
