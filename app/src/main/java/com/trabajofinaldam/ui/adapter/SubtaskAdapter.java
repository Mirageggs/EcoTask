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

    private List<Subtarea> subtasks;
    private final OnSubtaskStatusChangeListener listener;

    public interface OnSubtaskStatusChangeListener {
        void onStatusChanged(Subtarea subtarea, boolean isChecked);
    }

    public SubtaskAdapter(List<Subtarea> subtasks, OnSubtaskStatusChangeListener listener) {
        this.subtasks = (subtasks != null) ? subtasks : new ArrayList<>();
        this.listener = listener;
    }

    public SubtaskAdapter(List<Subtarea> subtasks) {
        this(subtasks, null);
    }

    public void setSubtasks(List<Subtarea> nuevas) {
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
        Subtarea sub = subtasks.get(position);
        holder.tvName.setText(sub.getDescripcion());

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(sub.isCompletada());
        aplicarTachado(holder.tvName, sub.isCompletada());

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sub.setCompletada(isChecked);
            aplicarTachado(holder.tvName, isChecked);
            if (listener != null) {
                listener.onStatusChanged(sub, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() { return subtasks.size(); }

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
