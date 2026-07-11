package com.trabajofinaldam.ui.dashboard;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.model.Subtarea;
import com.trabajofinaldam.data.model.TaskModel;
import com.trabajofinaldam.data.repository.TaskRepository;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditTaskDialogFragment extends DialogFragment {

    private TaskModel task;
    private TaskRepository repository;

    private TextInputEditText etTitulo, etDesc, etFecha, etHora;
    private LinearLayout layoutSubtasks;

    public static EditTaskDialogFragment newInstance(TaskModel task) {
        EditTaskDialogFragment fragment = new EditTaskDialogFragment();
        fragment.task = task;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_DeviceDefault_NoActionBar_Fullscreen);
        repository = TaskRepository.getInstance(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_edit_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etTitulo = view.findViewById(R.id.et_edit_titulo);
        etDesc = view.findViewById(R.id.et_edit_desc);
        etFecha = view.findViewById(R.id.et_edit_fecha);
        etHora = view.findViewById(R.id.et_edit_hora);
        layoutSubtasks = view.findViewById(R.id.layout_subtasks_edit);
        MaterialButton btnAddSubtask = view.findViewById(R.id.btn_add_subtask);
        MaterialButton btnSave = view.findViewById(R.id.btn_save_edit);

        if (task != null) {
            etTitulo.setText(task.getTitulo());
            etDesc.setText(task.getDescripcion());
            etFecha.setText(task.getFechaLimite());
            etHora.setText(task.getHora());
            cargarSubtareas();
        }

        etFecha.setOnClickListener(v -> mostrarDatePicker());
        etHora.setOnClickListener(v -> mostrarTimePicker());
        btnAddSubtask.setOnClickListener(v -> agregarSubtareaVista(null));
        btnSave.setOnClickListener(v -> guardarCambios());
    }

    private void cargarSubtareas() {
        List<Subtarea> currentSubtasks = repository.obtenerSubtareasDeTarea(task.getId());
        layoutSubtasks.removeAllViews();
        for (Subtarea s : currentSubtasks) {
            agregarSubtareaVista(s);
        }
    }

    private void agregarSubtareaVista(Subtarea sub) {
        View v = getLayoutInflater().inflate(R.layout.item_subtask_edit, layoutSubtasks, false);
        EditText etName = v.findViewById(R.id.et_subtask_name);
        View btnRemove = v.findViewById(R.id.btn_remove_subtask);

        if (sub != null) {
            etName.setText(sub.getDescripcion());
        }

        btnRemove.setOnClickListener(v1 -> layoutSubtasks.removeView(v));
        layoutSubtasks.addView(v);
    }

    private void mostrarDatePicker() {
        final Calendar c = Calendar.getInstance();
        new DatePickerDialog(requireContext(), (view, year, month, dayOfMonth) -> {
            String fecha = String.format(Locale.getDefault(), "%02d/%02d/%04d", dayOfMonth, month + 1, year);
            etFecha.setText(fecha);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarTimePicker() {
        final Calendar c = Calendar.getInstance();
        new TimePickerDialog(requireContext(), (view, hourOfDay, minute) -> {
            String hora = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            etHora.setText(hora);
        }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
    }

    private void guardarCambios() {
        if (etTitulo.getText() == null) return;
        
        task.setTitulo(etTitulo.getText().toString());
        task.setDescripcion(etDesc.getText() != null ? etDesc.getText().toString() : "");
        task.setFechaLimite(etFecha.getText() != null ? etFecha.getText().toString() : "");
        task.setHora(etHora.getText() != null ? etHora.getText().toString() : "");

        repository.actualizarTarea(task);

        // Sincronizar subtareas: Borrar todas y re-insertar
        repository.borrarSubtareasDeTarea(task.getId());
        for (int i = 0; i < layoutSubtasks.getChildCount(); i++) {
            View v = layoutSubtasks.getChildAt(i);
            EditText et = v.findViewById(R.id.et_subtask_name);
            String desc = et.getText().toString().trim();
            if (!desc.isEmpty()) {
                repository.insertarSubtarea(new Subtarea(task.getId(), desc, false));
            }
        }
        
        Toast.makeText(requireContext(), "Tarea actualizada", Toast.LENGTH_SHORT).show();
        dismiss();
    }
}
