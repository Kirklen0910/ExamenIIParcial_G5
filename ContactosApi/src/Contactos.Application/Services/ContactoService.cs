using Contactos.Application.DTOs;
using Contactos.Application.Exceptions;
using Contactos.Application.Interfaces;
using Contactos.Application.Validation;
using Contactos.Domain.Entities;

namespace Contactos.Application.Services;

public class ContactoService : IContactoService
{
    private readonly IContactoRepository _repositorio;
    private readonly IAlmacenArchivos _almacen;

    public ContactoService(IContactoRepository repositorio, IAlmacenArchivos almacen)
    {
        _repositorio = repositorio;
        _almacen = almacen;
    }

    public async Task<List<ContactoDto>> ListarAsync(string? buscar, CancellationToken ct = default)
    {
        var contactos = await _repositorio.ListarAsync(buscar?.Trim(), ct);
        return contactos.Select(ADto).ToList();
    }

    public async Task<ContactoDto> ObtenerAsync(int id, CancellationToken ct = default)
    {
        return ADto(await BuscarAsync(id, ct));
    }

    public async Task<ContactoDto> CrearAsync(GuardarContactoRequest request, ArchivoEntrada? archivo, CancellationToken ct = default)
    {
        ValidadorContacto.Validar(request);
        await using var png = await ValidadorArchivo.ValidarAsync(archivo, ct);

        var ruta = await _almacen.GuardarAsync(png, ReglasArchivo.Extension, ct);
        var contacto = new Contacto
        {
            Nombre = request.Nombre!.Trim(),
            Telefono = request.Telefono!.Trim(),
            Latitud = request.Latitud!.Value,
            Longitud = request.Longitud!.Value,
            ArchivoRuta = ruta,
            ArchivoMime = ReglasArchivo.MimePermitido,
            ArchivoTamano = png.Length,
            FechaCreacion = DateTime.UtcNow
        };

        try
        {
            await _repositorio.AgregarAsync(contacto, ct);
        }
        catch
        {
            _almacen.Eliminar(ruta);
            throw;
        }

        return ADto(contacto);
    }

    public async Task<ContactoDto> ActualizarAsync(int id, GuardarContactoRequest request, ArchivoEntrada? archivo, CancellationToken ct = default)
    {
        var contacto = await BuscarAsync(id, ct);
        ValidadorContacto.Validar(request);

        string? rutaAnterior = null;
        if (archivo is not null && archivo.Tamano > 0)
        {
            await using var png = await ValidadorArchivo.ValidarAsync(archivo, ct);
            rutaAnterior = contacto.ArchivoRuta;
            contacto.ArchivoRuta = await _almacen.GuardarAsync(png, ReglasArchivo.Extension, ct);
            contacto.ArchivoMime = ReglasArchivo.MimePermitido;
            contacto.ArchivoTamano = png.Length;
        }

        contacto.Nombre = request.Nombre!.Trim();
        contacto.Telefono = request.Telefono!.Trim();
        contacto.Latitud = request.Latitud!.Value;
        contacto.Longitud = request.Longitud!.Value;
        contacto.FechaActualizacion = DateTime.UtcNow;

        await _repositorio.ActualizarAsync(contacto, ct);
        if (rutaAnterior is not null) _almacen.Eliminar(rutaAnterior);

        return ADto(contacto);
    }

    public async Task EliminarAsync(int id, CancellationToken ct = default)
    {
        var contacto = await BuscarAsync(id, ct);
        await _repositorio.EliminarAsync(contacto, ct);
        _almacen.Eliminar(contacto.ArchivoRuta);
    }

    public async Task<ArchivoSalida> ObtenerArchivoAsync(int id, CancellationToken ct = default)
    {
        var contacto = await BuscarAsync(id, ct);
        var contenido = _almacen.Abrir(contacto.ArchivoRuta)
            ?? throw new NoEncontradoException($"La firma del contacto {id} no se encontró en el servidor.");
        return new ArchivoSalida(contenido, contacto.ArchivoMime);
    }

    private async Task<Contacto> BuscarAsync(int id, CancellationToken ct)
    {
        return await _repositorio.ObtenerAsync(id, ct)
            ?? throw new NoEncontradoException($"No existe un contacto con id {id}.");
    }

    private static ContactoDto ADto(Contacto c) => new(
        c.Id,
        c.Nombre,
        c.Telefono,
        c.Latitud,
        c.Longitud,
        c.ArchivoMime,
        c.ArchivoTamano,
        $"/api/contactos/{c.Id}/archivo",
        c.FechaCreacion,
        c.FechaActualizacion);
}
