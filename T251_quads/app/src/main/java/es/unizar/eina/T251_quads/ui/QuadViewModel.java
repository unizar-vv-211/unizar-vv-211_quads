package es.unizar.eina.T251_quads.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.List;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.QuadRoomDatabase;

/**
 * ViewModel para la entidad Quad.
 * Se ha diseñado esta clase para actuar como puente entre el {@code QuadListActivity}
 * y el {@code QuadRepository}. Es el encargado de exponer los datos de los Quads
 * a la interfaz de usuario mediante {@code LiveData} y de gestionar la lógica
 * del negocio, como el borrado condicional.
 */
public class QuadViewModel extends AndroidViewModel {

    /** Referencia al Repositorio de Quads, donde se gestiona el acceso a la base de datos. */
    private QuadRepository mRepository;
    /** {@code LiveData} que contiene la lista observable de todos los Quads. */
    private final LiveData<List<Quad>> mAllQuads;

    /**
     * {@code LiveData} utilizado para enviar eventos de notificaciones (mensajes Toast)
     * a la Activity, especialmente útil para notificar fallos en operaciones asíncronas.
     */
    private final MutableLiveData<String> mToastEvent = new MutableLiveData<>();

    /**
     * Constructor del ViewModel.
     * Se ha inicializado el repositorio y se ha recuperado la lista observable de Quads.
     *
     * @param application El contexto de la aplicación.
     */
    public QuadViewModel(Application application) {
        super(application);
        mRepository = new QuadRepository(application);
        mAllQuads = mRepository.getAllQuads();
    }

    /**
     * Se ha recuperado la lista observable de todos los Quads.
     *
     * @return {@code LiveData} que contiene la lista de entidades {@code Quad}.
     */
    LiveData<List<Quad>> getAllQuads() {
        return mAllQuads;
    }

    /**
     * Se ha recuperado el {@code LiveData} de eventos de mensaje.
     * Este LiveData ha sido utilizado por la Activity para observar mensajes de error
     * o notificaciones asíncronas.
     *
     * @return {@code LiveData} que emite mensajes de texto.
     */
    public LiveData<String> getToastEvent() {
        return mToastEvent;
    }

    /**
     * Se ha delegado la inserción de un nuevo Quad al repositorio.
     *
     * @param quad La entidad Quad que se ha de insertar.
     */
    public void insert(Quad quad) {
        mRepository.insert(quad);
    }

    /**
     * Se ha delegado la actualización de un Quad al repositorio.
     *
     * @param quad La entidad Quad con los datos que se han de actualizar.
     */
    public void update(Quad quad) {
        mRepository.update(quad);
    }

    /**
     * Se ha gestionado la lógica de borrado de un Quad.
     * Se ha implementado una regla de negocio que comprueba si el Quad está en uso
     * por alguna reserva antes de ejecutar la eliminación.
     *
     * @param quad La entidad Quad que se ha de eliminar.
     */
    public void delete(Quad quad) {
        // Se ha lanzado la comprobación y el borrado en el hilo del ejecutor de la base de datos.
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            // 1. Se ha comprobado si el quad tiene reservas asociadas.
            int count = mRepository.getReservasCountForQuad(quad.getMatricula());

            if (count == 0) {
                // 2a. Si el conteo es cero, se ha procedido a borrarlo.
                mRepository.delete(quad);
            } else {
                // 2b. Si el conteo es mayor que cero, se ha denegado el borrado
                // y se ha notificado a la interfaz de usuario mediante un evento.
                String msg = "No se puede borrar. Quad " + quad.getMatricula() + " está en " + count + " reserva(s).";
                mToastEvent.postValue(msg);
            }
        });
    }
    
    /**
     * Elimina todos los Quads de la base de datos.
     */
    public void deleteAll() {
        mRepository.deleteAll();
    }
}