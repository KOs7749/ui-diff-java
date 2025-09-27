package com.example.uidiff.controller;

import com.example.uidiff.service.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@RestController
@RequestMapping("/site")
public class SiteDiffController {

    private final CrawlerService crawlerService;
    private final ScreenshotService screenshotService;
    private final ImageDiffService imageDiffService;
    private final DomDiffService domDiffService;
    private final ReportService reportService;

    @Value("${app.out-dir:./run-results}")
    private String outDir;

    public SiteDiffController(CrawlerService crawlerService,
                              ScreenshotService screenshotService,
                              ImageDiffService imageDiffService,
                              DomDiffService domDiffService,
                              ReportService reportService) {
        this.crawlerService = crawlerService;
        this.screenshotService = screenshotService;
        this.imageDiffService = imageDiffService;
        this.domDiffService = domDiffService;
        this.reportService = reportService;
    }

    /** Ví dụ: /site/compare?base1=http://localhost:8020&base2=http://localhost:8021 */
    @GetMapping("/compare")
    public String compare(@RequestParam String base1, @RequestParam String base2) throws Exception {
        String sessionId = UUID.randomUUID().toString().substring(0,8) + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        reportService.newSession(sessionId);

        Set<String> s1 = crawlerService.crawlPaths(base1);
        Set<String> s2 = crawlerService.crawlPaths(base2);
        Set<String> common = new TreeSet<>(s1);
        common.retainAll(s2);

        for (String path : common) {
            String url1 = normalize(base1, path);
            String url2 = normalize(base2, path);

            Path pageDir = Path.of(outDir, sessionId, sanitize(path));
            Path v1Png = pageDir.resolve("v1.png");
            Path v2Png = pageDir.resolve("v2.png");
            Path diffPng = pageDir.resolve("diff.png");
            Path v1Html = screenshotService.htmlPath(v1Png);
            Path v2Html = screenshotService.htmlPath(v2Png);

            screenshotService.capture(url1, v1Png);
            screenshotService.capture(url2, v2Png);

            imageDiffService.diff(v1Png.toString(), v2Png.toString(), diffPng.toString());
            String domHtml = domDiffService.diffHtmlFiles(v1Html, v2Html);

            reportService.addEntry(path,
                    v1Png.toAbsolutePath().toString(),
                    v2Png.toAbsolutePath().toString(),
                    diffPng.toAbsolutePath().toString(),
                    domHtml);
        }

        String reportPath = reportService.generate();
        return "✅ Hoàn tất! Mở báo cáo: " + reportPath;
    }

    private String normalize(String base, String path) {
        if (path.startsWith("http")) return path;
        if (!base.endsWith("/")) base += "/";
        if (path.startsWith("/")) path = path.substring(1);
        return base + path;
    }
    private String sanitize(String p) {
        return p.replaceAll("[^a-zA-Z0-9._/-]", "_")
                .replaceAll("^/+", "")
                .replaceAll("/+$","");
    }
}
