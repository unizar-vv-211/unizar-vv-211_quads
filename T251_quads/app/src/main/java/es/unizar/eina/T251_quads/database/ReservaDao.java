package es.unizar.eina.T251_quads.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Reserva y la gestión de sus relaciones.
 * Se ha definido esta interfaz para especificar los métodos de acceso a los datos
 * (CRUD y consultas complejas) de la tabla reserva_table y la tabla de unión.
 * La implementación de los métodos es generada automáticamente por la librería Room.
 */
@Dao
public interface ReservaDao {

    /**
     * Se ha insertado una nueva entidad Reserva en la base de datos.
     *
     * @param reserva La entidad Reserva que se ha de insertar.
     * @return El ID (long) de la fila que se ha insertado, lo que permite asociar las relaciones M:N posteriormente.
     */
    @Insert
    long insert(Reserva reserva);

    /**
     * Se ha actualizado una entidad Reserva existente en la base de datos.
     *
     * @param reserva La entidad Reserva con los datos que se han de actualizar.
     */
    @Update
    void update(Reserva reserva);

    /**
     * Se ha eliminado una entidad Reserva de la base de datos.
     * Las filas asociadas en la tabla de unión se han eliminado automáticamente
     * gracias a la configuración {@code ForeignKey.CASCADE}.
     *
     * @param reserva La entidad Reserva que se ha de eliminar.
     * @return El número de filas eliminadas.
     */
    @Delete
    int delete(Reserva reserva);

    /**
     * Se han eliminado todas las entidades Reserva de la tabla.
     * Este método se utiliza principalmente para tareas de inicialización o pruebas.
     */
    @Query("DELETE FROM reserva_table")
    void deleteAll();

    /**
     * Se ha insertado una nueva relación entre una reserva y un quad.
     * Se ha definido la estrategia {@code OnConflictStrategy.REPLACE} para evitar duplicados
     * en caso de que la relación (clave primaria compuesta) ya exista.
     *
     * @param relacion El objeto de unión {@code RelacionReservaQuad} que se ha de insertar.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertRelacion(RelacionReservaQuad relacion);

    /**
     * Se han eliminado todas las relaciones de quads asociadas a una reserva específica.
     * Este método se ha diseñado para ser invocado durante el proceso de actualización
     * de una reserva (borrar las antiguas relaciones e insertar las nuevas).
     *
     * @param reservaId El ID de la reserva cuyas relaciones se han de borrar.
     */
    @Query("DELETE FROM relacion_reserva_quad_table WHERE reservaId = :reservaId")
    void deleteRelacionesDeReserva(int reservaId);

    /**
     * Se han obtenido todas las reservas del sistema, y para cada una,
     * se ha incluido la lista completa de {@code Quad}s asociados.
     * {@code @Transaction} ha sido utilizado para asegurar que la operación (que consulta múltiples tablas)
     * se ejecute de forma atómica y consistente. Los resultados se han ordenado ascendentemente por fecha de recogida.
     *
     * @return Un objeto LiveData que contiene la lista de {@code ReservaConQuads}.
     */
    @Transaction
    @Query("SELECT * FROM reserva_table ORDER BY fechaRecogida ASC")
    LiveData<List<ReservaConQuads>> getReservasConQuads();

    /**
     * Obtiene las reservas futuras (fecha de recogida > hoy).
     */
    @Transaction
    @Query("SELECT * FROM reserva_table WHERE fechaRecogida > :fechaActual ORDER BY fechaRecogida ASC")
    LiveData<List<ReservaConQuads>> getReservasFuturas(String fechaActual);

    /**
     * Obtiene las reservas activas (hoy entre recogida y devolución).
     */
    @Transaction
    @Query("SELECT * FROM reserva_table WHERE :fechaActual BETWEEN fechaRecogida AND fechaDevolucion ORDER BY fechaRecogida ASC")
    LiveData<List<ReservaConQuads>> getReservasActivas(String fechaActual);

    /**
     * Obtiene las reservas pasadas (fecha de devolución < hoy).
     */
    @Transaction
    @Query("SELECT * FROM reserva_table WHERE fechaDevolucion < :fechaActual ORDER BY fechaRecogida DESC")
    LiveData<List<ReservaConQuads>> getReservasPasadas(String fechaActual);

    /**
     * Se han obtenido las reservas que se solapan con un rango de fechas dado para un quad específico.
     * Esta consulta ha sido diseñada para verificar la disponibilidad de un quad antes de crear o modificar una reserva.
     * Se ha utilizado una unión (INNER JOIN) con la tabla de relación para filtrar por quad.
     * La lógica de solapamiento verifica que NO se cumplan las condiciones de no-solapamiento:
     * - La fecha de devolución de la reserva existente es anterior al inicio del rango solicitado, O
     * - La fecha de recogida de la reserva existente es posterior al fin del rango solicitado.
     *
     * @param matricula La matrícula del quad que se ha de verificar.
     * @param fechaInicio La fecha de inicio del rango solicitado (formato YYYY-MM-DD).
     * @param fechaFin La fecha de fin del rango solicitado (formato YYYY-MM-DD).
     * @param reservaId El ID de la reserva actual (para excluirla en modo edición, usar -1 en modo creación).
     * @return Lista de reservas que se solapan con el rango de fechas especificado.
     */
    @Query("SELECT r.* FROM reserva_table r " +
           "INNER JOIN relacion_reserva_quad_table rq ON r.id = rq.reservaId " +
           "WHERE rq.quadId = :matricula " +
           "AND r.id != :reservaId " +
           "AND NOT (r.fechaDevolucion < :fechaInicio OR r.fechaRecogida > :fechaFin)")
    List<Reserva> getReservasSolapadas(String matricula, String fechaInicio, String fechaFin, int reservaId);
}