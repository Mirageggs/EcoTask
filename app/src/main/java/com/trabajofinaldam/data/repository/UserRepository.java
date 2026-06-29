package com.trabajofinaldam.data.repository;

import android.content.Context;

import com.trabajofinaldam.data.local.DatabaseHelper;
import com.trabajofinaldam.data.model.User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class UserRepository {

    private final DatabaseHelper dbHelper;
    private static UserRepository instance;

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context.getApplicationContext());
        }
        return instance;
    }

    private UserRepository(Context context) {
        dbHelper = DatabaseHelper.getInstance(context);
    }

    /** Registra un nuevo usuario. Devuelve el id generado o -1 si el email ya existe. */
    public long registrar(String nombre, String email, String password, String rol) {
        if (dbHelper.emailExiste(email)) return -1;
        String hash = hashPassword(password);
        return dbHelper.insertarUsuario(new User(nombre, email, hash, rol));
    }

    /**
     * Verifica credenciales. Devuelve el User si son correctas, null si no.
     */
    public User login(String email, String password) {
        User usuario = dbHelper.obtenerUsuarioPorEmail(email);
        if (usuario == null) return null;
        String hash = hashPassword(password);
        return hash.equals(usuario.getPasswordHash()) ? usuario : null;
    }

    public boolean emailExiste(String email) {
        return dbHelper.emailExiste(email);
    }

    // SHA-256 — suficiente para un proyecto académico
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return password; // fallback (nunca debería pasar)
        }
    }
}
