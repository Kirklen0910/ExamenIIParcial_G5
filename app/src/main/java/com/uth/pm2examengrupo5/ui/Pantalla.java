package com.uth.pm2examengrupo5.ui;

import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.uth.pm2examengrupo5.R;

final class Pantalla {
    private Pantalla() { }

    static void configurar(AppCompatActivity actividad, CharSequence titulo, boolean conAtras) {
        MaterialToolbar toolbar = actividad.findViewById(R.id.toolbar);
        actividad.setSupportActionBar(toolbar);
        if (actividad.getSupportActionBar() != null) {
            actividad.getSupportActionBar().setTitle(titulo);
            actividad.getSupportActionBar().setDisplayHomeAsUpEnabled(conAtras);
        }
        if (conAtras) {
            toolbar.setNavigationOnClickListener(v -> actividad.getOnBackPressedDispatcher().onBackPressed());
        }

        View raiz = actividad.findViewById(R.id.raiz);
        View barra = actividad.findViewById(R.id.appBar);
        ViewCompat.setOnApplyWindowInsetsListener(raiz, (v, insets) -> {
            Insets sistema = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets teclado = insets.getInsets(WindowInsetsCompat.Type.ime());
            barra.setPadding(0, sistema.top, 0, 0);
            v.setPadding(sistema.left, 0, sistema.right, Math.max(sistema.bottom, teclado.bottom));
            return WindowInsetsCompat.CONSUMED;
        });
    }

    static void mostrarCarga(View capa, boolean visible) {
        capa.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}
