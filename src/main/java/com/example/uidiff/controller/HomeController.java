package com.example.uidiff.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Controller
public class HomeController {

    // Trang index (form nhập URL hoặc link sang trang upload)
    @GetMapping("/")
    public String index() {
        return "index";
    }

    // Có thể dùng cho check nhanh server
    @RestController
    static class Health {
        @GetMapping("/health")
        public String health() { return "OK"; }
    }
}
