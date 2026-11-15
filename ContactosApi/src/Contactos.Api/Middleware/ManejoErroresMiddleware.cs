using Contactos.Application.Exceptions;
using Microsoft.AspNetCore.Mvc;

namespace Contactos.Api.Middleware;

public class ManejoErroresMiddleware
{
    private readonly RequestDelegate _siguiente;
    private readonly ILogger<ManejoErroresMiddleware> _logger;

    public ManejoErroresMiddleware(RequestDelegate siguiente, ILogger<ManejoErroresMiddleware> logger)
    {
        _siguiente = siguiente;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext contexto)
    {
        try
        {
            await _siguiente(contexto);
        }
        catch (ValidacionException ex)
        {
            var problema = new ValidationProblemDetails(ex.Errores)
            {
                Title = "Datos no válidos",
                Status = StatusCodes.Status400BadRequest,
                Detail = string.Join(" ", ex.Errores.SelectMany(e => e.Value))
            };
            await EscribirAsync(contexto, problema.Status!.Value, problema);
        }
        catch (TipoArchivoNoSoportadoException ex)
        {
            await EscribirAsync(contexto, StatusCodes.Status415UnsupportedMediaType, new ProblemDetails
            {
                Title = "Tipo de archivo no soportado",
                Status = StatusCodes.Status415UnsupportedMediaType,
                Detail = ex.Message
            });
        }
        catch (NoEncontradoException ex)
        {
            await EscribirAsync(contexto, StatusCodes.Status404NotFound, new ProblemDetails
            {
                Title = "No encontrado",
                Status = StatusCodes.Status404NotFound,
                Detail = ex.Message
            });
        }
        catch (OperationCanceledException) when (contexto.RequestAborted.IsCancellationRequested)
        {
            _logger.LogInformation("La solicitud fue cancelada por el cliente.");
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Error no controlado");
            await EscribirAsync(contexto, StatusCodes.Status500InternalServerError, new ProblemDetails
            {
                Title = "Error interno del servidor",
                Status = StatusCodes.Status500InternalServerError,
                Detail = "Ocurrió un error inesperado. Intente de nuevo."
            });
        }
    }

    private static async Task EscribirAsync(HttpContext contexto, int estado, ProblemDetails problema)
    {
        if (contexto.Response.HasStarted) return;
        contexto.Response.Clear();
        contexto.Response.StatusCode = estado;
        contexto.Response.ContentType = "application/problem+json";
        await contexto.Response.WriteAsJsonAsync(problema, problema.GetType(), options: null, contentType: "application/problem+json");
    }
}
