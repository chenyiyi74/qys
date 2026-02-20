// java
package qys;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NetworkService {
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "net-monitor");
        t.setDaemon(true);
        return t;
    });
    private final ExecutorService worker = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "net-worker");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean lastOnline = false;

    public void startMonitoring(Consumer<Boolean> statusCallback) {
        scheduler.scheduleAtFixedRate(() -> {
            boolean online = checkOnline();
            lastOnline = online;
            statusCallback.accept(online);
        }, 0, 5, TimeUnit.SECONDS);
    }

    private boolean checkOnline() {
        try {
            URL url = new URL("https://v1.hitokoto.cn/");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(1500);
            conn.setReadTimeout(1500);
            conn.setRequestMethod("GET");
            int code = conn.getResponseCode();
            conn.disconnect();
            return code >= 200 && code < 400;
        } catch (Exception e) {
            return false;
        }
    }

    public void fetchQuoteAsync(Consumer<String> callback) {
        worker.submit(() -> {
            String q = fetchHitokoto();
            callback.accept(q);
        });
    }

    private String fetchHitokoto() {
        try {
            URL url = new URL("https://v1.hitokoto.cn/");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("GET");

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) sb.append(line).append('\n');
                String body = sb.toString();
                String hit = parseHitokoto(body);
                return hit;
            } finally {
                conn.disconnect();
            }
        } catch (Exception e) {
            return null;
        }
    }

    private static final Pattern H_PATTERN = Pattern.compile("\"hitokoto\"\\s*:\\s*\"(.*?)\"", Pattern.DOTALL);

    private String parseHitokoto(String json) {
        Matcher m = H_PATTERN.matcher(json);
        if (m.find()) {
            String raw = m.group(1);
            return unescapeJavaString(raw);
        }
        return null;
    }

    private String unescapeJavaString(String s) {
        return s.replaceAll("\\\\n", "\n")
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\\\t", "\t")
                .replaceAll("\\\\/", "/")
                .replaceAll("\\\\\"", "\"")
                .replaceAll("\\\\\\\\", "\\\\");
    }

    public void shutdown() {
        try {
            scheduler.shutdownNow();
            worker.shutdownNow();
        } catch (Exception ignored) {
        }
    }
}