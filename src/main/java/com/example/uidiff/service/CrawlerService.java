package com.example.uidiff.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Duration;
import java.util.*;

@Service
public class CrawlerService {

    @Value("${app.crawl.maxPages:200}") private int maxPages;
    @Value("${app.crawl.sameDomainOnly:true}") private boolean sameDomainOnly;
    @Value("${app.crawl.timeoutMs:15000}") private int timeoutMs;
    @Value("${app.crawl.userAgent:UiDiffBot/1.0}") private String userAgent;

    /** Trả về danh sách "đường dẫn tương đối" (/, /product/abc, …) */
    public Set<String> crawlPaths(String baseUrl) throws Exception {
        URI base = URI.create(stripTrailingSlash(baseUrl));
        String origin = base.getScheme() + "://" + base.getHost() + (base.getPort() > 0 ? ":" + base.getPort() : "");

        Queue<String> q = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        Set<String> paths = new LinkedHashSet<>();

        q.add(baseUrl);

        while (!q.isEmpty() && visited.size() < maxPages) {
            String url = q.poll();
            if (!visited.add(url)) continue;

            Document doc = Jsoup.connect(url)
                    .userAgent(userAgent)
                    .timeout(timeoutMs)
                    .get();

            String rel = toRelative(origin, url);
            paths.add(rel.isEmpty() ? "/" : rel);

            Elements links = doc.select("a[href]");
            links.forEach(a -> {
                String abs = a.absUrl("href");
                if (abs == null || abs.isBlank()) return;
                try {
                    URI u = URI.create(abs);
                    if (sameDomainOnly && !Objects.equals(u.getHost(), base.getHost())) return;
                    if (u.getScheme() != null && !u.getScheme().startsWith("http")) return;     // skip mailto:, tel:
                    if (abs.matches(".*\\.(png|jpe?g|gif|svg|css|js|ico|pdf)(\\?.*)?$")) return; // skip assets
                    if (!visited.contains(abs)) q.add(abs);
                } catch (Exception ignored) {}
            });
        }
        return paths;
    }

    private String stripTrailingSlash(String s) {
        if (s.endsWith("/")) return s.substring(0, s.length() - 1);
        return s;
    }
    private String toRelative(String origin, String url) {
        if (url.startsWith(origin)) {
            String p = url.substring(origin.length());
            return p.isEmpty() ? "/" : (p.startsWith("/") ? p : "/" + p);
        }
        return url;
    }
}
