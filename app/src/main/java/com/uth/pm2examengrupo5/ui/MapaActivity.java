package com.uth.pm2examengrupo5.ui;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.uth.pm2examengrupo5.R;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.Locale;

public class MapaActivity extends AppCompatActivity {
    public static final String EXTRA_NOMBRE = "mapa_nombre";
    public static final String EXTRA_TELEFONO = "mapa_telefono";
    public static final String EXTRA_LATITUD = "mapa_latitud";
    public static final String EXTRA_LONGITUD = "mapa_longitud";

    private MapView mapa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE));
        Configuration.getInstance().setUserAgentValue(getPackageName());

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_mapa);

        String nombre = getIntent().getStringExtra(EXTRA_NOMBRE);
        String telefono = getIntent().getStringExtra(EXTRA_TELEFONO);
        double latitud = getIntent().getDoubleExtra(EXTRA_LATITUD, 0);
        double longitud = getIntent().getDoubleExtra(EXTRA_LONGITUD, 0);

        Pantalla.configurar(this, nombre == null ? getString(R.string.titulo_mapa) : nombre, true);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(String.format(Locale.US, "%.6f, %.6f", latitud, longitud));
        }

        GeoPoint punto = new GeoPoint(latitud, longitud);
        mapa = findViewById(R.id.mapa);
        mapa.setTileSource(TileSourceFactory.MAPNIK);
        mapa.setMultiTouchControls(true);
        mapa.getController().setZoom(17.0);
        mapa.getController().setCenter(punto);

        Marker marcador = new Marker(mapa);
        marcador.setPosition(punto);
        marcador.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marcador.setTitle(nombre);
        marcador.setSnippet(telefono);
        mapa.getOverlays().add(marcador);
        marcador.showInfoWindow();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapa.onResume();
    }

    @Override
    protected void onPause() {
        mapa.onPause();
        super.onPause();
    }
}
