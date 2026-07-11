package com.trabajofinaldam.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.model.TaskModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<TaskModel> tasks;

    public interface OnTaskClickListener {
        void onTaskClick(TaskModel tarea);
        void onCompletarTarea(TaskModel tarea);
        void onEliminarTarea(TaskModel tarea);
        void onPostergarTarea(TaskModel tarea);
        void onEditarTarea(TaskModel tarea);
    }

    private final OnTaskClickListener listener;

    public TaskAdapter(List<TaskModel> tasks, OnTaskClickListener listener) {
        this.tasks = (tasks != null) ? tasks : new ArrayList<>();
        this.listener = listener;
    }

    public TaskAdapter(List<TaskModel> tasks) {
        this(tasks, null);
    }

    public void setTasks(List<TaskModel> newTasks) {
        this.tasks = (newTasks != null) ? newTasks : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        TaskModel tarea = tasks.get(position);

        holder.tvTaskName.setText(tarea.getTitulo());

        // Manejo de opacidad para tareas finalizadas e inconclusas
        if (tarea.isCompletada() || tarea.isVencida()) {
            holder.itemView.setAlpha(0.4f);
        } else {
            holder.itemView.setAlpha(1.0f);
        }

        // Mostrar fecha de fin si está completada, sino la hora programada
        if (tarea.isCompletada() && tarea.getFechaCompletada() != null) {
            holder.tvTaskTime.setText(tarea.getFechaCompletada());
            holder.tvTaskTime.setVisibility(View.VISIBLE);
            holder.tvDueDate.setVisibility(View.GONE);
        } else {
            if (tarea.getHora() != null && !tarea.getHora().isEmpty()) {
                holder.tvTaskTime.setText(formatearHora(tarea.getHora()));
                holder.tvTaskTime.setVisibility(View.VISIBLE);
            } else {
                holder.tvTaskTime.setVisibility(View.GONE);
            }
            
            // Fecha límite para tareas no completadas
            if (tarea.getFechaLimite() != null && !tarea.getFechaLimite().isEmpty()) {
                holder.tvDueDate.setText(holder.itemView.getContext().getString(R.string.due_date_format, tarea.getFechaLimite()));
                holder.tvDueDate.setVisibility(View.VISIBLE);
            } else {
                holder.tvDueDate.setVisibility(View.GONE);
            }
        }

        int colorPrioridad = obtenerColorPrioridad(holder.itemView, tarea.getPrioridad());
        holder.viewPriorityBar.setBackgroundColor(
                holder.itemView.getResources().getColor(R.color.eco_green_primary, holder.itemView.getContext().getTheme()));
        
        // Si es inconclusa, forzamos color rojo en borde e icono
        if (tarea.isVencida()) {
            int red = 0xFFFF5252;
            holder.cardTask.setStrokeColor(red);
            holder.tvTaskName.setTextColor(red);
            if (holder.ivAlertIcon != null) holder.ivAlertIcon.setVisibility(View.VISIBLE);
        } else {
            holder.cardTask.setStrokeColor(colorPrioridad);
            holder.tvTaskName.setTextColor(
                holder.itemView.getResources()
                        .getColor(R.color.text_primary, holder.itemView.getContext().getTheme()));
            if (holder.ivAlertIcon != null) holder.ivAlertIcon.setVisibility(View.GONE);
        }

        if (tarea.getTotalSubtareas() > 0) {
            holder.pbSubtareas.setVisibility(View.VISIBLE);
            holder.pbSubtareas.setMax(tarea.getTotalSubtareas());
            holder.pbSubtareas.setProgress(tarea.getSubtareasCompletadas());
        } else {
            holder.pbSubtareas.setVisibility(View.GONE);
        }

        if (holder.chipAuto != null) {
            holder.chipAuto.setVisibility(View.VISIBLE);
            if (tarea.isCompletada()) {
                holder.chipAuto.setText("Finalizada");
                holder.chipAuto.setBackgroundResource(R.drawable.bg_badge_green);
                holder.chipAuto.setTextColor(holder.itemView.getResources().getColor(R.color.eco_green_badge_text, holder.itemView.getContext().getTheme()));
            } else if (tarea.isVencida()) {
                holder.chipAuto.setText("Incompleta");
                holder.chipAuto.setBackgroundResource(R.drawable.bg_badge_orange); // Usando naranja por ahora
                holder.chipAuto.setTextColor(0xFFFF5252); // Rojo
            } else if (tarea.isIniciada()) {
                holder.chipAuto.setText("Continuar");
                holder.chipAuto.setBackgroundResource(R.drawable.bg_badge_green);
                holder.chipAuto.setTextColor(holder.itemView.getResources().getColor(R.color.eco_green_badge_text, holder.itemView.getContext().getTheme()));
            } else {
                holder.chipAuto.setText("Comenzar");
                holder.chipAuto.setBackgroundResource(R.drawable.bg_badge_orange);
                holder.chipAuto.setTextColor(holder.itemView.getResources().getColor(R.color.background_primary, holder.itemView.getContext().getTheme()));
            }
        }

        // Limpiar el listener antes de setChecked evita disparos al reciclar la vista
        holder.cbCompletada.setOnCheckedChangeListener(null);
        holder.cbCompletada.setChecked(tarea.isCompletada());

        // BLOQUEO: Si tiene subtareas pendientes, deshabilitar check principal
        boolean tieneSubPendientes = tarea.getSubtareasCompletadas() < tarea.getTotalSubtareas();
        if (tieneSubPendientes && !tarea.isCompletada()) {
            holder.cbCompletada.setEnabled(false);
            holder.cbCompletada.setAlpha(0.3f);
        } else {
            holder.cbCompletada.setEnabled(true);
            holder.cbCompletada.setAlpha(1.0f);
        }

        holder.cbCompletada.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) listener.onCompletarTarea(tarea);
        });

        holder.btnMenu.setOnClickListener(v -> {
            androidx.appcompat.widget.PopupMenu popup = new androidx.appcompat.widget.PopupMenu(v.getContext(), v);
            popup.getMenuInflater().inflate(R.menu.task_menu, popup.getMenu());
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_edit) {
                    if (listener != null) listener.onEditarTarea(tarea);
                    return true;
                } else if (item.getItemId() == R.id.action_delete) {
                    if (listener != null) listener.onEliminarTarea(tarea);
                    return true;
                }
                return false;
            });
            popup.show();
        });

        // POSTERGAR: Click en la fecha/hora para cambiarla
        View.OnClickListener postergarListener = v -> {
            if (listener != null) listener.onPostergarTarea(tarea);
        };
        holder.tvTaskTime.setOnClickListener(postergarListener);
        holder.tvDueDate.setOnClickListener(postergarListener);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTaskClick(tarea);
        });
    }

    @Override
    public int getItemCount() { return tasks.size(); }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCardView cardTask;
        final TextView    tvTaskName;
        final ImageView   ivAlertIcon;
        final ProgressBar pbSubtareas;
        final TextView    tvDueDate;
        final TextView    tvTaskTime;
        final View        viewPriorityBar;
        final TextView    chipAuto;
        final CheckBox    cbCompletada;
        final ImageButton btnMenu;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cardTask        = itemView.findViewById(R.id.card_task);
            tvTaskName      = itemView.findViewById(R.id.tv_task_name);
            ivAlertIcon     = itemView.findViewById(R.id.iv_alert_icon);
            pbSubtareas     = itemView.findViewById(R.id.pb_subtareas);
            tvDueDate       = itemView.findViewById(R.id.tv_due_date);
            tvTaskTime      = itemView.findViewById(R.id.tv_task_time);
            viewPriorityBar = itemView.findViewById(R.id.view_priority_bar);
            chipAuto        = itemView.findViewById(R.id.chip_auto);
            cbCompletada    = itemView.findViewById(R.id.cb_completada);
            btnMenu         = itemView.findViewById(R.id.btn_menu);
        }
    }

    private int obtenerColorPrioridad(View view, String prioridad) {
        int colorRes;
        if (TaskModel.PRIORIDAD_ALTA.equals(prioridad)) {
            colorRes = R.color.priority_high;
        } else if (TaskModel.PRIORIDAD_MEDIA.equals(prioridad)) {
            colorRes = R.color.priority_medium;
        } else if (TaskModel.PRIORIDAD_BAJA.equals(prioridad)) {
            colorRes = R.color.priority_low;
        } else {
            colorRes = R.color.eco_green_primary;
        }
        return view.getResources().getColor(colorRes, view.getContext().getTheme());
    }

    private String formatearHora(String hora24) {
        if (hora24 == null || !hora24.contains(":")) return hora24;
        try {
            String[] partes = hora24.split(":");
            int h = Integer.parseInt(partes[0]);
            int m = Integer.parseInt(partes[1]);
            String sufijo = h >= 12 ? "PM" : "AM";
            int h12 = h > 12 ? h - 12 : (h == 0 ? 12 : h);
            return String.format(Locale.getDefault(), "%d:%02d %s", h12, m, sufijo);
        } catch (Exception e) {
            return hora24;
        }
    }
}
