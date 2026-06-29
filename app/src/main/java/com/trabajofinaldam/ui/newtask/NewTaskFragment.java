package com.trabajofinaldam.ui.newtask;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.model.TaskModel;

import java.util.Calendar;
import java.util.Locale;

/**
 * NewTaskFragment — VIEW de "Nueva Tarea"
 *
 * Tras guardar (o pulsar "Volver") regresa al destino Inicio con
 * popBackStack, en lugar de finish(). El Dashboard recarga en su onResume().
 */
public class NewTaskFragment extends Fragment {

    private TextInputEditText etDescripcion;
    private TextInputEditText etFecha;
    private TextInputEditText etHoras;
    private MaterialButtonToggleGroup togglePrioridad;
    private MaterialButton btnDividirIa;
    private LinearLayout layoutBack;

    private NewTaskViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_new_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        viewModel = new ViewModelProvider(this).get(NewTaskViewModel.class);
        setupDatePicker();
        setupPrioridadDefault();
        setupListeners();
        observeViewModel();
    }

    private void bindViews(View v) {
        etDescripcion   = v.findViewById(R.id.et_descripcion);
        etFecha         = v.findViewById(R.id.et_fecha);
        etHoras         = v.findViewById(R.id.et_horas);
        togglePrioridad = v.findViewById(R.id.toggle_prioridad);
        btnDividirIa    = v.findViewById(R.id.btn_dividir_ia);
        layoutBack      = v.findViewById(R.id.layout_back);
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> mostrarDatePicker());
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                requireContext(),
                (view, year, month, dayOfMonth) -> {
                    String fecha = String.format(Locale.getDefault(),
                            "%02d/%02d/%04d", dayOfMonth, month + 1, year);
                    etFecha.setText(fecha);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    private void setupPrioridadDefault() {
        togglePrioridad.check(R.id.btn_prioridad_media);
    }

    private void setupListeners() {
        layoutBack.setOnClickListener(v -> volverAInicio());
        btnDividirIa.setOnClickListener(v -> guardarTarea());
    }

    private void guardarTarea() {
        String descripcion = etDescripcion.getText() != null
                ? etDescripcion.getText().toString().trim() : "";
        String fecha = etFecha.getText() != null
                ? etFecha.getText().toString().trim() : "";
        String horas = etHoras.getText() != null
                ? etHoras.getText().toString().trim() : "";

        if (TextUtils.isEmpty(descripcion)) {
            etDescripcion.setError("Ingresa una descripción");
            etDescripcion.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(fecha)) {
            Toast.makeText(requireContext(), "Selecciona una fecha límite",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String prioridad = obtenerPrioridadSeleccionada();

        int horasEstimadas = 0;
        if (!TextUtils.isEmpty(horas)) {
            try {
                horasEstimadas = Integer.parseInt(horas);
            } catch (NumberFormatException e) {
                horasEstimadas = 0;
            }
        }

        viewModel.guardarNuevaTarea(descripcion, fecha, prioridad, horasEstimadas);
    }

    private String obtenerPrioridadSeleccionada() {
        int checkedId = togglePrioridad.getCheckedButtonId();
        if (checkedId == R.id.btn_prioridad_baja)  return TaskModel.PRIORIDAD_BAJA;
        if (checkedId == R.id.btn_prioridad_alta)  return TaskModel.PRIORIDAD_ALTA;
        return TaskModel.PRIORIDAD_MEDIA;
    }

    private void observeViewModel() {
        viewModel.getGuardadoExitoso().observe(getViewLifecycleOwner(), exitoso -> {
            if (exitoso == null) return;
            if (exitoso) {
                Toast.makeText(requireContext(), "Tarea guardada y dividida con IA ✨",
                        Toast.LENGTH_SHORT).show();
                volverAInicio();
            } else {
                Toast.makeText(requireContext(), "Error al guardar la tarea",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getDividiendoIa().observe(getViewLifecycleOwner(), dividiendo -> {
            if (dividiendo != null) {
                btnDividirIa.setEnabled(!dividiendo);
                btnDividirIa.setText(dividiendo ? "Generando subtareas..." : "Dividir con IA ✨");
            }
        });
    }

    private void volverAInicio() {
        // Vuelve al destino raíz (Inicio); su onResume recargará la lista
        NavHostFragment.findNavController(this)
                .popBackStack(R.id.dashboardFragment, false);
    }
}
