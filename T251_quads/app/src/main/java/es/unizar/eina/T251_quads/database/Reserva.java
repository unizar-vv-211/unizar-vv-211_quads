package es.unizar.eina.T251_quads.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad de persistencia que representa una reserva en la base de datos.
 * Esta clase ha sido anotada con {@code @Entity} de Room para mapear directamente
 * a la tabla {@code reserva_table}.
 */
@Entity(tableName = "reserva_table")
public class Reserva {

    /**
     * Clave primaria única que identifica la reserva.
     * Esta clave se genera automáticamente por Room.
     */
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private int mId;

    /**
     * Nombre completo del cliente que ha realizado la reserva.
     * Este campo es obligatorio.
     */
    @NonNull
    @ColumnInfo(name = "cliente")
    private String mCliente;

    /**
     * Número de teléfono de contacto del cliente.
     * Este campo es obligatorio y se ha sometido a validación de formato (9 dígitos).
     */
    @NonNull
    @ColumnInfo(name = "telefono")
    private String mTelefono;

    /**
     * Fecha en la que se ha acordado la recogida de los quads.
     * El formato esperado es String (YYYY-MM-DD) para almacenamiento en base de datos.
     */
    @NonNull
    @ColumnInfo(name = "fechaRecogida")
    private String mFechaRecogida;

    /**
     * Fecha acordada para la devolución de los quads.
     * El formato esperado es String (YYYY-MM-DD) para almacenamiento en base de datos.
     */
    @NonNull
    @ColumnInfo(name = "fechaDevolucion")
    private String mFechaDevolucion;

    /**
     * Número de cascos solicitados para la reserva.
     * El valor por defecto es 0. Este campo ha sido validado según los tipos
     * de quads incluidos en la reserva (Monoplaza: máx 1, Biplaza: máx 2 por quad).
     */
    @ColumnInfo(name = "cascos", defaultValue = "0")
    private int mCascos;

    /**
     * Constructor utilizado para crear una nueva entidad Reserva.
     * Nota: El campo mId no se incluye, ya que ha sido configurado para generarse automáticamente.
     *
     * @param cliente El nombre del cliente.
     * @param telefono El teléfono de contacto del cliente.
     * @param fechaRecogida La fecha de inicio de la reserva (formato YYYY-MM-DD).
     * @param fechaDevolucion La fecha de finalización de la reserva (formato YYYY-MM-DD).
     * @param cascos El número de cascos solicitados.
     */
    public Reserva(@NonNull String cliente, @NonNull String telefono,
                   @NonNull String fechaRecogida, @NonNull String fechaDevolucion, int cascos) {
        this.mCliente = cliente;
        this.mTelefono = telefono;
        this.mFechaRecogida = fechaRecogida;
        this.mFechaDevolucion = fechaDevolucion;
        this.mCascos = cascos;
    }

    /**
     * Se ha recuperado el identificador único de la reserva.
     *
     * @return El ID de la reserva.
     */
    public int getId() {
        return this.mId;
    }

    /**
     * Se ha recuperado el nombre del cliente.
     *
     * @return El nombre del cliente.
     */
    public String getCliente() {
        return this.mCliente;
    }

    /**
     * Se ha recuperado el teléfono de contacto del cliente.
     *
     * @return El teléfono.
     */
    public String getTelefono() {
        return this.mTelefono;
    }

    /**
     * Se ha recuperado la fecha de recogida de la reserva.
     *
     * @return La fecha de recogida en formato String (YYYY-MM-DD).
     */
    public String getFechaRecogida() {
        return this.mFechaRecogida;
    }

    /**
     * Se ha recuperado la fecha de devolución de la reserva.
     *
     * @return La fecha de devolución en formato String (YYYY-MM-DD).
     */
    public String getFechaDevolucion() {
        return this.mFechaDevolucion;
    }

    /**
     * Se ha recuperado el número de cascos solicitados para la reserva.
     *
     * @return El número de cascos.
     */
    public int getCascos() {
        return this.mCascos;
    }

    /**
     * Se ha establecido el identificador de la reserva.
     * Este mutador ha sido incluido principalmente para el uso interno de la librería Room.
     *
     * @param id El nuevo ID de la reserva.
     */
    public void setId(int id) {
        this.mId = id;
    }

    /**
     * Se ha establecido el número de cascos para la reserva.
     *
     * @param cascos El nuevo número de cascos.
     */
    public void setCascos(int cascos) {
        this.mCascos = cascos;
    }
}