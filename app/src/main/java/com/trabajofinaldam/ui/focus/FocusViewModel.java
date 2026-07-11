package com.trabajofinaldam.ui.focus;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trabajofinaldam.data.local.DatabaseHelper;
import com.trabajofinaldam.data.model.Subtarea;

import java.util.List;
import java.util.Locale;

public class FocusViewModel extends AndroidViewModel {

    private static final long INTERVALO_MS       = 1000L;
    private static final int  PUNTOS_POR_MINUTO  = 1;

    private long duracionTotalMs = 25 * 60 * 1000L; // Por defecto 25 min

    private final MutableLiveData<String>  tiempoRestante = new MutableLiveData<>();
    private final MutableLiveData<Integer> progreso       = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRunning      = new MutableLiveData<>();
    private final MutableLiveData<Integer> ecoPuntos      = new MutableLiveData<>();
    private final MutableLiveData<List<Subtarea>> subtasks = new MutableLiveData<>();

    private int puntosTotales = 0;
    private int puntosSesion = 0;

    private CountDownTimer countDownTimer;
    private long tiempoRestanteMs = duracionTotalMs;

    private final DatabaseHelper dbHelper;

    public FocusViewModel(@NonNull Application application) {
        super(application);
        dbHelper = DatabaseHelper.getInstance(application);

        android.content.SharedPreferences prefs = application.getSharedPreferences(FocusFragment.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        puntosTotales = prefs.getInt(FocusFragment.KEY_ECO_PUNTOS, 0);

        tiempoRestante.setValue(formatearTiempo(duracionTotalMs));
        progreso.setValue(0);
        isRunning.setValue(false);
        ecoPuntos.setValue(puntosSesion);
    }

    public LiveData<String>  getTiempoRestante() { return tiempoRestante; }
    public LiveData<Integer> getProgreso()       { return progreso; }
    public LiveData<Boolean> getIsRunning()      { return isRunning; }
    public LiveData<Integer> getEcoPuntos()      { return ecoPuntos; }

    // 2. Getter para que el Fragment pueda observar las subtareas
    public LiveData<List<Subtarea>> getSubtasks() { return subtasks; }

    public void setDuracion(int minutos) {
        if (Boolean.TRUE.equals(isRunning.getValue())) return;
        this.duracionTotalMs = minutos * 60 * 1000L;
        this.tiempoRestanteMs = duracionTotalMs;
        this.tiempoRestante.setValue(formatearTiempo(duracionTotalMs));
        this.progreso.setValue(0);
    }

    public int getDuracionMinutos() {
        return (int) (duracionTotalMs / (60 * 1000L));
    }

    // ===================================================================
    // LÓGICA DE SUBTAREAS (BD)
    // ===================================================================

    // Método para cargar desde SQLite
    public void cargarSubtareas(int tareaId) {
        List<Subtarea> lista = dbHelper.obtenerSubtareasDeTarea(tareaId);
        subtasks.setValue(lista);
    }

    // Método para marcar check/uncheck
    public void actualizarSubtarea(Subtarea subtarea, boolean completada) {
        // Asegúrate de que el método actualizarEstadoSubtarea exista en tu DatabaseHelper
        dbHelper.actualizarEstadoSubtarea(subtarea.getId(), completada);

        // Refrescar la lista actual para que la UI se entere del cambio
        List<Subtarea> current = subtasks.getValue();
        if (current != null) {
            subtasks.setValue(current); // Fuerza al observador a actualizar
        }
    }

    // ===================================================================
    // LÓGICA DEL TEMPORIZADOR
    // ===================================================================

    public void startTimer() {
        countDownTimer = new CountDownTimer(tiempoRestanteMs, INTERVALO_MS) {
            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteMs = millisUntilFinished;
                tiempoRestante.setValue(formatearTiempo(millisUntilFinished));
                long transcurrido = duracionTotalMs - millisUntilFinished;
                int porcentaje = (int) ((transcurrido * 100) / duracionTotalMs);
                progreso.setValue(porcentaje);
            }

            @Override
            public void onFinish() {
                tiempoRestanteMs = 0;
                tiempoRestante.setValue(formatearTiempo(0));
                progreso.setValue(100);
                isRunning.setValue(false);
                calcularEcoPuntos();
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
        int puntosTiempo = minutosCompletados * PUNTOS_POR_MINUTO;
        
        puntosTotales += puntosTiempo;
        puntosSesion += puntosTiempo;
        persistirPuntos();
        
        ecoPuntos.setValue(puntosSesion);
    }

    public void sumarPuntosSubtarea() {
        puntosTotales += 5;
        puntosSesion += 5;
        persistirPuntos();
        ecoPuntos.setValue(puntosSesion);
    }

    private void persistirPuntos() {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences(FocusFragment.PREFS_NAME, android.content.Context.MODE_PRIVATE);
        prefs.edit().putInt(FocusFragment.KEY_ECO_PUNTOS, puntosTotales).apply();
    }

    public void resetTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        tiempoRestanteMs = duracionTotalMs;
        tiempoRestante.setValue(formatearTiempo(duracionTotalMs));
        progreso.setValue(0);
        isRunning.setValue(false);
    }

    private String formatearTiempo(long millis) {
        long minutos = (millis / 1000) / 60;
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