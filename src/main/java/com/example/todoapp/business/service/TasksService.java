package com.example.todoapp.business.service;

import com.example.todoapp.Task;
import com.example.todoapp.TaskDao;
import java.util.List;
import java.util.Optional;

public class TasksService {

    private final TaskDao dao = new TaskDao();

    public Task createTask(Task task) {
        return dao.save(task);
    }

    public Optional<Task> getTaskById(int id) {
        return dao.findById(id);
    }

    public List<Task> getAllTasks(boolean todoOnly) {
        return todoOnly ? dao.findAllTodoOnly() : dao.findAll();
    }

    public boolean deleteTask(int id) {
        return dao.deleteById(id);
    }

    public boolean updateTask(int id, Task task) {
        return dao.update(id, task);
    }
}