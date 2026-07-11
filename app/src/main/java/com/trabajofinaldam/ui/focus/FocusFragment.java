package com.trabajofinaldam.ui.focus;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.trabajofinaldam.R;
import com.trabajofinaldam.ui.adapter.SubtaskAdapter;

import java.util.ArrayList;
import java.util.List;

import com.trabajofinaldam.data.model.TaskModel;
import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.data.repository.SelectedTaskRepository;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class FocusFragment extends Fragment {

    public static final String PREFS_NAME    = "ecotask_prefs";
    public static final String KEY_ECO_PUNTOS = "total_eco_puntos";
    private LinearLayout layoutEmptyState;
    private LinearLayout layoutFocusContent;

    private TextView tvTaskTitle;
    private TextView tvTimer;
    private TextView tvEcoPoints;
    private TextView tvSubtaskCount;
    private TextView tvDurationAdjust;
    private CircularProgressIndicator progressFocus;
    private MaterialButton btnStartPause;
    private MaterialButton btnFinish;
    private MaterialButton btnPlusTime;
    private MaterialButton btnMinusTime;
    private LinearLayout layoutBack;
    private LinearLayout layoutAdjustTime;
    private View layoutStartMessage;
    private RecyclerView recyclerSubtasks;
    private SubtaskAdapter subtaskAdapter;
    private int currentMinutes = 25;

    private FocusViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_focus, container, false);

        layoutEmptyState = view.findViewById(R.id.layoutEmptyState);
        layoutFocusContent = view.findViewById(R.id.layoutFocusContent);
        layoutStartMessage = view.findViewById(R.id.layout_start_message);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        viewModel = new ViewModelProvider(this).get(FocusViewModel.class);

        setupSubtasks();
        setupListeners();
        observeViewModel();
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarTituloTarea();
    }

    private void bindViews(View v) {
        tvTaskTitle      = v.findViewById(R.id.tv_task_title);
        tvTimer          = v.findViewById(R.id.tv_timer);
        tvEcoPoints      = v.findViewById(R.id.tv_eco_points);
        tvSubtaskCount   = v.findViewById(R.id.tv_subtask_count);
        tvDurationAdjust = v.findViewById(R.id.tv_duration_adjust);
        progressFocus    = v.findViewById(R.id.progress_focus);
        btnStartPause    = v.findViewById(R.id.btn_start_pause);
        btnFinish        = v.findViewById(R.id.btn_finish);
        btnPlusTime      = v.findViewById(R.id.btn_plus_time);
        btnMinusTime     = v.findViewById(R.id.btn_minus_time);
        layoutBack       = v.findViewById(R.id.layout_back);
        layoutAdjustTime = v.findViewById(R.id.layout_adjust_time);
        recyclerSubtasks = v.findViewById(R.id.recycler_subtasks);
    }

    private void setupSubtasks() {
        // Inicializamos el adaptador con la lista y el listener de puntos
        subtaskAdapter = new SubtaskAdapter(new ArrayList<>(), (subtarea, isChecked) -> {
            viewModel.actualizarSubtarea(subtarea, isChecked);
            if (isChecked) {
                viewModel.sumarPuntosSubtarea();
            }
        });

        recyclerSubtasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerSubtasks.setNestedScrollingEnabled(false);
        recyclerSubtasks.setAdapter(subtaskAdapter);

        tvSubtaskCount.setText("0 / 0 completadas");
    }

    private void sumarPuntosPorSubtarea() {
        // Este método ya no es necesario si usamos el ViewModel
        // Pero lo mantengo si hay otras llamadas, o lo borro
    }

    private void cargarTituloTarea() {
        TaskModel tarea = SelectedTaskRepository.getSelectedTask();

        if (tarea == null) {
            layoutEmptyState.setVisibility(View.VISIBLE);
            layoutFocusContent.setVisibility(View.GONE);
            return;
        }

        layoutEmptyState.setVisibility(View.GONE);
        layoutFocusContent.setVisibility(View.VISIBLE);

        tvTaskTitle.setText(tarea.getDescripcion());

        // ADIÓS DATOS HARCODEADOS.
        // Ahora simplemente le pedimos al ViewModel que busque las subtareas reales en SQLite
        viewModel.cargarSubtareas(tarea.getId());
    }

    private void setupListeners() {
        layoutBack.setOnClickListener(v -> volverAInicio());

        btnPlusTime.setOnClickListener(v -> {
            if (currentMinutes < 120) {
                currentMinutes += 5;
                actualizarInterfazTiempo();
            }
        });

        btnMinusTime.setOnClickListener(v -> {
            if (currentMinutes > 5) {
                currentMinutes -= 5;
                actualizarInterfazTiempo();
            }
        });

        btnStartPause.setOnClickListener(v -> {
            Boolean running = viewModel.getIsRunning().getValue();
            if (running != null && running) {
                viewModel.pauseTimer();
            } else {
                if (layoutStartMessage != null) {
                    layoutStartMessage.setVisibility(View.VISIBLE);
                } else {
                    viewModel.startTimer();
                }
            }
        });

        if (layoutStartMessage != null) {
            layoutStartMessage.setOnClickListener(v -> {
                layoutStartMessage.setVisibility(View.GONE);
                viewModel.startTimer();
            });
        }

        btnFinish.setOnClickListener(v -> {
            viewModel.stopTimer();
            finalizarSesion();
        });
    }

    private void actualizarInterfazTiempo() {
        if (tvDurationAdjust != null) {
            tvDurationAdjust.setText(currentMinutes + " min");
        }
        viewModel.setDuracion(currentMinutes);
    }

    private void observeViewModel() {
        viewModel.getTiempoRestante().observe(getViewLifecycleOwner(), tvTimer::setText);

        viewModel.getProgreso().observe(getViewLifecycleOwner(), progreso ->
                progressFocus.setProgressCompat(progreso, true));

        viewModel.getIsRunning().observe(getViewLifecycleOwner(), running -> {
            if (running != null && running) {
                btnStartPause.setText("⏸  Pausar");
                if (layoutAdjustTime != null) layoutAdjustTime.setVisibility(View.INVISIBLE);
            } else {
                btnStartPause.setText("▶  Iniciar Enfoque");
                if (layoutAdjustTime != null) layoutAdjustTime.setVisibility(View.VISIBLE);
            }
        });

        viewModel.getEcoPuntos().observe(getViewLifecycleOwner(), puntos ->
                tvEcoPoints.setText("+" + puntos));

        // NUEVO: Observamos las subtareas reales que vienen de la base de datos
        viewModel.getSubtasks().observe(getViewLifecycleOwner(), list -> {
            if (list != null) {
                subtaskAdapter.setSubtasks(list);
                actualizarContador(list);
            }
        });
    }

    // Método auxiliar para calcular cuántas subtareas reales se han completado
    private void actualizarContador(List<Subtarea> list) {
        if (list == null || list.isEmpty()) {
            tvSubtaskCount.setText("0 / 0 completadas");
            return;
        }

        int completadas = 0;
        for (Subtarea s : list) {
            if (s.isCompletada()) completadas++;
        }
        tvSubtaskCount.setText(completadas + " / " + list.size() + " completadas");
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
        BottomNavigationView bottom =
                requireActivity().findViewById(R.id.bottom_nav);

        bottom.setSelectedItemId(R.id.dashboardFragment);
    }
}