package com.trabajofinaldam;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;

/**
 * DashboardFragment — VIEW de "Inicio" (antes DashboardActivity).
 *
 * Solo observa LiveData y actualiza vistas. La navegación entre pestañas
 * la maneja la BottomNavigationView en MainActivity; aquí solo navegamos
 * a Enfoque cuando el usuario toca una tarea concreta.
 */
public class DashboardFragment extends Fragment {

    private TextView tvGreeting;
    private TextView tvEcoPointsTotal;
    private CircularProgressIndicator progressCircular;
    private TextView tvProgressPercentInside;
    private TextView tvProgressPercentBig;
    private TextView tvProgressMessage;
    private RecyclerView recyclerTasks;
    private TaskAdapter taskAdapter;

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
        setupRecyclerView();
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        observeLiveData();
    }

    @Override
    public void onResume() {
        super.onResume();
        // Recarga al volver de Nueva / Enfoque
        if (viewModel != null) viewModel.refreshData();
    }

    private void bindViews(View v) {
        tvGreeting              = v.findViewById(R.id.tv_greeting);
        tvEcoPointsTotal        = v.findViewById(R.id.tv_eco_points_total);
        progressCircular        = v.findViewById(R.id.progress_circular);
        tvProgressPercentInside = v.findViewById(R.id.tv_progress_percent_inside);
        tvProgressPercentBig    = v.findViewById(R.id.tv_progress_percent_big);
        tvProgressMessage       = v.findViewById(R.id.tv_progress_message);
        recyclerTasks           = v.findViewById(R.id.recycler_tasks);
    }

    private void setupRecyclerView() {
        recyclerTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        taskAdapter = new TaskAdapter(new ArrayList<>(), tarea -> abrirEnfoque(tarea.getDescripcion()));
        recyclerTasks.setAdapter(taskAdapter);
    }

    private void abrirEnfoque(String titulo) {
        Bundle args = new Bundle();
        args.putString("taskTitle", titulo);
        NavController nav = NavHostFragment.findNavController(this);
        // Navegar al destino Enfoque; MainActivity sincroniza la pestaña sola
        nav.navigate(R.id.focusFragment, args);
    }

    private void observeLiveData() {
        viewModel.getGreetingText().observe(getViewLifecycleOwner(), saludo ->
                tvGreeting.setText(saludo));

        viewModel.getTodayProgress().observe(getViewLifecycleOwner(), progreso -> {
            progressCircular.setProgressCompat(progreso, true);
            String txt = progreso + "%";
            tvProgressPercentInside.setText(txt);
            tvProgressPercentBig.setText(txt);
            tvProgressMessage.setText(mensajeMotivacional(progreso));
        });

        viewModel.getCalendarTasks().observe(getViewLifecycleOwner(), tareas ->
                taskAdapter.setTasks(tareas));

        viewModel.getEcoPuntos().observe(getViewLifecycleOwner(), puntos ->
                tvEcoPointsTotal.setText("🌱 " + puntos + " Eco-Puntos"));
    }

    private String mensajeMotivacional(int progreso) {
        if (progreso >= 80) return "Increible! Casi terminas 🌿";
        if (progreso >= 60) return "Gran avance! 🎉";
        if (progreso >= 40) return "Vas muy bien! 💪";
        if (progreso >= 20) return "Sigue adelante! 🌱";
        return "Empieza tu dia! ☀️";
    }
}
