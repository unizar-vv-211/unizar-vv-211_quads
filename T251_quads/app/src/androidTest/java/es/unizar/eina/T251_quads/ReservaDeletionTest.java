package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaRepository;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

@RunWith(AndroidJUnit4.class)
public class ReservaDeletionTest {

    @Rule
    public ActivityScenarioRule<QuadListActivity> scenarioRule =
            new ActivityScenarioRule<>(QuadListActivity.class);

    private QuadRepository quadRepository;
    private ReservaRepository reservaRepository;

    @Before
    public void setUp() throws InterruptedException {
        quadRepository = new QuadRepository(ApplicationProvider.getApplicationContext());
        reservaRepository = new ReservaRepository(ApplicationProvider.getApplicationContext());

        // Limpiar respetando el orden de Foreign Key
        reservaRepository.deleteAll();
        Thread.sleep(300);
        quadRepository.deleteAll();
        Thread.sleep(300);

        // Insertar quad base para las pruebas
        Quad quad = new Quad("BASEDEL1", "Biplaza", 80.0f, "Quad base para tests de borrado");
        quadRepository.insert(quad);
    }

    // Prueba de borrado de una reserva existente
    @Test
    public void testDeleteExistingReserva() {
        // Insertar reserva y recuperar el ID generado
        Reserva reserva = new Reserva("Carlos Ruiz", "698765432",
                "10-04-2026", "15-04-2026", 2, 400.0);
        long idGenerado = reservaRepository.insert(reserva, Arrays.asList("BASEDEL1"));

        // Asignar el ID al objeto para que Room pueda localizarla
        reserva.setId((int) idGenerado);

        int filasAfectadas = reservaRepository.delete(reserva);
        assertEquals("El borrado de una reserva existente debería afectar 1 fila", 1, filasAfectadas);
    }

    @After
    public void tearDown() throws InterruptedException {
        reservaRepository.deleteAll();
        Thread.sleep(300);
        quadRepository.deleteAll();
        Thread.sleep(300);
    }
}
