package es.unizar.eina.T251_quads;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.core.app.ApplicationProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaRepository;
import es.unizar.eina.T251_quads.ui.ReservaListActivity;

@RunWith(AndroidJUnit4.class)
public class ReservaCreationTest {

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
    
    @Test
    public void testReservaConMismaFechaRecogidaYDevolucionEsValida() throws InterruptedException {
        // Se usa onView porque esta regla se valida en el formulario real de ReservaEdit,
        // no en ReservaRepository/DAO.
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ReservaListActivity.class);
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_cliente)).perform(replaceText("Laura Pérez"), closeSoftKeyboard());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600111222"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("10-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("10-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard());
            onView(withText(containsString("BASECR01"))).perform(scrollTo(), click());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());

            Thread.sleep(1000);
            onView(withText("Laura Pérez")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testReservaConFechaDevolucionAnteriorEsRechazada() throws InterruptedException {
        // Se usa onView porque el rechazo de fechas se produce al guardar desde ReservaEdit.
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ReservaListActivity.class);
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_cliente)).perform(replaceText("Laura Pérez"), closeSoftKeyboard());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600111222"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("11-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("10-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard());
            onView(withText(containsString("BASECR01"))).perform(scrollTo(), click());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());

            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testMonoplazaConUnCascoEsValida() throws InterruptedException {
        // Se usa onView porque el máximo de cascos por tipo de quad se comprueba en ReservaEdit.
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ReservaListActivity.class);
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_cliente)).perform(replaceText("Pedro Ruiz"), closeSoftKeyboard());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600222333"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("12-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("12-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard());
            onView(withText(containsString("BASECR01"))).perform(scrollTo(), click());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());

            Thread.sleep(1000);
            onView(withText("Pedro Ruiz")).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testMonoplazaConMasCascosDeLosPermitidosEsRechazada() throws InterruptedException {
        // Se usa onView porque ReservaRepository no rechaza cascos inválidos; esa validación es de UI.
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ReservaListActivity.class);
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_cliente)).perform(replaceText("Cliente Cascos"), closeSoftKeyboard());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600555666"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("13-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("14-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("2"), closeSoftKeyboard());
            onView(withText(containsString("BASECR01"))).perform(scrollTo(), click());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());

            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testBiplazaConMasCascosDeLosPermitidosEsRechazada() throws InterruptedException {
        quadRepository.insert(new Quad("BASEBI01", "Biplaza", 75.0f, "Quad biplaza para test de cascos"));

        // Se usa onView porque el límite de cascos por biplaza solo se aplica al guardar el formulario.
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ReservaListActivity.class);
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_cliente)).perform(replaceText("Cliente Biplaza"), closeSoftKeyboard());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600777888"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("15-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("16-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("3"), closeSoftKeyboard());
            onView(withText(containsString("BASEBI01"))).perform(scrollTo(), click());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());

            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testPrecioTotalReservaNoCambiaAlModificarPrecioQuad() throws InterruptedException {
        Quad quad = new Quad("PREC001", "Monoplaza", 50.0f, "Quad precio congelado");
        quadRepository.insert(quad);

        double precioTotalOriginal = 50.0;
        Reserva reserva = new Reserva("Sergio Martín", "600333444",
                "10-05-2026", "10-05-2026", 1, precioTotalOriginal);
        long id = reservaRepository.insert(reserva, Arrays.asList("PREC001"));
        assertTrue("La reserva debería crearse correctamente", id > 0);

        quad.setPrecioDia(200.0f);
        quadRepository.update(quad);
        Thread.sleep(300);

        Reserva reservaRecuperada = reservaRepository.getReservaById((int) id);
        assertNotNull("La reserva debería existir", reservaRecuperada);
        assertEquals("El precio total de la reserva debería mantenerse",
                precioTotalOriginal, reservaRecuperada.getPrecioTotal(), 0.01);
    }

    // Prueba de detección de solapamiento de fechas
    @Test
    public void testDetectOverlappingReserva() throws InterruptedException {
        // Insertar una primera reserva
        Reserva reserva1 = new Reserva("María López", "612345678",
                "01-01-2026", "05-01-2026", 1, 220.0);
        reservaRepository.insert(reserva1, Arrays.asList("BASECR01"));
        Thread.sleep(500);

        // Comprobar disponibilidad del mismo quad en las mismas fechas 
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

    // Prueba de particiones de equivalencia para el nombre del cliente 
    @Test
    public void testClienteValidationPartitions() throws InterruptedException {
        // Se usa onView porque las particiones de nombre de cliente se validan en ReservaEdit
        // antes de crear la reserva; el repositorio no aplica estas reglas de formulario.
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), ReservaListActivity.class);
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(intent)) {
            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_cliente)).perform(replaceText("Juan Pérez"), closeSoftKeyboard());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600444555"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("14-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("14-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard());
            onView(withText(containsString("BASECR01"))).perform(scrollTo(), click());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());

            Thread.sleep(1000);
            onView(withText("Juan Pérez")).check(matches(isDisplayed()));

            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600444555"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("15-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("15-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard());
            onView(withText(containsString("BASECR01"))).perform(scrollTo(), click());

            onView(withId(R.id.edit_cliente)).perform(replaceText("Juan Perez"), closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(1000);
            onView(withText("Juan Perez")).check(matches(isDisplayed()));

            onView(withId(R.id.fab)).perform(click());
            onView(withId(R.id.edit_telefono)).perform(replaceText("600444555"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_recogida)).perform(replaceText("16-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText("16-05-2026"), closeSoftKeyboard());
            onView(withId(R.id.edit_cascos)).perform(scrollTo(), replaceText("1"), closeSoftKeyboard());
            onView(withText(containsString("BASECR01"))).perform(scrollTo(), click());

            onView(withId(R.id.edit_cliente)).perform(replaceText("Ana89"), closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));

            onView(withId(R.id.edit_cliente)).perform(replaceText(""), closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 256; i++) {
                sb.append("A");
            }
            onView(withId(R.id.edit_cliente)).perform(replaceText(sb.toString()), closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));

            onView(withId(R.id.edit_cliente)).perform(replaceText("Juan\nPerez"), closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));

            onView(withId(R.id.edit_cliente)).perform(replaceText("Juan\rPerez"), closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));

            onView(withId(R.id.edit_cliente)).perform(
                    replaceText("Juan" + new String(Character.toChars(0x0001)) + "Perez"),
                    closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));

            onView(withId(R.id.edit_cliente)).perform(
                    replaceText("Juan" + new String(Character.toChars(0x007F)) + "Perez"),
                    closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));

            onView(withId(R.id.edit_cliente)).perform(
                    replaceText("Juan" + new String(Character.toChars(0x00A0)) + "Perez"),
                    closeSoftKeyboard());
            onView(withId(R.id.button_save)).perform(scrollTo(), click());
            Thread.sleep(500);
            onView(withId(R.id.edit_cliente)).perform(scrollTo()).check(matches(isDisplayed()));
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        reservaRepository.deleteAll();
        Thread.sleep(300);
        quadRepository.deleteAll();
        Thread.sleep(300);
    }
}
