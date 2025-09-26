package com.example.uidiff.controller;

import com.example.uidiff.model.DiffResult;
import com.example.uidiff.service.DiffService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DiffController {

    private final DiffService diffService;

    @GetMapping("/")
    public String index() { return "index"; }

    /** So sánh 2 WEBSITE theo URL: V1 (baseline), V2 (candidate) */
    @PostMapping(path = "/compare-urls")
    public String compareByUrls(
            @RequestParam("urlV1") String urlV1,
            @RequestParam("urlV2") String urlV2,
            Model model) {

        try {
            DiffResult r = diffService.runForUrls(urlV1, urlV2);
            model.addAttribute("r", r);
            model.addAttribute("hasDom", true);     // có dom diff cho luồng URL
            return "result";
        } catch (Exception e) {
            log.error("Compare URLs failed. V1={}, V2={}", urlV1, urlV2, e);
            model.addAttribute("error", "Lỗi so sánh: " + e.getMessage());
            return "index";
        }
    }
}
