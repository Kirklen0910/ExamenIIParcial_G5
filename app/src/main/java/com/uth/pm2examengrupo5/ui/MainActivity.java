package com.uth.pm2examengrupo5.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.uth.pm2examengrupo5.R;
import com.uth.pm2examengrupo5.data.ContactoRepository;
import com.uth.pm2examengrupo5.data.ErrorApi;
import com.uth.pm2examengrupo5.data.ResultadoCallback;
import com.uth.pm2examengrupo5.data.remote.dto.ContactoDto;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "contacto_id";
    public static final String EXTRA_NOMBRE = "contacto_nombre";
    public static final String EXTRA_TELEFONO = "contacto_telefono";
    public static final String EXTRA_LATITUD = "contacto_latitud";
    public static final String EXTRA_LONGITUD = "contacto_longitud";

    private static final Pattern TELEFONO_VALIDO = Pattern.compile("^[0-9]{8,15}$");
    private static final long TAMANO_MAXIMO = 1024 * 1024;

    private final ContactoRepository repositorio = new ContactoRepository();

    private FirmaView firma;
    private TextInputLayout layoutNombre;
    private TextInputLayout layoutTelefono;
    private TextInputEditText editNombre;
    private TextInputEditText editTelefono;
    private TextInputEditText editLatitud;
    private TextInputEditText editLongitud;
    private Button botonSalvar;
    private View capaCarga;

    private FusedLocationProviderClient ubicacion;
    private LocationCallback receptorUbicacion;
    private boolean siguiendoUbicacion;
    private boolean permisoSolicitado;
    private AlertDialog avisoUbicacion;

    private int contactoId = -1;
    private Call<?> llamada;

    private final ActivityResultLauncher<String[]> solicitarPermisos =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), resultado -> {
                if (tienePermisoUbicacion()) iniciarUbicacion();
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        contactoId = getIntent().getIntExtra(EXTRA_ID, -1);
        Pantalla.configurar(this, getString(esEdicion() ? R.string.titulo_actualizar : R.string.titulo_registro), esEdicion());

        firma = findViewById(R.id.firma);
        layoutNombre = findViewById(R.id.layoutNombre);
        layoutTelefono = findViewById(R.id.layoutTelefono);
        editNombre = findViewById(R.id.editNombre);
        editTelefono = findViewById(R.id.editTelefono);
        editLatitud = findViewById(R.id.editLatitud);
        editLongitud = findViewById(R.id.editLongitud);
        botonSalvar = findViewById(R.id.botonSalvar);
        capaCarga = findViewById(R.id.capaCarga);
        Button botonLista = findViewById(R.id.botonLista);
        Button botonUbicacion = findViewById(R.id.botonUbicacion);

        ubicacion = LocationServices.getFusedLocationProviderClient(this);
        receptorUbicacion = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult resultado) {
                Location ultima = resultado.getLastLocation();
                if (ultima != null) mostrarUbicacion(ultima.getLatitude(), ultima.getLongitude());
            }
        };

        findViewById(R.id.botonLimpiarFirma).setOnClickListener(v -> firma.limpiar());
        botonSalvar.setOnClickListener(v -> guardar());
        botonLista.setOnClickListener(v -> startActivity(new Intent(this, ListaContactosActivity.class)));
        botonUbicacion.setOnClickListener(v -> {
            siguiendoUbicacion = true;
            iniciarUbicacion();
        });

        if (esEdicion()) {
            botonSalvar.setText(R.string.actualizar_contacto);
            botonLista.setVisibility(View.GONE);
            botonUbicacion.setVisibility(View.VISIBLE);
            editNombre.setText(getIntent().getStringExtra(EXTRA_NOMBRE));
            editTelefono.setText(getIntent().getStringExtra(EXTRA_TELEFONO));
            mostrarUbicacion(getIntent().getDoubleExtra(EXTRA_LATITUD, 0), getIntent().getDoubleExtra(EXTRA_LONGITUD, 0));
            cargarFirmaExistente();
        } else {
            siguiendoUbicacion = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (siguiendoUbicacion) iniciarUbicacion();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ubicacion.removeLocationUpdates(receptorUbicacion);
    }

    @Override
    protected void onDestroy() {
        if (llamada != null) llamada.cancel();
        super.onDestroy();
    }

    private boolean esEdicion() {
        return contactoId > 0;
    }

    private boolean tienePermisoUbicacion() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean gpsActivo() {
        LocationManager gestor = (LocationManager) getSystemService(LOCATION_SERVICE);
        return gestor != null && (gestor.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || gestor.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    @SuppressLint("MissingPermission")
    private void iniciarUbicacion() {
        if (!permisoSolicitado && (!tienePermisoUbicacion() || !PermisoRedLocal.concedido(this))) {
            permisoSolicitado = true;
            solicitarPermisos.launch(permisosNecesarios());
            return;
        }
        if (!tienePermisoUbicacion()) {
            avisarPermisoDenegado();
            return;
        }
        if (!gpsActivo()) {
            mostrarAvisoUbicacion(new MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.alerta)
                    .setMessage(R.string.gps_inactivo)
                    .setPositiveButton(R.string.activar, (d, w) ->
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)))
                    .setNegativeButton(R.string.cancelar, null));
            return;
        }
        LocationRequest solicitud = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();
        ubicacion.requestLocationUpdates(solicitud, receptorUbicacion, Looper.getMainLooper());
        ubicacion.getLastLocation().addOnSuccessListener(this, ultima -> {
            if (ultima != null) mostrarUbicacion(ultima.getLatitude(), ultima.getLongitude());
        });
    }

    private String[] permisosNecesarios() {
        List<String> permisos = new ArrayList<>();
        permisos.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permisos.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (PermisoRedLocal.requerido()) permisos.add(PermisoRedLocal.PERMISO);
        return permisos.toArray(new String[0]);
    }

    private void avisarPermisoDenegado() {
        mostrarAvisoUbicacion(new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.alerta)
                .setMessage(R.string.permiso_ubicacion_denegado)
                .setPositiveButton(R.string.configuracion, (d, w) -> {
                    Intent ajustes = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", getPackageName(), null));
                    startActivity(ajustes);
                })
                .setNegativeButton(R.string.cancelar, null));
    }

    private void mostrarAvisoUbicacion(MaterialAlertDialogBuilder constructor) {
        if (avisoUbicacion != null && avisoUbicacion.isShowing()) return;
        avisoUbicacion = constructor.show();
    }

    private void mostrarUbicacion(double latitud, double longitud) {
        editLatitud.setText(String.format(Locale.US, "%.6f", latitud));
        editLongitud.setText(String.format(Locale.US, "%.6f", longitud));
    }

    private void cargarFirmaExistente() {
        Pantalla.mostrarCarga(capaCarga, true);
        llamada = repositorio.descargarFirma(contactoId, new ResultadoCallback<Bitmap>() {
            @Override
            public void onExito(Bitmap imagen) {
                Pantalla.mostrarCarga(capaCarga, false);
                firma.mostrarFirma(imagen);
            }

            @Override
            public void onError(ErrorApi error) {
                Pantalla.mostrarCarga(capaCarga, false);
                Dialogos.error(MainActivity.this, error, () -> cargarFirmaExistente());
            }
        });
    }

    private void guardar() {
        layoutNombre.setError(null);
        layoutTelefono.setError(null);

        String nombre = texto(editNombre);
        String telefono = texto(editTelefono);
        boolean valido = true;

        if (nombre.isEmpty()) {
            layoutNombre.setError(getString(R.string.error_nombre));
            valido = false;
        } else if (nombre.length() > 100) {
            layoutNombre.setError(getString(R.string.error_nombre_largo));
            valido = false;
        }
        if (!TELEFONO_VALIDO.matcher(telefono).matches()) {
            layoutTelefono.setError(getString(R.string.error_telefono));
            valido = false;
        }
        if (!valido) return;

        if (firma.estaVacia()) {
            Dialogos.alerta(this, getString(R.string.firma_vacia));
            return;
        }

        Double latitud = numero(editLatitud);
        Double longitud = numero(editLongitud);
        if (latitud == null || longitud == null) {
            Dialogos.alerta(this, getString(R.string.sin_ubicacion));
            return;
        }

        File archivo = null;
        if (!esEdicion() || firma.fueModificada()) {
            try {
                archivo = firma.exportarPng(new File(getCacheDir(), "firma.png"));
            } catch (IOException e) {
                Dialogos.alerta(this, getString(R.string.error_png));
                return;
            }
            if (archivo.length() > TAMANO_MAXIMO) {
                Dialogos.alerta(this, getString(R.string.error_tamano));
                return;
            }
        }

        enviar(nombre, telefono, latitud, longitud, archivo);
    }

    private void enviar(String nombre, String telefono, double latitud, double longitud, File archivo) {
        Pantalla.mostrarCarga(capaCarga, true);
        botonSalvar.setEnabled(false);

        ResultadoCallback<ContactoDto> alTerminar = new ResultadoCallback<ContactoDto>() {
            @Override
            public void onExito(ContactoDto contacto) {
                Pantalla.mostrarCarga(capaCarga, false);
                botonSalvar.setEnabled(true);
                if (esEdicion()) {
                    Toast.makeText(MainActivity.this, R.string.contacto_actualizado, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(MainActivity.this, R.string.contacto_guardado, Toast.LENGTH_SHORT).show();
                    limpiarFormulario();
                }
            }

            @Override
            public void onError(ErrorApi error) {
                Pantalla.mostrarCarga(capaCarga, false);
                botonSalvar.setEnabled(true);
                Dialogos.error(MainActivity.this, error,
                        () -> enviar(nombre, telefono, latitud, longitud, archivo));
            }
        };

        llamada = esEdicion()
                ? repositorio.actualizar(contactoId, nombre, telefono, latitud, longitud, archivo, alTerminar)
                : repositorio.crear(nombre, telefono, latitud, longitud, archivo, alTerminar);
    }

    private void limpiarFormulario() {
        firma.limpiar();
        editNombre.setText("");
        editTelefono.setText("");
        editNombre.requestFocus();
    }

    private static String texto(TextInputEditText campo) {
        return campo.getText() == null ? "" : campo.getText().toString().trim();
    }

    private static Double numero(TextInputEditText campo) {
        String valor = texto(campo);
        if (valor.isEmpty()) return null;
        try {
            return Double.parseDouble(valor);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
