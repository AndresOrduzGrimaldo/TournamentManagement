package com.tournament.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class SimpleTestController {

    @GetMapping("/simple")
    public String simpleTest() {
        return "Simple test controller working!";
    }
} 