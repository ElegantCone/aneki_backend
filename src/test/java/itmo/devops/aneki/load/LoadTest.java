package itmo.devops.aneki.load;

import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoadTest {
    private static final Pattern TOKEN_PATTERN = Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"");

    private static final String BASE_URL = "http://localhost:8080";

    private static final String USER_PREFIX = "load";
    private static final String PASSWORD = "LoadTest123!";
    private static final String COOKIE_NAME = "access_token";

    public static final ObjectMapper mapper = new ObjectMapper();

    private static final int USERS_COUNT = 20;
    private static final long DURATION_SECONDS = 120;
    private static final long SLEEP_MILLIS = 1000;
    private static final int CREATE_EVERY = 5;
    private static final long REQUEST_TIMEOUT_SECONDS = 10;

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();

        String token = signup(client);
        System.out.printf(
                "Running load test: baseUrl=%s users_count=%d duration=%ds%n",
                BASE_URL,
                USERS_COUNT,
                DURATION_SECONDS
        );
        AtomicBoolean running = new AtomicBoolean(true);
        ExecutorService executor = Executors.newFixedThreadPool(USERS_COUNT);
        Instant deadline = Instant.now().plusSeconds(DURATION_SECONDS);

        for (int i = 0; i < USERS_COUNT; i++) {
            int workerId = i + 1;
            executor.submit(() -> {
                try {
                    runWorker(client, token, workerId, deadline, running);
                } catch (IOException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(DURATION_SECONDS + 30L, TimeUnit.SECONDS);
        running.set(false);
    }

    private static void runWorker(
            HttpClient client,
            String token,
            int workerId,
            Instant deadline,
            AtomicBoolean running
    ) throws IOException, InterruptedException {
        long iteration = 0;
        while (running.get() && Instant.now().isBefore(deadline)) {
            iteration++;
            client.send(baseRequest("/api/jokes")
                    .header("Cookie", COOKIE_NAME + "=" + token).GET().build(), null);

            if (iteration % CREATE_EVERY == 0) {
                var node = mapper.createObjectNode()
                        .put("content", String.format("Load test joke worker=%d iteration=%d", workerId, iteration))
                        .toString();
                client.send(baseRequest("/api/jokes")
                        .header("Cookie", COOKIE_NAME + "=" + token).POST(HttpRequest.BodyPublishers.ofString(node)).build(), null);
            }

            if (SLEEP_MILLIS > 0) {
                try {
                    Thread.sleep(SLEEP_MILLIS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private static String signup(HttpClient client) throws IOException, InterruptedException {
        String email = USER_PREFIX + "-" + System.currentTimeMillis() + "@load.test";
        var node = mapper.createObjectNode()
                .put("name", "Load Test")
                .put("email", email)
                .put("password", PASSWORD)
                .toString();

        HttpRequest request = baseRequest("/api/auth/signup")
                .POST(HttpRequest.BodyPublishers.ofString(node))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Signup failed: status=" + response.statusCode() + " body=" + response.body());
        }

        Optional<String> cookieToken = response.headers().firstValue("set-cookie")
                .flatMap(LoadTest::extractCookie);
        if (cookieToken.isPresent()) {
            return cookieToken.get();
        }

        Matcher matcher = TOKEN_PATTERN.matcher(response.body());
        if (matcher.find()) {
            return matcher.group(1);
        }

        throw new IllegalStateException("Signup response does not contain auth token");
    }

    private static HttpRequest.Builder baseRequest(String path) {
        return HttpRequest.newBuilder(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }

    private static Optional<String> extractCookie(String setCookie) {
        String prefix = LoadTest.COOKIE_NAME + "=";
        for (String part : setCookie.split(";")) {
            String trimmed = part.trim();
            if (trimmed.startsWith(prefix)) {
                return Optional.of(trimmed.substring(prefix.length()));
            }
        }
        return Optional.empty();
    }
}
