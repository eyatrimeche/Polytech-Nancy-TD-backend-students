package com.example.todoapp.business.model;

/**
 * Modèle de données immuable représentant une tâche au sein de l'application.
 * Ce record fait le pont entre la couche d'accès aux données (DAO) et la logique métier.
 *
 * @param id          L'identifiant unique de la tâche généré par la base de données.
 * @param title       Le titre de la tâche (limité à 50 caractères maximum).
 * @param description Le détail de la tâche (limité à 255 caractères maximum, optionnel).
 * @param done        Le statut de la tâche (true si terminée, false sinon).
 */
public record TasksModel(int id, String title, String description, boolean done) {
}