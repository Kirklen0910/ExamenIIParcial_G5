using Contactos.Application.Interfaces;
using Contactos.Application.Services;
using Contactos.Infrastructure.Persistence;
using Contactos.Infrastructure.Repositories;
using Contactos.Infrastructure.Storage;
using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace Contactos.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AgregarInfraestructura(this IServiceCollection services, IConfiguration configuration, string raizContenido)
    {
        var conexion = new SqliteConnectionStringBuilder(
            configuration.GetConnectionString("Contactos") ?? "Data Source=contactos.db");
        if (!Path.IsPathRooted(conexion.DataSource))
            conexion.DataSource = Path.Combine(raizContenido, conexion.DataSource);
        services.AddDbContext<ContactosDbContext>(o => o.UseSqlite(conexion.ToString()));

        var carpeta = Path.Combine(raizContenido, configuration["Almacenamiento:Carpeta"] ?? "Archivos");
        services.AddSingleton<IAlmacenArchivos>(new AlmacenArchivosLocal(carpeta));

        services.AddScoped<IContactoRepository, ContactoRepository>();
        services.AddScoped<IContactoService, ContactoService>();
        return services;
    }

    public static async Task CrearBaseDeDatosAsync(this IServiceProvider services)
    {
        using var scope = services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<ContactosDbContext>();
        await db.Database.EnsureCreatedAsync();
    }
}
