using Contactos.Api.Models;
using Contactos.Application.DTOs;
using Contactos.Application.Services;
using Microsoft.AspNetCore.Mvc;

namespace Contactos.Api.Controllers;

[ApiController]
[Route("api/contactos")]
[Produces("application/json")]
public class ContactosController : ControllerBase
{
    private readonly IContactoService _servicio;

    public ContactosController(IContactoService servicio)
    {
        _servicio = servicio;
    }

    [HttpGet]
    [ProducesResponseType(typeof(List<ContactoDto>), StatusCodes.Status200OK)]
    public async Task<ActionResult<List<ContactoDto>>> Listar([FromQuery] string? buscar, CancellationToken ct)
    {
        return Ok(await _servicio.ListarAsync(buscar, ct));
    }

    [HttpGet("{id:int}")]
    [ProducesResponseType(typeof(ContactoDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<ActionResult<ContactoDto>> Obtener(int id, CancellationToken ct)
    {
        return Ok(await _servicio.ObtenerAsync(id, ct));
    }

    [HttpGet("{id:int}/archivo")]
    [Produces("image/png", "application/json")]
    [ProducesResponseType(typeof(FileStreamResult), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> ObtenerArchivo(int id, CancellationToken ct)
    {
        var archivo = await _servicio.ObtenerArchivoAsync(id, ct);
        return File(archivo.Contenido, archivo.ContentType, $"firma_{id}.png");
    }

    [HttpPost]
    [Consumes("multipart/form-data")]
    [ProducesResponseType(typeof(ContactoDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ValidationProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status415UnsupportedMediaType)]
    public async Task<ActionResult<ContactoDto>> Crear([FromForm] ContactoFormulario formulario, CancellationToken ct)
    {
        var creado = await _servicio.CrearAsync(ARequest(formulario), AArchivo(formulario.Archivo), ct);
        return CreatedAtAction(nameof(Obtener), new { id = creado.Id }, creado);
    }

    [HttpPut("{id:int}")]
    [Consumes("multipart/form-data")]
    [ProducesResponseType(typeof(ContactoDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ValidationProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status415UnsupportedMediaType)]
    public async Task<ActionResult<ContactoDto>> Actualizar(int id, [FromForm] ContactoFormulario formulario, CancellationToken ct)
    {
        return Ok(await _servicio.ActualizarAsync(id, ARequest(formulario), AArchivo(formulario.Archivo), ct));
    }

    [HttpDelete("{id:int}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Eliminar(int id, CancellationToken ct)
    {
        await _servicio.EliminarAsync(id, ct);
        return NoContent();
    }

    private static GuardarContactoRequest ARequest(ContactoFormulario f) =>
        new(f.Nombre, f.Telefono, f.Latitud, f.Longitud);

    private static ArchivoEntrada? AArchivo(IFormFile? archivo) =>
        archivo is null ? null : new ArchivoEntrada(archivo.OpenReadStream(), archivo.ContentType, archivo.Length);
}
