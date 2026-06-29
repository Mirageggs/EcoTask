package com.trabajofinaldam.data.repository;

import android.content.Context;

import com.trabajofinaldam.data.local.DatabaseHelper;
import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.data.model.TaskModel;

import java.util.List;

public class TaskRepository {
    private final DatabaseHelper dbHelper;
    private static TaskRepository instance;

    public static synchronized TaskRepository getInstance(Context context) {
        if (instance == null) {
            instance = new TaskRepository(context.getApplicationContext());
        }
        return instance;
    }

    private TaskRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    public long insertarTarea(TaskModel tarea)          { return dbHelper.insertarTarea(tarea); }
    public List<TaskModel> obtenerTareasPendientes()    { return dbHelper.obtenerTareasPendientes(); }
    public List<TaskModel> obtenerTodasLasTareas()      { return dbHelper.obtenerTodasLasTareas(); }
    public int marcarCompletada(int tareaId)            { return dbHelper.marcarCompletada(tareaId); }
    public int eliminarTarea(int tareaId)               { return dbHelper.eliminarTarea(tareaId); }

    public long insertarSubtarea(Subtarea subtarea) {
        return dbHelper.insertarSubtarea(subtarea);
    }

    public void insertarSubtareas(List<String> nombres, int tareaId) {
        for (String nombre : nombres) {
            dbHelper.insertarSubtarea(new Subtarea(tareaId, nombre));
        }
    }

    public List<Subtarea> obtenerSubtareasDeTarea(int tareaId) {
        return dbHelper.obtenerSubtareasDeTarea(tareaId);
    }

    public int marcarSubtareaCompletada(int subtareaId) {
        return dbHelper.marcarSubtareaCompletada(subtareaId);
    }

    public boolean todasSubtareasCompletadas(int tareaId) {
        return dbHelper.todasCompletadas(tareaId);
    }
}
