package com.uth.pm2examengrupo5.data;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.uth.pm2examengrupo5.data.remote.dto.ProblemaDto;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

public final class ErrorApi {
    public static final String SIN_CONEXION = "Sin conexión con la API. Intente de nuevo.";
    public static final String TIEMPO_AGOTADO = "La API tardó demasiado en responder. Intente de nuevo.";

    private final String mensaje;
    private final int codigo;
    private final boolean sinConexion;

    private ErrorApi(String mensaje, int codigo, boolean sinConexion) {
        this.mensaje = mensaje;
        this.codigo = codigo;
        this.sinConexion = sinConexion;
    }

    public String getMensaje() { return mensaje; }
    public int getCodigo() { return codigo; }
    public boolean isSinConexion() { return sinConexion; }

    public static ErrorApi local(String mensaje) {
        return new ErrorApi(mensaje, 0, false);
    }

    static ErrorApi desdeFallo(Throwable t) {
        if (t instanceof SocketTimeoutException) return new ErrorApi(TIEMPO_AGOTADO, 0, true);
        if (t instanceof IOException) return new ErrorApi(SIN_CONEXION, 0, true);
        return new ErrorApi("Error inesperado: " + t.getMessage(), 0, false);
    }

    static ErrorApi desdeRespuesta(Response<?> respuesta) {
        int codigo = respuesta.code();
        String mensaje = leerProblema(respuesta.errorBody());
        if (mensaje == null || mensaje.trim().isEmpty()) {
            mensaje = porCodigo(codigo);
        }
        return new ErrorApi(mensaje, codigo, false);
    }

    private static String leerProblema(ResponseBody cuerpo) {
        if (cuerpo == null) return null;
        try {
            ProblemaDto problema = new Gson().fromJson(cuerpo.charStream(), ProblemaDto.class);
            if (problema == null) return null;
            if (problema.getErrors() != null && !problema.getErrors().isEmpty()) {
                List<String> mensajes = new ArrayList<>();
                for (Map.Entry<String, List<String>> e : problema.getErrors().entrySet()) {
                    if (e.getValue() != null) mensajes.addAll(e.getValue());
                }
                if (!mensajes.isEmpty()) return android.text.TextUtils.join("\n", mensajes);
            }
            if (problema.getDetail() != null) return problema.getDetail();
            return problema.getTitle();
        } catch (JsonParseException | IllegalStateException e) {
            return null;
        } finally {
            cuerpo.close();
        }
    }

    private static String porCodigo(int codigo) {
        if (codigo == 400) return "Los datos enviados no son válidos.";
        if (codigo == 404) return "El contacto no existe.";
        if (codigo == 415) return "Tipo de archivo no soportado. Solo se permite image/png.";
        if (codigo >= 500) return "La API tuvo un problema (código " + codigo + "). Intente más tarde.";
        return "La API respondió con el código " + codigo + ".";
    }
}
