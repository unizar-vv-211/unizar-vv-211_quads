package es.unizar.eina.T251_quads.database;


import android.app.Application;
import android.util.Log;

import androidx.lifecycle.LiveData;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Repositorio de datos para la entidad Quad.
 * Esta clase se ha diseñado para abstraer la capa de acceso a datos del resto de la aplicación,
 * actuando como mediador entre los ViewModels y la fuente de datos (SQLite/Room a través del DAO).
 */
public class QuadRepository {

    /** Referencia al Data Access Object para la entidad Quad. */
    private final QuadDao mQuadDao;
    /** Lista de todos los Quads disponibles. */
    private final LiveData<List<Quad>> mAllQuads;

    /** Constante que define el tiempo máximo de espera para las operaciones. */
    private static final long TIMEOUT = 15000;

    /**
     * Constructor del repositorio de Quads
     *
     * @param application Contexto de la aplicación, necesario para la instanciación de la base de datos (Singleton).
     */
    public QuadRepository(Application application) {
        QuadRoomDatabase db = QuadRoomDatabase.getDatabase(application);
        mQuadDao = db.quadDao();
        mAllQuads = mQuadDao.getAllQuads();
    }

    /**
     * Se han recuperado todos los Quads disponibles en la base de datos.
     * La consulta ha sido ejecutada por Room en un hilo separado. El objeto LiveData
     * notifica automáticamente a sus observadores cuando los datos han sido modificados.
     *
     * @return Objeto LiveData que contiene la lista de todas las entidades Quad.
     */
    public LiveData<List<Quad>> getAllQuads() {
        return mAllQuads;
    }

    /**
     * Se ha insertado un nuevo Quad en la base de datos.
     * Esta operación se ha lanzado en un hilo de ejecución separado para no bloquear el hilo principal.
     * Se ha utilizado un objeto Future para sincronizar
     * y obtener el resultado de la inserción de forma síncrona.
     *
     * @param quad La entidad Quad que se tiene que insertar.
     * @return El Id de la fila creada si la inserción se ha realizado correctamente. En caso
     * de fallo o excepción se ha devuelto -1.
     */
    public long insert(Quad quad) {
        Future<Long> future = QuadRoomDatabase.databaseWriteExecutor.submit(
                () -> mQuadDao.insert(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Se ha actualizado un Quad existente en la base de datos.
     *
     * @param quad La entidad Quad que se tiene que actualizar. El atributo 'matricula' (clave primaria)
     * se ha utilizado para identificar el Quad que se desea modificar.
     * @return Un valor entero con el número de filas que han sido modificadas.
     */
    public int update(Quad quad) {
        Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(
                () -> mQuadDao.update(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }


    /**
     * Se ha eliminado un Quad de la base de datos.
     *
     * @param quad Objeto Quad que contiene la clave primaria (matricula) del quad que se va a eliminar.
     * @return Un valor entero con el número de filas que han sido eliminadas.
     */
    public int delete(Quad quad) {
        Future<Integer> future = QuadRoomDatabase.databaseWriteExecutor.submit(
                () -> mQuadDao.delete(quad));
        try {
            return future.get(TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException ex) {
            Log.d("QuadRepository", ex.getClass().getSimpleName() + ex.getMessage());
            return -1;
        }
    }

    /**
     * Se ha obtenido el número de reservas asociadas a un quad.
     * Este metodo se ha diseñado para ser invocado desde un hilo secundario
     * para dar soporte a operaciones de borrado condicional.
     *
     * @param matricula La matrícula del quad que se ha de comprobar.
     * @return El número de reservas que incluyen el quad, en formato entero.
     */
    public int getReservasCountForQuad(String matricula) {
        // La consulta es ejecutada directamente por el DAO
        return mQuadDao.countReservasForQuad(matricula);
    }
    
    /**
     * Elimina todos los Quads de la base de datos.
     * Útil para limpiar el sistema después de pruebas.
     */
    public void deleteAll() {
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            mQuadDao.deleteAll();
        });
    }
}