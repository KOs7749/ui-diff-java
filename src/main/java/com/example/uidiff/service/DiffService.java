package com.example.uidiff.service;

import com.example.uidiff.model.DiffResult;
import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.List;

@Service
public class DiffService {

    private final Path resultsDir = Path.of("run-results");

    public List<DiffResult> compareUrls(String url1, String url2) {
        try {
            String sessionId = UUID.randomUUID().toString();
            Path sessionPath = resultsDir.resolve(sessionId);
            Files.createDirectories(sessionPath);

            // 1. Crawl & screenshot
            String v1Img = captureScreenshot(url1, sessionPath.resolve("v1.png").toFile());
            String v2Img = captureScreenshot(url2, sessionPath.resolve("v2.png").toFile());

            // 2. Diff ảnh
            String diffImg = createDiffImage(
                    new File(v1Img), new File(v2Img),
                    sessionPath.resolve("diff.png").toFile()
            );

            // 3. DOM diff
            String domDiff = compareDom(url1, url2);
            Path domFile = sessionPath.resolve("domdiff.txt");
            Files.writeString(domFile, domDiff);

            // 4. Kết quả
            DiffResult result = new DiffResult();
            result.setId(sessionId);
            result.setV1Shot("/" + v1Img);
            result.setV2Shot("/" + v2Img);
            result.setImgDiff("/" + diffImg);
            result.setDomDiff(domDiff);

            return List.of(result);

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    private String captureScreenshot(String url, File outputFile) throws Exception {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new", "--no-sandbox", "--disable-gpu", "--window-size=1920,1080");

        WebDriver driver = new ChromeDriver(options);
        driver.get(url);

        File screenshot = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(screenshot, outputFile);

        driver.quit();
        return outputFile.getPath();
    }

    private String createDiffImage(File img1, File img2, File outFile) throws Exception {
        BufferedImage bi1 = ImageIO.read(img1);
        BufferedImage bi2 = ImageIO.read(img2);

        int width = Math.min(bi1.getWidth(), bi2.getWidth());
        int height = Math.min(bi1.getHeight(), bi2.getHeight());

        BufferedImage diff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = diff.createGraphics();

        g.drawImage(bi2, 0, 0, null);
        g.setColor(new Color(255, 0, 0, 128)); // đỏ trong suốt

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bi1.getRGB(x, y) != bi2.getRGB(x, y)) {
                    g.fillRect(x, y, 1, 1);
                }
            }
        }
        g.dispose();

        ImageIO.write(diff, "png", outFile);
        return outFile.getPath();
    }

    private String compareDom(String url1, String url2) throws Exception {
        Document doc1 = Jsoup.connect(url1).get();
        Document doc2 = Jsoup.connect(url2).get();

        // So sánh DOM đơn giản bằng cách so sánh string
        if (!doc1.outerHtml().equals(doc2.outerHtml())) {
            return Jsoup.parse("<h3>DOM khác nhau</h3><pre>"
                    + diffStrings(doc1.outerHtml(), doc2.outerHtml())
                    + "</pre>").outerHtml();
        } else {
            return "DOM giống nhau.";
        }
    }

    // Diff text cơ bản (bạn có thể thay thế bằng Google Diff Match Patch lib)
    private String diffStrings(String s1, String s2) {
        StringBuilder sb = new StringBuilder();
        String[] lines1 = s1.split("\n");
        String[] lines2 = s2.split("\n");
        int max = Math.max(lines1.length, lines2.length);

        for (int i = 0; i < max; i++) {
            String l1 = i < lines1.length ? lines1[i] : "";
            String l2 = i < lines2.length ? lines2[i] : "";
            if (!l1.equals(l2)) {
                sb.append("- ").append(l1).append("\n");
                sb.append("+ ").append(l2).append("\n");
            }
        }
        return sb.toString();
    }
}
