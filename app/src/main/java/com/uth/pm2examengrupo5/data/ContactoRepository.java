package com.uth.pm2examengrupo5.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.uth.pm2examengrupo5.data.remote.ApiClient;
import com.uth.pm2examengrupo5.data.remote.ApiService;
import com.uth.pm2examengrupo5.data.remote.dto.ContactoDto;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContactoRepository {
    private static final MediaType TEXTO = MediaType.parse("text/plain");
    private static final MediaType PNG = MediaType.parse("image/png");

    private final ApiService api = ApiClient.get();

    public Call<List<ContactoDto>> listar(@Nullable String buscar, ResultadoCallback<List<ContactoDto>> callback) {
        String filtro = buscar == null || buscar.trim().isEmpty() ? null : buscar.trim();
        Call<List<ContactoDto>> call = api.listar(filtro);
        call.enqueue(adaptar(callback));
        return call;
    }

    public Call<ContactoDto> crear(String nombre, String telefono, double latitud, double longitud,
                                   File firma, ResultadoCallback<ContactoDto> callback) {
        Call<ContactoDto> call = api.crear(texto(nombre), texto(telefono),
                texto(numero(latitud)), texto(numero(longitud)), parteArchivo(firma));
        call.enqueue(adaptar(callback));
        return call;
    }

    public Call<ContactoDto> actualizar(int id, String nombre, String telefono, double latitud, double longitud,
                                        @Nullable File firma, ResultadoCallback<ContactoDto> callback) {
        Call<ContactoDto> call = api.actualizar(id, texto(nombre), texto(telefono),
                texto(numero(latitud)), texto(numero(longitud)),
                firma == null ? null : parteArchivo(firma));
        call.enqueue(adaptar(callback));
        return call;
    }

    public Call<Void> eliminar(int id, ResultadoCallback<Void> callback) {
        Call<Void> call = api.eliminar(id);
        call.enqueue(adaptar(callback));
        return call;
    }

    public Call<ResponseBody> descargarFirma(int id, ResultadoCallback<Bitmap> callback) {
        Call<ResponseBody> call = api.descargarFirma(id);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> c, @NonNull Response<ResponseBody> respuesta) {
                if (c.isCanceled()) return;
                if (!respuesta.isSuccessful() || respuesta.body() == null) {
                    callback.onError(ErrorApi.desdeRespuesta(respuesta));
                    return;
                }
                try (ResponseBody cuerpo = respuesta.body()) {
                    byte[] bytes = cuerpo.bytes();
                    Bitmap imagen = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    if (imagen == null) {
                        callback.onError(ErrorApi.local("La firma recibida no es una imagen válida."));
                    } else {
                        callback.onExito(imagen);
                    }
                } catch (IOException e) {
                    callback.onError(ErrorApi.desdeFallo(e));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> c, @NonNull Throwable t) {
                if (!c.isCanceled()) callback.onError(ErrorApi.desdeFallo(t));
            }
        });
        return call;
    }

    private static RequestBody texto(String valor) {
        return RequestBody.create(valor, TEXTO);
    }

    private static MultipartBody.Part parteArchivo(File archivo) {
        RequestBody cuerpo = RequestBody.create(archivo, PNG);
        return MultipartBody.Part.createFormData("archivo", archivo.getName(), cuerpo);
    }

    private static String numero(double valor) {
        return String.format(Locale.US, "%.6f", valor);
    }

    private static <T> Callback<T> adaptar(ResultadoCallback<T> callback) {
        return new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call, @NonNull Response<T> respuesta) {
                if (call.isCanceled()) return;
                if (respuesta.isSuccessful()) {
                    callback.onExito(respuesta.body());
                } else {
                    callback.onError(ErrorApi.desdeRespuesta(respuesta));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
                if (!call.isCanceled()) callback.onError(ErrorApi.desdeFallo(t));
            }
        };
    }
}
