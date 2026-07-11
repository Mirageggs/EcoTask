package com.trabajofinaldam.data.repository;
import com.trabajofinaldam.data.model.TaskModel;
public class SelectedTaskRepository {

    private static TaskModel selectedTask;

    public static void setSelectedTask(TaskModel task) {
        selectedTask = task;
    }

    public static TaskModel getSelectedTask() {
        return selectedTask;
    }

    public static void clear() {
        selectedTask = null;
    }
}
