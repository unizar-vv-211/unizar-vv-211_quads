package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NavigationSystemTest {

    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    // Camino A: MainActivity -> QuadListActivity -> QuadEdit
    @Test
    public void testCaminoProfundidad2_CrearQuad() {
        // Profundidad 1
        onView(withId(R.id.card_quads)).perform(click());
        // Verificación exhaustiva del Nivel 1
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        // Profundidad 2
        onView(withId(R.id.fab)).perform(click());
        // Verificación exhaustiva del Nivel 2 (Pantalla QuadEdit completa)
        onView(withId(R.id.edit_matricula)).check(matches(isDisplayed()));
        onView(withId(R.id.spinner_tipo)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_precio_dia)).check(matches(isDisplayed()));
        onView(withId(R.id.button_save)).check(matches(isDisplayed()));
    }

    // Camino B: MainActivity -> QuadListActivity -> MainActivity
    @Test
    public void testCaminoProfundidad2_RetornoDesdeQuads() {
        // Profundidad 1
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // Profundidad 2
        pressBack();
        // Verificación exhaustiva del Nivel 2 (Retorno al nodo raíz)
        onView(withId(R.id.card_quads)).check(matches(isDisplayed()));
        onView(withId(R.id.card_reservas)).check(matches(isDisplayed()));
    }

    // Camino C: MainActivity -> ReservaListActivity -> ReservaEdit
    @Test
    public void testCaminoProfundidad2_CrearReserva() {
        // Profundidad 1
        onView(withId(R.id.card_reservas)).perform(click());
        // Verificación exhaustiva del Nivel 1
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withId(R.id.fab)).check(matches(isDisplayed()));

        // Profundidad 2
        onView(withId(R.id.fab)).perform(click());
        // Verificación exhaustiva del Nivel 2 (Pantalla ReservaEdit completa)
        onView(withId(R.id.edit_cliente)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_fecha_recogida)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_fecha_devolucion)).check(matches(isDisplayed()));
        onView(withId(R.id.button_save)).check(matches(isDisplayed()));
    }

    // Camino D: MainActivity -> ReservaListActivity -> MainActivity
    @Test
    public void testCaminoProfundidad2_RetornoDesdeReservas() {
        // Profundidad 1
        onView(withId(R.id.card_reservas)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // Profundidad 2
        pressBack();
        // Verificación exhaustiva del Nivel 2 (Retorno al nodo raíz)
        onView(withId(R.id.card_quads)).check(matches(isDisplayed()));
        onView(withId(R.id.card_reservas)).check(matches(isDisplayed()));
    }
}