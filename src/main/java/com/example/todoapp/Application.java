package com.example.todoapp;

import com.example.todoapp.presentation.TasksController;
import com.sun.net.httpserver.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * Classe principale de l'application TODO.
 * Responsable du démarrage, de la configuration du serveur HTTP et de la mise en place des routes.
 */
public class Application {

    private static final Logger log =
            LoggerFactory.getLogger(Application.class);

    /**
     * Point d'entrée principal de l'application.
     * Initialise le serveur HTTP sur le port 8080, associe le contrôleur aux routes
     * d'API et démarre l'écoute des requêtes.
     *
     * @param args Arguments de la ligne de commande (non utilisés ici)
     * @throws Exception Si une erreur survient lors de la création ou du démarrage du serveur HTTP
     */
    public static void main(String[] args) throws Exception {

        log.info("In-memory repository initialised");

        // Création du serveur HTTP sur le port 8080 avec une taille de file d'attente par défaut
        HttpServer server =
                HttpServer.create(new InetSocketAddress(8080), 0);

        // Liaison de la racine de l'API /tasks avec le contrôleur de présentation
        server.createContext("/tasks", new TasksController());

        // Utilise l'exécuteur par défaut pour gérer les requêtes HTTP de manière synchrone
        server.setExecutor(null);
        server.start();

        log.info("HTTP server started on http://localhost:8080");
    }
}