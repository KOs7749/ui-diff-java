package com.example.uidiff.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index(Model model) {
        // gợi ý URL mẫu để test nhanh (tuỳ chọn)
        model.addAttribute("sampleV1", "http://localhost:8020/");
        model.addAttribute("sampleV2", "http://localhost:8021/");
        return "index";
    }
}
