package com.trabajofinaldam.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.trabajofinaldam.data.model.User;
import com.trabajofinaldam.data.repository.SessionManager;
import com.trabajofinaldam.data.repository.UserRepository;

public class AuthViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> loginExitoso    = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registroExitoso = new MutableLiveData<>();
    private final MutableLiveData<String>  error           = new MutableLiveData<>();

    private final UserRepository  userRepository;
    private final SessionManager  sessionManager;

    public AuthViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
        sessionManager = SessionManager.getInstance(application);
    }

    public LiveData<Boolean> getLoginExitoso()    { return loginExitoso; }
    public LiveData<Boolean> getRegistroExitoso() { return registroExitoso; }
    public LiveData<String>  getError()           { return error; }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            error.setValue("Completa todos los campos");
            return;
        }
        User usuario = userRepository.login(email.trim().toLowerCase(), password);
        if (usuario != null) {
            sessionManager.guardarSesion(
                    usuario.getId(), usuario.getNombre(),
                    usuario.getEmail(), usuario.getRol());
            loginExitoso.setValue(true);
        } else {
            error.setValue("Email o contraseña incorrectos");
        }
    }

    public void registrar(String nombre, String email, String password,
                          String confirmPassword, String rol) {
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            error.setValue("Completa todos los campos");
            return;
        }
        if (!password.equals(confirmPassword)) {
            error.setValue("Las contraseñas no coinciden");
            return;
        }
        if (password.length() < 6) {
            error.setValue("La contraseña debe tener al menos 6 caracteres");
            return;
        }
        long id = userRepository.registrar(nombre, email.trim().toLowerCase(), password, rol);
        if (id == -1) {
            error.setValue("Ese email ya está registrado");
        } else {
            sessionManager.guardarSesion((int) id, nombre, email.trim().toLowerCase(), rol);
            registroExitoso.setValue(true);
        }
    }
}
