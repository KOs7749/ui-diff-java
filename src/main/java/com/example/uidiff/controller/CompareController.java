package com.example.uidiff.controller;

import com.example.uidiff.model.DiffResult;
import com.example.uidiff.service.DiffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class CompareController {

    private final DiffService diffService;

    @Autowired
    public CompareController(DiffService diffService) {
        this.diffService = diffService;
    }

    @PostMapping("/compare")
    public String compareByUrls(
            @RequestParam("url1") String url1,
            @RequestParam("url2") String url2,
            Model model
    ) {
        // Gọi service
        List<DiffResult> results = diffService.compareUrls(url1, url2);

        // Truyền kết quả sang view
        model.addAttribute("results", results);
        return "result"; // result.html
    }
}
