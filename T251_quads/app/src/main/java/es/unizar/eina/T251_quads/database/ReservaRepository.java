package es.unizar.eina.T251_quads.database;

import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Repositorio de datos para la entidad Reserva.
 * Esta clase se ha diseñado para abstraer la capa de acceso a datos del ViewModel.
 * Es el encargado de implementar la lógica transaccional de la relación Muchos-a-Muchos (M:N)
 * y de proporcionar métodos de validación de disponibilidad de quads.
 */
public class ReservaRepository {

    /** Referencia al Data Access Object para la entidad Reserva. */
    private ReservaDao mReservaDao;
    
    /** Lista observable de todas las Reservas con sus Quads asociados. */
    private LiveData<List<ReservaConQuads>> mAllReservasConQuads;

    /** Constante que define el tiempo máximo de espera para las operaciones. */
    private static final long TIMEOUT = 15000;

    /**
     * Constructor del repositorio de Reservas.
     * Se ha instanciado la base de datos de Room y se ha obtenido una referencia al ReservaDao.
     * Los datos observables de las reservas con sus quads han sido recuperados.
     *
     * @param application Contexto de la aplicación, esencial para obtener la instancia única de la base de datos.
     */
    public ReservaRepository(Application application) {
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(application);
        mReservaDao = db.reservaDao();
        mAllReservasConQuads = mReservaDao.getReservasConQuads();
    }

    /**
     * Se ha recuperado la lista observable de todas las reservas, incluyendo la lista de quads
     * asociados para cada una de ellas.
     *
     * @return {@code LiveData} que contiene la lista de {@code ReservaConQuads}.
     */
    public LiveData<List<ReservaConQuads>> getAllReservasConQuads() {
        return mAllReservasConQuads;
    }

    /**
     * Devuelve las reservas futuras (recogida > actual).
     */
    public LiveData<List<ReservaConQuads>> getReservasFuturas(String fechaActual) {
        return mReservaDao.getReservasFuturas(fechaActual);
    }

    /**
     * Devuelve las reservas activas (actual entre recogida y devolución).
     */
    public LiveData<List<ReservaConQuads>> getReservasActivas(String fechaActual) {
        return mReservaDao.getReservasActivas(fechaActual);
    }

    /**
     * Devuelve las reservas pasadas (devolución < actual).
     */
    public LiveData<List<ReservaConQuads>> getReservasPasadas(String fechaActual) {
        return mReservaDao.getReservasPasadas(fechaActual);
    }

    /**
     * Se ha insertado una nueva reserva y se ha gestionado la creación de sus relaciones M:N.
     * La operación se ha lanzado en un hilo de ejecución separado y devuelve el ID de la reserva creada.
     * Se ha utilizado un objeto Future para sincronizar y obtener el resultado de forma síncrona.
     *
     * @param reserva La entidad Reserva que se ha de insertar.
     * @param quadIds La lista de matrículas (String) de los quads que se han de asociar a la nueva reserva.
     * @return El ID de la reserva insertada si la operación se ha realizado correctamente. En caso
     * de fallo o excepción se ha devuelto -1.
     */
    public long insert(Reserva reserva, List<String> quadIds) {
        Future<Long> future = QuadRoomDatabase.databaseWriteExecutor.submit(() -> {
            long newReservaId = mReservaDao.insert(reserva);
            if (quadIds != null && !quadIds.isEmpty()) {
                for (String quadId : quadIds) {
                    RelacionReservaQuad relacion = new RelacionReservaQuad((int) newReservaId, quadId);
                    mReservaDao.insertRelacion(relacion);
                }
            }
            return newReservaId;
        });
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Se ha actualizado una reserva existente y se ha modificado su lista de quads asociados.
     * La operación se ha lanzado en un hilo de ejecución separado.
     * La actualización se ha realizado en tres pasos:
     * 1) actualizar los datos de la {@code Reserva},
     * 2) borrar todas las relaciones antiguas asociadas a su ID, y
     * 3) insertar la nueva lista de relaciones.
     *
     * @param reserva La entidad Reserva con los datos actualizados.
     * @param quadIds La *nueva* lista de matrículas de quads que se han de asociar a la reserva.
     */
    public void update(Reserva reserva, List<String> quadIds) {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReservaDao.update(reserva);
            mReservaDao.deleteRelacionesDeReserva(reserva.getId());
            if (quadIds != null && !quadIds.isEmpty()) {
                for (String quadId : quadIds) {
                    RelacionReservaQuad relacion = new RelacionReservaQuad(reserva.getId(), quadId);
                    mReservaDao.insertRelacion(relacion);
                }
            }
        });
    }

    /**
     * Se ha eliminado una reserva de la base de datos.
     * Gracias a la configuración {@code onDelete = ForeignKey.CASCADE} definida en la entidad
     * {@code RelacionReservaQuad}, la base de datos se ha encargado de eliminar automáticamente
     * todas las filas de la tabla de unión que estaban asociadas a esta reserva.
     * La operación se ha lanzado en un hilo de ejecución separado y devuelve el número de filas eliminadas.
     *
     * @param reserva La entidad Reserva que se ha de eliminar.
     * @return Un valor entero con el número de filas que han sido eliminadas. En caso de error devuelve -1.
     */
    public int delete(Reserva reserva) {
        Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(
                () -> mReservaDao.delete(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Se ha comprobado la disponibilidad de una lista de quads en un rango de fechas específico.
     * Este método ejecuta de forma síncrona (debe ser llamado desde un thread en background).
     * Para cada quad en la lista, se ha consultado la base de datos para verificar si existen
     * reservas que se solapen con el rango de fechas proporcionado.
     *
     * @param quadIds Lista de matrículas de los quads que se han de verificar.
     * @param fechaInicio Fecha de inicio del rango (formato YYYY-MM-DD).
     * @param fechaFin Fecha de fin del rango (formato YYYY-MM-DD).
     * @param reservaId ID de la reserva actual (para excluirla en modo edición, usar -1 en modo creación).
     * @return Lista de matrículas de quads que NO están disponibles (tienen reservas solapadas).
     */
    public List<String> comprobarDisponibilidad(List<String> quadIds, String fechaInicio, String fechaFin, int reservaId) {
        List<String> noDisponibles = new ArrayList<>();
        for (String matricula : quadIds) {
            List<Reserva> reservasSolapadas = mReservaDao.getReservasSolapadas(matricula, fechaInicio, fechaFin, reservaId);
            if (reservasSolapadas != null && !reservasSolapadas.isEmpty()) {
                noDisponibles.add(matricula);
            }
        }
        return noDisponibles;
    }

    /**
     * Se ha insertado una nueva reserva sin asociar quads (método auxiliar para pruebas).
     * La operación se ha lanzado en un hilo de ejecución separado y devuelve el ID de la reserva creada.
     *
     * @param reserva La entidad Reserva que se ha de insertar.
     * @return El ID de la reserva insertada si la operación se ha realizado correctamente. En caso
     * de fallo o excepción se ha devuelto -1.
     */
    public long insertReservaSinQuads(Reserva reserva) {
        Future<Long> future = QuadRoomDatabase.databaseWriteExecutor.submit(
                () -> mReservaDao.insert(reserva));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("ReservaRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }
    
    /**
     * Elimina todas las Reservas de la base de datos.
     * Útil para limpiar el sistema después de pruebas.
     */
    public void deleteAll() {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mReservaDao.deleteAll();
        });
    }
}