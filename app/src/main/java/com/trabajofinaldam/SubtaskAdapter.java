package com.trabajofinaldam;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;

import java.util.ArrayList;
import java.util.List;

/**
 * SubtaskAdapter — Adaptador para la lista de subtareas del Modo Enfoque.
 *
 * Recibe una List<String> con los nombres de las subtareas y las pinta
 * con un CheckBox. Al marcar el checkbox, tacha el texto (efecto visual).
 *
 * Java estricto. MVVM: solo presentación, sin lógica de negocio.
 */
public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {

    private List<String> subtasks;

    public SubtaskAdapter(List<String> subtasks) {
        this.subtasks = (subtasks != null) ? subtasks : new ArrayList<>();
    }

    // Actualiza la lista y refresca
    public void setSubtasks(List<String> nuevas) {
        this.subtasks = (nuevas != null) ? nuevas : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SubtaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_subtask, parent, false);
        return new SubtaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubtaskViewHolder holder, int position) {
        String nombre = subtasks.get(position);
        holder.tvName.setText(nombre);

        // Reiniciar estado (importante por el reciclaje de vistas)
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(false);
        aplicarTachado(holder.tvName, false);

        // Al marcar: tacha el texto
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                aplicarTachado(holder.tvName, isChecked));
    }

    @Override
    public int getItemCount() {
        return subtasks.size();
    }

    // ViewHolder
    static class SubtaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCheckBox checkBox;
        final TextView tvName;

        SubtaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_subtask);
            tvName   = itemView.findViewById(R.id.tv_subtask_name);
        }
    }

    // HELPER — Aplica o quita el efecto de texto tachado
    private void aplicarTachado(TextView tv, boolean tachado) {
        if (tachado) {
            tv.setPaintFlags(tv.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            tv.setAlpha(0.5f);
        } else {
            tv.setPaintFlags(tv.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            tv.setAlpha(1.0f);
        }
    }
}
