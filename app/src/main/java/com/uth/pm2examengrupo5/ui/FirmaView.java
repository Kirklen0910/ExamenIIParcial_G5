package com.uth.pm2examengrupo5.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.uth.pm2examengrupo5.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class FirmaView extends View {
    private final Paint pincel = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint pincelImagen = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Path trazo = new Path();
    private final RectF destino = new RectF();
    private Bitmap firmaExistente;
    private boolean tieneTrazos;

    public FirmaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        pincel.setColor(ContextCompat.getColor(context, R.color.trazo_firma));
        pincel.setStyle(Paint.Style.STROKE);
        pincel.setStrokeJoin(Paint.Join.ROUND);
        pincel.setStrokeCap(Paint.Cap.ROUND);
        pincel.setStrokeWidth(5f * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.WHITE);
        if (firmaExistente != null) {
            ajustar(firmaExistente, destino);
            canvas.drawBitmap(firmaExistente, null, destino, pincelImagen);
        }
        canvas.drawPath(trazo, pincel);
    }

    @Override
    public boolean onTouchEvent(MotionEvent evento) {
        float x = evento.getX();
        float y = evento.getY();
        switch (evento.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                trazo.moveTo(x, y);
                trazo.lineTo(x + 0.1f, y + 0.1f);
                tieneTrazos = true;
                break;
            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < evento.getHistorySize(); i++) {
                    trazo.lineTo(evento.getHistoricalX(i), evento.getHistoricalY(i));
                }
                trazo.lineTo(x, y);
                break;
            case MotionEvent.ACTION_UP:
                trazo.lineTo(x, y);
                getParent().requestDisallowInterceptTouchEvent(false);
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);
                break;
            default:
                return false;
        }
        invalidate();
        return true;
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    public void limpiar() {
        trazo.reset();
        firmaExistente = null;
        tieneTrazos = false;
        invalidate();
    }

    public void mostrarFirma(Bitmap firma) {
        trazo.reset();
        tieneTrazos = false;
        firmaExistente = firma;
        invalidate();
    }

    public boolean estaVacia() {
        return !tieneTrazos && firmaExistente == null;
    }

    public boolean fueModificada() {
        return tieneTrazos;
    }

    public File exportarPng(File destinoArchivo) throws IOException {
        Bitmap imagen = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas lienzo = new Canvas(imagen);
        lienzo.drawColor(Color.WHITE);
        if (firmaExistente != null) {
            ajustar(firmaExistente, destino);
            lienzo.drawBitmap(firmaExistente, null, destino, pincelImagen);
        }
        lienzo.drawPath(trazo, pincel);
        try (FileOutputStream salida = new FileOutputStream(destinoArchivo)) {
            if (!imagen.compress(Bitmap.CompressFormat.PNG, 100, salida)) {
                throw new IOException("No se pudo generar el PNG de la firma.");
            }
        } finally {
            imagen.recycle();
        }
        return destinoArchivo;
    }

    private void ajustar(Bitmap imagen, RectF rect) {
        float escala = Math.min((float) getWidth() / imagen.getWidth(), (float) getHeight() / imagen.getHeight());
        float ancho = imagen.getWidth() * escala;
        float alto = imagen.getHeight() * escala;
        float izquierda = (getWidth() - ancho) / 2f;
        float arriba = (getHeight() - alto) / 2f;
        rect.set(izquierda, arriba, izquierda + ancho, arriba + alto);
    }
}
