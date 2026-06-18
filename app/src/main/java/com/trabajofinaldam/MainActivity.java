package com.trabajofinaldam;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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

        // 2) Cambio de pestaña con animación y opciones consistentes
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        // 3) Mantener resaltada la pestaña correcta aunque la navegación
        //    sea programática (p. ej. tocar una tarea abre Enfoque)
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            Menu menu = bottomNav.getMenu();
            for (int i = 0; i < menu.size(); i++) {
                MenuItem item = menu.getItem(i);
                if (item.getItemId() == destination.getId()) {
                    item.setChecked(true);
                    break;
                }
            }
        });

        // Evita que volver a tocar la pestaña activa recargue el fragment
        bottomNav.setOnItemReselectedListener(item -> { /* no-op */ });
    }

    private boolean onNavItemSelected(@NonNull MenuItem item) {
        NavOptions options = new NavOptions.Builder()
                .setLaunchSingleTop(true)              // no duplica el destino
                .setRestoreState(true)                 // restaura el estado de la pestaña
                .setEnterAnim(R.anim.nav_fade_in)
                .setExitAnim(R.anim.nav_fade_out)
                .setPopEnterAnim(R.anim.nav_fade_in)
                .setPopExitAnim(R.anim.nav_fade_out)
                // Vuelve siempre a la raíz (Inicio) guardando el estado de cada pestaña
                .setPopUpTo(navController.getGraph().getStartDestinationId(), false, true)
                .build();
        try {
            navController.navigate(item.getItemId(), null, options);
            return true;
        } catch (IllegalArgumentException e) {
            return false; // el id del menú no es un destino del grafo
        }
    }
}
