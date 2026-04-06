package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaRepository;
import es.unizar.eina.T251_quads.database.UnitTests;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

@RunWith(AndroidJUnit4.class)
public class ReservaCreationTest {

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
        Quad quad = new Quad("BASECR01", "Monoplaza", 55.0f, "Quad base para tests de reserva");
        quadRepository.insert(quad);
    }

    // Prueba de inserción válida de una reserva
    @Test
    public void testInsertValidReserva() {
        Reserva reserva = new Reserva("Juan García", "656123456",
                "01-03-2026", "05-03-2026", 1, 55.0);
        long id = reservaRepository.insert(reserva, Arrays.asList("BASECR01"));
        assertTrue("La inserción de una reserva válida debería devolver un id > 0", id > 0);
    }

    // Prueba de detección de solapamiento de fechas
    @Test
    public void testDetectOverlappingReserva() throws InterruptedException {
        // Insertar una primera reserva
        Reserva reserva1 = new Reserva("María López", "612345678",
                "01-01-2026", "05-01-2026", 1, 220.0);
        reservaRepository.insert(reserva1, Arrays.asList("BASECR01"));
        Thread.sleep(500);

        // Comprobar disponibilidad del mismo quad en las mismas fechas desde un hilo secundario
        AtomicReference<List<String>> resultado = new AtomicReference<>();
        Thread bgThread = new Thread(() -> {
            List<String> noDisponibles = reservaRepository.comprobarDisponibilidad(
                    Arrays.asList("BASECR01"), "01-01-2026", "05-01-2026", -1);
            resultado.set(noDisponibles);
        });
        bgThread.start();
        bgThread.join(5000);

        // La lista no debe estar vacía: el quad ya está reservado en esas fechas
        assertFalse("La lista de no disponibles no debería estar vacía",
                resultado.get() == null || resultado.get().isEmpty());
        assertTrue("La matrícula base debería aparecer como no disponible",
                resultado.get().contains("BASECR01"));
    }

    // Prueba de particiones de equivalencia para el nombre del cliente (PE-01 a PE-05)
    @Test
    public void testClienteValidationPartitions() {
        UnitTests helper = new UnitTests(quadRepository, reservaRepository);

        // PE-01: Válida
        String pe01 = "Juan Pérez";
        assertTrue("PE-01 debería ser válido", helper.validateCliente(pe01));
        assertEquals("PE-01 debería devolver Registro exitoso", "Registro exitoso.", helper.getClienteValidationError(pe01));

        // PE-02: Dígitos
        String pe02 = "Ana89";
        assertFalse("PE-02 debería ser inválido", helper.validateCliente(pe02));
        assertEquals("PE-02 debería devolver Formato no permitido", "Error: Formato no permitido.", helper.getClienteValidationError(pe02));

        // PE-03: Especiales
        String pe03 = "L@ura!";
        assertFalse("PE-03 debería ser inválido", helper.validateCliente(pe03));
        assertEquals("PE-03 debería devolver Formato no permitido", "Error: Formato no permitido.", helper.getClienteValidationError(pe03));

        // PE-04: Vacía
        String pe04 = "";
        assertFalse("PE-04 debería ser inválido", helper.validateCliente(pe04));
        assertEquals("PE-04 debería devolver Campo obligatorio", "Error: Campo obligatorio.", helper.getClienteValidationError(pe04));

        // PE-05: Longitud
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 256; i++) sb.append("A");
        String pe05 = sb.toString();
        assertFalse("PE-05 debería ser inválido", helper.validateCliente(pe05));
        assertEquals("PE-05 debería devolver Longitud excedida", "Error: Longitud excedida.", helper.getClienteValidationError(pe05));
    }

    @After
    public void tearDown() throws InterruptedException {
        reservaRepository.deleteAll();
        Thread.sleep(300);
        quadRepository.deleteAll();
        Thread.sleep(300);
    }
}
