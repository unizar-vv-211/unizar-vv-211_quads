package es.unizar.eina.T251_quads.ui;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaConQuads;
import es.unizar.eina.T251_quads.database.ReservaRepository;

/**
 * ViewModel para la entidad Reserva.
 * Se ha diseñado esta clase para actuar como puente entre la {@code ReservaListActivity}
 * y el {@code ReservaRepository}. Es el encargado de exponer los datos de las reservas
 * con sus quads asociados (relación M:N) a la interfaz de usuario mediante {@code LiveData}
 * y de gestionar las peticiones de creación, modificación, eliminación y validación de disponibilidad.
 */
public class ReservaViewModel extends AndroidViewModel {

    /** Referencia al Repositorio de Reservas, donde se gestiona la lógica de datos. */
    private ReservaRepository mRepository;
    
    /**
     * {@code LiveData} que contiene la lista observable de todas las reservas,
     * incluyendo la lista de Quads asociados a cada una (mediante el POJO {@code ReservaConQuads}).
     */
    private final LiveData<List<ReservaConQuads>> mAllReservasConQuads;

    /**
     * Constructor del ViewModel.
     * Se ha inicializado el repositorio y se ha recuperado la lista observable de reservas.
     *
     * @param application El contexto de la aplicación.
     */
    public ReservaViewModel(Application application) {
        super(application);
        mRepository = new ReservaRepository(application);
        mAllReservasConQuads = mRepository.getAllReservasConQuads();
    }

    /**
     * Se ha recuperado la lista observable de todas las reservas con sus quads.
     *
     * @return {@code LiveData} que contiene la lista de entidades {@code ReservaConQuads}.
     */
    LiveData<List<ReservaConQuads>> getAllReservasConQuads() {
        return mAllReservasConQuads;
    }

    /**
     * Se ha delegado la inserción de una nueva reserva y la creación de sus relaciones M:N al repositorio.
     *
     * @param reserva La entidad Reserva que se ha de insertar.
     * @param quadIds La lista de matrículas de quads que se han de asociar a la nueva reserva.
     */
    public void insert(Reserva reserva, List<String> quadIds) {
        mRepository.insert(reserva, quadIds);
    }

    /**
     * Se ha delegado la actualización de una reserva existente y la modificación de sus relaciones al repositorio.
     *
     * @param reserva La entidad Reserva con los datos que se han de actualizar.
     * @param quadIds La *nueva* lista de matrículas de quads que se han de asociar.
     */
    public void update(Reserva reserva, List<String> quadIds) {
        mRepository.update(reserva, quadIds);
    }

    /**
     * Se ha delegado la eliminación de una reserva al repositorio.
     *
     * @param reserva La entidad Reserva que se ha de eliminar.
     */
    public void delete(Reserva reserva) {
        mRepository.delete(reserva);
    }

    /**
     * Se ha comprobado la disponibilidad de una lista de quads en un rango de fechas específico.
     * Este método delega la operación al repositorio, que ejecuta la consulta de forma síncrona
     * en el thread desde el que se invoca (debe ser llamado desde un background thread).
     *
     * @param quadIds Lista de matrículas de quads que se han de verificar.
     * @param fechaInicio Fecha de inicio del rango (formato YYYY-MM-DD).
     * @param fechaFin Fecha de fin del rango (formato YYYY-MM-DD).
     * @param reservaId ID de la reserva actual (para excluirla en modo edición, usar-1 en modo creación).
     * @return Lista de matrículas de quads que NO están disponibles en ese rango de fechas.
     */
    public List<String> comprobarDisponibilidad(List<String> quadIds, String fechaInicio, String fechaFin, int reservaId) {
        return mRepository.comprobarDisponibilidad(quadIds, fechaInicio, fechaFin, reservaId);
    }
    
    /**
     * Elimina todas las Reservas de la base de datos.
     */
    public void deleteAll() {
        mRepository.deleteAll();
    }
}