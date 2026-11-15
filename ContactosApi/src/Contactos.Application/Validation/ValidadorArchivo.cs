using Contactos.Application.DTOs;
using Contactos.Application.Exceptions;

namespace Contactos.Application.Validation;

public static class ValidadorArchivo
{
    private static readonly byte[] FirmaPng = { 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A };

    public static async Task<MemoryStream> ValidarAsync(ArchivoEntrada? archivo, CancellationToken ct)
    {
        if (archivo is null || archivo.Tamano == 0)
            throw new ValidacionException("archivo", "Debe adjuntar la firma en formato PNG.");

        if (archivo.Tamano > ReglasArchivo.TamanoMaximo)
            throw new ValidacionException("archivo", "El archivo supera el tamaño máximo de 1 MB.");

        if (!string.Equals(archivo.ContentType, ReglasArchivo.MimePermitido, StringComparison.OrdinalIgnoreCase))
            throw new TipoArchivoNoSoportadoException(
                $"Tipo de archivo no soportado ({archivo.ContentType ?? "desconocido"}). Solo se permite image/png.");

        var copia = new MemoryStream();
        await archivo.Contenido.CopyToAsync(copia, ct);

        if (copia.Length > ReglasArchivo.TamanoMaximo)
            throw new ValidacionException("archivo", "El archivo supera el tamaño máximo de 1 MB.");

        if (!EsPng(copia))
            throw new TipoArchivoNoSoportadoException("El contenido del archivo no es una imagen PNG válida.");

        copia.Position = 0;
        return copia;
    }

    private static bool EsPng(MemoryStream contenido)
    {
        if (contenido.Length < FirmaPng.Length) return false;
        var buffer = contenido.GetBuffer();
        for (var i = 0; i < FirmaPng.Length; i++)
        {
            if (buffer[i] != FirmaPng[i]) return false;
        }
        return true;
    }
}
