package com.trabajofinaldam;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * TaskAdapter — Adaptador del RecyclerView que muestra las tareas
 * del "Calendario Inteligente" en el Dashboard.
 *
 * Infla item_task.xml por cada TaskModel de la lista.
 *
 * MVVM: el Adapter solo PRESENTA datos. No accede a SQLite ni
 * contiene lógica de negocio; recibe la lista ya lista desde la VIEW.
 */
 class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    // Lista de datos a mostrar
    private List<TaskModel> tasks;

    // Interfaz opcional para detectar clicks (útil para Modo Enfoque)
    public interface OnTaskClickListener {
        void onTaskClick(TaskModel tarea);
    }

    private final OnTaskClickListener listener;

    // ===================================================================
    // CONSTRUCTORES
    // ===================================================================
    public TaskAdapter(List<TaskModel> tasks, OnTaskClickListener listener) {
        this.tasks = (tasks != null) ? tasks : new ArrayList<>();
        this.listener = listener;
    }

    // Constructor simple si no necesitas clicks
    public TaskAdapter(List<TaskModel> tasks) {
        this(tasks, null);
    }

    // ===================================================================
    // setTasks — Actualiza la lista y refresca la UI
    // Llamado desde el observer del LiveData en DashboardActivity.
    // ===================================================================
    public void setTasks(List<TaskModel> newTasks) {
        this.tasks = (newTasks != null) ? newTasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    // ===================================================================
    // onCreateViewHolder — Infla item_task.xml
    // ===================================================================
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    // ===================================================================
    // onBindViewHolder — Enlaza los datos de cada TaskModel a las vistas
    // ===================================================================
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel tarea = tasks.get(position);

        // --- Título de la tarea ---
        holder.tvTaskName.setText(tarea.getDescripcion());

        // --- Hora (formateada a 12h, oculta si está vacía) ---
        if (tarea.getHora() != null && !tarea.getHora().isEmpty()) {
            holder.tvTaskTime.setText(formatearHora(tarea.getHora()));
            holder.tvTaskTime.setVisibility(View.VISIBLE);
        } else {
            holder.tvTaskTime.setVisibility(View.GONE);
        }

        // --- Color de la barra de prioridad ---
        int colorPrioridad = obtenerColorPrioridad(holder.itemView, tarea.getPrioridad());
        holder.viewPriorityBar.setBackgroundColor(colorPrioridad);

        // --- Resalte especial para prioridad ALTA ---
        // El nombre de la tarea se pinta con color más intenso si es Alta.
        if (TaskModel.PRIORIDAD_ALTA.equals(tarea.getPrioridad())) {
            holder.tvTaskName.setTextColor(
                    holder.itemView.getResources()
                            .getColor(R.color.priority_high, holder.itemView.getContext().getTheme()));
        } else {
            holder.tvTaskName.setTextColor(
                    holder.itemView.getResources()
                            .getColor(R.color.text_primary, holder.itemView.getContext().getTheme()));
        }

        // --- Badge "Auto-programada" (mostrar/ocultar) ---
        if (holder.chipAuto != null) {
            holder.chipAuto.setVisibility(
                    tarea.isAutoProgramada() ? View.VISIBLE : View.GONE);
        }

        // --- Click en el ítem ---
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClick(tarea);
        });
    }

    // ===================================================================
    // getItemCount — Tamaño de la lista
    // ===================================================================
    @Override
    public int getItemCount() {
        return tasks.size();
    }

    // ===================================================================
    // VIEWHOLDER — Cachea las referencias de cada item_task.xml
    // ===================================================================
    static class TaskViewHolder extends RecyclerView.ViewHolder {

        final TextView tvTaskName;
        final TextView tvTaskTime;
        final View     viewPriorityBar;
        final TextView chipAuto; // puede ser null si tu item no lo tiene

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskName      = itemView.findViewById(R.id.tv_task_name);
            tvTaskTime      = itemView.findViewById(R.id.tv_task_time);
            viewPriorityBar = itemView.findViewById(R.id.view_priority_bar);
            chipAuto        = itemView.findViewById(R.id.chip_auto); // opcional
        }
    }

    // ===================================================================
    // HELPER — Color según prioridad (lee de colors.xml)
    // ===================================================================
    private int obtenerColorPrioridad(View view, String prioridad) {
        int colorRes;
        switch (prioridad) {
            case TaskModel.PRIORIDAD_ALTA:
                colorRes = R.color.priority_high;   // naranja/amarillo intenso
                break;
            case TaskModel.PRIORIDAD_BAJA:
                colorRes = R.color.priority_low;    // verde
                break;
            default: // MEDIA
                colorRes = R.color.eco_green_primary;
                break;
        }
        return view.getResources().getColor(colorRes, view.getContext().getTheme());
    }

    // ===================================================================
    // HELPER — Convierte "14:00" → "2:00 PM"
    // ===================================================================
    private String formatearHora(String hora24) {
        try {
            String[] partes = hora24.split(":");
            int h = Integer.parseInt(partes[0]);
            int m = Integer.parseInt(partes[1]);
            String sufijo = h >= 12 ? "PM" : "AM";
            if (h > 12) h -= 12;
            if (h == 0) h = 12;
            return String.format(Locale.getDefault(), "%d:%02d %s", h, m, sufijo);
        } catch (Exception e) {
            return hora24;
        }
    }
}
