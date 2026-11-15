using Contactos.Domain.Entities;

namespace Contactos.Application.Interfaces;

public interface IContactoRepository
{
    Task<List<Contacto>> ListarAsync(string? buscar, CancellationToken ct = default);
    Task<Contacto?> ObtenerAsync(int id, CancellationToken ct = default);
    Task AgregarAsync(Contacto contacto, CancellationToken ct = default);
    Task ActualizarAsync(Contacto contacto, CancellationToken ct = default);
    Task EliminarAsync(Contacto contacto, CancellationToken ct = default);
}
