package com.trabajofinaldam;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Arrays;
import java.util.List;

/**
 * FocusActivity — VIEW del Modo Enfoque (Pomodoro).
 *
 * MVVM:
 *   - Observa los LiveData de FocusViewModel para actualizar la UI.
 *   - El temporizador vive en el ViewModel (sobrevive rotaciones).
 *   - Al finalizar, suma los Eco-Puntos al total global usando SharedPreferences.
 *
 * Layout: res/layout/activity_focus.xml
 */
public class Focusactivity extends AppCompatActivity {

    // ===================================================================
    // CONSTANTES DE SHAREDPREFERENCES
    // ===================================================================
    public static final String PREFS_NAME       = "ecotask_prefs";
    public static final String KEY_ECO_PUNTOS    = "total_eco_puntos";

    // ===================================================================
    // VISTAS
    // ===================================================================
    private TextView tvTaskTitle;
    private TextView tvTimer;
    private TextView tvEcoPoints;
    private TextView tvSubtaskCount;
    private CircularProgressIndicator progressFocus;
    private MaterialButton btnStartPause;
    private MaterialButton btnFinish;
    private LinearLayout layoutBack;
    private RecyclerView recyclerSubtasks;
    private SubtaskAdapter subtaskAdapter;

    // ===================================================================
    // VIEWMODEL
    // ===================================================================
    private Focusviewmodel viewModel;

    // ===================================================================
    // CICLO DE VIDA
    // ===================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_focus);

        bindViews();
        initViewModel();
        setupNavigation();
        cargarTituloTarea();
        setupSubtasks();
        setupListeners();
        observeViewModel();
    }

    // ===================================================================
    // PASO 1 — BIND VIEWS
    // ===================================================================
    private void bindViews() {
        tvTaskTitle    = findViewById(R.id.tv_task_title);
        tvTimer        = findViewById(R.id.tv_timer);
        tvEcoPoints    = findViewById(R.id.tv_eco_points);
        tvSubtaskCount = findViewById(R.id.tv_subtask_count);
        progressFocus  = findViewById(R.id.progress_focus);
        btnStartPause  = findViewById(R.id.btn_start_pause);
        btnFinish         = findViewById(R.id.btn_finish);
        layoutBack        = findViewById(R.id.layout_back);
        recyclerSubtasks  = findViewById(R.id.recycler_subtasks);
    }

    // ===================================================================
    // PASO 2 — INIT VIEWMODEL
    // ===================================================================
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(Focusviewmodel.class);
    }

    // ===================================================================
    // PASO 2b — SETUP SUBTAREAS
    // ===================================================================
    private void setupSubtasks() {
        List<String> subtareas = Arrays.asList(
                "Leer artículos de investigación",
                "Redactar esquema del proyecto",
                "Diseñar métricas de sostenibilidad"
        );

        subtaskAdapter = new SubtaskAdapter(subtareas);
        recyclerSubtasks.setLayoutManager(new LinearLayoutManager(this));
        recyclerSubtasks.setNestedScrollingEnabled(false);
        recyclerSubtasks.setAdapter(subtaskAdapter);

        tvSubtaskCount.setText("0 / " + subtareas.size() + " completadas");
    }

    // ===================================================================
    // Carga el nombre de la tarea recibido por Intent (desde el Dashboard)
    // ===================================================================
    private void cargarTituloTarea() {
        String titulo = getIntent().getStringExtra("TASK_TITLE");
        if (titulo != null && !titulo.isEmpty()) {
            tvTaskTitle.setText(titulo);
        }
    }

    // ===================================================================
    // PASO 3 — LISTENERS
    // ===================================================================
    private void setupListeners() {

        // Volver al Dashboard
        layoutBack.setOnClickListener(v -> finish());

        // Iniciar / Pausar según el estado actual
        btnStartPause.setOnClickListener(v -> {
            Boolean running = viewModel.getIsRunning().getValue();
            if (running != null && running) {
                viewModel.pauseTimer();
            } else {
                viewModel.startTimer();
            }
        });

        // Finalizar y sumar Eco-Puntos
        btnFinish.setOnClickListener(v -> {
            viewModel.stopTimer(); // dispara calcularEcoPuntos()
            finalizarSesion();
        });
    }

    // ===================================================================
    // PASO 4 — OBSERVAR LIVEDATA
    // ===================================================================
    private void observeViewModel() {

        // Texto gigante del temporizador "MM:SS"
        viewModel.getTiempoRestante().observe(this, tiempo ->
                tvTimer.setText(tiempo));

        // Progreso del círculo (0–100)
        viewModel.getProgreso().observe(this, progreso ->
                progressFocus.setProgressCompat(progreso, true));

        // Estado: cambia el texto del botón Iniciar/Pausar
        viewModel.getIsRunning().observe(this, running -> {
            if (running != null && running) {
                btnStartPause.setText("⏸  Pausar");
            } else {
                btnStartPause.setText("▶  Iniciar Enfoque");
            }
        });

        // Eco-Puntos de la sesión (preview en pantalla)
        viewModel.getEcoPuntos().observe(this, puntos ->
                tvEcoPoints.setText("+" + puntos));
    }

    // ===================================================================
    // NAVEGACIÓN INFERIOR — listeners + resalte del tab activo
    // ===================================================================
    private void setupNavigation() {
        LinearLayout navItemHome  = findViewById(R.id.nav_item_home);
        LinearLayout navItemNew   = findViewById(R.id.nav_item_new);
        LinearLayout navItemFocus = findViewById(R.id.nav_item_focus);

        // Inicio → volver al Dashboard
        navItemHome.setOnClickListener(v -> finish());

        // Nueva → abrir pantalla de nueva tarea
        navItemNew.setOnClickListener(v ->
                startActivity(new Intent(this, NewtaskActivity.class)));

        // Enfoque → ya estamos aquí, no hace nada

        // Resaltar "Enfoque" como tab activo
        setNavItemActive(navItemFocus, true);
        setNavItemActive(navItemHome, false);
        setNavItemActive(navItemNew, false);
    }

    private void setNavItemActive(LinearLayout navItem, boolean active) {
        int color = active
                ? getResources().getColor(R.color.eco_green_primary, getTheme())
                : getResources().getColor(R.color.text_nav_inactive, getTheme());
        ((TextView) navItem.getChildAt(0)).setTextColor(color);
        ((TextView) navItem.getChildAt(1)).setTextColor(color);
    }

    // ===================================================================
    // FINALIZAR SESIÓN — Suma los Eco-Puntos al total con SharedPreferences
    // (núcleo del ODS 12: recompensa el uso responsable del tiempo)
    // ===================================================================
    private void finalizarSesion() {

        // Eco-Puntos ganados en esta sesión (ya calculados por el ViewModel)
        Integer puntosSesion = viewModel.getEcoPuntos().getValue();
        if (puntosSesion == null) puntosSesion = 0;

        // 1. Abrir SharedPreferences
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // 2. LEER el total actual (0 si no existe aún)
        int totalActual = prefs.getInt(KEY_ECO_PUNTOS, 0);

        // 3. SUMAR los nuevos puntos
        int nuevoTotal = totalActual + puntosSesion;

        // 4. GUARDAR el total actualizado
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_ECO_PUNTOS, nuevoTotal);
        editor.apply(); // apply() es asíncrono (no bloquea la UI)

        // 5. Toast de felicitación sostenible
        Toast.makeText(
                this,
                "🌿 ¡Enfoque sostenible! Ganaste " + puntosSesion +
                        " Eco-Puntos. Total: " + nuevoTotal,
                Toast.LENGTH_LONG
        ).show();

        // 6. Volver al Dashboard
        finish();
    }
}