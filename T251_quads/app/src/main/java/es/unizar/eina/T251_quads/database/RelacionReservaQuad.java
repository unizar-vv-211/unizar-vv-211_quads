package es.unizar.eina.T251_quads.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

/**
 * Entidad de persistencia que representa la tabla de unión
 * para modelar la relación Muchos-a-Muchos (M:N) entre las entidades {@code Reserva} y {@code Quad}.
 */
@Entity(tableName = "relacion_reserva_quad_table",
        // Se ha definido la clave primaria compuesta por ambas claves foráneas para
        // asegurar que un mismo quad no puede estar dos veces en la misma reserva.
        primaryKeys = {"reservaId", "quadId"},

        // Se han añadido índices a las claves foráneas para optimizar la velocidad de las consultas.
        indices = {@Index("reservaId"), @Index("quadId")},

        // Se han definido las claves foráneas (ForeignKeys) para garantizar la integridad referencial.
        foreignKeys = {
                @ForeignKey(entity = Reserva.class,
                        parentColumns = "id", // ID de la Reserva
                        childColumns = "reservaId",
                        onDelete = ForeignKey.CASCADE), // Si se ha eliminado la Reserva, se elimina la relación
                @ForeignKey(entity = Quad.class,
                        parentColumns = "matricula", // Matrícula (PK) del Quad
                        childColumns = "quadId",
                        onDelete = ForeignKey.RESTRICT)  // Si se intenta eliminar el Quad con reservas, se rechaza
        })
public class RelacionReservaQuad {

    /**
     * Clave foránea que hace referencia al ID de la Reserva asociada.
     */
    private int reservaId;

    /**
     * Clave foránea que hace referencia a la matrícula del Quad asociado.
     * Se ha definido como campo obligatorio.
     */
    @NonNull
    private String quadId;

    /**
     * Constructor utilizado para crear una nueva relación entre una Reserva y un Quad.
     *
     * @param reservaId El identificador de la reserva.
     * @param quadId La matrícula del quad. Este campo ha sido marcado como no nulo.
     */
    public RelacionReservaQuad(int reservaId, @NonNull String quadId) {
        this.reservaId = reservaId;
        this.quadId = quadId;
    }

    /**
     * Devuelve el ID de la reserva asociada.
     *
     * @return El ID de la reserva.
     */
    public int getReservaId() {
        return reservaId;
    }

    /**
     * Devuelve la matrícula del quad asociado.
     *
     * @return La matrícula del quad.
     */
    @NonNull
    public String getQuadId() {
        return quadId;
    }
}