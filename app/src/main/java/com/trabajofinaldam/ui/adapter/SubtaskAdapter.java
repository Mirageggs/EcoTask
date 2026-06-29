package com.trabajofinaldam.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.checkbox.MaterialCheckBox;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.model.Subtarea;

import java.util.ArrayList;
import java.util.List;

public class SubtaskAdapter extends RecyclerView.Adapter<SubtaskAdapter.SubtaskViewHolder> {

    public interface OnSubtaskCheckedListener {
        void onSubtaskChecked(Subtarea subtarea, boolean isChecked, boolean allCompleted);
    }

    private List<Subtarea> subtasks;
    private final OnSubtaskCheckedListener listener;
    private int checkedCount = 0;

    public SubtaskAdapter(List<Subtarea> subtasks, OnSubtaskCheckedListener listener) {
        this.subtasks = subtasks != null ? subtasks : new ArrayList<>();
        this.listener = listener;
    }

    public void setSubtasks(List<Subtarea> nuevas) {
        this.subtasks    = nuevas != null ? nuevas : new ArrayList<>();
        this.checkedCount = 0;
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
        Subtarea subtarea = subtasks.get(position);
        holder.tvName.setText(subtarea.getDescripcion());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(subtarea.isCompletada());
        aplicarTachado(holder.tvName, subtarea.isCompletada());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            subtarea.setCompletada(isChecked);
            aplicarTachado(holder.tvName, isChecked);
            if (isChecked) checkedCount++; else if (checkedCount > 0) checkedCount--;
            boolean allDone = checkedCount >= subtasks.size() && !subtasks.isEmpty();
            if (listener != null) listener.onSubtaskChecked(subtarea, isChecked, allDone);
        });
    }

    @Override
    public int getItemCount() { return subtasks.size(); }

    public int getTotalSubtareas() { return subtasks.size(); }

    static class SubtaskViewHolder extends RecyclerView.ViewHolder {
        final MaterialCheckBox checkBox;
        final TextView tvName;

        SubtaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.checkbox_subtask);
            tvName   = itemView.findViewById(R.id.tv_subtask_name);
        }
    }

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
