package es.unizar.eina.T251_quads.database;

import androidx.room.Embedded;
import androidx.room.Junction;
import androidx.room.Relation;

import java.util.List;

/**
 * Se ha diseñado esta clase para representar una entidad {@code Reserva} junto con la lista
 * de todos los {@code Quad}s asociados a ella, modelando así la relación Muchos-a-Muchos (M:N)
 * a través de la tabla de unión {@code RelacionReservaQuad}.
 */
public class ReservaConQuads {

    /**
     * Se ha utilizado la anotación {@code @Embedded} para incluir todos los campos de la entidad
     * {@code Reserva} directamente en el resultado de la consulta. Esta es la entidad "padre" de la relación.
     */
    @Embedded
    public Reserva reserva;

    /**
     * Lista de entidades {@code Quad} asociadas a la reserva.
     * Se ha definido esta lista mediante la anotación {@code @Relation} para que Room
     * se encargue de poblarla automáticamente utilizando la tabla de unión.
     */
    @Relation(
            parentColumn = "id", // Columna de la entidad Reserva (el ID de la reserva)
            entity = Quad.class, // La entidad hija que se desea incluir
            entityColumn = "matricula", // Columna que conecta el Quad con la tabla de unión
            associateBy = @Junction(
                    value = RelacionReservaQuad.class, // La tabla de unión que almacena la relación
                    parentColumn = "reservaId", // Columna de la tabla de unión que hace referencia a la Reserva
                    entityColumn = "quadId" // Columna de la tabla de unión que hace referencia al Quad
            )
    )
    public List<Quad> quads;
}