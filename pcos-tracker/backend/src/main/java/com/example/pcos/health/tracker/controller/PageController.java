package com.example.pcos.health.tracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/user-dashboard")
    public String dashboard() {
        return "user-dashboard";
    }

    @GetMapping("/nutrition")
    public String nutrition() {
        return "nutrition";
    }

}
