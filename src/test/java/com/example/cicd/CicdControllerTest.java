package com.example.cicd;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CicdControllerTest {
    @Test
    void helloShouldFillUnknownWhenNull() {
        CicdService service = new CicdService();
        var result = service.hello(null, null);

        assertEquals("hello", result.get("message"));
        assertEquals("unknown", result.get("version"));
        assertEquals("unknown", result.get("hostname"));
    }
}
