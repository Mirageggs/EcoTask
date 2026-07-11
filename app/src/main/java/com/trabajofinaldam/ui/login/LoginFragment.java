package com.trabajofinaldam.ui.login;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.trabajofinaldam.R;
import com.trabajofinaldam.data.local.DatabaseHelper;

public class LoginFragment extends Fragment {

    private TextInputEditText etEmail, etPassword;
    private DatabaseHelper dbHelper;
    
    public static final String SESSION_PREFS = "session_prefs";
    public static final String KEY_LOGGED_IN = "is_logged_in";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        dbHelper = DatabaseHelper.getInstance(getContext());

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        MaterialButton btnLogin = view.findViewById(R.id.btn_login);
        TextView tvGoRegister = view.findViewById(R.id.tv_go_register);

        btnLogin.setOnClickListener(v -> handleLogin());
        tvGoRegister.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.registerFragment));
    }

    private void handleLogin() {
        if (etEmail.getText() == null || etPassword.getText() == null) return;
        
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(getContext(), "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.validarLogin(email, pass)) {
            guardarSesion();
            Navigation.findNavController(requireView()).navigate(R.id.dashboardFragment);
        } else {
            Toast.makeText(getContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarSesion() {
        SharedPreferences prefs = requireActivity().getSharedPreferences(SESSION_PREFS, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_LOGGED_IN, true).apply();
    }
}
