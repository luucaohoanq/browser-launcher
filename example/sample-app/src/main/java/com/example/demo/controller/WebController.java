package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller for serving HTML pages
 */
@Controller
public class WebController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Browser Launcher Demo");
        model.addAttribute("message", "Welcome to the Browser Launcher Demo Application!");
        return "index";
    }
}
