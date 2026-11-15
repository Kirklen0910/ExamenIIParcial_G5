using Contactos.Application.Interfaces;
using Contactos.Domain.Entities;
using Contactos.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace Contactos.Infrastructure.Repositories;

public class ContactoRepository : IContactoRepository
{
    private readonly ContactosDbContext _db;

    public ContactoRepository(ContactosDbContext db)
    {
        _db = db;
    }

    public async Task<List<Contacto>> ListarAsync(string? buscar, CancellationToken ct = default)
    {
        var consulta = _db.Contactos.AsNoTracking();
        if (!string.IsNullOrWhiteSpace(buscar))
        {
            var patron = $"%{buscar}%";
            consulta = consulta.Where(c => EF.Functions.Like(c.Nombre, patron) || EF.Functions.Like(c.Telefono, patron));
        }
        return await consulta.OrderBy(c => c.Nombre).ToListAsync(ct);
    }

    public Task<Contacto?> ObtenerAsync(int id, CancellationToken ct = default)
    {
        return _db.Contactos.FirstOrDefaultAsync(c => c.Id == id, ct);
    }

    public async Task AgregarAsync(Contacto contacto, CancellationToken ct = default)
    {
        _db.Contactos.Add(contacto);
        await _db.SaveChangesAsync(ct);
    }

    public async Task ActualizarAsync(Contacto contacto, CancellationToken ct = default)
    {
        _db.Contactos.Update(contacto);
        await _db.SaveChangesAsync(ct);
    }

    public async Task EliminarAsync(Contacto contacto, CancellationToken ct = default)
    {
        _db.Contactos.Remove(contacto);
        await _db.SaveChangesAsync(ct);
    }
}
