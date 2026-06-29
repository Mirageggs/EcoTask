package com.trabajofinaldam.ui.newtask;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trabajofinaldam.data.model.DivisionRequest;
import com.trabajofinaldam.data.model.DivisionResponse;
import com.trabajofinaldam.data.model.TaskModel;
import com.trabajofinaldam.data.network.ApiClient;
import com.trabajofinaldam.data.network.ApiService;
import com.trabajofinaldam.data.repository.TaskRepository;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewTaskViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> guardadoExitoso = new MutableLiveData<>();
    private final MutableLiveData<Boolean> dividiendoIa    = new MutableLiveData<>(false);
    private final TaskRepository repository;

    public NewTaskViewModel(@NonNull Application application) {
        super(application);
        repository = TaskRepository.getInstance(application);
    }

    public LiveData<Boolean> getGuardadoExitoso() { return guardadoExitoso; }
    public LiveData<Boolean> getDividiendoIa()    { return dividiendoIa; }

    public void guardarNuevaTarea(String descripcion, String fecha,
                                  String prioridad, int horasEstimadas) {
        // 1. Guardar la tarea principal
        TaskModel nuevaTarea = new TaskModel(descripcion, fecha, prioridad, false, true, "");
        long tareaId = repository.insertarTarea(nuevaTarea);

        if (tareaId == -1) {
            guardadoExitoso.setValue(false);
            return;
        }

        // 2. Llamar a la IA para dividir la tarea en subtareas
        dividiendoIa.setValue(true);
        DivisionRequest request = new DivisionRequest(descripcion, horasEstimadas);
        ApiService api = ApiClient.getClient().create(ApiService.class);

        api.dividirTarea(request).enqueue(new Callback<DivisionResponse>() {
            @Override
            public void onResponse(Call<DivisionResponse> call, Response<DivisionResponse> response) {
                dividiendoIa.postValue(false);
                if (response.isSuccessful() && response.body() != null
                        && response.body().getSubtareas() != null
                        && !response.body().getSubtareas().isEmpty()) {
                    // Guardar subtareas generadas por IA
                    repository.insertarSubtareas(response.body().getSubtareas(), (int) tareaId);
                }
                // Notificar éxito independientemente del resultado de la IA
                guardadoExitoso.postValue(true);
            }

            @Override
            public void onFailure(Call<DivisionResponse> call, Throwable t) {
                // La IA falló pero la tarea ya fue guardada — notificar éxito igual
                dividiendoIa.postValue(false);
                guardadoExitoso.postValue(true);
            }
        });
    }
}
