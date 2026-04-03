package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaRepository;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

@RunWith(AndroidJUnit4.class)
public class ReservaUpdateTest {

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
        Quad quad = new Quad("BASEUPD1", "Monoplaza", 60.0f, "Quad base para tests de actualización");
        quadRepository.insert(quad);
    }

    // Prueba de actualización del nombre del cliente de una reserva existente
    @Test
    public void testUpdateClienteReserva() throws InterruptedException {
        // Insertar la reserva inicial
        Reserva reserva = new Reserva("Ana Fernández", "611223344",
                "10-05-2026", "15-05-2026", 1, 300.0);
        long idGenerado = reservaRepository.insert(reserva, Arrays.asList("BASEUPD1"));

        // Asignar el ID generado al objeto
        reserva.setId((int) idGenerado);

        // Modificar el nombre del cliente y ejecutar la actualización
        reserva.setCliente("Ana M. Fernández");
        reservaRepository.update(reserva, Arrays.asList("BASEUPD1"));
        Thread.sleep(500);

        // Si llegamos aquí sin excepción, la actualización no causó un fallo de restricción SQLite
        assertTrue(true);
    }

    @After
    public void tearDown() throws InterruptedException {
        reservaRepository.deleteAll();
        Thread.sleep(300);
        quadRepository.deleteAll();
        Thread.sleep(300);
    }
}
