package com.trabajofinaldam.ui.register;

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

public class RegisterFragment extends Fragment {

    private TextInputEditText etName, etEmail, etPass;
    private MaterialButton btnRegister;
    private TextView tvGoLogin;
    private DatabaseHelper dbHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        dbHelper = DatabaseHelper.getInstance(getContext());

        etName = view.findViewById(R.id.et_reg_name);
        etEmail = view.findViewById(R.id.et_reg_email);
        etPass = view.findViewById(R.id.et_reg_pass);
        btnRegister = view.findViewById(R.id.btn_register);
        tvGoLogin = view.findViewById(R.id.tv_go_login);

        btnRegister.setOnClickListener(v -> handleRegister());
        tvGoLogin.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }

    private void handleRegister() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPass.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)) {
            Toast.makeText(getContext(), "Por favor rellena todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        long id = dbHelper.registrarUsuario(name, email, pass);
        if (id != -1) {
            Toast.makeText(getContext(), "Usuario registrado con éxito", Toast.LENGTH_SHORT).show();
            Navigation.findNavController(getView()).popBackStack();
        } else {
            Toast.makeText(getContext(), "Error: El correo ya existe", Toast.LENGTH_SHORT).show();
        }
    }
}
