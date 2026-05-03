package es.unizar.eina.T251_quads.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Clase abstracta que representa la base de datos principal de la aplicación.
 * Se han definido las entidades que componen el esquema de datos:
 * {@code Quad}, {@code Reserva}, y la tabla de unión
 * {@code RelacionReservaQuad} para la relación M:N.
 * Se ha implementado el patrón Singleton para garantizar una única instancia de la base de datos.
 */
@Database(entities = {Quad.class, Reserva.class, RelacionReservaQuad.class}, version = 4, exportSchema = false)
public abstract class QuadRoomDatabase extends RoomDatabase {

    /**
     * Se ha definido el método abstracto para obtener el Data Access Object (DAO) de la entidad Quad.
     *
     * @return El DAO para Quads.
     */
    public abstract QuadDao quadDao();

    /**
     * Se ha definido el método abstracto para obtener el Data Access Object (DAO) de la entidad Reserva.
     *
     * @return El DAO para Reservas.
     */
    public abstract ReservaDao reservaDao();

    /** Instancia única de la base de datos (patrón Singleton). */
    private static volatile QuadRoomDatabase INSTANCE;
    
    /** Número de hilos que se han asignado al ejecutor. */
    private static final int NUMBER_OF_THREADS = 4;

    /**
     * Servicio de ejecución que se ha utilizado para lanzar operaciones de escritura y
     * lectura en un hilo secundario, evitando el bloqueo del hilo principal de la UI.
     * Ha sido declarado público para ser utilizado por los repositorios y ViewModels.
     */
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Se ha devuelto la instancia única de la base de datos.
     * Si la instancia no existe, se ha procedido a crearla utilizando doble comprobación
     * de bloqueo (double-checked locking) para garantizar thread-safety.
     *
     * @param context El contexto de la aplicación, necesario para la construcción de la base de datos.
     * @return La instancia única de QuadRoomDatabase.
     */
    public static QuadRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (QuadRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    QuadRoomDatabase.class, "quad_database")
                            // Se ha definido la migración destructiva al cambiar la versión (v3)
                            .fallbackToDestructiveMigration()
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Callback que se ha definido para la base de datos.
     * Este objeto permite ejecutar código en el momento de la creación inicial de la base de datos.
     * Se ha utilizado para poblar la tabla Quads con datos de prueba.
     */
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        
        /**
         * Se ha invocado cuando la base de datos se ha creado por primera vez.
         * Se han insertado datos de prueba en la tabla de Quads.
         *
         * @param db La base de datos SQLite subyacente.
         */
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            databaseWriteExecutor.execute(() -> {
                // Se ha poblado la base de datos con Quads de prueba
                QuadDao quadDao = INSTANCE.quadDao();
                quadDao.deleteAll();

                Quad quad = new Quad("RSH0202", "Monoplaza", 1000, "Gasolina");
                quadDao.insert(quad);
                quad = new Quad("JMM0404", "Monoplaza", 1000 , "Gasolina");
                quadDao.insert(quad);
                quad = new Quad("XXX8989", "Biplaza", 450,"Eléctrico");
                quadDao.insert(quad);
            });
        }
    };
}