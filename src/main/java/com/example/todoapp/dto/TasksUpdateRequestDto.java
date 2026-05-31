package com.example.todoapp.dto;

/**
 * Objet de transfert de données (DTO) utilisé pour la mise à jour complète d'une tâche existante.
 * Il permet au client de modifier le titre, la description ainsi que le statut d'achèvement de la tâche.
 *
 * @param title       Le nouveau titre de la tâche (limité à 50 caractères maximum).
 * @param description La nouvelle description de la tâche (limité à 255 caractères maximum).
 * @param done        Le nouveau statut d'achèvement de la tâche (true pour terminée, false pour à faire).
 */
public record TasksUpdateRequestDto(String title, String description, boolean done) {
}