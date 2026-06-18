package com.trabajofinaldam;


import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * NewTaskViewModel — VIEWMODEL de la pantalla "Nueva Tarea".
 *
 * RESPONSABILIDADES:
 *   ✅ Recibir datos CRUDOS desde NewTaskActivity.
 *   ✅ Construir el objeto TaskModel (lógica de negocio).
 *   ✅ Llamar a DatabaseHelper para insertar en SQLite.
 *   ✅ Notificar el resultado a la VIEW vía LiveData.
 *
 * PROHIBIDO:
 *   ❌ Referencias a vistas, Activity o Context de UI.
 *
 * Extiende AndroidViewModel para acceder al Application Context
 * (necesario para obtener el DatabaseHelper sin causar memory leaks).
 */
public class Newtaskviewmodel extends AndroidViewModel {

    // LiveData que informa a la Activity si el guardado fue exitoso
    private final MutableLiveData<Boolean> guardadoExitoso = new MutableLiveData<>();

    // Dependencia: DatabaseHelper actuando como Repository
    private final DatabaseHelper dbHelper;

    public Newtaskviewmodel(@NonNull Application application) {
        super(application);
        dbHelper = DatabaseHelper.getInstance(application);
    }

    // Getter del LiveData (la Activity observa este)
    public LiveData<Boolean> getGuardadoExitoso() {
        return guardadoExitoso;
    }

    // ===================================================================
    // GUARDAR NUEVA TAREA
    // Recibe datos crudos → construye TaskModel → inserta en SQLite
    // ===================================================================
    public void guardarNuevaTarea(String descripcion, String fecha,
                                  String prioridad, int horasEstimadas) {

        // 1. Construir el objeto del modelo (capa Model)
        //    - completada = false (es nueva)
        //    - autoProgramada = true (simula que la "IA" la programó)
        //    - hora = "" por ahora; el algoritmo de IA la asignaría
        TaskModel nuevaTarea = new TaskModel(
                descripcion,
                fecha,
                prioridad,
                false,   // completada
                true,    // autoProgramada (badge "Auto-programada")
                ""       // hora (se asignaría con la lógica de programación)
        );

        // 2. Insertar en SQLite a través del DatabaseHelper
        long resultado = dbHelper.insertarTarea(nuevaTarea);

        // 3. Notificar a la VIEW: insert() devuelve -1 si falla
        guardadoExitoso.setValue(resultado != -1);
    }
}
