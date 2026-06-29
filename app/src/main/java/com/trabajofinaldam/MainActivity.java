package com.trabajofinaldam;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * MainActivity —  Activity de EcoTask
 *
 * Hospeda un NavHostFragment que intercambia los 3 destinos
 * (Inicio / Nueva / Enfoque) y conecta la BottomNavigationView con el
 * NavController.
 *
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

        // 2) Cambio de pestaña
        bottomNav.setOnItemSelectedListener(this::onNavItemSelected);

        // 3) Ocultar nav en auth; resaltar pestaña activa en el resto
        navController.addOnDestinationChangedListener((controller, destination, args) -> {
            boolean esAuth = destination.getId() == R.id.loginFragment
                          || destination.getId() == R.id.registerFragment;
            bottomNav.setVisibility(esAuth ? View.GONE : View.VISIBLE);

            if (!esAuth) {
                Menu menu = bottomNav.getMenu();
                for (int i = 0; i < menu.size(); i++) {
                    MenuItem item = menu.getItem(i);
                    if (item.getItemId() == destination.getId()) {
                        item.setChecked(true);
                        break;
                    }
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
