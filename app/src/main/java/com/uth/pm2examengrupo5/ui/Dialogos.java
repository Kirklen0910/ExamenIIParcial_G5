package com.uth.pm2examengrupo5.ui;

import android.content.Context;

import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.uth.pm2examengrupo5.R;
import com.uth.pm2examengrupo5.data.ErrorApi;

final class Dialogos {
    private Dialogos() { }

    static void alerta(Context context, String mensaje) {
        new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.alerta)
                .setMessage(mensaje)
                .setPositiveButton(R.string.aceptar, null)
                .show();
    }

    static void error(Context context, ErrorApi error, @Nullable Runnable reintentar) {
        if (error.isSinConexion() && reintentar != null) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle(R.string.alerta)
                    .setMessage(error.getMensaje())
                    .setCancelable(false)
                    .setPositiveButton(R.string.reintentar, (d, w) -> reintentar.run())
                    .setNegativeButton(R.string.cancelar, null)
                    .show();
        } else {
            alerta(context, error.getMensaje());
        }
    }
}
