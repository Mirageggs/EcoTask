package com.trabajofinaldam.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Calendar;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {

    private final MutableLiveData<String> greetingText = new MutableLiveData<>();
    private final MutableLiveData<String> ecoConsejo = new MutableLiveData<>();
    private final MutableLiveData<Integer> todayProgress = new MutableLiveData<>();
    private final MutableLiveData<List<com.trabajofinaldam.data.model.TaskModel>> calendarTasks = new MutableLiveData<>();
    private final MutableLiveData<List<com.trabajofinaldam.data.model.TaskModel>> finishedTasks = new MutableLiveData<>();
    private final MutableLiveData<List<com.trabajofinaldam.data.model.TaskModel>> inconclusasTasks = new MutableLiveData<>();
    private final MutableLiveData<Integer> ecoPuntos = new MutableLiveData<>();

    private final com.trabajofinaldam.data.repository.TaskRepository repository;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        repository = com.trabajofinaldam.data.repository.TaskRepository.getInstance(application);
        refreshData();
    }

    public LiveData<String> getGreetingText() { return greetingText; }
    public LiveData<String> getEcoConsejo() { return ecoConsejo; }
    public LiveData<Integer> getTodayProgress() { return todayProgress; }
    public LiveData<List<com.trabajofinaldam.data.model.TaskModel>> getCalendarTasks() { return calendarTasks; }
    public LiveData<List<com.trabajofinaldam.data.model.TaskModel>> getFinishedTasks() { return finishedTasks; }
    public LiveData<List<com.trabajofinaldam.data.model.TaskModel>> getInconclusasTasks() { return inconclusasTasks; }
    public LiveData<Integer> getEcoPuntos() { return ecoPuntos; }

    public void refreshData() {
        greetingText.setValue(buildGreeting());
        loadTasksFromDatabase();
        cargarEcoPuntos();
        cargarEcoConsejo();
    }

    public void refreshEcoConsejo() {
        cargarEcoConsejo();
    }

    private String buildGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hour < 12) return "¡Buenos días!";
        if (hour < 20) return "¡Buenas tardes!";
        return "¡Buenas noches!";
    }

    private void loadTasksFromDatabase() {
        List<com.trabajofinaldam.data.model.TaskModel> allTasks = repository.obtenerTodasLasTareas();
        List<com.trabajofinaldam.data.model.TaskModel> pendingRaw = repository.obtenerTareasPendientes();
        List<com.trabajofinaldam.data.model.TaskModel> doneTasks = repository.obtenerTareasFinalizadas();

        List<com.trabajofinaldam.data.model.TaskModel> inCourse = new java.util.ArrayList<>();
        List<com.trabajofinaldam.data.model.TaskModel> failed = new java.util.ArrayList<>();

        for (com.trabajofinaldam.data.model.TaskModel t : pendingRaw) {
            if (t.isVencida()) {
                failed.add(t);
            } else {
                inCourse.add(t);
            }
        }

        calendarTasks.postValue(inCourse);
        inconclusasTasks.postValue(failed);
        finishedTasks.postValue(doneTasks);
        updateProgress(allTasks);
    }

    private void updateProgress(List<com.trabajofinaldam.data.model.TaskModel> allTasks) {
        if (allTasks == null || allTasks.isEmpty()) {
            todayProgress.postValue(0);
            return;
        }
        int completed = 0;
        for (com.trabajofinaldam.data.model.TaskModel t : allTasks) {
            if (t.isCompletada()) {
                completed++;
            }
        }
        int percentage = (completed * 100) / allTasks.size();
        todayProgress.postValue(percentage);

        if (percentage == 100) {
            // "Se debe mantener ahí por un momento... Después... debe cambiar a 0%"
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                Integer current = todayProgress.getValue();
                if (current != null && current == 100) {
                    todayProgress.postValue(0);
                }
            }, 5000); // 5 segundos de espera
        }
    }

    private void cargarEcoPuntos() {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences(
                com.trabajofinaldam.ui.focus.FocusFragment.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int total = prefs.getInt(com.trabajofinaldam.ui.focus.FocusFragment.KEY_ECO_PUNTOS, 0);
        ecoPuntos.setValue(total);
    }

    private void cargarEcoConsejo() {
        com.trabajofinaldam.data.network.ApiService apiService = com.trabajofinaldam.data.network.ApiClient.getClient().create(com.trabajofinaldam.data.network.ApiService.class);
        apiService.obtenerConsejoHoy().enqueue(new retrofit2.Callback<com.trabajofinaldam.data.model.EcoConsejo>() {
            @Override
            public void onResponse(retrofit2.Call<com.trabajofinaldam.data.model.EcoConsejo> call,
                                   retrofit2.Response<com.trabajofinaldam.data.model.EcoConsejo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ecoConsejo.postValue(response.body().getTexto());
                } else {
                    ecoConsejo.postValue("Haz de hoy un día sostenible 🌿");
                }
            }

            @Override
            public void onFailure(retrofit2.Call<com.trabajofinaldam.data.model.EcoConsejo> call, Throwable t) {
                ecoConsejo.postValue("Planifica tus tareas y ahorra energía 🔋");
            }
        });
    }

    public void completarTarea(int tareaId) {
        repository.marcarCompletada(tareaId);
        refreshData();
    }

    public void desmarcarTarea(int tareaId) {
        repository.desmarcarTarea(tareaId);
        refreshData();
    }

    public void marcarIniciada(int tareaId) {
        repository.marcarIniciada(tareaId);
        refreshData();
    }

    public void postergarTarea(int id, String nuevaFecha) {
        repository.postergarTarea(id, nuevaFecha);
        refreshData();
    }

    public void eliminarTarea(int tareaId) {
        repository.eliminarTarea(tareaId);
        
        // Si la tarea eliminada era la seleccionada para Enfoque, la limpiamos
        com.trabajofinaldam.data.model.TaskModel selected = 
                com.trabajofinaldam.data.repository.SelectedTaskRepository.getSelectedTask();
        if (selected != null && selected.getId() == tareaId) {
            com.trabajofinaldam.data.repository.SelectedTaskRepository.clear();
        }

        refreshData();
    }
}
