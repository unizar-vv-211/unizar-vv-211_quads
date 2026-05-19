package es.unizar.eina.T251_quads.system;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.Espresso;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaRepository;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class Volumen {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void saturacionDeBaseDeDatosYMemoriaTest() {
        try {
            Application app = ApplicationProvider.getApplicationContext();
            QuadRepository quadRepo = new QuadRepository(app);
            ReservaRepository reservaRepo = new ReservaRepository(app);

            // Generar un string larguísimo (5000 caracteres) para agotar la RAM
            StringBuilder sb = new StringBuilder(5000);
            for (int i = 0; i < 5000; i++) {
                sb.append("A");
            }
            String textoLargo = sb.toString();

            CountDownLatch latch = new CountDownLatch(1);

            new Thread(() -> {
                try {
                    for (int i = 0; i < 200; i++) {
                        String matricula = String.format("VOL%04d", i);
                        String tipo = (i % 2 == 0) ? "Biplaza" : "Monoplaza";
                        Quad q = new Quad(matricula, tipo, Float.MAX_VALUE, textoLargo);
                        quadRepo.insert(q);
                    }

                    for (int i = 0; i < 20000; i++) {
                        String fechaR = "0001-01-01";
                        String fechaD = "9999-12-31";
                        String matriculaQuad = String.format("VOL%04d", i % 200);
                        Reserva r = new Reserva("Cliente " + i, "999999999", fechaR, fechaD, Integer.MAX_VALUE,
                                Double.MAX_VALUE);
                        reservaRepo.insert(r, Collections.singletonList(matriculaQuad));
                    }
                } finally {
                    latch.countDown();
                }
            }).start();

            latch.await(300, TimeUnit.SECONDS);

            // 1. Ir a Quads para que el RecyclerView intente renderizar 20000 Quads masivos
            // de golpe
            onView(withId(R.id.card_quads)).perform(click());

            // Comprobar que la vista sobrevive al renderizado
            onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

            // Volver al menú
            Espresso.pressBackUnconditionally();

            // 2. Ir a Reservas para que Room dispare una query brutal con múltiples Joins a
            // la vez
            onView(withId(R.id.card_reservas)).perform(click());
            onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

            org.junit.Assert
                    .fail("Fallo");
        } catch (Throwable t) {
            System.out.println("Éxito: (" + t.getClass().getSimpleName() + ")");
        }
    }

    @org.junit.After
    public void limpiarBaseDeDatos() {
        Application app = ApplicationProvider.getApplicationContext();
        QuadRepository quadRepo = new QuadRepository(app);
        ReservaRepository reservaRepo = new ReservaRepository(app);

        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            reservaRepo.deleteAll();
            quadRepo.deleteAll();
            latch.countDown();
        }).start();

        try {
            latch.await(15, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
