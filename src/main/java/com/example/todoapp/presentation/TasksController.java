package com.example.todoapp.presentation;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.Task;
import com.example.todoapp.business.service.TasksService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

public class TasksController implements HttpHandler {

    private static final Logger log =
            LoggerFactory.getLogger(TasksController.class);

    private static final TasksService tasksService = new TasksService();

    private static final Pattern ID_PATH =
            Pattern.compile("^/tasks/([0-9]+)$");

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();

        System.out.println(method + " " + path);

        Matcher m = ID_PATH.matcher(path);

        // ================= POST /tasks =================
        if ("POST".equals(method) && "/tasks".equals(path)) {

            String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);

            Task input = JsonUtils.deserialize(body, Task.class);

            Task created = tasksService.createTask(input);

            exchange.getResponseHeaders().add(
                    "Location",
                    "/tasks/" + created.id()
            );

            sendResponse(exchange, 201, JsonUtils.serialize(created));
            return;
        }

        // ================= GET /tasks/{id} =================
        if ("GET".equals(method) && m.matches()) {

            int id = Integer.parseInt(m.group(1));

            Optional<Task> task = tasksService.getTaskById(id);

            if (task.isPresent()) {
                sendResponse(exchange, 200, JsonUtils.serialize(task.get()));
            } else {
                sendResponse(exchange, 404, null);
            }

            return;
        }

        // ================= GET /tasks =================
        if ("GET".equals(method) && "/tasks".equals(path)) {

            String query = exchange.getRequestURI().getQuery();

            boolean todoOnly = nonNull(query) && query.contains("todo-only=true");

            var tasks = tasksService.getAllTasks(todoOnly);

            if (tasks.isEmpty()) {
                sendResponse(exchange, 204, null);
            } else {
                sendResponse(exchange, 200, JsonUtils.serialize(tasks));
            }

            return;
        }

        // ================= DELETE /tasks/{id} =================
        if ("DELETE".equals(method) && m.matches()) {

            int id = Integer.parseInt(m.group(1));

            boolean deleted = tasksService.deleteTask(id);

            sendResponse(exchange, deleted ? 204 : 404, null);
            return;
        }

        // ================= PUT /tasks/{id} =================
        if ("PUT".equals(method) && m.matches()) {

            int id = Integer.parseInt(m.group(1));

            String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);

            Task input = JsonUtils.deserialize(body, Task.class);

            boolean updated = tasksService.updateTask(id, input);

            sendResponse(exchange, updated ? 204 : 404, null);
            return;
        }

        // ================= DEFAULT =================
        sendResponse(exchange, 404, null);
    }

    private void sendResponse(HttpExchange exchange, int status, String json)
            throws IOException {

        if (json != null) {

            exchange.getResponseHeaders().set(
                    "Content-Type",
                    "application/json; charset=utf-8"
            );

            byte[] bytes = json.getBytes(UTF_8);

            exchange.sendResponseHeaders(status, bytes.length);

            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

        } else {
            exchange.sendResponseHeaders(status, -1);
        }

        exchange.close();
    }
}