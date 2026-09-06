package support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** Educational in-memory HTTP service. It is not a Celadon backend. */
public final class PracticeApi implements AutoCloseable {
    private final ObjectMapper json = new ObjectMapper();
    private final Map<Integer, Map<String, Object>> tasks = new ConcurrentHashMap<>();
    private final AtomicInteger sequence = new AtomicInteger();
    private final HttpServer server;

    public PracticeApi() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/tasks", this::handle);
        server.start();
    }

    public String baseUrl() { return "http://127.0.0.1:" + server.getAddress().getPort(); }
    public void close() { server.stop(0); }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            String method = exchange.getRequestMethod();
            boolean collection = path.equals("/tasks");
            Integer id = path.matches("/tasks/[0-9]+")
                    ? Integer.valueOf(path.substring(7)) : null;
            if (!collection && (id == null || !tasks.containsKey(id))) {
                reply(exchange, 404, Map.of("error", "Task not found")); return;
            }
            if (method.equals("GET")) {
                reply(exchange, 200, collection ? tasks.values() : tasks.get(id)); return;
            }
            if (method.equals("DELETE") && !collection) {
                tasks.remove(id); exchange.sendResponseHeaders(204, -1); return;
            }
            if ((method.equals("POST") && collection) || (method.equals("PUT") && !collection)) {
                String type = exchange.getRequestHeaders().getFirst("Content-Type");
                if (type == null || !type.split(";")[0].trim().equalsIgnoreCase("application/json")) {
                    reply(exchange, 415, Map.of("error", "Expected application/json")); return;
                }
                com.fasterxml.jackson.databind.JsonNode body;
                try { body = json.readTree(exchange.getRequestBody()); }
                catch (IOException invalid) {
                    reply(exchange, 400, Map.of("error", "Malformed JSON")); return;
                }
                if (body == null || !body.isObject() || !body.path("title").isTextual()
                        || body.path("title").asText().isBlank()
                        || body.path("title").asText().length() > 100
                        || (body.has("completed") && !body.get("completed").isBoolean())) {
                    reply(exchange, 400, Map.of("error", "Invalid task")); return;
                }
                int taskId = collection ? sequence.incrementAndGet() : id;
                Map<String, Object> task = Map.of("id", taskId, "title", body.get("title").asText(),
                        "completed", body.path("completed").asBoolean(false));
                tasks.put(taskId, task);
                if (collection) exchange.getResponseHeaders().set("Location", "/tasks/" + taskId);
                reply(exchange, collection ? 201 : 200, task); return;
            }
            exchange.getResponseHeaders().set("Allow", collection ? "GET, POST" : "GET, PUT, DELETE");
            reply(exchange, 405, Map.of("error", "Method not allowed"));
        } finally { exchange.close(); }
    }

    private void reply(HttpExchange exchange, int status, Object body) throws IOException {
        byte[] bytes = json.writeValueAsBytes(body);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(status, bytes.length);
        exchange.getResponseBody().write(bytes);
    }
}
