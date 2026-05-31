package com.example.todoapp.dao;

import com.example.todoapp.business.model.TasksModel;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Data Access Object (DAO) gérant la persistance des tâches dans une base de données SQLite.
 * Assure les opérations CRUD (Create, Read, Update, Delete) ainsi que l'initialisation du schéma.
 */
@SuppressWarnings({"SqlNoDataSourceInspection", "SqlDialectInspection"})
public class SQLiteDao {

    private static final String DB_URL = "jdbc:sqlite:todo_v3.db";

    /**
     * Initialise le DAO et crée la table 'tasks' si elle n'existe pas.
     */
    public SQLiteDao() {
        String createTableSql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT,
                done INTEGER NOT NULL DEFAULT 0
            );
            """;
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSql);
        } catch (SQLException e) {
            throw new RuntimeException("Erreur d'initialisation SQLite", e);
        }
    }

    /**
     * Insère une nouvelle tâche en base de données.
     *
     * @param task Le modèle de tâche à sauvegarder.
     * @return La tâche créée avec son identifiant (ID) récupéré de la base.
     */
    public TasksModel save(TasksModel task) {
        String insertSql = "INSERT INTO tasks (title, description, done) VALUES (?, ?, ?);";
        String idSql = "SELECT last_insert_rowid();";

        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                pstmt.setString(1, task.title());
                pstmt.setString(2, task.description());
                pstmt.setInt(3, task.done() ? 1 : 0);
                pstmt.executeUpdate();
            }

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(idSql)) {
                if (rs.next()) {
                    return new TasksModel(rs.getInt(1), task.title(), task.description(), task.done());
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erreur lors de l'insertion en BDD", e);
        }
        return task;
    }

    /**
     * Recherche une tâche par son identifiant unique.
     *
     * @param id L'identifiant de la tâche.
     * @return Un {@link Optional} contenant la tâche trouvée, ou vide.
     */
    public Optional<TasksModel> findById(int id) {
        String sql = "SELECT id, title, description, done FROM tasks WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new TasksModel(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("done") == 1
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

    /**
     * Récupère toutes les tâches enregistrées.
     *
     * @return Une liste contenant toutes les tâches.
     */
    public List<TasksModel> findAll() {
        return executeQuery("SELECT id, title, description, done FROM tasks;");
    }

    /**
     * Récupère uniquement les tâches non terminées (done = 0).
     *
     * @return Une liste des tâches à faire.
     */
    public List<TasksModel> findAllTodoOnly() {
        return executeQuery("SELECT id, title, description, done FROM tasks WHERE done = 0;");
    }

    /**
     * Supprime une tâche de la base de données par son identifiant.
     *
     * @param id L'identifiant de la tâche.
     * @return true si une ligne a été supprimée, false sinon.
     */
    public boolean deleteById(int id) {
        String sql = "DELETE FROM tasks WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Met à jour les informations d'une tâche existante.
     *
     * @param id   L'identifiant de la tâche à modifier.
     * @param task Le modèle avec les nouvelles données.
     * @return true si la mise à jour a réussi, false si l'ID n'existe pas.
     */
    public boolean update(int id, TasksModel task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, done = ? WHERE id = ?;";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, task.title());
            pstmt.setString(2, task.description());
            pstmt.setInt(3, task.done() ? 1 : 0);
            pstmt.setInt(4, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Méthode privée générique pour exécuter des requêtes SELECT et mapper le résultat.
     *
     * @param sql La requête SQL à exécuter.
     * @return Une liste de {@link TasksModel} peuplée.
     */
    private List<TasksModel> executeQuery(String sql) {
        List<TasksModel> tasks = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tasks.add(new TasksModel(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("description"),
                        rs.getInt("done") == 1
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }
}