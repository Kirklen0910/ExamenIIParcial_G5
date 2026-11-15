package com.uth.pm2examengrupo5.data.remote;

import com.uth.pm2examengrupo5.BuildConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public final class ApiClient {
    private static volatile ApiService servicio;

    private ApiClient() { }

    public static ApiService get() {
        if (servicio == null) {
            synchronized (ApiClient.class) {
                if (servicio == null) servicio = crearRetrofit().create(ApiService.class);
            }
        }
        return servicio;
    }

    private static Retrofit crearRetrofit() {
        HttpLoggingInterceptor registro = new HttpLoggingInterceptor();
        registro.setLevel(BuildConfig.DEBUG
                ? HttpLoggingInterceptor.Level.HEADERS
                : HttpLoggingInterceptor.Level.NONE);

        OkHttpClient cliente = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(registro)
                .build();

        return new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(cliente)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
