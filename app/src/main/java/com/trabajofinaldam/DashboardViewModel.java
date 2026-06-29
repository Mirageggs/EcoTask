package com.trabajofinaldam;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trabajofinaldam.network.ApiClient;
import com.trabajofinaldam.network.ApiService;

import java.util.Calendar;
import java.util.List;

/**
 * DashboardViewModel — Capa ViewModel en MVVM.
 *
 * RESPONSABILIDADES:
 *   ✅ Exponer LiveData a DashboardActivity (la VIEW solo observa).
 *   ✅ Sobrevivir rotaciones de pantalla.
 *   ✅ Leer tareas de SQLite y calcular el progreso.
 *   ✅ Leer el total de Eco-Puntos de SharedPreferences (ODS 12).
 */
public class DashboardViewModel extends AndroidViewModel {

    // ===================================================================
    // LIVEDATA
    // ===================================================================
    private final MutableLiveData<String>          greetingText  = new MutableLiveData<>();
    private final MutableLiveData<String> ecoConsejo = new MutableLiveData<>();
    private final MutableLiveData<Integer>         todayProgress = new MutableLiveData<>();
    private final MutableLiveData<List<TaskModel>> calendarTasks = new MutableLiveData<>();
    private final MutableLiveData<Integer>         ecoPuntos     = new MutableLiveData<>();

    // ===================================================================
    // DEPENDENCIAS
    // ===================================================================
    private final DatabaseHelper dbHelper;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        dbHelper = DatabaseHelper.getInstance(application);
        refreshData();
    }

    // ===================================================================
    // GETTERS
    // ===================================================================
    public LiveData<String>          getGreetingText()  { return greetingText; }
    public LiveData<String> getEcoConsejo() { return ecoConsejo; }
    public LiveData<Integer>         getTodayProgress() { return todayProgress; }
    public LiveData<List<TaskModel>> getCalendarTasks() { return calendarTasks; }
    public LiveData<Integer>         getEcoPuntos()     { return ecoPuntos; }

    // ===================================================================
    // refreshData — llamado desde onResume() de la Activity
    // ===================================================================
    public void refreshData() {
        greetingText.setValue(buildGreeting());
        loadTasksFromDatabase();
        cargarEcoPuntos();
        cargarEcoConsejo();
    }

    // ===================================================================
    // cargarEcoPuntos — Lee el total guardado en SharedPreferences
    // Las constantes PREFS_NAME y KEY_ECO_PUNTOS están en FocusActivity.
    // ===================================================================
    public void cargarEcoPuntos() {
        SharedPreferences prefs = getApplication()
                .getSharedPreferences(FocusFragment.PREFS_NAME, Application.MODE_PRIVATE);
        int total = prefs.getInt(FocusFragment.KEY_ECO_PUNTOS, 0);
        ecoPuntos.setValue(total);
    }

    private void cargarEcoConsejo() {

        ApiService apiService =
                ApiClient.getClient().create(ApiService.class);

        apiService.obtenerConsejoHoy().enqueue(new retrofit2.Callback<EcoConsejo>() {

            @Override
            public void onResponse(retrofit2.Call<EcoConsejo> call,
                                   retrofit2.Response<EcoConsejo> response) {

                if (response.isSuccessful() && response.body() != null) {
                    ecoConsejo.postValue(response.body().getTexto());
                }
            }

            @Override
            public void onFailure(retrofit2.Call<EcoConsejo> call,
                                  Throwable t) {

                ecoConsejo.postValue(
                        "Hagamos de hoy un día productivo y sostenible"
                );
            }
        });
    }

    // ===================================================================
    // CARGA DE TAREAS DESDE SQLITE
    // ===================================================================
    private void loadTasksFromDatabase() {
        List<TaskModel> todasLasTareas = dbHelper.obtenerTodasLasTareas();

        if (todasLasTareas.isEmpty()) {
            seedMockData();
            todasLasTareas = dbHelper.obtenerTodasLasTareas();
        }

        List<TaskModel> pendientes = dbHelper.obtenerTareasPendientes();
        calendarTasks.setValue(pendientes);
        todayProgress.setValue(calcularProgreso(todasLasTareas));
    }

    private int calcularProgreso(List<TaskModel> tareas) {
        if (tareas == null || tareas.isEmpty()) return 0;
        int completadas = 0;
        for (TaskModel t : tareas) {
            if (t.isCompletada()) completadas++;
        }
        return (int) ((completadas / (float) tareas.size()) * 100);
    }

    public void completarTarea(int tareaId) {
        dbHelper.marcarCompletada(tareaId);
        loadTasksFromDatabase();
    }

    private String buildGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12)      return "Buenos dias,\nEstudiante!";
        else if (hour < 18) return "Buenas tardes,\nEstudiante!";
        else                return "Buenas noches,\nEstudiante!";
    }

    private void seedMockData() {
        dbHelper.insertarTarea(new TaskModel(
                "Completar tarea de Matematicas", "20/06/2026",
                TaskModel.PRIORIDAD_ALTA, false, true, "14:00"));
        dbHelper.insertarTarea(new TaskModel(
                "Repasar apuntes de Sistemas", "20/06/2026",
                TaskModel.PRIORIDAD_MEDIA, false, true, "16:30"));
        dbHelper.insertarTarea(new TaskModel(
                "Investigacion de sostenibilidad", "21/06/2026",
                TaskModel.PRIORIDAD_MEDIA, false, false, "18:00"));
        dbHelper.insertarTarea(new TaskModel(
                "Leer articulos de computacion sostenible", "22/06/2026",
                TaskModel.PRIORIDAD_BAJA, true, false, "10:00"));
        dbHelper.insertarTarea(new TaskModel(
                "Redactar esquema del proyecto", "22/06/2026",
                TaskModel.PRIORIDAD_ALTA, true, false, "11:00"));
    }
}
