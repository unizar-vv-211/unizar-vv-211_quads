package es.unizar.eina.T251_quads.system;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;
import static es.unizar.eina.T251_quads.system.SystemTestHelpers.*;

@RunWith(AndroidJUnit4.class)
public class ReservaSystemPathsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    /**
     * Camino 4: Ciclo de Vida Completo de Reserva
     * Secuencia: T11 -> T18 -> T20 -> T18 -> T21 -> T19 -> T20 -> T19 -> T21 -> T13
     * -> T12 -> T11 -> T12 -> T1
     */
    @Test
    public void camino4CicloDeVidaCompletoReservaTest() {
        // Asegurar Quad disponible
        irAQuads();
        pulsarCrear();
        rellenarYGuardarQuad("RES1111", "Biplaza", "50", "Quad testing reservas");
        volverAtras();

        irAReservas(); // T11: Navegar a Reservas

        pulsarCrear(); // T18: Abrir formulario creación
        cancelarFormulario(); // T20: Cancelar (Vuelta a lista)

        pulsarCrear(); // T18: Abrir formulario creación de nuevo
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        } // Pausa UI
        rellenarYGuardarReserva("Raul Samper", "600123456", "15-05-2030", "18-05-2030", "1", "RES1111"); // T21: Guardar
                                                                                                         // Reserva

        pulsarEditarReservaEnLista(0); // T19: Abrir formulario modificación
        cancelarFormulario(); // T20: Cancelar (Vuelta a lista)

        pulsarEditarReservaEnLista(0); // T19: Abrir formulario modificación de nuevo
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Raul y Samuel", "600123456", "15-05-2030", "22-05-2030", "2", null); // T21: Guardar
                                                                                                      // cambios

        pulsarEliminarEnLista(0); // T13: Eliminar la reserva
        volverAtras(); // T12: Volver al menú principal

        irAReservas(); // T11: Volver a entrar a Reservas
        volverAtras(); // T12: Volver al menú principal
        irAQuads(); // T1: Entrar a Quads
    }

    /**
     * Camino 5: Gestión de Filtros de Reservas
     * Secuencia base: T11 -> T14 -> T15 -> T16 -> T17 -> T14 -> T13 -> T15 -> T18
     * -> T21 -> T12
     */
    @Test
    public void camino5GestionDeFiltrosReservasTest() {
        // Asegurar Quad disponible
        irAQuads();
        pulsarCrear();
        rellenarYGuardarQuad("RES2222", "Biplaza", "50", "Quad testing filtros");
        volverAtras();

        irAReservas(); // T11: Navegar a Reservas

        seleccionarFiltroReservas(R.string.menu_filtro_pasadas); // T14: Filtro Pasadas
        seleccionarFiltroReservas(R.string.menu_filtro_actuales); // T15: Filtro Actuales
        seleccionarFiltroReservas(R.string.menu_filtro_futuras); // T16: Filtro Futuras
        seleccionarFiltroReservas(R.string.menu_filtro_todas); // T17: Filtro Todas
        seleccionarFiltroReservas(R.string.menu_filtro_pasadas); // T14: Filtro Pasadas

        // (T17 -> T18 -> T21): Preparamos datos para eliminar en pasadas
        seleccionarFiltroReservas(R.string.menu_filtro_todas);
        pulsarCrear();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Cliente Eliminar", "111222333", "01-01-2025", "05-01-2025", "0", "RES2222");

        seleccionarFiltroReservas(R.string.menu_filtro_pasadas); // T14: Volvemos a Filtro Pasadas
        pulsarEliminarEnLista(0); // T13: Eliminar reserva en vista filtrada

        seleccionarFiltroReservas(R.string.menu_filtro_actuales); // T15: Filtro Actuales

        pulsarCrear(); // T18: Abrir formulario creación
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Serrano", "987654321", "13-05-2031", "14-05-2031", "1", "RES2222"); // T21: Guardar
                                                                                                     // Reserva

        volverAtras(); // T12: Volver al menú principal
    }
}