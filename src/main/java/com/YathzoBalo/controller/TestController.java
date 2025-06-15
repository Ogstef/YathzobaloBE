package com.YathzoBalo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/hello")
    public Map<String, String> hello() {
        return Map.of(
                "message", "YathzoBalo Backend is running! 🎲",
                "status", "success"
        );
    }
}