package com.trabajofinaldam.ui.focus;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trabajofinaldam.data.model.EcoConsejo;
import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.data.network.ApiClient;
import com.trabajofinaldam.data.network.ApiService;
import com.trabajofinaldam.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FocusViewModel extends AndroidViewModel {

    private static final long INTERVALO_MS      = 1000L;
    private static final int  PUNTOS_POR_MINUTO = 1;

    private long duracionTotalMs = 25 * 60 * 1000L;

    private final MutableLiveData<String>        tiempoRestante  = new MutableLiveData<>();
    private final MutableLiveData<Integer>       progreso        = new MutableLiveData<>();
    private final MutableLiveData<Boolean>       isRunning       = new MutableLiveData<>();
    private final MutableLiveData<Integer>       ecoPuntos       = new MutableLiveData<>();
    private final MutableLiveData<String>        ecoConsejo      = new MutableLiveData<>();
    private final MutableLiveData<List<Subtarea>> subtareas      = new MutableLiveData<>();
    private final MutableLiveData<Boolean>       tareaCompletada = new MutableLiveData<>();

    private CountDownTimer  countDownTimer;
    private long            tiempoRestanteMs = duracionTotalMs;
    private int             currentTaskId    = -1;
    private final TaskRepository repository;

    public FocusViewModel(@NonNull Application application) {
        super(application);
        repository = TaskRepository.getInstance(application);
        tiempoRestante.setValue(formatearTiempo(duracionTotalMs));
        progreso.setValue(0);
        isRunning.setValue(false);
        ecoPuntos.setValue(0);
        ecoConsejo.setValue("Inicia tu sesión para recibir un Eco-Consejo 🌿");
        subtareas.setValue(new ArrayList<>());
    }

    public LiveData<String>        getTiempoRestante()  { return tiempoRestante; }
    public LiveData<Integer>       getProgreso()        { return progreso; }
    public LiveData<Boolean>       getIsRunning()       { return isRunning; }
    public LiveData<Integer>       getEcoPuntos()       { return ecoPuntos; }
    public LiveData<String>        getEcoConsejo()      { return ecoConsejo; }
    public LiveData<List<Subtarea>> getSubtareas()      { return subtareas; }
    public LiveData<Boolean>       getTareaCompletada() { return tareaCompletada; }

    public void cargarSubtareas(int taskId) {
        this.currentTaskId = taskId;
        if (taskId == -1) return;
        List<Subtarea> lista = repository.obtenerSubtareasDeTarea(taskId);
        subtareas.setValue(lista);
    }

    public void onSubtareaChecked(Subtarea subtarea, boolean isChecked, boolean allCompleted) {
        if (subtarea.getId() != -1) {
            repository.marcarSubtareaCompletada(subtarea.getId());
        }
        if (allCompleted && currentTaskId != -1) {
            repository.marcarCompletada(currentTaskId);
            tareaCompletada.setValue(true);
        }
    }

    public void setDuracion(int minutos) {
        if (Boolean.TRUE.equals(isRunning.getValue())) return;
        if (minutos < 1) minutos = 1;
        duracionTotalMs  = minutos * 60 * 1000L;
        tiempoRestanteMs = duracionTotalMs;
        tiempoRestante.setValue(formatearTiempo(duracionTotalMs));
        progreso.setValue(0);
        ecoPuntos.setValue(0);
    }

    public void startTimer() {
        pedirEcoConsejo();
        final long duracion = duracionTotalMs;
        countDownTimer = new CountDownTimer(tiempoRestanteMs, INTERVALO_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteMs = millisUntilFinished;
                tiempoRestante.setValue(formatearTiempo(millisUntilFinished));
                long transcurrido = duracion - millisUntilFinished;
                progreso.setValue((int) ((transcurrido * 100) / duracion));
            }

            @Override
            public void onFinish() {
                tiempoRestanteMs = 0;
                tiempoRestante.setValue(formatearTiempo(0));
                progreso.setValue(100);
                isRunning.setValue(false);
                calcularEcoPuntos();
                // Al completar el pomodoro, también completa la tarea
                if (currentTaskId != -1) {
                    repository.marcarCompletada(currentTaskId);
                    tareaCompletada.setValue(true);
                }
            }
        }.start();
        isRunning.setValue(true);
    }

    public void pauseTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning.setValue(false);
    }

    public void stopTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        isRunning.setValue(false);
        calcularEcoPuntos();
    }

    public void calcularEcoPuntos() {
        long transcurridoMs = duracionTotalMs - tiempoRestanteMs;
        int minutosCompletados = (int) (transcurridoMs / (60 * 1000L));
        ecoPuntos.setValue(minutosCompletados * PUNTOS_POR_MINUTO);
    }

    public void pedirEcoConsejo() {
        ecoConsejo.setValue("Cargando consejo...");
        ApiService api = ApiClient.getClient().create(ApiService.class);
        api.obtenerConsejoRandom().enqueue(new Callback<EcoConsejo>() {
            @Override
            public void onResponse(Call<EcoConsejo> call, Response<EcoConsejo> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getTexto() != null) {
                    ecoConsejo.postValue(response.body().getTexto());
                } else {
                    ecoConsejo.postValue(fallback());
                }
            }

            @Override
            public void onFailure(Call<EcoConsejo> call, Throwable t) {
                ecoConsejo.postValue(fallback());
            }
        });
    }

    private String fallback() {
        return "Divide tus tareas grandes en pasos pequeños: avanzar poco a poco es más sostenible. 🌱";
    }

    public void resetTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        tiempoRestanteMs = duracionTotalMs;
        tiempoRestante.setValue(formatearTiempo(duracionTotalMs));
        progreso.setValue(0);
        isRunning.setValue(false);
    }

    private String formatearTiempo(long millis) {
        long minutos  = (millis / 1000) / 60;
        long segundos = (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}
