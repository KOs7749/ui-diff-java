package com.example.uidiff.service;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Service
public class DomDiffService {

    /** Diff 2 file HTML → trả HTML highlight đơn giản */
    public String diffHtmlFiles(Path html1, Path html2) {
        try {
            String a = Files.exists(html1) ? Files.readString(html1) : "";
            String b = Files.exists(html2) ? Files.readString(html2) : "";
            return diffHtml(a, b);
        } catch (Exception e) {
            throw new RuntimeException("DOM diff failed", e);
        }
    }

    public String diffHtml(String html1, String html2) {
        List<String> A = List.of(html1.split("\\R"));
        List<String> B = List.of(html2.split("\\R"));
        var patch = DiffUtils.diff(A, B);

        StringBuilder sb = new StringBuilder();
        sb.append("<div style='font-family:monospace;font-size:13px;'>");
        for (AbstractDelta<String> d : patch.getDeltas()) {
            sb.append("<div style='margin:6px 0;'>")
                    .append("<span style='background:#eee;border:1px solid #ddd;border-radius:4px;padding:2px 6px;'>")
                    .append(d.getType()).append("</span><br/>");
            d.getSource().getLines().forEach(line ->
                    sb.append("<div style='color:#dc2626'>- ").append(esc(line)).append("</div>"));
            d.getTarget().getLines().forEach(line ->
                    sb.append("<div style='color:#16a34a'>+ ").append(esc(line)).append("</div>"));
            sb.append("</div>");
        }
        sb.append("</div>");
        return sb.toString();
    }

    private String esc(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }
}
