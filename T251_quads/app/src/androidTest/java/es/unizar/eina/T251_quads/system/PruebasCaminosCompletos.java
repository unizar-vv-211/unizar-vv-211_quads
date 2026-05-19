package es.unizar.eina.T251_quads.system;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;
import static es.unizar.eina.T251_quads.system.UtilidadesPruebas.*;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.pressBack;

@RunWith(AndroidJUnit4.class)
public class PruebasCaminosCompletos {

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

    // Motor lector del vector
    private void recorrerCamino(int[] camino) {
        for (int i = 0; i < camino.length; i++) {
            int numeroTransicion = camino[i];
            System.out.println(
                    ">>> EJECUTANDO PASO " + (i + 1) + "/" + camino.length + " -> TRANSICIÓN: T" + numeroTransicion);

            ejecutarTransicion(numeroTransicion);
        }
    }

    // Inyector de datos
    private void crearDatosIniciales() {
        irAQuads();
        for (int i = 0; i < 4; i++) {
            ejecutarTransicion(7); // T7: Pulsar botón "+" de Quads
            ejecutarTransicion(10); // T10: Rellenar y guardar
        }
        volverAtras();

        irAReservas();

        // 4 Reservas en el pasado
        for (int i = 0; i < 4; i++) {
            pulsarCrear();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            String nombre = "Pasado " + String.valueOf((char) ('A' + i));
            String fIda = String.format(java.util.Locale.getDefault(), "%02d-01-2010", (i * 5) + 1);
            String fVuelta = String.format(java.util.Locale.getDefault(), "%02d-01-2010", (i * 5) + 4);
            try {
                rellenarYGuardarReserva(nombre, "123456789", fIda, fVuelta, "1", "CPS0001");
            } catch (Exception e) {
                System.out.println("Fallo al guardar Reserva (pasado). Abortando inyección.");
                try {
                    onView(withId(android.R.id.content)).perform(closeSoftKeyboard());
                } catch (Exception ignored) {
                }
                pressBack();
            }
        }

        // 4 Reservas actuales
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault());
        for (int i = 0; i < 4; i++) {
            pulsarCrear();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            String nombre = "Actual " + String.valueOf((char) ('A' + i));
            long baseTime = System.currentTimeMillis() + (i * 10 * 86400000L); // 10 días de separación
            String fIda = sdf.format(new java.util.Date(baseTime));
            String fVuelta = sdf.format(new java.util.Date(baseTime + (3 * 86400000L))); // 3 días de duración
            try {
                rellenarYGuardarReserva(nombre, "123456789", fIda, fVuelta, "1", "CPS0002");
            } catch (Exception e) {
                System.out.println("Fallo al guardar Reserva (actual). Abortando inyección.");
                try {
                    onView(withId(android.R.id.content)).perform(closeSoftKeyboard());
                } catch (Exception ignored) {
                }
                pressBack();
            }
        }

        // 4 Reservas en el futuro
        for (int i = 0; i < 4; i++) {
            pulsarCrear();
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            String nombre = "Futuro " + String.valueOf((char) ('A' + i));
            String fIda = String.format(java.util.Locale.getDefault(), "%02d-01-2050", (i * 5) + 1);
            String fVuelta = String.format(java.util.Locale.getDefault(), "%02d-01-2050", (i * 5) + 4);
            try {
                rellenarYGuardarReserva(nombre, "123456789", fIda, fVuelta, "1", "CPS0003");
            } catch (Exception e) {
                System.out.println("Fallo al guardar Reserva (futuro). Abortando inyección.");
                try {
                    onView(withId(android.R.id.content)).perform(closeSoftKeyboard());
                } catch (Exception ignored) {
                }
                pressBack();
            }
        }

        volverAtras();

        // Sincronización: asegurar que la app ha vuelto al menú principal
        onView(withId(R.id.card_quads)).check(matches(isDisplayed()));
    }

    // MÓDULO QUADS

    @Test
    public void navegacionYAccionesBasicasQuadsTest() {
        crearDatosIniciales();
        int[] camino = { 1, 3, 2, 1, 4, 2, 1, 5, 2, 1, 6, 2, 1, 7, 9, 2, 1, 8, 10, 2 };
        recorrerCamino(camino);
    }

    @Test
    public void ordenacionYBorradoQuadsTest() {
        crearDatosIniciales();
        int[] camino = { 1, 4, 4, 5, 4, 6, 4, 3, 5, 5, 6, 5, 3, 6, 6, 3, 3, 4, 2 };
        recorrerCamino(camino);
    }

    @Test
    public void interaccionFormulariosYListaQuadsTest() {
        crearDatosIniciales();
        int[] camino = { 1, 7, 9, 4, 7, 10, 4, 8, 9, 5, 8, 10, 5, 3, 7, 10, 6, 8, 9, 6, 3, 8, 10, 3, 7, 9, 3, 2 };
        recorrerCamino(camino);
    }

    // MÓDULO RESERVAS

    @Test
    public void navegacionYAccionesBasicasReservasTest() {
        crearDatosIniciales();
        int[] camino = { 11, 13, 12, 11, 14, 12, 11, 15, 12, 11, 16, 12, 11, 17, 12, 11, 18, 20, 12, 11, 19, 21, 12 };
        recorrerCamino(camino);
    }

    @Test
    public void filtrosYBorradoReservasTest() {
        crearDatosIniciales();
        int[] camino = { 11, 14, 14, 15, 14, 16, 14, 17, 14, 13, 15, 15, 16, 15, 17, 15, 13, 16, 16, 17, 16, 13, 17, 17,
                13, 13, 14, 12 };
        recorrerCamino(camino);
    }

    @Test
    public void interaccionFormulariosYFiltrosReservasTest() {
        crearDatosIniciales();
        int[] camino = {
                11, 17, 15, 17, 16, 17, 17, 18, 20, 12,
                11, 18, 21, 12,
                11, 17, 19, 20, 13, 19, 21, 13,
                18, 20, 14, 18, 21, 14, 19, 20, 15,
                19, 21, 15, 18, 20, 16, 19, 21, 16,
                18, 20, 17, 19, 21, 17, 18, 20,
                18, 21, 18, 20, 19, 21, 19,
                20, 12
        };
        recorrerCamino(camino);
    }

    @Test
    public void navegacionCruzadaModulosTest() {
        int[] camino = { 1, 2, 1, 2, 11, 12, 11, 12, 1 };
        recorrerCamino(camino);
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

        // Reseteamos los contadores estáticos para la próxima prueba
        UtilidadesPruebas.generadorMatriculas = 1;
        UtilidadesPruebas.generadorClientes = 10;
        UtilidadesPruebas.ultimaTransicion = -1;
    }
}