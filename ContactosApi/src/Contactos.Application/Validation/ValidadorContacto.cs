using System.Text.RegularExpressions;
using Contactos.Application.DTOs;
using Contactos.Application.Exceptions;

namespace Contactos.Application.Validation;

public static class ValidadorContacto
{
    private static readonly Regex SoloDigitos = new(@"^[0-9]{8,15}$", RegexOptions.Compiled);

    public static void Validar(GuardarContactoRequest request)
    {
        var errores = new Dictionary<string, string[]>();

        if (string.IsNullOrWhiteSpace(request.Nombre))
            errores["nombre"] = new[] { "El nombre es obligatorio." };
        else if (request.Nombre.Trim().Length > 100)
            errores["nombre"] = new[] { "El nombre no puede superar 100 caracteres." };

        if (string.IsNullOrWhiteSpace(request.Telefono))
            errores["telefono"] = new[] { "El teléfono es obligatorio." };
        else if (!SoloDigitos.IsMatch(request.Telefono.Trim()))
            errores["telefono"] = new[] { "El teléfono debe tener entre 8 y 15 dígitos." };

        if (request.Latitud is null)
            errores["latitud"] = new[] { "La latitud es obligatoria." };
        else if (request.Latitud < -90 || request.Latitud > 90)
            errores["latitud"] = new[] { "La latitud debe estar entre -90 y 90." };

        if (request.Longitud is null)
            errores["longitud"] = new[] { "La longitud es obligatoria." };
        else if (request.Longitud < -180 || request.Longitud > 180)
            errores["longitud"] = new[] { "La longitud debe estar entre -180 y 180." };

        if (errores.Count > 0) throw new ValidacionException(errores);
    }
}
