namespace Contactos.Application.DTOs;

public record ContactoDto(
    int Id,
    string Nombre,
    string Telefono,
    double Latitud,
    double Longitud,
    string ArchivoMime,
    long ArchivoTamano,
    string UrlArchivo,
    DateTime FechaCreacion,
    DateTime? FechaActualizacion);

public record GuardarContactoRequest(string? Nombre, string? Telefono, double? Latitud, double? Longitud);

public record ArchivoEntrada(Stream Contenido, string? ContentType, long Tamano);

public record ArchivoSalida(Stream Contenido, string ContentType);
