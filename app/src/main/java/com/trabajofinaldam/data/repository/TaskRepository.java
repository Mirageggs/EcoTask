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

    public long insertarTarea(TaskModel tarea) {
        return dbHelper.insertarTarea(tarea);
    }

    public long insertarSubtarea(Subtarea subtarea) {
        return dbHelper.insertarSubtarea(subtarea);
    }

    public List<TaskModel> obtenerTareasPendientes() {
        return dbHelper.obtenerTareasPendientes();
    }

    public List<TaskModel> obtenerTareasFinalizadas() {
        return dbHelper.obtenerTareasFinalizadas();
    }

    public List<TaskModel> obtenerTodasLasTareas() {
        return dbHelper.obtenerTodasLasTareas();
    }

    public int marcarCompletada(int tareaId) {
        return dbHelper.marcarCompletada(tareaId);
    }

    public int desmarcarTarea(int tareaId) {
        return dbHelper.desmarcarTarea(tareaId);
    }

    public int marcarIniciada(int tareaId) {
        return dbHelper.marcarIniciada(tareaId);
    }

    public int postergarTarea(int id, String nuevaFecha) {
        return dbHelper.postergarTarea(id, nuevaFecha);
    }

    public void actualizarEstadoSubtarea(int subtareaId, boolean completada) {
        dbHelper.actualizarEstadoSubtarea(subtareaId, completada);
    }

    public List<Subtarea> obtenerSubtareasDeTarea(int tareaId) {
        return dbHelper.obtenerSubtareasDeTarea(tareaId);
    }

    public int actualizarTarea(TaskModel tarea) {
        // Debo implementar este método en DatabaseHelper también
        return dbHelper.actualizarTarea(tarea);
    }

    public int eliminarSubtarea(int subtareaId) {
        return dbHelper.eliminarSubtarea(subtareaId);
    }

    public void borrarSubtareasDeTarea(int tareaId) {
        dbHelper.borrarSubtareasDeTarea(tareaId);
    }

    public int eliminarTarea(int tareaId) {
        return dbHelper.eliminarTarea(tareaId);
    }
}
