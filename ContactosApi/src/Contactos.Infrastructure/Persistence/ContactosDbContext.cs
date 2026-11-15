using Contactos.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Contactos.Infrastructure.Persistence;

public class ContactosDbContext : DbContext
{
    public ContactosDbContext(DbContextOptions<ContactosDbContext> options) : base(options)
    {
    }

    public DbSet<Contacto> Contactos => Set<Contacto>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Contacto>(e =>
        {
            e.ToTable("Contactos");
            e.HasKey(c => c.Id);
            e.Property(c => c.Nombre).IsRequired().HasMaxLength(100);
            e.Property(c => c.Telefono).IsRequired().HasMaxLength(15);
            e.Property(c => c.ArchivoRuta).IsRequired().HasMaxLength(260);
            e.Property(c => c.ArchivoMime).IsRequired().HasMaxLength(50);
            e.HasIndex(c => c.Nombre);
        });
    }
}
