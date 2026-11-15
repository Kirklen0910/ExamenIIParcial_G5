package com.uth.pm2examengrupo5.ui;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.uth.pm2examengrupo5.R;
import com.uth.pm2examengrupo5.data.ContactoRepository;
import com.uth.pm2examengrupo5.data.ErrorApi;
import com.uth.pm2examengrupo5.data.ResultadoCallback;
import com.uth.pm2examengrupo5.data.remote.dto.ContactoDto;

import java.util.List;

import retrofit2.Call;

public class ListaContactosActivity extends AppCompatActivity {
    private static final long ESPERA_BUSQUEDA_MS = 400;

    private final ContactoRepository repositorio = new ContactoRepository();
    private final Handler manejador = new Handler(Looper.getMainLooper());
    private final Runnable buscarPendiente = this::cargar;

    private ContactoAdapter adaptador;
    private TextInputEditText editBuscar;
    private TextView textVacio;
    private View capaCarga;
    private Call<List<ContactoDto>> llamadaLista;
    private Call<?> llamadaAccion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_lista_contactos);
        Pantalla.configurar(this, getString(R.string.titulo_lista), true);

        editBuscar = findViewById(R.id.editBuscar);
        textVacio = findViewById(R.id.textVacio);
        capaCarga = findViewById(R.id.capaCarga);

        adaptador = new ContactoAdapter(this::preguntarIrAlMapa);
        RecyclerView lista = findViewById(R.id.listaContactos);
        lista.setLayoutManager(new LinearLayoutManager(this));
        lista.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        lista.setAdapter(adaptador);

        editBuscar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                manejador.removeCallbacks(buscarPendiente);
                manejador.postDelayed(buscarPendiente, ESPERA_BUSQUEDA_MS);
            }
        });

        findViewById(R.id.botonVerFirma).setOnClickListener(v -> conSeleccion(this::verFirma));
        findViewById(R.id.botonEliminar).setOnClickListener(v -> conSeleccion(this::confirmarEliminar));
        findViewById(R.id.botonActualizar).setOnClickListener(v -> conSeleccion(this::abrirEdicion));
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargar();
    }

    @Override
    protected void onDestroy() {
        manejador.removeCallbacks(buscarPendiente);
        if (llamadaLista != null) llamadaLista.cancel();
        if (llamadaAccion != null) llamadaAccion.cancel();
        super.onDestroy();
    }

    private void cargar() {
        if (llamadaLista != null) llamadaLista.cancel();
        String filtro = editBuscar.getText() == null ? "" : editBuscar.getText().toString();
        Pantalla.mostrarCarga(capaCarga, true);
        llamadaLista = repositorio.listar(filtro, new ResultadoCallback<List<ContactoDto>>() {
            @Override
            public void onExito(List<ContactoDto> contactos) {
                Pantalla.mostrarCarga(capaCarga, false);
                adaptador.mostrar(contactos);
                textVacio.setVisibility(adaptador.getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onError(ErrorApi error) {
                Pantalla.mostrarCarga(capaCarga, false);
                Dialogos.error(ListaContactosActivity.this, error, () -> cargar());
            }
        });
    }

    private interface AccionContacto {
        void ejecutar(ContactoDto contacto);
    }

    private void conSeleccion(AccionContacto accion) {
        ContactoDto contacto = adaptador.getSeleccionado();
        if (contacto == null) {
            Toast.makeText(this, R.string.seleccione_contacto, Toast.LENGTH_SHORT).show();
            return;
        }
        accion.ejecutar(contacto);
    }

    private void preguntarIrAlMapa(ContactoDto contacto) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.accion)
                .setMessage(getString(R.string.ir_a_ubicacion, contacto.getNombre()))
                .setPositiveButton(R.string.si, (d, w) -> abrirMapa(contacto))
                .setNegativeButton(R.string.no, null)
                .show();
    }

    private void abrirMapa(ContactoDto contacto) {
        Intent intento = new Intent(this, MapaActivity.class);
        intento.putExtra(MapaActivity.EXTRA_NOMBRE, contacto.getNombre());
        intento.putExtra(MapaActivity.EXTRA_TELEFONO, contacto.getTelefono());
        intento.putExtra(MapaActivity.EXTRA_LATITUD, contacto.getLatitud());
        intento.putExtra(MapaActivity.EXTRA_LONGITUD, contacto.getLongitud());
        startActivity(intento);
    }

    private void abrirEdicion(ContactoDto contacto) {
        Intent intento = new Intent(this, MainActivity.class);
        intento.putExtra(MainActivity.EXTRA_ID, contacto.getId());
        intento.putExtra(MainActivity.EXTRA_NOMBRE, contacto.getNombre());
        intento.putExtra(MainActivity.EXTRA_TELEFONO, contacto.getTelefono());
        intento.putExtra(MainActivity.EXTRA_LATITUD, contacto.getLatitud());
        intento.putExtra(MainActivity.EXTRA_LONGITUD, contacto.getLongitud());
        startActivity(intento);
    }

    private void confirmarEliminar(ContactoDto contacto) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.eliminar_contacto)
                .setMessage(getString(R.string.confirmar_eliminar, contacto.getNombre()))
                .setPositiveButton(R.string.eliminar, (d, w) -> eliminar(contacto))
                .setNegativeButton(R.string.cancelar, null)
                .show();
    }

    private void eliminar(ContactoDto contacto) {
        Pantalla.mostrarCarga(capaCarga, true);
        llamadaAccion = repositorio.eliminar(contacto.getId(), new ResultadoCallback<Void>() {
            @Override
            public void onExito(Void datos) {
                Pantalla.mostrarCarga(capaCarga, false);
                Toast.makeText(ListaContactosActivity.this, R.string.contacto_eliminado, Toast.LENGTH_SHORT).show();
                cargar();
            }

            @Override
            public void onError(ErrorApi error) {
                Pantalla.mostrarCarga(capaCarga, false);
                Dialogos.error(ListaContactosActivity.this, error, () -> eliminar(contacto));
            }
        });
    }

    private void verFirma(ContactoDto contacto) {
        View vista = LayoutInflater.from(this).inflate(R.layout.dialog_firma, null);
        ImageView imagen = vista.findViewById(R.id.imagenFirma);
        ProgressBar progreso = vista.findViewById(R.id.progresoFirma);
        TextView detalle = vista.findViewById(R.id.textDetalleFirma);

        AlertDialog dialogo = new MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.firma_de, contacto.getNombre()))
                .setView(vista)
                .setPositiveButton(R.string.cerrar, null)
                .show();

        Call<?> descarga = repositorio.descargarFirma(contacto.getId(), new ResultadoCallback<Bitmap>() {
            @Override
            public void onExito(Bitmap firma) {
                progreso.setVisibility(View.GONE);
                imagen.setImageBitmap(firma);
                detalle.setText(getString(R.string.detalle_firma, contacto.getArchivoMime(), contacto.getArchivoTamano()));
            }

            @Override
            public void onError(ErrorApi error) {
                progreso.setVisibility(View.GONE);
                dialogo.dismiss();
                Dialogos.error(ListaContactosActivity.this, error, () -> verFirma(contacto));
            }
        });
        dialogo.setOnDismissListener(d -> descarga.cancel());
    }
}
