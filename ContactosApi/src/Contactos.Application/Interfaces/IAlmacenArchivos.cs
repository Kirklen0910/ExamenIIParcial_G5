namespace Contactos.Application.Interfaces;

public interface IAlmacenArchivos
{
    Task<string> GuardarAsync(Stream contenido, string extension, CancellationToken ct = default);
    Stream? Abrir(string ruta);
    void Eliminar(string ruta);
}
