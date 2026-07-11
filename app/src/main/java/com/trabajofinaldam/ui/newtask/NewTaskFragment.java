package com.trabajofinaldam.ui.newtask;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.model.TaskModel;

import java.util.Calendar;
import java.util.Locale;

import android.widget.RadioGroup;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NewTaskFragment extends Fragment {

    private TextInputEditText etNombre;
    private TextInputEditText etDescripcion;
    private TextInputEditText etFecha;
    private TextInputEditText etHora;
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
        setupTimePicker();
        setupPrioridadDefault();
        setupListeners();
        observeViewModel();

        RadioGroup rgCantidadObjetivos = view.findViewById(R.id.rgCantidadObjetivos);
        TextInputLayout tilCantidadPersonalizada =
                view.findViewById(R.id.tilCantidadPersonalizada);

        tilCantidadPersonalizada.setVisibility(View.GONE);

        rgCantidadObjetivos.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbPersonalizada) {
                tilCantidadPersonalizada.setVisibility(View.VISIBLE);
            } else {
                tilCantidadPersonalizada.setVisibility(View.GONE);
            }
        });
    }

    private void bindViews(View v) {
        etNombre        = v.findViewById(R.id.et_nombre);
        etDescripcion   = v.findViewById(R.id.et_descripcion);
        etFecha         = v.findViewById(R.id.et_fecha);
        etHora          = v.findViewById(R.id.et_hora);
        togglePrioridad = v.findViewById(R.id.toggle_prioridad);
        btnDividirIa    = v.findViewById(R.id.btn_dividir_ia);
        layoutBack      = v.findViewById(R.id.layout_back);
    }

    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> mostrarDatePicker());
    }

    private void setupTimePicker() {
        etHora.setOnClickListener(v -> mostrarTimePicker());
    }

    private void mostrarTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(requireContext(),
                (view, hourOfDay, minuteOfHour) -> {
                    String hora = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
                    etHora.setText(hora);
                }, hour, minute, true);
        timePickerDialog.show();
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
        if (getView() != null) {
            RadioGroup rg = getView().findViewById(R.id.rgCantidadObjetivos);
            rg.check(R.id.rbAutomatica);
        }
    }

    private void setupListeners() {
        layoutBack.setOnClickListener(v -> volverAInicio());
        btnDividirIa.setOnClickListener(v -> guardarTarea());
    }

    private void guardarTarea() {
        String nombre = etNombre.getText() != null
                ? etNombre.getText().toString().trim() : "";
        String descripcion = etDescripcion.getText() != null
                ? etDescripcion.getText().toString().trim() : "";
        String fecha = etFecha.getText() != null
                ? etFecha.getText().toString().trim() : "";
        String hora = etHora.getText() != null
                ? etHora.getText().toString().trim() : "";

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingresa un nombre para la tarea");
            etNombre.requestFocus();
            return;
        }
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
        if (TextUtils.isEmpty(hora)) {
            Toast.makeText(requireContext(), "Selecciona una hora",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        String prioridad = obtenerPrioridadSeleccionada();
        int cantidadObjetivos = obtenerCantidadObjetivosSeleccionada();

        viewModel.guardarNuevaTarea(nombre, descripcion, fecha, hora, prioridad, cantidadObjetivos);
    }

    private int obtenerCantidadObjetivosSeleccionada() {
        if (getView() == null) return 0;
        RadioGroup rg = getView().findViewById(R.id.rgCantidadObjetivos);
        int checkedId = rg.getCheckedRadioButtonId();
        if (checkedId == R.id.rbPersonalizada) {
            TextInputEditText et = getView().findViewById(R.id.etCantidadPersonalizada);
            if (et.getText() != null && !et.getText().toString().isEmpty()) {
                try { return Integer.parseInt(et.getText().toString()); }
                catch (NumberFormatException e) { return 5; }
            }
        }
        return 0; // Automática
    }

    private String obtenerPrioridadSeleccionada() {
        int checkedId = togglePrioridad.getCheckedButtonId();
        if (checkedId == R.id.btn_prioridad_baja) return TaskModel.PRIORIDAD_BAJA;
        if (checkedId == R.id.btn_prioridad_alta) return TaskModel.PRIORIDAD_ALTA;
        return TaskModel.PRIORIDAD_MEDIA;
    }

    private void observeViewModel() {
        viewModel.getGuardadoExitoso().observe(getViewLifecycleOwner(), exitoso -> {
            if (exitoso == null) return;
            if (exitoso) {
                Toast.makeText(requireContext(), "Tarea y subtareas generadas correctamente",
                        Toast.LENGTH_SHORT).show();
                volverAInicio();
            } else {
                Toast.makeText(requireContext(), "Error al guardar la tarea",
                        Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getCargando().observe(getViewLifecycleOwner(), estaCargando -> {
            if (estaCargando != null) {
                btnDividirIa.setEnabled(!estaCargando);
                btnDividirIa.setText(estaCargando ? "Generando subtareas con IA..." : "✦ Crear tarea");
            }
        });
    }

    private void volverAInicio() {
        BottomNavigationView bottom =
                requireActivity().findViewById(R.id.bottom_nav);
        bottom.setSelectedItemId(R.id.dashboardFragment);
    }
}
