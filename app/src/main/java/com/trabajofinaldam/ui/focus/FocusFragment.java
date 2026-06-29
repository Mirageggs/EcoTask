package com.trabajofinaldam.ui.focus;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.ui.adapter.SubtaskAdapter;

import java.util.ArrayList;
import java.util.List;

public class FocusFragment extends Fragment {

    public static final String PREFS_NAME     = "ecotask_prefs";
    public static final String KEY_ECO_PUNTOS = "total_eco_puntos";

    private TextView                  tvTaskTitle;
    private TextView                  tvTimer;
    private TextView                  tvEcoPoints;
    private TextView                  tvSubtaskCount;
    private TextView                  tvEcoConsejo;
    private CircularProgressIndicator progressFocus;
    private MaterialButton            btnStartPause;
    private MaterialButton            btnFinish;
    private MaterialButton            btnNuevoConsejo;
    private LinearLayout              layoutBack;
    private RecyclerView              recyclerSubtasks;
    private TextInputLayout           layoutDuracion;
    private TextInputEditText         etDuracion;
    private SubtaskAdapter            subtaskAdapter;

    private FocusViewModel viewModel;
    private int taskId = -1;

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
        viewModel = new ViewModelProvider(this).get(FocusViewModel.class);
        cargarArgumentos();
        setupRecyclerView();
        setupListeners();
        observeViewModel();
    }

    private void bindViews(View v) {
        tvTaskTitle      = v.findViewById(R.id.tv_task_title);
        tvTimer          = v.findViewById(R.id.tv_timer);
        tvEcoPoints      = v.findViewById(R.id.tv_eco_points);
        tvSubtaskCount   = v.findViewById(R.id.tv_subtask_count);
        tvEcoConsejo     = v.findViewById(R.id.tv_eco_consejo);
        progressFocus    = v.findViewById(R.id.progress_focus);
        btnStartPause    = v.findViewById(R.id.btn_start_pause);
        btnFinish        = v.findViewById(R.id.btn_finish);
        btnNuevoConsejo  = v.findViewById(R.id.btn_nuevo_consejo);
        layoutBack       = v.findViewById(R.id.layout_back);
        recyclerSubtasks = v.findViewById(R.id.recycler_subtasks);
        layoutDuracion   = v.findViewById(R.id.layout_duracion);
        etDuracion       = v.findViewById(R.id.et_duracion);
    }

    private void cargarArgumentos() {
        Bundle args = getArguments();
        String titulo = args != null ? args.getString("taskTitle") : null;
        taskId        = args != null ? args.getInt("taskId", -1) : -1;
        tvTaskTitle.setText(titulo != null && !titulo.isEmpty() ? titulo : "Sesión de Enfoque");
        viewModel.cargarSubtareas(taskId);
    }

    private void setupRecyclerView() {
        subtaskAdapter = new SubtaskAdapter(new ArrayList<>(), this::onSubtareaChecked);
        recyclerSubtasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSubtasks.setNestedScrollingEnabled(false);
        recyclerSubtasks.setAdapter(subtaskAdapter);
    }

    private void onSubtareaChecked(Subtarea subtarea, boolean isChecked, boolean allCompleted) {
        viewModel.onSubtareaChecked(subtarea, isChecked, allCompleted);
        int total     = subtaskAdapter.getTotalSubtareas();
        int completed = 0;
        List<Subtarea> lista = viewModel.getSubtareas().getValue();
        if (lista != null) {
            for (Subtarea s : lista) { if (s.isCompletada()) completed++; }
        }
        tvSubtaskCount.setText(completed + " / " + total + " completadas");
    }

    private void setupListeners() {
        layoutBack.setOnClickListener(v -> volverAInicio());

        btnStartPause.setOnClickListener(v -> {
            Boolean running = viewModel.getIsRunning().getValue();
            if (running != null && running) {
                viewModel.pauseTimer();
            } else {
                aplicarDuracionYIniciar();
            }
        });

        btnFinish.setOnClickListener(v -> {
            viewModel.stopTimer();
            finalizarSesion();
        });

        btnNuevoConsejo.setOnClickListener(v -> viewModel.pedirEcoConsejo());
    }

    private void aplicarDuracionYIniciar() {
        String input = etDuracion.getText() != null ? etDuracion.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(input)) {
            try { viewModel.setDuracion(Integer.parseInt(input)); }
            catch (NumberFormatException ignored) { }
        }
        viewModel.startTimer();
    }

    private void observeViewModel() {
        viewModel.getTiempoRestante().observe(getViewLifecycleOwner(), tvTimer::setText);

        viewModel.getProgreso().observe(getViewLifecycleOwner(), p ->
                progressFocus.setProgressCompat(p, true));

        viewModel.getIsRunning().observe(getViewLifecycleOwner(), running -> {
            boolean activo = running != null && running;
            layoutDuracion.setVisibility(activo ? View.GONE : View.VISIBLE);
            btnStartPause.setText(activo ? "⏸  Pausar" : "▶  Iniciar Enfoque");
        });

        viewModel.getEcoPuntos().observe(getViewLifecycleOwner(), puntos ->
                tvEcoPoints.setText("+" + puntos));

        viewModel.getEcoConsejo().observe(getViewLifecycleOwner(), consejo ->
                tvEcoConsejo.setText(consejo));

        viewModel.getSubtareas().observe(getViewLifecycleOwner(), lista -> {
            subtaskAdapter.setSubtasks(lista);
            tvSubtaskCount.setText("0 / " + lista.size() + " completadas");
        });

        viewModel.getTareaCompletada().observe(getViewLifecycleOwner(), completada -> {
            if (completada != null && completada) {
                Toast.makeText(requireContext(),
                        "✅ ¡Tarea completada! El progreso se ha actualizado.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void finalizarSesion() {
        Integer puntosSesion = viewModel.getEcoPuntos().getValue();
        if (puntosSesion == null) puntosSesion = 0;

        SharedPreferences prefs = requireContext()
                .getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE);
        int totalActual = prefs.getInt(KEY_ECO_PUNTOS, 0);
        int nuevoTotal  = totalActual + puntosSesion;
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
