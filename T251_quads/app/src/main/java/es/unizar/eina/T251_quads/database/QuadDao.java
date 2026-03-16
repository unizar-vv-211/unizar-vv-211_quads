package es.unizar.eina.T251_quads.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object (DAO) para la entidad Quad.
 * Se ha definido esta interfaz para especificar los métodos de acceso a los datos
 * de la tabla "quads".
 * La implementación de los métodos es generada automáticamente por la librería Room.
 */
@Dao
public interface QuadDao {

    /**
     * Inserta una nueva entidad Quad en la base de datos.
     * Se ha definido la estrategia {@code OnConflictStrategy.IGNORE} para los casos
     * en que la clave primaria (matrícula) ya exista en la tabla.
     *
     * @param quad La entidad Quad que se ha de insertar.
     * @return El identificador de la fila insertada en formato long.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Quad quad);

    /**
     * Actualiza una entidad Quad existente en la base de datos.
     *
     * @param quad La entidad Quad con los datos que se han de actualizar.
     * @return El número de filas que han sido modificadas.
     */
    @Update
    int update(Quad quad);

    /**
     * Elimina una entidad Quad de la base de datos.
     *
     * @param quad La entidad Quad que se ha de eliminar.
     * @return El número de filas que han sido eliminadas.
     */
    @Delete
    int delete(Quad quad);

    /**
     * Elimina todos los Quads de la tabla.
     * Este metodo se ha diseñado para ser utilizado en tareas de inicialización o pruebas.
     */
    @Query("DELETE FROM quads")
    void deleteAll();

    /**
     * Se han recuperado todos los Quads disponibles en la base de datos.
     * Los resultados se proporcionan como un objeto LiveData, lo que permite que
     * la interfaz de usuario pueda observar los cambios. Los quads se ordenan ascendentemente
     * por el campo 'matricula'.
     *
     * @return Un objeto LiveData que contiene la lista completa de Quads.
     */
    @Query("SELECT * FROM quads ORDER BY matricula ASC")
    LiveData<List<Quad>> getAllQuads();

    /**
     * Cuenta el número de reservas asociadas a un quad específico.
     * La consulta se ejecuta sobre la tabla de unión 'relacion_reserva_quad_table'.
     *
     * @param matricula La matrícula (ID) del quad que se ha de comprobar.
     * @return El número (int) de reservas que incluyen este quad.
     */
    @Query("SELECT COUNT(*) FROM relacion_reserva_quad_table WHERE quadId = :matricula")
    int countReservasForQuad(String matricula);

    /**
     * Cuenta el número total de quads en la tabla.
     * Útil para las pruebas instrumentadas.
     */
    @Query("SELECT COUNT(*) FROM quads")
    int getNumQuads();

    /**
     * Recupera un Quad específico por su matrícula de forma síncrona.
     */
    @Query("SELECT * FROM quads WHERE matricula = :matricula LIMIT 1")
    Quad getQuadByMatricula(String matricula);
}