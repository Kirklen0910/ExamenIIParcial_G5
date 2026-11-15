using Contactos.Application.DTOs;

namespace Contactos.Application.Services;

public interface IContactoService
{
    Task<List<ContactoDto>> ListarAsync(string? buscar, CancellationToken ct = default);
    Task<ContactoDto> ObtenerAsync(int id, CancellationToken ct = default);
    Task<ContactoDto> CrearAsync(GuardarContactoRequest request, ArchivoEntrada? archivo, CancellationToken ct = default);
    Task<ContactoDto> ActualizarAsync(int id, GuardarContactoRequest request, ArchivoEntrada? archivo, CancellationToken ct = default);
    Task EliminarAsync(int id, CancellationToken ct = default);
    Task<ArchivoSalida> ObtenerArchivoAsync(int id, CancellationToken ct = default);
}
