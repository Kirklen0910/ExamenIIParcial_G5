package com.uth.pm2examengrupo5.data;

public interface ResultadoCallback<T> {
    void onExito(T datos);
    void onError(ErrorApi error);
}
