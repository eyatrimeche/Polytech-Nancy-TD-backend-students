package com.example.todoapp.presentation;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.business.model.TasksModel;
import com.example.todoapp.business.service.TasksService;
import com.example.todoapp.dto.TasksRequestDto;
import com.example.todoapp.dto.TasksResponseDto;
import com.example.todoapp.dto.TasksUpdateRequestDto;
import com.example.todoapp.dto.ValidationErrorDto;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;

/**
 * Contrôleur de présentation responsable de la gestion des requêtes HTTP pour les tâches.
 * Implémente {@link HttpHandler} pour router les requêtes vers les services appropriés,
 * valider les DTOs d'entrée et formater les réponses en JSON.
 */
public class TasksController implements HttpHandler {

    private static final Logger log = LoggerFactory.getLogger(TasksController.class);
    private final TasksService tasksService = new TasksService();
    // La regex s'adapte au chemin partiel fourni par le serveur
    private static final Pattern ID_PATH = Pattern.compile("^/([0-9]+)$");

    /**
     * Traite les requêtes HTTP entrantes, identifie la méthode et le chemin,
     * puis délègue l'action au service métier.
     * Gère également les erreurs de validation et les exceptions non capturées.
     *
     * @param exchange L'échange HTTP contenant la requête et permettant d'envoyer la réponse.
     * @throws IOException Si une erreur d'entrée/sortie survient lors du traitement.
     */
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            log.info("Requête reçue : {} {}", method, path);

            // On nettoie le chemin pour enlever le préfixe si le serveur l'a laissé
            if (path.startsWith("/tasks")) {
                path = path.substring(6);
            }
            if (path.isEmpty()) {
                path = "/";
            }

            Matcher m = ID_PATH.matcher(path);

            // ================= POST /tasks =================
            if ("POST".equals(method) && "/".equals(path)) {
                String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                TasksRequestDto input = JsonUtils.deserialize(body, TasksRequestDto.class);

                if (input.title() == null || input.title().isBlank()) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ValidationErrorDto("title", "Title cannot be empty")));
                    return;
                }
                if (input.title().length() > 50) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ValidationErrorDto("title", "Title size maximum is 50 characters")));
                    return;
                }
                if (input.description() != null && input.description().length() > 255) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ValidationErrorDto("description", "Description size maximum is 255 characters")));
                    return;
                }

                TasksModel taskToCreate = new TasksModel(0, input.title(), input.description(), false);
                TasksModel created = tasksService.createTask(taskToCreate);

                exchange.getResponseHeaders().add("Location", "/tasks/" + created.id());
                TasksResponseDto response = new TasksResponseDto(created.id(), created.title(), created.description(), created.done());
                sendResponse(exchange, 201, JsonUtils.serialize(response));
                return;
            }

            // ================= GET /tasks/{id} =================
            if ("GET".equals(method) && m.matches()) {
                int id = Integer.parseInt(m.group(1));
                Optional<TasksModel> task = tasksService.getTaskById(id);

                if (task.isPresent()) {
                    TasksModel t = task.get();
                    TasksResponseDto response = new TasksResponseDto(t.id(), t.title(), t.description(), t.done());
                    sendResponse(exchange, 200, JsonUtils.serialize(response));
                } else {
                    sendResponse(exchange, 404, null);
                }
                return;
            }

            // ================= GET /tasks =================
            if ("GET".equals(method) && "/".equals(path)) {
                String query = exchange.getRequestURI().getQuery();
                boolean todoOnly = nonNull(query) && query.contains("todo-only=true");

                List<TasksModel> tasks = tasksService.getAllTasks(todoOnly);

                if (tasks.isEmpty()) {
                    sendResponse(exchange, 204, null);
                } else {
                    List<TasksResponseDto> responseList = tasks.stream()
                            .map(t -> new TasksResponseDto(t.id(), t.title(), t.description(), t.done()))
                            .toList();
                    sendResponse(exchange, 200, JsonUtils.serialize(responseList));
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
                TasksUpdateRequestDto input = JsonUtils.deserialize(body, TasksUpdateRequestDto.class);

                if (input.title() == null || input.title().isBlank()) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ValidationErrorDto("title", "Title cannot be empty")));
                    return;
                }
                if (input.title().length() > 50) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ValidationErrorDto("title", "Title size maximum is 50 characters")));
                    return;
                }
                if (input.description() != null && input.description().length() > 255) {
                    sendResponse(exchange, 400, JsonUtils.serialize(new ValidationErrorDto("description", "Description size maximum is 255 characters")));
                    return;
                }

                TasksModel taskToUpdate = new TasksModel(id, input.title(), input.description(), input.done());
                boolean updated = tasksService.updateTask(id, taskToUpdate);
                sendResponse(exchange, updated ? 204 : 404, null);
                return;
            }

            // Si aucune route ne correspond
            sendResponse(exchange, 404, null);

        } catch (Exception e) {
            log.error("ERREUR : ", e);
            try {
                sendResponse(exchange, 500, "{\"error\": \"Internal Server Error\"}");
            } catch (IOException ignored) {}
        }
    }

    /**
     * Méthode utilitaire pour envoyer une réponse HTTP formatée au client.
     *
     * @param exchange L'échange HTTP en cours.
     * @param status   Le code de statut HTTP à retourner.
     * @param json     Le contenu JSON à envoyer dans le corps de la réponse (peut être nul).
     * @throws IOException Si une erreur survient lors de l'écriture dans le flux de sortie.
     */
    private void sendResponse(HttpExchange exchange, int status, String json) throws IOException {
        if (json != null) {
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
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