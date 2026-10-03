package dao;

/**
 * La base de datos rechazó la operación por una regla de los datos: un valor repetido,
 * registros asociados que impiden borrar, una referencia a algo que no existe o un CHECK.
 * No es una falla técnica: el usuario puede corregir los datos y reintentar.
 */
public class RestriccionException extends PersistenciaException {

    /**
     * @param mensaje explicación para el usuario.
     * @param causa   la excepción original.
     */
    public RestriccionException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }

    @Override
    public boolean esFallaTecnica() {
        return false;
    }
}