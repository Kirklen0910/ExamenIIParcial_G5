using System.Globalization;
using Contactos.Api.Middleware;
using Contactos.Infrastructure;
using Microsoft.AspNetCore.Mvc;
using Microsoft.OpenApi.Models;

CultureInfo.DefaultThreadCurrentCulture = CultureInfo.InvariantCulture;
CultureInfo.DefaultThreadCurrentUICulture = CultureInfo.InvariantCulture;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AgregarInfraestructura(builder.Configuration, builder.Environment.ContentRootPath);

builder.Services.AddControllers();
builder.Services.Configure<ApiBehaviorOptions>(opciones =>
{
    opciones.InvalidModelStateResponseFactory = contexto =>
    {
        var problema = new ValidationProblemDetails(contexto.ModelState)
        {
            Title = "Datos no válidos",
            Status = StatusCodes.Status400BadRequest
        };
        problema.Detail = string.Join(" ", problema.Errors.SelectMany(e => e.Value));
        return new BadRequestObjectResult(problema) { ContentTypes = { "application/problem+json" } };
    };
});

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo
    {
        Title = "PM2 Examen Grupo 5 - API de Contactos",
        Version = "v1",
        Description = "Contactos con firma digital (image/png, máximo 1 MB) y ubicación GPS."
    });
});

var app = builder.Build();

await app.Services.CrearBaseDeDatosAsync();

app.UseMiddleware<ManejoErroresMiddleware>();

app.UseSwagger();
app.UseSwaggerUI(c => c.SwaggerEndpoint("/swagger/v1/swagger.json", "API de Contactos v1"));

app.MapControllers();
app.MapGet("/", () => Results.Redirect("/swagger")).ExcludeFromDescription();

app.Run();
