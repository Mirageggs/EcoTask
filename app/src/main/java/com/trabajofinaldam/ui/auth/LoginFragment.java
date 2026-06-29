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
import com.google.android.material.textfield.TextInputEditText;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.repository.SessionManager;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private MaterialButton    btnLogin;
    private TextView          tvIrRegistro;

    private AuthViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Si ya hay sesión activa, saltar directo al Dashboard
        if (SessionManager.getInstance(requireContext()).estaLogueado()) {
            irAlDashboard();
            return;
        }

        bindViews(view);
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupListeners();
        observeViewModel();
    }

    private void bindViews(View v) {
        etEmail      = v.findViewById(R.id.et_email);
        etPassword   = v.findViewById(R.id.et_password);
        btnLogin     = v.findViewById(R.id.btn_login);
        tvIrRegistro = v.findViewById(R.id.tv_ir_registro);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            String email    = etEmail.getText()    != null ? etEmail.getText().toString()    : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";
            viewModel.login(email, password);
        });

        tvIrRegistro.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_loginFragment_to_registerFragment));
    }

    private void observeViewModel() {
        viewModel.getLoginExitoso().observe(getViewLifecycleOwner(), ok -> {
            if (ok != null && ok) irAlDashboard();
        });

        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    private void irAlDashboard() {
        NavHostFragment.findNavController(this)
                .navigate(R.id.action_loginFragment_to_dashboardFragment);
    }
}
