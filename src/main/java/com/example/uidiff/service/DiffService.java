package com.example.uidiff.service;

import com.example.uidiff.model.DiffResult;
import com.example.uidiff.util.DomDiffUtil;
import com.example.uidiff.util.ImageDiffUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class DiffService {

    @Value("${app.out-dir}") private String outDir;

    private final ScreenshotService screenshotService;

    public DiffService(ScreenshotService screenshotService) {
        this.screenshotService = screenshotService;
    }

    public DiffResult runForUrls(String v1, String v2) {
        String id = UUID.randomUUID().toString().substring(0, 8) + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        Path base = Path.of(outDir, id);
        Path v1Png = base.resolve("v1.png");
        Path v2Png = base.resolve("v2.png");
        Path diffPng = base.resolve("diff.png");
        Path domTxt = base.resolve("domdiff.txt");

        var r1 = screenshotService.capture(v1, v1Png);
        var r2 = screenshotService.capture(v2, v2Png);

        ImageDiffUtil.diffWithBoxes(v1Png.toString(), v2Png.toString(), diffPng.toString());
        DomDiffUtil.writeDiff(r1.normalizedDom(), r2.normalizedDom(), domTxt);

        return DiffResult.builder()
                .id(id)
                .v1Shot("/results/" + id + "/v1.png")
                .v2Shot("/results/" + id + "/v2.png")
                .imgDiff("/results/" + id + "/diff.png")
                .domDiff("/results/" + id + "/domdiff.txt")
                .build();
    }
}
