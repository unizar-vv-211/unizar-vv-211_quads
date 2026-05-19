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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

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

        // Limpiar 
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

        // Recuperar la reserva y verificar el cambio
        Reserva reservaRecuperada = reservaRepository.getReservaById((int) idGenerado);
        assertNotNull("La reserva debería existir tras actualizarla", reservaRecuperada);
        assertEquals("El cliente debería haberse actualizado",
                "Ana M. Fernández", reservaRecuperada.getCliente());
    }

    @Test
    public void testUpdateReservaConSolapeEsRechazada() throws InterruptedException {
        Reserva reserva1 = new Reserva("Cliente Uno", "611111111",
                "10-05-2026", "15-05-2026", 1, 300.0);
        long id1 = reservaRepository.insert(reserva1, Arrays.asList("BASEUPD1"));

        Reserva reserva2 = new Reserva("Cliente Dos", "622222222",
                "20-05-2026", "22-05-2026", 1, 180.0);
        long id2 = reservaRepository.insert(reserva2, Arrays.asList("BASEUPD1"));
        Thread.sleep(300);

        List<String> noDisponibles = reservaRepository.comprobarDisponibilidad(
                Arrays.asList("BASEUPD1"), "12-05-2026", "16-05-2026", (int) id2);

        assertTrue("La reserva base debería haberse creado", id1 > 0);
        assertTrue("La reserva que se intenta actualizar debería haberse creado", id2 > 0);
        assertFalse("La actualización con solape debería rechazarse",
                noDisponibles == null || noDisponibles.isEmpty());
        assertTrue("El quad debería aparecer como no disponible",
                noDisponibles.contains("BASEUPD1"));
    }

    @Test
    public void testUpdateReservaMantienePrecioOriginal() throws InterruptedException {
        double precioOriginal = 300.0;
        Reserva reserva = new Reserva("Ana Fernández", "611223344",
                "10-05-2026", "15-05-2026", 1, precioOriginal);
        long idGenerado = reservaRepository.insert(reserva, Arrays.asList("BASEUPD1"));

        reserva.setId((int) idGenerado);
        reserva.setCliente("Ana M. Fernández");
        reservaRepository.update(reserva, Arrays.asList("BASEUPD1"));
        Thread.sleep(500);

        Reserva reservaRecuperada = reservaRepository.getReservaById((int) idGenerado);
        assertNotNull("La reserva debería existir tras actualizarla", reservaRecuperada);
        assertEquals("El precio original de la reserva debería mantenerse",
                precioOriginal, reservaRecuperada.getPrecioTotal(), 0.01);
    }

    @After
    public void tearDown() throws InterruptedException {
        reservaRepository.deleteAll();
        Thread.sleep(300);
        quadRepository.deleteAll();
        Thread.sleep(300);
    }
}
