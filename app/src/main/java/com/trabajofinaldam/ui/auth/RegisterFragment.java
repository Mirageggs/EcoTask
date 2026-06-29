package com.trabajofinaldam.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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
import com.trabajofinaldam.data.model.User;

public class RegisterFragment extends Fragment {

    private TextInputEditText        etNombre;
    private TextInputEditText        etEmail;
    private TextInputEditText        etPassword;
    private TextInputEditText        etConfirmPassword;
    private MaterialButtonToggleGroup toggleRol;
    private MaterialButton           btnRegistrar;
    private TextView                 tvIrLogin;

    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        toggleRol.check(R.id.btn_rol_estudiante); // rol por defecto
        setupListeners();
        observeViewModel();
    }

    private void bindViews(View v) {
        etNombre          = v.findViewById(R.id.et_nombre);
        etEmail           = v.findViewById(R.id.et_email);
        etPassword        = v.findViewById(R.id.et_password);
        etConfirmPassword = v.findViewById(R.id.et_confirm_password);
        toggleRol         = v.findViewById(R.id.toggle_rol);
        btnRegistrar      = v.findViewById(R.id.btn_registrar);
        tvIrLogin         = v.findViewById(R.id.tv_ir_login);
    }

    private void setupListeners() {
        btnRegistrar.setOnClickListener(v -> {
            String nombre   = getText(etNombre);
            String email    = getText(etEmail);
            String pass     = getText(etPassword);
            String confirm  = getText(etConfirmPassword);
            String rol      = obtenerRol();
            viewModel.registrar(nombre, email, pass, confirm, rol);
        });

        tvIrLogin.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());
    }

    private void observeViewModel() {
        viewModel.getRegistroExitoso().observe(getViewLifecycleOwner(), ok -> {
            if (ok != null && ok) {
                Toast.makeText(requireContext(), "¡Cuenta creada! Bienvenido/a",
                        Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_registerFragment_to_dashboardFragment);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private String obtenerRol() {
        int id = toggleRol.getCheckedButtonId();
        if (id == R.id.btn_rol_profesional) return User.ROL_PROFESIONAL;
        if (id == R.id.btn_rol_general)     return User.ROL_GENERAL;
        return User.ROL_ESTUDIANTE;
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
