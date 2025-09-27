package com.example.uidiff.service;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jsoup.Jsoup;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class ScreenshotService {

  @Value("${app.viewport.width:1366}")  private int width;
  @Value("${app.viewport.height:900}")  private int height;

  private ChromeDriver newDriver() {
    WebDriverManager.chromedriver().setup();
    ChromeOptions opts = new ChromeOptions();
    opts.addArguments("--headless=new","--disable-gpu","--no-sandbox",
            "--remote-allow-origins=*","--disable-dev-shm-usage");
    return new ChromeDriver(opts);
  }

  /** Chụp viewport screenshot & lưu HTML chuẩn hoá cạnh file PNG */
  public void capture(String url, Path outPng) {
    ChromeDriver driver = newDriver();
    try {
      driver.manage().window().setSize(new Dimension(width, height));
      driver.get(url);
      sleep(800); // có thể thay bằng WebDriverWait nếu cần

      byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
      Files.createDirectories(outPng.getParent());
      Files.write(outPng, png);

      String html = driver.getPageSource();
      String normalized = Jsoup.parse(html).outerHtml().replaceAll("\\s+"," ").trim();
      Files.writeString(htmlPath(outPng), normalized);
    } catch (Exception e) {
      throw new RuntimeException("Capture failed: " + url, e);
    } finally {
      driver.quit();
    }
  }

  public Path htmlPath(Path pngPath) {
    String htmlName = pngPath.getFileName().toString().replaceFirst("\\.png$", ".html");
    return pngPath.getParent().resolve(htmlName);
  }

  private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
