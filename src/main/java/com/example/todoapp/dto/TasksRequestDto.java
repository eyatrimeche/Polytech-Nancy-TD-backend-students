package com.example.todoapp.dto;

/**
 * Objet de transfert de données (DTO) représentant la requête de création d'une tâche.
 * Utilisé pour réceptionner et valider les données envoyées par le client via la méthode POST.
 *
 * @param title       Le titre de la tâche. Ce champ est obligatoire et sa taille
 * est limitée à 50 caractères maximum.
 * @param description Le détail de la tâche. Ce champ est optionnel (nullable) et sa taille
 * est limitée à 255 caractères maximum.
 */
public record TasksRequestDto(String title, String description) {
}