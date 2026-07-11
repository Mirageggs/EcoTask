package com.trabajofinaldam.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.model.TaskModel;
import com.trabajofinaldam.ui.adapter.TaskAdapter;

import java.util.ArrayList;


public class DashboardFragment extends Fragment {

    private TextView tvGreeting;
    private TextView tvGreetingSubtitle;
    private TextView tvEcoPointsTotal;
    private CircularProgressIndicator progressCircular;
    private TextView tvProgressPercentInside;
    private TextView tvProgressPercentBig;
    private TextView tvProgressMessage;
    private TextView tvEmptyTasks;
    private TextView tvEmptyFinishedTasks;
    private TextView tvEmptyInconclusasTasks;
    
    private RecyclerView recyclerTasks;
    private RecyclerView recyclerFinishedTasks;
    private RecyclerView recyclerInconclusasTasks;
    
    private TaskAdapter taskAdapter;
    private TaskAdapter finishedTaskAdapter;
    private TaskAdapter inconclusasTaskAdapter;
    private com.google.android.material.button.MaterialButton btnLogout;

    private DashboardViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupRecyclerViews();
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        observeLiveData();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) viewModel.refreshData();
    }

    private void bindViews(View v) {
        tvGreeting              = v.findViewById(R.id.tv_greeting);
        tvGreetingSubtitle      = v.findViewById(R.id.tv_greeting_subtitle);
        tvEcoPointsTotal        = v.findViewById(R.id.tv_eco_points_total);
        progressCircular        = v.findViewById(R.id.progress_circular);
        tvProgressPercentInside = v.findViewById(R.id.tv_progress_percent_inside);
        tvProgressPercentBig    = v.findViewById(R.id.tv_progress_percent_big);
        tvProgressMessage       = v.findViewById(R.id.tv_progress_message);
        tvEmptyTasks            = v.findViewById(R.id.tv_empty_tasks);
        tvEmptyFinishedTasks    = v.findViewById(R.id.tv_empty_finished_tasks);
        tvEmptyInconclusasTasks  = v.findViewById(R.id.tv_empty_inconclusas_tasks);
        btnLogout               = v.findViewById(R.id.btn_logout);
        
        recyclerTasks           = v.findViewById(R.id.recycler_tasks);
        recyclerFinishedTasks   = v.findViewById(R.id.recycler_finished_tasks);
        recyclerInconclusasTasks = v.findViewById(R.id.recycler_inconclusas_tasks);

        androidx.core.widget.NestedScrollView scrollView = v.findViewById(R.id.scrollView);
        if (scrollView != null && btnLogout != null) {
            scrollView.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener) (v12, scrollX, scrollY, oldScrollX, scrollY1) -> {
                View child = v12.getChildAt(0);
                if (child != null) {
                    int diff = (child.getBottom() - (v12.getHeight() + v12.getScrollY()));
                    if (diff <= 0) {
                        if (btnLogout.getVisibility() == View.GONE) {
                            btnLogout.setVisibility(View.VISIBLE);
                            btnLogout.setAlpha(0f);
                            btnLogout.animate().alpha(1f).setDuration(500).start();
                        }
                    }
                }
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v1 -> cerrarSesion());
        }
    }

    private void cerrarSesion() {
        android.content.SharedPreferences prefs = requireActivity().getSharedPreferences(com.trabajofinaldam.ui.login.LoginFragment.SESSION_PREFS, android.content.Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
        NavHostFragment.findNavController(this).navigate(R.id.loginFragment);
    }

    private void setupRecyclerViews() {
        // 1. Tareas en curso
        recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(TaskModel tarea) {
                viewModel.marcarIniciada(tarea.getId());
                com.trabajofinaldam.data.repository.SelectedTaskRepository.setSelectedTask(tarea);
                abrirEnfoque(tarea.getTitulo());
            }

            @Override
            public void onCompletarTarea(TaskModel tarea) {
                if (tarea.isCompletada()) {
                    viewModel.desmarcarTarea(tarea.getId());
                } else if (tarea.getSubtareasCompletadas() < tarea.getTotalSubtareas()) {
                    Toast.makeText(requireContext(), "Completa primero los objetivos", Toast.LENGTH_SHORT).show();
                    viewModel.refreshData();
                } else {
                    viewModel.completarTarea(tarea.getId());
                }
            }

            @Override
            public void onEliminarTarea(TaskModel tarea) {
                viewModel.eliminarTarea(tarea.getId());
            }

            @Override
            public void onPostergarTarea(TaskModel tarea) {
                mostrarDatePickerPostergar(tarea);
            }

            @Override
            public void onEditarTarea(TaskModel tarea) {
                EditTaskDialogFragment.newInstance(tarea).show(getChildFragmentManager(), "edit_task");
            }
        });
        recyclerTasks.setAdapter(taskAdapter);

        // 2. Tareas finalizadas
        recyclerFinishedTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        finishedTaskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(TaskModel tarea) {}
            @Override
            public void onCompletarTarea(TaskModel tarea) {
                viewModel.desmarcarTarea(tarea.getId());
            }
            @Override
            public void onEliminarTarea(TaskModel tarea) {
                viewModel.eliminarTarea(tarea.getId());
            }
            @Override
            public void onPostergarTarea(TaskModel tarea) {}
            @Override
            public void onEditarTarea(TaskModel tarea) {}
        });
        recyclerFinishedTasks.setAdapter(finishedTaskAdapter);

        // 3. Tareas inconclusas
        recyclerInconclusasTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        inconclusasTaskAdapter = new TaskAdapter(new ArrayList<>(), new TaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(TaskModel tarea) {}
            @Override
            public void onCompletarTarea(TaskModel tarea) {
                viewModel.completarTarea(tarea.getId());
            }
            @Override
            public void onEliminarTarea(TaskModel tarea) {
                viewModel.eliminarTarea(tarea.getId());
            }
            @Override
            public void onPostergarTarea(TaskModel tarea) {
                mostrarDatePickerPostergar(tarea);
            }
            @Override
            public void onEditarTarea(TaskModel tarea) {}
        });
        recyclerInconclusasTasks.setAdapter(inconclusasTaskAdapter);
    }

    private void mostrarDatePickerPostergar(TaskModel tarea) {
        final java.util.Calendar calendar = java.util.Calendar.getInstance();
        new android.app.DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String nuevaFecha = String.format(java.util.Locale.getDefault(),
                            "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    viewModel.postergarTarea(tarea.getId(), nuevaFecha);
                },
                calendar.get(java.util.Calendar.YEAR),
                calendar.get(java.util.Calendar.MONTH),
                calendar.get(java.util.Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void abrirEnfoque(String titulo) {
        Bundle args = new Bundle();
        args.putString("taskTitle", titulo);
        NavController nav = NavHostFragment.findNavController(this);
        nav.navigate(R.id.focusFragment, args);
    }

    private void observeLiveData() {
        viewModel.getGreetingText().observe(getViewLifecycleOwner(), tvGreeting::setText);

        viewModel.getEcoConsejo().observe(getViewLifecycleOwner(), consejo ->
                tvGreetingSubtitle.setText(consejo));

        viewModel.getTodayProgress().observe(getViewLifecycleOwner(), progreso -> {
            progressCircular.setProgressCompat(progreso, true);
            String txt = progreso + "%";
            tvProgressPercentInside.setText(txt);
            tvProgressPercentBig.setText(txt);
            tvProgressMessage.setText(mensajeMotivacional(progreso));
        });

        viewModel.getCalendarTasks().observe(getViewLifecycleOwner(), tareas -> {
            taskAdapter.setTasks(tareas);
            tvEmptyTasks.setVisibility((tareas == null || tareas.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        viewModel.getFinishedTasks().observe(getViewLifecycleOwner(), tareas -> {
            finishedTaskAdapter.setTasks(tareas);
            tvEmptyFinishedTasks.setVisibility((tareas == null || tareas.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        viewModel.getInconclusasTasks().observe(getViewLifecycleOwner(), tareas -> {
            inconclusasTaskAdapter.setTasks(tareas);
            tvEmptyInconclusasTasks.setVisibility((tareas == null || tareas.isEmpty()) ? View.VISIBLE : View.GONE);
        });

        viewModel.getEcoPuntos().observe(getViewLifecycleOwner(), puntos ->
                tvEcoPointsTotal.setText(getString(R.string.eco_points_format, puntos)));
    }

    private String mensajeMotivacional(int progreso) {
        if (progreso >= 100) return "¡Felicidades! Completaste todo 🌿";
        if (progreso >= 80) return "Increíble! Casi terminas 🌿";
        if (progreso >= 60) return "Gran avance! 🎉";
        if (progreso >= 40) return "Vas muy bien! 💪";
        if (progreso >= 20) return "Sigue adelante! 🌱";
        return "Empieza tu día! ☀️";
    }
}
