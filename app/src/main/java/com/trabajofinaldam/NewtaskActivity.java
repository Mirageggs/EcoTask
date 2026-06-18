package com.trabajofinaldam;


import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Calendar;
import java.util.Locale;

/**
 * NewTaskActivity — VIEW de la pantalla "Nueva Tarea".
 *
 * MVVM:
 *   - Lee los datos del formulario (descripción, fecha, prioridad, horas).
 *   - Se los pasa CRUDOS al NewTaskViewModel.
 *   - El ViewModel construye el TaskModel y lo guarda vía DatabaseHelper.
 *   - La Activity NUNCA toca la base de datos directamente.
 *
 * Layout: res/layout/activity_new_task.xml
 */
public class NewtaskActivity extends AppCompatActivity {

    // ===================================================================
    // VISTAS DEL FORMULARIO
    // ===================================================================
    private TextInputEditText etDescripcion;
    private TextInputEditText etFecha;
    private TextInputEditText etHoras;
    private MaterialButtonToggleGroup togglePrioridad;
    private MaterialButton btnDividirIa;

    // Botón volver (LinearLayout clickable del XML)
    private android.widget.LinearLayout layoutBack;

    // ===================================================================
    // VIEWMODEL
    // ===================================================================
    private Newtaskviewmodel viewModel;

    // ===================================================================
    // CICLO DE VIDA
    // ===================================================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task); // ✅ enlaza el XML

        bindViews();
        initViewModel();
        setupNavigation();
        setupDatePicker();
        setupPrioridadDefault();
        setupListeners();
        observeViewModel();
    }

    // ===================================================================
    // PASO 1 — BIND VIEWS (los findViewById que pediste)
    // ===================================================================
    private void bindViews() {
        etDescripcion   = findViewById(R.id.et_descripcion);
        etFecha         = findViewById(R.id.et_fecha);
        etHoras         = findViewById(R.id.et_horas);
        togglePrioridad = findViewById(R.id.toggle_prioridad);
        btnDividirIa    = findViewById(R.id.btn_dividir_ia);
        layoutBack      = findViewById(R.id.layout_back);
    }

    // ===================================================================
    // PASO 2 — INIT VIEWMODEL
    // ===================================================================
    private void initViewModel() {
        viewModel = new ViewModelProvider(this).get(Newtaskviewmodel.class);
    }

    // ===================================================================
    // PASO 3 — DATEPICKER
    // Al tocar el EditText de fecha, abre el calendario.
    // Al elegir, escribe la fecha en formato dd/MM/yyyy.
    // ===================================================================
    private void setupDatePicker() {
        etFecha.setOnClickListener(v -> mostrarDatePicker());
    }

    private void mostrarDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int anio = calendar.get(Calendar.YEAR);
        int mes  = calendar.get(Calendar.MONTH);
        int dia  = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                NewtaskActivity.this,
                (view, year, month, dayOfMonth) -> {
                    // month es 0-based → sumamos 1 para mostrarlo correctamente
                    String fechaFormateada = String.format(
                            Locale.getDefault(),
                            "%02d/%02d/%04d",
                            dayOfMonth, month + 1, year
                    );
                    etFecha.setText(fechaFormateada);
                },
                anio, mes, dia
        );

        // No permite seleccionar fechas pasadas
        dialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        dialog.show();
    }

    // ===================================================================
    // Selecciona "Media" como prioridad por defecto al abrir
    // ===================================================================
    private void setupPrioridadDefault() {
        togglePrioridad.check(R.id.btn_prioridad_media);
    }

    // ===================================================================
    // PASO 4 — LISTENERS (volver + guardar)
    // ===================================================================
    private void setupListeners() {

        // Botón volver → cierra la Activity y regresa al Dashboard
        layoutBack.setOnClickListener(v -> finish());

        // Botón "Dividir y Programar con IA" → guarda la tarea
        btnDividirIa.setOnClickListener(v -> guardarTarea());
    }

    // ===================================================================
    // RECOGER DATOS DEL FORMULARIO Y ENVIARLOS AL VIEWMODEL
    // Aquí ocurre el flujo: Activity → ViewModel → DatabaseHelper
    // ===================================================================
    private void guardarTarea() {

        // 1. Leer datos crudos del formulario
        String descripcion = etDescripcion.getText() != null
                ? etDescripcion.getText().toString().trim() : "";
        String fecha = etFecha.getText() != null
                ? etFecha.getText().toString().trim() : "";
        String horas = etHoras.getText() != null
                ? etHoras.getText().toString().trim() : "";

        // 2. Validación básica en la VIEW (UX inmediata)
        if (TextUtils.isEmpty(descripcion)) {
            etDescripcion.setError("Ingresa una descripción");
            etDescripcion.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(fecha)) {
            Toast.makeText(this, "Selecciona una fecha límite",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Obtener la prioridad seleccionada del ToggleGroup
        String prioridad = obtenerPrioridadSeleccionada();

        // 4. Convertir horas (opcional) a entero
        int horasEstimadas = 0;
        if (!TextUtils.isEmpty(horas)) {
            try {
                horasEstimadas = Integer.parseInt(horas);
            } catch (NumberFormatException e) {
                horasEstimadas = 0;
            }
        }

        // 5. ✅ Enviar datos CRUDOS al ViewModel (no construye TaskModel aquí)
        viewModel.guardarNuevaTarea(descripcion, fecha, prioridad, horasEstimadas);
    }

    // ===================================================================
    // Traduce el botón seleccionado del ToggleGroup a la constante del modelo
    // ===================================================================
    private String obtenerPrioridadSeleccionada() {
        int checkedId = togglePrioridad.getCheckedButtonId();

        if (checkedId == R.id.btn_prioridad_baja) {
            return TaskModel.PRIORIDAD_BAJA;
        } else if (checkedId == R.id.btn_prioridad_alta) {
            return TaskModel.PRIORIDAD_ALTA;
        } else {
            return TaskModel.PRIORIDAD_MEDIA; // por defecto
        }
    }

    // ===================================================================
    // OBSERVAR RESULTADO DEL GUARDADO
    // El ViewModel expone un LiveData<Boolean> con el éxito de la operación.
    // ===================================================================
    private void observeViewModel() {
        viewModel.getGuardadoExitoso().observe(this, exitoso -> {
            if (exitoso == null) return;

            if (exitoso) {
                Toast.makeText(this, "Tarea guardada correctamente",
                        Toast.LENGTH_SHORT).show();
                // Regresa al Dashboard; su onResume() recargará la lista
                finish();
            } else {
                Toast.makeText(this, "Error al guardar la tarea",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ===================================================================
    // NAVEGACIÓN INFERIOR — listeners + resalte del tab activo
    // ===================================================================
    private void setupNavigation() {
        LinearLayout navItemHome  = findViewById(R.id.nav_item_home);
        LinearLayout navItemNew   = findViewById(R.id.nav_item_new);
        LinearLayout navItemFocus = findViewById(R.id.nav_item_focus);

        // Inicio → volver al Dashboard
        navItemHome.setOnClickListener(v -> finish());

        // Nueva → ya estamos aquí, no hace nada

        // Enfoque → abrir sin tarea específica
        navItemFocus.setOnClickListener(v -> {
            startActivity(new Intent(this, Focusactivity.class));
        });

        // Resaltar "Nueva" como tab activo
        setNavItemActive(navItemNew, true);
        setNavItemActive(navItemHome, false);
        setNavItemActive(navItemFocus, false);
    }

    private void setNavItemActive(LinearLayout navItem, boolean active) {
        int color = active
                ? getResources().getColor(R.color.eco_green_primary, getTheme())
                : getResources().getColor(R.color.text_nav_inactive, getTheme());
        // getChildAt(0) = icono, getChildAt(1) = etiqueta
        ((TextView) navItem.getChildAt(0)).setTextColor(color);
        ((TextView) navItem.getChildAt(1)).setTextColor(color);
    }
}