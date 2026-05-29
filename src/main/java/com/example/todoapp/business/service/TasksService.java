package com.example.todoapp.business.service;

import com.example.todoapp.Task;
import com.example.todoapp.dao.SQLiteDao;
import java.util.List;
import java.util.Optional;

public class TasksService {

    private final SQLiteDao dao = new SQLiteDao();

    public Task createTask(Task task) {
        // Erreur possible ici si le type retourné par le DAO n'était pas synchronisé
        return dao.save(task);
    }

    public Optional<Task> getTaskById(int id) {
        // Erreur possible ici si l'Optional du DAO contenait un autre type
        return dao.findById(id);
    }

    public List<Task> getAllTasks(boolean todoOnly) {
        // Erreur possible ici si la liste du DAO contenait un autre type
        return todoOnly ? dao.findAllTodoOnly() : dao.findAll();
    }

    public boolean deleteTask(int id) {
        return dao.deleteById(id);
    }

    public boolean updateTask(int id, Task task) {
        return dao.update(id, task);
    }
}