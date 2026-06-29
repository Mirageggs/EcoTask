package com.trabajofinaldam;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.Arrays;
import java.util.List;

/**
 * FocusFragment — VIEW del Modo Enfoque (antes Focusactivity).
 *
 * Recibe el título de la tarea por argumento de navegación ("taskTitle").
 * Las constantes de SharedPreferences viven aquí y las reutiliza
 * DashboardViewModel.
 */
public class FocusFragment extends Fragment {

    public static final String PREFS_NAME    = "ecotask_prefs";
    public static final String KEY_ECO_PUNTOS = "total_eco_puntos";

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

    private Focusviewmodel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_focus, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        viewModel = new ViewModelProvider(this).get(Focusviewmodel.class);
        cargarTituloTarea();
        setupSubtasks();
        setupListeners();
        observeViewModel();
    }

    private void bindViews(View v) {
        tvTaskTitle      = v.findViewById(R.id.tv_task_title);
        tvTimer          = v.findViewById(R.id.tv_timer);
        tvEcoPoints      = v.findViewById(R.id.tv_eco_points);
        tvSubtaskCount   = v.findViewById(R.id.tv_subtask_count);
        progressFocus    = v.findViewById(R.id.progress_focus);
        btnStartPause    = v.findViewById(R.id.btn_start_pause);
        btnFinish        = v.findViewById(R.id.btn_finish);
        layoutBack       = v.findViewById(R.id.layout_back);
        recyclerSubtasks = v.findViewById(R.id.recycler_subtasks);
    }

    private void setupSubtasks() {
        List<String> subtareas = Arrays.asList(
                "Leer artículos de investigación",
                "Redactar esquema del proyecto",
                "Diseñar métricas de sostenibilidad");

        subtaskAdapter = new SubtaskAdapter(subtareas);
        recyclerSubtasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSubtasks.setNestedScrollingEnabled(false);
        recyclerSubtasks.setAdapter(subtaskAdapter);

        tvSubtaskCount.setText("0 / " + subtareas.size() + " completadas");
    }

    private void cargarTituloTarea() {
        String titulo = getArguments() != null ? getArguments().getString("taskTitle") : null;
        if (titulo == null || titulo.isEmpty()) {
            titulo = "Sesión de Enfoque";
        }
        tvTaskTitle.setText(titulo);
    }

    private void setupListeners() {
        layoutBack.setOnClickListener(v -> volverAInicio());

        btnStartPause.setOnClickListener(v -> {
            Boolean running = viewModel.getIsRunning().getValue();
            if (running != null && running) {
                viewModel.pauseTimer();
            } else {
                viewModel.startTimer();
            }
        });

        btnFinish.setOnClickListener(v -> {
            viewModel.stopTimer();
            finalizarSesion();
        });
    }

    private void observeViewModel() {
        viewModel.getTiempoRestante().observe(getViewLifecycleOwner(), tvTimer::setText);

        viewModel.getProgreso().observe(getViewLifecycleOwner(), progreso ->
                progressFocus.setProgressCompat(progreso, true));

        viewModel.getIsRunning().observe(getViewLifecycleOwner(), running -> {
            if (running != null && running) {
                btnStartPause.setText("⏸  Pausar");
            } else {
                btnStartPause.setText("▶  Iniciar Enfoque");
            }
        });

        viewModel.getEcoPuntos().observe(getViewLifecycleOwner(), puntos ->
                tvEcoPoints.setText("+" + puntos));
    }

    private void finalizarSesion() {
        Integer puntosSesion = viewModel.getEcoPuntos().getValue();
        if (puntosSesion == null) puntosSesion = 0;

        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int totalActual = prefs.getInt(KEY_ECO_PUNTOS, 0);
        int nuevoTotal = totalActual + puntosSesion;
        prefs.edit().putInt(KEY_ECO_PUNTOS, nuevoTotal).apply();

        Toast.makeText(requireContext(),
                "🌿 ¡Enfoque sostenible! Ganaste " + puntosSesion +
                        " Eco-Puntos. Total: " + nuevoTotal,
                Toast.LENGTH_LONG).show();

        volverAInicio();
    }

    private void volverAInicio() {
        NavHostFragment.findNavController(this)
                .popBackStack(R.id.dashboardFragment, false);
    }
}
