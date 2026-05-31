package com.example.todoapp.dto;

/**
 * Objet de transfert de données (DTO) utilisé pour renvoyer des détails
 * sur une erreur de validation (Code HTTP 400).
 * Permet au client de savoir exactement quel champ n'est pas conforme et pourquoi.
 *
 * @param field       Le nom du champ qui a causé l'erreur de validation (ex: "title").
 * @param description Un message explicatif détaillant la règle non respectée
 * (ex: "La taille maximale est de 50 caractères").
 */
public record ValidationErrorDto(String field, String description) {
}