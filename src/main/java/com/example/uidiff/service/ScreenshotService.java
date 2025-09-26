package com.example.uidiff.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.TakesScreenshot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ScreenshotService {

    @Value("${app.viewport.width:1280}")  private int width;
    @Value("${app.viewport.height:800}")  private int height;

    private ChromeDriver newDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions opts = new ChromeOptions();
        opts.addArguments("--headless=new", "--disable-gpu", "--no-sandbox");
        return new ChromeDriver(opts);
    }

    public Result capture(String url, Path outPng) {
        ChromeDriver driver = newDriver();
        try {
            driver.manage().window().setSize(new Dimension(width, height));
            driver.get(url);
            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Files.createDirectories(outPng.getParent());
            Files.write(outPng, png);

            String html = driver.getPageSource();
            Document doc = Jsoup.parse(html);
            String normalized = doc.outerHtml().replaceAll("\\s+", " ").trim();
            return new Result(outPng.toString(), normalized);
        } catch (Exception e) {
            throw new RuntimeException("Capture failed for: " + url, e);
        } finally {
            driver.quit();
        }
    }

    public record Result(String screenshotPath, String normalizedDom) {}
}
