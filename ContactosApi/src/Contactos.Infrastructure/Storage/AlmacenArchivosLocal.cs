using Contactos.Application.Interfaces;

namespace Contactos.Infrastructure.Storage;

public class AlmacenArchivosLocal : IAlmacenArchivos
{
    private readonly string _carpeta;

    public AlmacenArchivosLocal(string carpeta)
    {
        _carpeta = carpeta;
        Directory.CreateDirectory(_carpeta);
    }

    public async Task<string> GuardarAsync(Stream contenido, string extension, CancellationToken ct = default)
    {
        var nombre = $"{Guid.NewGuid():N}{extension}";
        var destino = Path.Combine(_carpeta, nombre);
        await using var archivo = new FileStream(destino, FileMode.CreateNew, FileAccess.Write);
        await contenido.CopyToAsync(archivo, ct);
        return nombre;
    }

    public Stream? Abrir(string ruta)
    {
        var completa = RutaSegura(ruta);
        return completa is not null && File.Exists(completa)
            ? new FileStream(completa, FileMode.Open, FileAccess.Read, FileShare.Read)
            : null;
    }

    public void Eliminar(string ruta)
    {
        var completa = RutaSegura(ruta);
        if (completa is not null && File.Exists(completa)) File.Delete(completa);
    }

    private string? RutaSegura(string ruta)
    {
        if (string.IsNullOrWhiteSpace(ruta)) return null;
        var completa = Path.GetFullPath(Path.Combine(_carpeta, Path.GetFileName(ruta)));
        return completa.StartsWith(Path.GetFullPath(_carpeta), StringComparison.OrdinalIgnoreCase) ? completa : null;
    }
}
