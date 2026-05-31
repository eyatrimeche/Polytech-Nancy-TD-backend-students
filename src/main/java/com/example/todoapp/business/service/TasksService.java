package com.example.todoapp.business.service;

import com.example.todoapp.business.model.TasksModel;
import com.example.todoapp.dao.SQLiteDao;
import java.util.List;
import java.util.Optional;

/**
 * Service métier responsable de la logique de traitement des tâches.
 * Il fait le pont entre le contrôleur de présentation et la couche d'accès aux données (DAO).
 */
public class TasksService {

    private final SQLiteDao dao = new SQLiteDao();

    /**
     * Sauvegarde une nouvelle tâche dans la base de données.
     *
     * @param task Le modèle de tâche à persister.
     * @return La tâche créée incluant son identifiant généré.
     */
    public TasksModel createTask(TasksModel task) {
        return dao.save(task);
    }

    /**
     * Recherche une tâche spécifique par son identifiant.
     *
     * @param id L'identifiant numérique de la tâche.
     * @return Un {@link Optional} contenant la tâche si trouvée, ou vide sinon.
     */
    public Optional<TasksModel> getTaskById(int id) {
        return dao.findById(id);
    }

    /**
     * Récupère la liste complète des tâches ou uniquement celles non terminées.
     *
     * @param todoOnly Si true, filtre pour ne retourner que les tâches avec done = false.
     * @return La liste des tâches correspondantes.
     */
    public List<TasksModel> getAllTasks(boolean todoOnly) {
        return todoOnly ? dao.findAllTodoOnly() : dao.findAll();
    }

    /**
     * Supprime une tâche de la base de données.
     *
     * @param id L'identifiant de la tâche à supprimer.
     * @return true si la tâche a été trouvée et supprimée, false sinon.
     */
    public boolean deleteTask(int id) {
        return dao.deleteById(id);
    }

    /**
     * Met à jour les informations d'une tâche existante.
     *
     * @param id   L'identifiant de la tâche à mettre à jour.
     * @param task Le modèle contenant les nouvelles informations.
     * @return true si la mise à jour a réussi, false si la tâche n'a pas été trouvée.
     */
    public boolean updateTask(int id, TasksModel task) {
        return dao.update(id, task);
    }
}