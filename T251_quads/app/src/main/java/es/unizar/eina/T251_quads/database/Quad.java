package es.unizar.eina.T251_quads.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entidad de persistencia que representa un vehículo quad en el inventario
 */
@Entity(tableName = "quads")
public class Quad {
    /**
     * Clave primaria y campo obligatorio que identifica al quad.
     * Corresponde a la matrícula del vehículo.
     */
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "matricula")
    private String matricula;

    /**
     * Tipo de vehículo quad (ej. monoplaza, biplaza).
     */
    @ColumnInfo(name = "tipo")
    private String tipo;

    /**
     * Precio del alquiler del quad por día.
     */
    @ColumnInfo(name = "precio_dia")
    private float precioDia;

    /**
     * Descripción textual del quad.
     */
    @ColumnInfo(name = "descripcion")
    private String descripcion;

    /**
     * Constructor utilizado para crear un nuevo objeto Quad.
     *
     * @param matricula El identificador único del quad. Este campo no puede ser nulo.
     * @param tipo El tipo de quad.
     * @param precioDia El coste del alquiler por día.
     * @param descripcion Una descripción breve del vehículo.
     */
    public Quad(@NonNull String matricula,
                @NonNull String tipo,
                float precioDia,
                String descripcion) {
        this.matricula = matricula;
        this.tipo = tipo;
        this.precioDia = precioDia;
        this.descripcion = descripcion;
    }

    /**
     * Devuelve la matrícula del quad.
     *
     * @return La matrícula del vehículo.
     */
    @NonNull
    public String getMatricula() {
        return matricula;
    }

    /**
     * Establece la matrícula del quad.
     *
     * @param matricula La nueva matrícula del vehículo. No puede ser nula.
     */
    public void setMatricula(@NonNull String matricula) {
        this.matricula = matricula;
    }

    /**
     * Devuelve el tipo de quad.
     *
     * @return El tipo de vehículo (ej. monoplaza).
     */
    @NonNull
    public String getTipo() {
        return tipo;
    }

    /**
     * Establece el tipo de quad.
     *
     * @param tipo El nuevo tipo de vehículo. No puede ser nulo.
     */
    public void setTipo(@NonNull String tipo) {
        this.tipo = tipo;
    }

    /**
     * Devuelve el precio de alquiler por día.
     *
     * @return El precio diario en formato float.
     */
    public float getPrecioDia() {
        return precioDia;
    }

    /**
     * Establece el precio de alquiler por día.
     *
     * @param precioDia El nuevo precio diario.
     */
    public void setPrecioDia(float precioDia) {
        this.precioDia = precioDia;
    }

    /**
     * Devuelve la descripción del quad.
     *
     * @return La descripción textual del vehículo.
     */
    public String getDescripcion() {
        return descripcion;
    }

    /**
     * Establece la descripción del quad.
     *
     * @param descripcion La nueva descripción del vehículo.
     */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}