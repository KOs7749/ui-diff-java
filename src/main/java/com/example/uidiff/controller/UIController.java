package com.example.uidiff.controller;

import com.example.uidiff.service.CrawlerService;
import com.example.uidiff.service.ScreenshotService;
import com.example.uidiff.service.ImageDiffService;
import com.example.uidiff.service.DomDiffService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.uidiff.model.DiffResult;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.ArrayList;
import java.util.List;


import java.nio.file.Path;
import java.util.UUID;

@Controller
public class UIController {

    private final CrawlerService crawlerService;
    private final ScreenshotService screenshotService;
    private final ImageDiffService imageDiffService;
    private final DomDiffService domDiffService;

    public UIController(CrawlerService crawlerService,
                        ScreenshotService screenshotService,
                        ImageDiffService imageDiffService,
                        DomDiffService domDiffService) {
        this.crawlerService = crawlerService;
        this.screenshotService = screenshotService;
        this.imageDiffService = imageDiffService;
        this.domDiffService = domDiffService;
    }

    /** Trang form nhập URL */
    @GetMapping("/")
    public String index() {
        return "index"; // resources/templates/index.html
    }

    /**
     * So sánh nhanh 2 URL (chụp ảnh + diff ảnh + diff DOM)
     * Kết quả render ở result.html
     */
    @PostMapping("/compare")
    public String compareOnce(@RequestParam("url1") String url1,
                              @RequestParam("url2") String url2,
                              Model model) {
        try {
            // Thư mục tạm cho lần chạy này
            String sid = UUID.randomUUID().toString().substring(0, 8);
            Path base = Path.of("run-results", sid);

            Path v1Png  = base.resolve("v1.png");
            Path v2Png  = base.resolve("v2.png");
            Path diffPng= base.resolve("diff.png");
            Path v1Html = screenshotService.htmlPath(v1Png);
            Path v2Html = screenshotService.htmlPath(v2Png);

            // Chụp ảnh & lưu HTML
            screenshotService.capture(url1, v1Png);
            screenshotService.capture(url2, v2Png);

            // Diff ảnh (vẽ khung đỏ lên ảnh v2)
            imageDiffService.diff(v1Png.toString(), v2Png.toString(), diffPng.toString());

            // Diff DOM (dạng text highlight)
            String domHtml = domDiffService.diffHtmlFiles(v1Html, v2Html);

            // Trả dữ liệu về view
            model.addAttribute("v1ImagePath",  v1Png.toAbsolutePath().toString());
            model.addAttribute("v2ImagePath",  v2Png.toAbsolutePath().toString());
            model.addAttribute("diffImagePath",diffPng.toAbsolutePath().toString());
            model.addAttribute("domDiffHtml",  domHtml == null ? "" : domHtml);
            model.addAttribute("hasDom",       domHtml != null && !domHtml.isBlank());
            model.addAttribute("sessionId",    sid);

            return "result"; // resources/templates/result.html
        } catch (Exception e) {
            model.addAttribute("error", "Lỗi so sánh: " + e.getMessage());
            return "index";
        }
    }
    @GetMapping("/demo/result-strings")
    public String showResultStrings(Model model) {
        List<String> images = new ArrayList<>();
        // TODO: thay các path bên dưới bằng file thực tế của bạn (ví dụ trong run-results/...)
        images.add("/results/sample/diff1.png");
        images.add("/results/sample/diff2.png");
        images.add("/results/sample/diff3.png");
        model.addAttribute("results", images);
        return "result-strings";
    }

    /** Demo 2: trả về List<DiffResult> -> result-objects.html */
    @GetMapping("/demo/result-objects")
    public String showResultObjects(Model model) {
        List<DiffResult> results = new ArrayList<>();
        // TODO: thay các path bằng file thực tế
        results.add(new DiffResult("home", "/results/sample/home-diff.png",
                "/results/sample/home-v1.png", "/results/sample/home-v2.png"));
        results.add(new DiffResult("product", "/results/sample/product-diff.png",
                "/results/sample/product-v1.png", "/results/sample/product-v2.png"));
        model.addAttribute("results", results);
        return "result-objects";
    }
}
