package com.example.todoapp.dto;

/**
 * Objet de transfert de données (DTO) renvoyé par l'API pour représenter l'état actuel d'une tâche.
 * Il est utilisé par les clients de l'API pour consulter les données après une création,
 * une récupération ou une mise à jour.
 *
 * @param id          L'identifiant unique de la tâche dans la base de données.
 * @param title       Le titre de la tâche.
 * @param description Le contenu ou les détails de la tâche.
 * @param done        Le statut actuel de la tâche (true si terminée, false sinon).
 */
public record TasksResponseDto(int id, String title, String description, boolean done) {
}