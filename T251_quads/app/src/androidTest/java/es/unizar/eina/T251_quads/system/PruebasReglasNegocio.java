package es.unizar.eina.T251_quads.system;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

import static es.unizar.eina.T251_quads.system.UtilidadesPruebas.*;

@RunWith(AndroidJUnit4.class)
public class PruebasReglasNegocio {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @org.junit.Before
    public void limpiarAntes() {
        android.app.Application app = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository quadRepo = new es.unizar.eina.T251_quads.database.QuadRepository(app);
        es.unizar.eina.T251_quads.database.ReservaRepository reservaRepo = new es.unizar.eina.T251_quads.database.ReservaRepository(app);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        new Thread(() -> {
            reservaRepo.deleteAll();
            quadRepo.deleteAll();
            latch.countDown();
        }).start();
        try { latch.await(5, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) {}
    }

    /**
     * PRUEBA 1: Mantenimiento de Precios (Precio Congelado)
     * Si el precio de un Quad cambia en el futuro, las reservas antiguas no deben
     * verse afectadas.
     */
    @Test
    public void mantenimientoPreciosTest() {
        // 1. Creamos un Quad a 50€
        irAQuads();
        pulsarCrear();
        rellenarYGuardarQuad("PRC1111", "Monoplaza", "50", "Quad de prueba de precio");
        volverAtras();

        // 2. Hacemos una reserva con ese Quad
        irAReservas();
        pulsarCrear();
        // Reserva de 3 días (Ej: 10 al 13 de mayo). 3 días x 50€ = 150€
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Cliente Precio", "123456789", "10-05-2030", "13-05-2030", "1", "PRC1111");
        volverAtras();

        // 3. El dueño sube el precio del Quad a 100€
        irAQuads();
        pulsarEditarQuadEnLista(0); // Editamos el PRC1111
        // Usamos null en tipo para dejar el que estaba, y cambiamos precio a 100
        rellenarYGuardarQuad("PRC1111", null, "100", "Precio subido");
        volverAtras();

        // 4. Verificamos que la reserva sigue mostrando el precio antiguo (calculado a
        // 50€)
        irAReservas();
        pulsarEditarReservaEnLista(0);
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        onView(withId(R.id.textViewPrecioFrozen)).check(matches(withText(containsString("150"))));

        cancelarFormulario();
    }

    /**
     * PRUEBA 2: Solape de fechas en Creación
     * Evitar que dos clientes reserven el mismo quad en los mismos días.
     */
    @Test
    public void bloqueoSolapeFechasCreacionTest() {
        // 1. Creamos un Quad
        irAQuads();
        pulsarCrear();
        rellenarYGuardarQuad("SOL2222", "Biplaza", "60", "Quad para solapes");
        volverAtras();

        // 2. Cliente A reserva del 10 al 20 de Mayo
        irAReservas();
        pulsarCrear();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Cliente A", "123456789", "10-05-2030", "20-05-2030", "2", "SOL2222");

        // 3. Cliente B intenta reservar del 15 al 25 de Mayo (Pisa las fechas del
        // Cliente A)
        pulsarCrear();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Cliente B", "123456789", "15-05-2030", "25-05-2030", "2", "SOL2222");
        onView(withId(R.id.button_save)).check(matches(isDisplayed()));

        cancelarFormulario();
    }

    /**
     * PRUEBA 3: Solape de fechas al Editar (Añadiendo un quad ocupado)
     */
    @Test
    public void bloqueoSolapeFechasEdicionTest() {
        // 1. Creamos DOS Quads
        irAQuads();
        pulsarCrear();
        rellenarYGuardarQuad("LIB3333", "Monoplaza", "40", "Quad Libre");
        pulsarCrear();
        rellenarYGuardarQuad("OCU4444", "Monoplaza", "40", "Quad Ocupado");
        volverAtras();

        irAReservas();

        // 2. Reserva 1: El Quad OCU4444 se reserva del 1 al 10 de Junio
        pulsarCrear();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Reserva Ocupada", "123456789", "01-06-2030", "10-06-2030", "1", "OCU4444");

        // 3. Reserva 2: El Quad LIB3333 se reserva en las mismas fechas (1 al 10 de
        // Junio)
        pulsarCrear();
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }
        rellenarYGuardarReserva("Reserva Libre", "123456789", "01-06-2030", "10-06-2030", "1", "LIB3333");

        // 4. Editamos la Reserva 2 e intentamos añadirle también el Quad OCU4444
        pulsarEditarReservaEnLista(0); // Abrimos la última reserva creada (la Libre)
        try {
            Thread.sleep(500);
        } catch (Exception e) {
        }

        // Intentamos marcar el quad ocupado (OCU4444) y guardar
        rellenarYGuardarReserva("Reserva Libre Modificada", "123456789", "01-06-2030", "10-06-2030", "1", "OCU4444");

        onView(withId(R.id.button_save)).check(matches(isDisplayed()));

        cancelarFormulario();
    }

    @org.junit.After
    public void limpiarTodoAlTerminar() {
        android.app.Application app = androidx.test.core.app.ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository quadRepo = new es.unizar.eina.T251_quads.database.QuadRepository(app);
        es.unizar.eina.T251_quads.database.ReservaRepository reservaRepo = new es.unizar.eina.T251_quads.database.ReservaRepository(app);
        java.util.concurrent.CountDownLatch latch = new java.util.concurrent.CountDownLatch(1);
        new Thread(() -> {
            reservaRepo.deleteAll();
            quadRepo.deleteAll();
            latch.countDown();
        }).start();
        try { latch.await(5, java.util.concurrent.TimeUnit.SECONDS); } catch (InterruptedException e) {}

        // Reseteamos los contadores estáticos
        UtilidadesPruebas.generadorMatriculas = 1;
        UtilidadesPruebas.generadorClientes = 10;
        UtilidadesPruebas.ultimaTransicion = -1;
    }
}