package com.trabajofinaldam;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList;
import java.util.List;

/**
 * DashboardActivity — VIEW en la arquitectura MVVM.
 *
 * REGLA DE ORO:
 *   Esta clase SOLO observa LiveData y actualiza vistas.
 *   NUNCA contiene lógica de negocio ni acceso a datos.
 *
 * Layout : res/layout/activity_dashboard.xml
 * Includes:
 *   - section_greeting.xml
 *   - card_progress_today.xml
 *   - card_smart_calendar.xml  (RecyclerView con item_task.xml)
 *   - bottom_nav_dashboard.xml
 */
public class DashboardActivity extends AppCompatActivity {

    // ===================================================================
    // VISTAS — section_greeting.xml
    // ===================================================================
    private TextView tvGreeting;
    private TextView tvGreetingSubtitle;
    private TextView tvEcoPointsTotal;

    // ===================================================================
    // VISTAS — card_progress_today.xml
    // ===================================================================
    private CircularProgressIndicator progressCircular;
    private TextView tvProgressPercentInside;
    private TextView tvProgressPercentBig;
    private TextView tvProgressMessage;

    // ===================================================================
    // VISTAS — card_smart_calendar.xml (RecyclerView)
    // ===================================================================
    private RecyclerView recyclerTasks;
    private TaskAdapter taskAdapter;

    // ===================================================================
    // VISTAS — bottom_nav_dashboard.xml
    // ===================================================================
    private LinearLayout navItemHome;
    private LinearLayout navItemNew;
    private LinearLayout navItemFocus;

    // ===================================================================
    // VIEWMODEL — único punto de contacto con los datos
    // ===================================================================
    private DashboardViewModel viewModel;

    // ===================================================================
    // CICLO DE VIDA
    // ===================================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bindViews();
        setupRecyclerView();
        initViewModel();
        observeLiveData();
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarga los datos al volver de NewTask / Focus
        if (viewModel != null) viewModel.refreshData();
    }

    // ===================================================================
    // PASO 1 — BIND VIEWS
    // ===================================================================
    private void bindViews() {
        // --- section_greeting.xml ---
        tvGreeting         = findViewById(R.id.tv_greeting);
        tvGreetingSubtitle = findViewById(R.id.tv_greeting_subtitle);
        tvEcoPointsTotal   = findViewById(R.id.tv_eco_points_total);

        // --- card_progress_today.xml ---
        progressCircular        = findViewById(R.id.progress_circular);
        tvProgressPercentInside = findViewById(R.id.tv_progress_percent_inside);
        tvProgressPercentBig    = findViewById(R.id.tv_progress_percent_big);
        tvProgressMessage       = findViewById(R.id.tv_progress_message);

        // --- card_smart_calendar.xml ---
        recyclerTasks = findViewById(R.id.recycler_tasks);

        // --- bottom_nav_dashboard.xml ---
        navItemHome  = findViewById(R.id.nav_item_home);
        navItemNew   = findViewById(R.id.nav_item_new);
        navItemFocus = findViewById(R.id.nav_item_focus);
    }

    // ===================================================================
    // PASO 2 — SETUP RECYCLERVIEW
    // ===================================================================
    private void setupRecyclerView() {
        recyclerTasks.setLayoutManager(new LinearLayoutManager(this));

        taskAdapter = new TaskAdapter(new ArrayList<>(), tarea -> {
            Intent intent = new Intent(this, Focusactivity.class);
            intent.putExtra("TASK_TITLE", tarea.getDescripcion());
            startActivity(intent);
        });

        recyclerTasks.setAdapter(taskAdapter);
    }

    // ===================================================================
    // PASO 3 — INIT VIEWMODEL
    // ===================================================================
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
    }

    // ===================================================================
    // PASO 4 — OBSERVAR LIVEDATA
    // ===================================================================
    private void observeLiveData() {

        // OBSERVER 1 — Saludo dinámico
        viewModel.getGreetingText().observe(this, saludo -> {
            tvGreeting.setText(saludo);
        });

        // OBSERVER 2 — Progreso de hoy
        viewModel.getTodayProgress().observe(this, progreso -> {
            progressCircular.setProgressCompat(progreso, true);

            String textoProgreso = progreso + "%";
            tvProgressPercentInside.setText(textoProgreso);
            tvProgressPercentBig.setText(textoProgreso);

            tvProgressMessage.setText(mensajeMotivacional(progreso));
        });

        // OBSERVER 3 — Calendario Inteligente (RecyclerView)
        viewModel.getCalendarTasks().observe(this, tareas -> {
            taskAdapter.setTasks(tareas);
        });

        // OBSERVER 4 — Total de Eco-Puntos (ODS 12)
        viewModel.getEcoPuntos().observe(this, puntos -> {
            tvEcoPointsTotal.setText("🌱 " + puntos + " Eco-Puntos");
        });
    }

    // ===================================================================
    // PASO 5 — NAVEGACIÓN INFERIOR
    // ===================================================================
    private void setupNavigation() {
        // Resaltar "Inicio" como tab activo
        setNavItemActive(navItemHome, true);
        setNavItemActive(navItemNew, false);
        setNavItemActive(navItemFocus, false);

        navItemHome.setOnClickListener(v -> {
            // Ya estamos aquí — sin acción necesaria
        });

        navItemNew.setOnClickListener(v -> {
            startActivity(new Intent(this, NewtaskActivity.class));
        });

        navItemFocus.setOnClickListener(v -> {
            // Abre la primera tarea pendiente, o sin título si no hay ninguna
            List<TaskModel> tareas = viewModel.getCalendarTasks().getValue();
            String titulo = (tareas != null && !tareas.isEmpty())
                    ? tareas.get(0).getDescripcion()
                    : "Sesión de Enfoque";
            Intent intent = new Intent(this, Focusactivity.class);
            intent.putExtra("TASK_TITLE", titulo);
            startActivity(intent);
        });
    }

    private void setNavItemActive(LinearLayout navItem, boolean active) {
        int color = active
                ? getResources().getColor(R.color.eco_green_primary, getTheme())
                : getResources().getColor(R.color.text_nav_inactive, getTheme());
        ((TextView) navItem.getChildAt(0)).setTextColor(color);
        ((TextView) navItem.getChildAt(1)).setTextColor(color);
    }

    // ===================================================================
    // HELPER — Mensaje motivacional
    // ===================================================================
    private String mensajeMotivacional(int progreso) {
        if (progreso >= 80) return "Increible! Casi terminas 🌿";
        if (progreso >= 60) return "Gran avance! 🎉";
        if (progreso >= 40) return "Vas muy bien! 💪";
        if (progreso >= 20) return "Sigue adelante! 🌱";
        return "Empieza tu dia! ☀️";
    }
}
