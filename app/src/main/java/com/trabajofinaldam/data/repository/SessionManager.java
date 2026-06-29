package com.trabajofinaldam.data.repository;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * SessionManager — Persiste la sesión activa del usuario en SharedPreferences.
 * Única fuente de verdad sobre si hay un usuario logueado.
 */
public class SessionManager {

    private static final String PREFS_SESSION   = "ecotask_session";
    private static final String KEY_LOGGED_IN   = "logged_in";
    private static final String KEY_USER_ID     = "user_id";
    private static final String KEY_USER_NAME   = "user_name";
    private static final String KEY_USER_EMAIL  = "user_email";
    private static final String KEY_USER_ROL    = "user_rol";

    private final SharedPreferences prefs;

    private static SessionManager instance;

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context.getApplicationContext());
        }
        return instance;
    }

    private SessionManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_SESSION, Context.MODE_PRIVATE);
    }

    public void guardarSesion(int userId, String nombre, String email, String rol) {
        prefs.edit()
                .putBoolean(KEY_LOGGED_IN,  true)
                .putInt    (KEY_USER_ID,    userId)
                .putString (KEY_USER_NAME,  nombre)
                .putString (KEY_USER_EMAIL, email)
                .putString (KEY_USER_ROL,   rol)
                .apply();
    }

    public void cerrarSesion() {
        prefs.edit().clear().apply();
    }

    public boolean estaLogueado()  { return prefs.getBoolean(KEY_LOGGED_IN, false); }
    public int     getUserId()     { return prefs.getInt    (KEY_USER_ID,   -1);    }
    public String  getUserName()   { return prefs.getString (KEY_USER_NAME,  "");   }
    public String  getUserEmail()  { return prefs.getString (KEY_USER_EMAIL, "");   }
    public String  getUserRol()    { return prefs.getString (KEY_USER_ROL,   "");   }
}
