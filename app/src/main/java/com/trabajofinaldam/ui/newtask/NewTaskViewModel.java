package com.trabajofinaldam.ui.newtask;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trabajofinaldam.data.model.DivisionRequest;
import com.trabajofinaldam.data.model.DivisionResponse;
import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.data.model.TaskModel;
import com.trabajofinaldam.data.network.ApiClient;
import com.trabajofinaldam.data.network.ApiService;
import com.trabajofinaldam.data.repository.TaskRepository;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewTaskViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> guardadoExitoso = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cargando = new MutableLiveData<>();
    private final TaskRepository repository;

    // TÚ CÓDIGO: LIVEDATA PARA ALERTAS DE LA IA
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public NewTaskViewModel(@NonNull Application application) {
        super(application);
        repository = TaskRepository.getInstance(application);
    }

    public LiveData<Boolean> getGuardadoExitoso() { return guardadoExitoso; }
    public LiveData<Boolean> getCargando() { return cargando; }
    public LiveData<String> getError() { return error; } // Getter del error

    public void guardarNuevaTarea(String titulo, String descripcion, String fecha, String hora,
                                  String prioridad, int targetCount) {
        cargando.setValue(true);
        TaskModel nuevaTarea = new TaskModel(
                titulo, descripcion, fecha, prioridad,
                false, true, hora);

        long tareaId = repository.insertarTarea(nuevaTarea);

        if (tareaId != -1) {
            dividirTareaConIA((int) tareaId, descripcion, targetCount);
        } else {
            cargando.setValue(false);
            guardadoExitoso.setValue(false);
            error.setValue("Error al guardar la tarea en la base de datos.");
        }
    }

    private void dividirTareaConIA(int tareaId, String descripcion, int targetCount) {
        ApiService apiService = ApiClient.getApiService();
        int requestHoras = (targetCount > 0) ? targetCount : 5;
        DivisionRequest request = new DivisionRequest(descripcion, requestHoras);

        apiService.dividirTarea(request).enqueue(new Callback<DivisionResponse>() {
            @Override
            public void onResponse(Call<DivisionResponse> call, Response<DivisionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> subtareasTexto = response.body().getSubtareas();
                    if (subtareasTexto != null && !subtareasTexto.isEmpty()) {
                        if (targetCount > 0) {
                            for (int i = 0; i < targetCount; i++) {
                                String texto;
                                if (i < subtareasTexto.size()) {
                                    texto = subtareasTexto.get(i);
                                } else {
                                    texto = "Subobjetivo adicional " + (i + 1);
                                }
                                repository.insertarSubtarea(new Subtarea(tareaId, texto, false));
                            }
                        } else {
                            for (String texto : subtareasTexto) {
                                repository.insertarSubtarea(new Subtarea(tareaId, texto, false));
                            }
                        }
                    } else if (targetCount > 0) {
                        for (int i = 1; i <= targetCount; i++) {
                            repository.insertarSubtarea(new Subtarea(tareaId, "Subobjetivo " + i, false));
                        }
                    }
                    guardadoExitoso.postValue(true);
                } else {
                    // TÚ CÓDIGO: Avisar si la API falló, pero generar las de respaldo
                    error.postValue("La IA falló (Código: " + response.code() + "). Usando subtareas de respaldo.");
                    if (targetCount > 0) {
                        for (int i = 1; i <= targetCount; i++) {
                            repository.insertarSubtarea(new Subtarea(tareaId, "Subobjetivo " + i, false));
                        }
                    }
                    guardadoExitoso.postValue(true);
                }
                cargando.postValue(false);
            }

            @Override
            public void onFailure(Call<DivisionResponse> call, Throwable t) {
                // TÚ CÓDIGO: Avisar si no hay internet, pero generar las de respaldo
                error.postValue("Error de red: " + t.getMessage() + ". Usando subtareas de respaldo.");
                if (targetCount > 0) {
                    for (int i = 1; i <= targetCount; i++) {
                        repository.insertarSubtarea(new Subtarea(tareaId, "Subobjetivo " + i, false));
                    }
                }
                guardadoExitoso.postValue(true);
                cargando.postValue(false);
            }
        });
    }
}