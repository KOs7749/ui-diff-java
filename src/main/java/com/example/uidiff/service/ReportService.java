package com.example.uidiff.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ReportService {

    @Value("${app.out-dir:./run-results}")
    private String outDir;

    private static class Entry {
        String path, v1Png, v2Png, diffPng, domDiffHtml;
    }

    private final List<Entry> entries = new ArrayList<>();
    private String sessionId = UUID.randomUUID().toString().substring(0,8) + "_" + System.currentTimeMillis();

    public void newSession(String sessionId) {
        this.sessionId = (sessionId == null || sessionId.isBlank()) ? this.sessionId : sessionId;
        entries.clear();
    }

    public void addEntry(String path, String v1Png, String v2Png, String diffPng, String domDiffHtml) {
        Entry e = new Entry();
        e.path = path; e.v1Png = v1Png; e.v2Png = v2Png; e.diffPng = diffPng; e.domDiffHtml = domDiffHtml;
        entries.add(e);
    }

    public String generate() {
        try {
            Path base = Path.of(outDir, sessionId);
            Files.createDirectories(base);
            Path report = base.resolve("report.html");

            StringBuilder html = new StringBuilder();
            html.append("""
                <!doctype html><html lang='vi'><head><meta charset='utf-8'>
                <title>UI Diff Report</title>
                <style>
                  body{font-family:Arial;margin:20px;}
                  h1{margin-bottom:4px}
                  .grid{display:grid;grid-template-columns:repeat(3,1fr);gap:14px}
                  .card{border:1px solid #e5e7eb;border-radius:8px;padding:10px;background:#fff}
                  img{max-width:100%;border:1px solid #e5e7eb;border-radius:6px}
                  .path{background:#111827;color:#fff;display:inline-block;padding:2px 6px;border-radius:4px;font-size:12px;}
                </style>
                </head><body>
            """);

            html.append("<h1>UI Diff Report</h1>")
                    .append("<div>Session: ").append(sessionId).append("</div>")
                    .append("<div>Generated: ").append(LocalDateTime.now()).append("</div><hr/>");

            for (Entry e : entries) {
                html.append("<h2><span class='path'>").append(esc(e.path)).append("</span></h2>")
                        .append("<div class='grid'>")
                        .append("<div class='card'><h3>V1</h3><img src='").append(rel(e.v1Png)).append("'></div>")
                        .append("<div class='card'><h3>V2</h3><img src='").append(rel(e.v2Png)).append("'></div>")
                        .append("<div class='card'><h3>Diff</h3><img src='").append(rel(e.diffPng)).append("'></div>")
                        .append("</div>")
                        .append("<div class='card' style='margin-top:12px'><h3>DOM diff</h3>")
                        .append(e.domDiffHtml)
                        .append("</div><hr/>");
            }

            html.append("</body></html>");
            Files.writeString(report, html.toString());
            return report.toAbsolutePath().toString();
        } catch (Exception e) {
            throw new RuntimeException("Generate report failed", e);
        }
    }

    private String rel(String abs) { return abs; } // mở từ filesystem
    private String esc(String s) { return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;"); }
}
