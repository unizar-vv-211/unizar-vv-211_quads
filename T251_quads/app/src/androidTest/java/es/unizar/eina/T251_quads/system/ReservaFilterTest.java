package es.unizar.eina.T251_quads.system;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaRepository;
import es.unizar.eina.T251_quads.ui.ReservaListActivity;

@RunWith(AndroidJUnit4.class)
public class ReservaFilterTest {

    private QuadRepository quadRepository;
    private ReservaRepository reservaRepository;

    @Before
    public void setUp() throws InterruptedException {
        Context context = ApplicationProvider.getApplicationContext();
        quadRepository = new QuadRepository((android.app.Application) context);
        reservaRepository = new ReservaRepository((android.app.Application) context);

        // Limpiar base de datos
        reservaRepository.deleteAll();
        Thread.sleep(300);
        quadRepository.deleteAll();
        Thread.sleep(300);

        // Insertar Quad base
        Quad quad = new Quad("1111FLT", "Monoplaza", 50.0f, "Quad Filtros");
        quadRepository.insert(quad);

        // Calcular fechas dinámicas basadas en HOY (Formato YYYY-MM-DD para la BD interna)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar cal = Calendar.getInstance();

        // 1. Partición Equivalencia: Reserva PASADA (Hace 10 días a hace 5 días)
        cal.add(Calendar.DAY_OF_YEAR, -10);
        String pasadoInicio = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 5);
        String pasadoFin = sdf.format(cal.getTime());
        Reserva resPasada = new Reserva("Cliente Pasado", "600111111", pasadoInicio, pasadoFin, 1, 100.0);

        // 2. Partición Equivalencia: Reserva ACTIVA (Hace 2 días a dentro de 2 días)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -2);
        String activaInicio = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 4);
        String activaFin = sdf.format(cal.getTime());
        Reserva resActiva = new Reserva("Cliente Activo", "600222222", activaInicio, activaFin, 1, 100.0);

        // 3. Partición Equivalencia: Reserva FUTURA (Dentro de 5 días a dentro de 10 días)
        cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, 5);
        String futuraInicio = sdf.format(cal.getTime());
        cal.add(Calendar.DAY_OF_YEAR, 5);
        String futuraFin = sdf.format(cal.getTime());
        Reserva resFutura = new Reserva("Cliente Futuro", "600333333", futuraInicio, futuraFin, 1, 100.0);

        // Inyectamos las reservas asociándolas al Quad
        reservaRepository.insert(resPasada, Arrays.asList("1111FLT"));
        reservaRepository.insert(resActiva, Arrays.asList("1111FLT"));
        reservaRepository.insert(resFutura, Arrays.asList("1111FLT"));
        Thread.sleep(500); // Esperar a que Room persista
    }

    @After
    public void tearDown() {
        reservaRepository.deleteAll();
        quadRepository.deleteAll();
    }

    @Test
    public void testParticiones_FiltrosDeVigencia() {
        // Lanzamos la actividad DESPUÉS de preparar los datos
        try (ActivityScenario<ReservaListActivity> scenario = ActivityScenario.launch(ReservaListActivity.class)) {

            // ESTADO INICIAL: "Todas" -> Deben verse los tres clientes
            onView(withText("Cliente Pasado")).check(matches(isDisplayed()));
            onView(withText("Cliente Activo")).check(matches(isDisplayed()));
            onView(withText("Cliente Futuro")).check(matches(isDisplayed()));

            // FILTRO 1: "Futuras"
            onView(withId(R.id.action_filter)).perform(click());
            onView(withText("Futuras")).perform(click());
            onView(withText("Cliente Futuro")).check(matches(isDisplayed()));
            onView(withText("Cliente Pasado")).check(doesNotExist()); // No debe existir en el Recycler

            // FILTRO 2: "Activas"
            onView(withId(R.id.action_filter)).perform(click());
            onView(withText("Activas")).perform(click());
            onView(withText("Cliente Activo")).check(matches(isDisplayed()));
            onView(withText("Cliente Futuro")).check(doesNotExist());

            // FILTRO 3: "Pasadas"
            onView(withId(R.id.action_filter)).perform(click());
            onView(withText("Pasadas")).perform(click());
            onView(withText("Cliente Pasado")).check(matches(isDisplayed()));
            onView(withText("Cliente Activo")).check(doesNotExist());
        }
    }
}