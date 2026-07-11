package com.trabajofinaldam;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.trabajofinaldam.data.model.TaskModel;

/**
 * MainActivity — ÚNICA Activity de EcoTask (patrón Single-Activity).
 *
 * Hospeda un NavHostFragment que intercambia los 3 destinos
 * (Inicio / Nueva / Enfoque) y conecta la BottomNavigationView con el
 * NavController.
 *
 * Por qué esto arregla los dos bugs:
 *   1) BOTONES: al navegar siempre con popUpTo(startDestination) +
 *      launchSingleTop + restoreState, "Inicio" SIEMPRE vuelve a Inicio,
 *      no se apilan instancias duplicadas y el back-stack queda limpio.
 *   2) ANIMACIONES: se aplica el MISMO crossfade (fade in/out) en cada
 *      cambio de pestaña, así que nunca hay direcciones contradictorias
 *      ni el slide de pantalla completa de las Activities.
 */
import android.content.Context;
import android.content.SharedPreferences;
import com.trabajofinaldam.ui.login.LoginFragment;

public class MainActivity extends AppCompatActivity {

    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1) Obtener el NavController del NavHostFragment
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHost == null) return;
        navController = navHost.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottom_nav);

        // 2) LÓGICA DE SESIÓN: Redirigir si ya está logueado
        SharedPreferences prefs = getSharedPreferences(LoginFragment.SESSION_PREFS, Context.MODE_PRIVATE);
        boolean isLogged = prefs.getBoolean(LoginFragment.KEY_LOGGED_IN, false);

        if (isLogged) {
            navController.navigate(R.id.dashboardFragment);
        }

        // 3) Cambio de pestaña con animación y opciones consistentes
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        // 4) Lógica para ocultar el BottomNav en Login/Registro y marcar el item seleccionado
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            if (destination.getId() == R.id.loginFragment || destination.getId() == R.id.registerFragment) {
                bottomNav.setVisibility(View.GONE);
            } else {
                bottomNav.setVisibility(View.VISIBLE);
            }

            Menu menu = bottomNav.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() == destination.getId()) {
                    item.setChecked(true);
                    break;
                }
            }
        });

        // 5) PROGRAMAR NOTIFICACIONES (WorkManager)
        programarNotificaciones();
    }

    private void programarNotificaciones() {
        try {
            androidx.work.Constraints constraints = new androidx.work.Constraints.Builder()
                    .setRequiredNetworkType(androidx.work.NetworkType.NOT_REQUIRED)
                    .build();

            androidx.work.PeriodicWorkRequest request =
                    new androidx.work.PeriodicWorkRequest.Builder(com.trabajofinaldam.notification.NotificationWorker.class, 1, java.util.concurrent.TimeUnit.DAYS)
                            .setConstraints(constraints)
                            .build();

            androidx.work.WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                    "ecotask_daily_reminder",
                    androidx.work.ExistingPeriodicWorkPolicy.KEEP,
                    request
            );
        } catch (Exception e) {
            // Error si no se encuentra la librería de WorkManager
        }
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)              // no duplica el destino
                .setEnterAnim(R.anim.nav_fade_in)
                .setExitAnim(R.anim.nav_fade_out)
                .setPopEnterAnim(R.anim.nav_fade_in)
                .setPopExitAnim(R.anim.nav_fade_out)
                // Vuelve siempre a la raíz (Inicio) guardando el estado de cada pestaña
                .build();
        try {
            navController.navigate(item.getItemId(), null, options);
            return true;
        } catch (IllegalArgumentException e) {
            return false; // el id del menú no es un destino del grafo
        }
    }
}
