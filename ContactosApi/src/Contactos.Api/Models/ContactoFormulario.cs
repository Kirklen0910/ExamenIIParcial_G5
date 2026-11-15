namespace Contactos.Api.Models;

public class ContactoFormulario
{
    public string? Nombre { get; set; }
    public string? Telefono { get; set; }
    public double? Latitud { get; set; }
    public double? Longitud { get; set; }
    public IFormFile? Archivo { get; set; }
}
