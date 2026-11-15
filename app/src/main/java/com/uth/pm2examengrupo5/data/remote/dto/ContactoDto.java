package com.uth.pm2examengrupo5.data.remote.dto;

public class ContactoDto {
    private int id;
    private String nombre;
    private String telefono;
    private double latitud;
    private double longitud;
    private String archivoMime;
    private long archivoTamano;
    private String urlArchivo;
    private String fechaCreacion;
    private String fechaActualizacion;

    public int getId() { return id; }
    public String getNombre() { return nombre; }
    public String getTelefono() { return telefono; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getArchivoMime() { return archivoMime; }
    public long getArchivoTamano() { return archivoTamano; }
    public String getUrlArchivo() { return urlArchivo; }
    public String getFechaCreacion() { return fechaCreacion; }
    public String getFechaActualizacion() { return fechaActualizacion; }
}
