namespace Contactos.Application.Exceptions;

public class ValidacionException : Exception
{
    public IDictionary<string, string[]> Errores { get; }

    public ValidacionException(IDictionary<string, string[]> errores)
        : base("Uno o más datos no son válidos.")
    {
        Errores = errores;
    }

    public ValidacionException(string campo, string mensaje)
        : this(new Dictionary<string, string[]> { [campo] = new[] { mensaje } })
    {
    }
}

public class TipoArchivoNoSoportadoException : Exception
{
    public TipoArchivoNoSoportadoException(string mensaje) : base(mensaje)
    {
    }
}

public class NoEncontradoException : Exception
{
    public NoEncontradoException(string mensaje) : base(mensaje)
    {
    }
}
