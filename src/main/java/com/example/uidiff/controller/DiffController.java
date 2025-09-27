package com.example.uidiff.controller;

import com.example.uidiff.service.CrawlerService;
import com.example.uidiff.service.ScreenshotService;
import com.example.uidiff.service.ImageDiffService;
import com.example.uidiff.service.DomDiffService;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class DiffController {

  private final CrawlerService crawlerService;
  private final ScreenshotService screenshotService;
  private final ImageDiffService imageDiffService;
  private final DomDiffService domDiffService;

  public DiffController(CrawlerService crawlerService,
                        ScreenshotService screenshotService,
                        ImageDiffService imageDiffService,
                        DomDiffService domDiffService) {
    this.crawlerService = crawlerService;
    this.screenshotService = screenshotService;
    this.imageDiffService = imageDiffService;
    this.domDiffService = domDiffService;
  }

  /** So sánh NHANH 1 trang (không crawl) */
  @GetMapping("/compare-once")
  public String compareOnce(@RequestParam String url1,
                            @RequestParam String url2) {

    String sid = "api_" + UUID.randomUUID().toString().substring(0,8);
    Path base   = Path.of("run-results", sid);
    Path v1Png  = base.resolve("v1.png");
    Path v2Png  = base.resolve("v2.png");
    Path diffPng= base.resolve("diff.png");
    Path v1Html = screenshotService.htmlPath(v1Png);
    Path v2Html = screenshotService.htmlPath(v2Png);

    screenshotService.capture(url1, v1Png);              // Path ✅
    screenshotService.capture(url2, v2Png);              // Path ✅
    imageDiffService.diff(v1Png.toString(),              // String ✅
            v2Png.toString(),
            diffPng.toString());
    String domHtml = domDiffService.diffHtmlFiles(v1Html, v2Html); // Path ✅

    return """
               ✅ DONE
               Images:
               - %s
               - %s
               - %s
               DOM diff saved inline (open result.html in UI flow)""".formatted(
            v1Png.toAbsolutePath(), v2Png.toAbsolutePath(), diffPng.toAbsolutePath());
  }

  /** Ví dụ crawl giao các path & xử lý (nếu bạn muốn giữ trong controller này) */
  @GetMapping("/compare-site")
  public String compareSite(@RequestParam String base1,
                            @RequestParam String base2) throws Exception {
    var s1 = crawlerService.crawlPaths(base1);  // ✅ đúng tên hàm
    var s2 = crawlerService.crawlPaths(base2);  // ✅ đúng tên hàm
    Set<String> common = new TreeSet<>(s1);
    common.retainAll(s2);
    return "Paths to compare: " + common.size();
  }
}
