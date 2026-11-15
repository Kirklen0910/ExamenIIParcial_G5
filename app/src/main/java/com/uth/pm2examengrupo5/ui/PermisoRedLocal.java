package com.uth.pm2examengrupo5.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

final class PermisoRedLocal {
    static final String PERMISO = "android.permission.ACCESS_LOCAL_NETWORK";
    private static final int API_RED_LOCAL = 37;

    private PermisoRedLocal() { }

    static boolean requerido() {
        return Build.VERSION.SDK_INT >= API_RED_LOCAL;
    }

    static boolean concedido(Context context) {
        return !requerido()
                || ContextCompat.checkSelfPermission(context, PERMISO) == PackageManager.PERMISSION_GRANTED;
    }
}
