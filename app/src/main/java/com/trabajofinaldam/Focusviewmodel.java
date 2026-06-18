package com.trabajofinaldam;


import android.os.CountDownTimer;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;

/**
 * FocusViewModel — VIEWMODEL del temporizador Pomodoro.
 *
 * RESPONSABILIDADES:
 *   ✅ Manejar el CountDownTimer (no se detiene al girar la pantalla).
 *   ✅ Exponer el tiempo restante, el progreso y el estado vía LiveData.
 *   ✅ Calcular los Eco-Puntos según minutos completados (ODS 12).
 *
 * Como el ViewModel sobrevive a la rotación, el temporizador
 * sigue corriendo correctamente sin perder el conteo.
 *
 * Extiende ViewModel (no AndroidViewModel) porque no necesita Context.
 */
public class Focusviewmodel extends ViewModel {

    // ===================================================================
    // CONFIGURACIÓN POMODORO
    // ===================================================================
    private static final long DURACION_TOTAL_MS = 25 * 60 * 1000L; // 25 min
    private static final long INTERVALO_MS       = 1000L;          // tick cada 1s
    private static final int  PUNTOS_POR_MINUTO  = 1;              // 1 EcoPunto/min

    // ===================================================================
    // LIVEDATA EXPUESTOS A LA VISTA
    // ===================================================================
    private final MutableLiveData<String>  tiempoRestante = new MutableLiveData<>();
    private final MutableLiveData<Integer> progreso        = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isRunning       = new MutableLiveData<>();
    private final MutableLiveData<Integer> ecoPuntos       = new MutableLiveData<>();

    // ===================================================================
    // ESTADO INTERNO
    // ===================================================================
    private CountDownTimer countDownTimer;
    private long tiempoRestanteMs = DURACION_TOTAL_MS;

    // ===================================================================
    // CONSTRUCTOR — inicializa valores por defecto
    // ===================================================================
    public Focusviewmodel() {
        tiempoRestante.setValue(formatearTiempo(DURACION_TOTAL_MS));
        progreso.setValue(0);
        isRunning.setValue(false);
        ecoPuntos.setValue(0);
    }

    // ===================================================================
    // GETTERS DE LIVEDATA
    // ===================================================================
    public LiveData<String>  getTiempoRestante() { return tiempoRestante; }
    public LiveData<Integer> getProgreso()       { return progreso; }
    public LiveData<Boolean> getIsRunning()      { return isRunning; }
    public LiveData<Integer> getEcoPuntos()      { return ecoPuntos; }

    // ===================================================================
    // startTimer — Inicia o reanuda la cuenta regresiva
    // ===================================================================
    public void startTimer() {
        countDownTimer = new CountDownTimer(tiempoRestanteMs, INTERVALO_MS) {

            @Override
            public void onTick(long millisUntilFinished) {
                tiempoRestanteMs = millisUntilFinished;

                // Actualiza texto "MM:SS"
                tiempoRestante.setValue(formatearTiempo(millisUntilFinished));

                // Calcula progreso 0–100 (tiempo transcurrido / total)
                long transcurrido = DURACION_TOTAL_MS - millisUntilFinished;
                int porcentaje = (int) ((transcurrido * 100) / DURACION_TOTAL_MS);
                progreso.setValue(porcentaje);
            }

            @Override
            public void onFinish() {
                // Pomodoro completo: 100% y se calculan los puntos
                tiempoRestanteMs = 0;
                tiempoRestante.setValue(formatearTiempo(0));
                progreso.setValue(100);
                isRunning.setValue(false);
                calcularEcoPuntos();
            }
        }.start();

        isRunning.setValue(true);
    }

    // ===================================================================
    // pauseTimer — Pausa sin reiniciar (conserva tiempoRestanteMs)
    // ===================================================================
    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning.setValue(false);
    }

    // ===================================================================
    // stopTimer — Detiene y calcula los Eco-Puntos ganados
    // Llamado al pulsar "Finalizar y Sumar Eco-Puntos"
    // ===================================================================
    public void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        isRunning.setValue(false);
        calcularEcoPuntos();
    }

    // ===================================================================
    // calcularEcoPuntos — Puntos según minutos efectivamente enfocados
    // (ODS 12: recompensa el uso responsable y eficiente del tiempo)
    // ===================================================================
    public void calcularEcoPuntos() {
        long transcurridoMs = DURACION_TOTAL_MS - tiempoRestanteMs;
        int minutosCompletados = (int) (transcurridoMs / (60 * 1000L));
        int puntos = minutosCompletados * PUNTOS_POR_MINUTO;
        ecoPuntos.setValue(puntos);
    }

    // ===================================================================
    // resetTimer — Reinicia a 25:00 (opcional, para nueva sesión)
    // ===================================================================
    public void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        tiempoRestanteMs = DURACION_TOTAL_MS;
        tiempoRestante.setValue(formatearTiempo(DURACION_TOTAL_MS));
        progreso.setValue(0);
        isRunning.setValue(false);
    }

    // ===================================================================
    // HELPER — Convierte milisegundos a "MM:SS"
    // ===================================================================
    private String formatearTiempo(long millis) {
        long minutos = (millis / 1000) / 60;
        long segundos = (millis / 1000) % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos);
    }

    // ===================================================================
    // onCleared — Limpia el timer cuando el ViewModel se destruye
    // Evita fugas de memoria.
    // ===================================================================
    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}