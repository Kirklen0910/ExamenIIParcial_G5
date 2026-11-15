namespace Contactos.Domain.Entities;

public class Contacto
{
    public int Id { get; set; }
    public string Nombre { get; set; } = string.Empty;
    public string Telefono { get; set; } = string.Empty;
    public double Latitud { get; set; }
    public double Longitud { get; set; }
    public string ArchivoRuta { get; set; } = string.Empty;
    public string ArchivoMime { get; set; } = string.Empty;
    public long ArchivoTamano { get; set; }
    public DateTime FechaCreacion { get; set; }
    public DateTime? FechaActualizacion { get; set; }
}
