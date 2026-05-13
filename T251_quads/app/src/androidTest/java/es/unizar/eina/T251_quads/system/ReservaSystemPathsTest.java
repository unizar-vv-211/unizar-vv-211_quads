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

    @Test
    public void camino4CicloDeVidaCompletoReservaTest() {
        // Pre-condición: Crear un Quad específico para asegurar que hay stock para
        // reservar
        irAQuads();
        pulsarCrear();
        rellenarYGuardarQuad("RES1111", "Biplaza", "50", "Quad para testing de reservas");
        volverAtras();

        // T11 -> T18 -> T20 -> T18 -> T21 -> T19 -> T20 -> T19 -> T21 -> T13 -> T12 ->
        // T11 -> T12 -> T1
        irAReservas();

        pulsarCrear();
        cancelarFormulario();

        pulsarCrear();
        // Pasamos 1 casco y seleccionamos el quad RES1111
        rellenarYGuardarReserva("Raul Samper", "600123456", "15-05-2026", "18-05-2026", "1", "RES1111");

        pulsarEditarReservaEnLista(0);
        cancelarFormulario();

        pulsarEditarReservaEnLista(0);
        // Pasamos 2 cascos (máximo para Biplaza)
        rellenarYGuardarReserva("Raul y Samuel", "600123456", "15-05-2026", "22-05-2026", "2", "RES1111");

        pulsarEliminarEnLista(0);
        volverAtras();

        irAReservas();
        volverAtras();
        irAQuads();
    }

    @Test
    public void camino5GestionDeFiltrosReservasTest() {
        // Pre-condición: Crear un Quad para asegurar que hay stock
        irAQuads();
        pulsarCrear();
        rellenarYGuardarQuad("RES2222", "Biplaza", "50", "Quad para filtros");
        volverAtras();

        // T11 -> T14 -> T15 -> T16 -> T17 -> T14 -> T13 -> T15 -> T18 -> T21 -> T12
        irAReservas();

        seleccionarFiltroReservas(R.string.menu_filtro_pasadas);
        seleccionarFiltroReservas(R.string.menu_filtro_actuales);
        seleccionarFiltroReservas(R.string.menu_filtro_futuras);
        seleccionarFiltroReservas(R.string.menu_filtro_todas);
        seleccionarFiltroReservas(R.string.menu_filtro_pasadas);

        // Creamos una reserva antigua para que el filtro "pasadas" tenga algo que
        // eliminar
        seleccionarFiltroReservas(R.string.menu_filtro_todas);
        pulsarCrear();
        rellenarYGuardarReserva("Cliente Eliminar", "111222333", "01-01-2025", "05-01-2025", "0", "RES2222");

        seleccionarFiltroReservas(R.string.menu_filtro_pasadas);
        pulsarEliminarEnLista(0);

        seleccionarFiltroReservas(R.string.menu_filtro_actuales);

        pulsarCrear();
        rellenarYGuardarReserva("Serrano", "987654321", "13-05-2026", "14-05-2026", "1", "RES2222");

        volverAtras();
    }
}