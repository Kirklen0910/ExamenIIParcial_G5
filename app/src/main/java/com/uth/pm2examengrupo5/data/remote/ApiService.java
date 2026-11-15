package com.uth.pm2examengrupo5.data.remote;

import com.uth.pm2examengrupo5.data.remote.dto.ContactoDto;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @GET("api/contactos")
    Call<List<ContactoDto>> listar(@Query("buscar") String buscar);

    @GET("api/contactos/{id}/archivo")
    Call<ResponseBody> descargarFirma(@Path("id") int id);

    @Multipart
    @POST("api/contactos")
    Call<ContactoDto> crear(
            @Part("nombre") RequestBody nombre,
            @Part("telefono") RequestBody telefono,
            @Part("latitud") RequestBody latitud,
            @Part("longitud") RequestBody longitud,
            @Part MultipartBody.Part archivo);

    @Multipart
    @PUT("api/contactos/{id}")
    Call<ContactoDto> actualizar(
            @Path("id") int id,
            @Part("nombre") RequestBody nombre,
            @Part("telefono") RequestBody telefono,
            @Part("latitud") RequestBody latitud,
            @Part("longitud") RequestBody longitud,
            @Part MultipartBody.Part archivo);

    @DELETE("api/contactos/{id}")
    Call<Void> eliminar(@Path("id") int id);
}
