package es.unizar.eina.T251_quads;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NavigationSystemTest {

    // El punto de entrada para todos los flujos de navegación es la pantalla principal (Nodo N1)
    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testProfundidad2_FlujoCompletoQuads() {
        // Vamos a probar todo el flujo de quads. Esto cubre los caminos a-c (N1->N2->N3), c-d (N2->N3->N2) y d-b (N3->N2->N1).

        // Hacemos la transición del Arco 'a' (N1 -> N2): Entramos a la lista de quads
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // Ahora el Arco 'c' (N2 -> N3): Pulsamos el botón para añadir un quad nuevo
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.edit_matricula)).check(matches(isDisplayed())); // Comprobamos que estamos en QuadEdit (N3)

        // Hacemos el Arco 'd' (N3 -> N2): Volvemos atrás a la lista
        pressBack();
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed())); // Comprobamos que volvemos a QuadList (N2)

        // Por último, el Arco 'b' (N2 -> N1): Volvemos atrás otra vez a la pantalla principal
        pressBack();
        onView(withId(R.id.card_quads)).check(matches(isDisplayed())); // Comprobamos que volvemos al inicio (N1)
    }

    @Test
    public void testProfundidad2_FlujoCompletoReservas() {
        // Ahora probamos el flujo de reservas. Esto cubre los caminos e-g (N1->N4->N5), g-h (N4->N5->N4) y h-f (N5->N4->N1).

        // Hacemos la transición del Arco 'e' (N1 -> N4): Entramos a la sección de reservas
        onView(withId(R.id.card_reservas)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // Seguimos con el Arco 'g' (N4 -> N5): Le damos a crear una nueva reserva
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.edit_cliente)).check(matches(isDisplayed())); // Vemos que sale el formulario ReservaEdit (N5)

        // Hacemos el Arco 'h' (N5 -> N4): Tiramos para atrás a la lista de reservas
        pressBack();
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed())); // Confirmamos que volvemos a ReservaList (N4)

        // Por último, el Arco 'f' (N4 -> N1): Atrás otra vez para salir al inicio
        pressBack();
        onView(withId(R.id.card_reservas)).check(matches(isDisplayed())); // Confirmamos que volvemos al menú principal (N1)
    }

    @Test
    public void testProfundidad2_TransicionCruzada() {
        // Vamos a probar a saltar entre secciones. Esto cubre el camino b-e (N2->N1->N4), esencial para probar pérdida de contexto.

        // Como precondición, primero llegamos a N2 metiéndonos en quads
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // Realizamos el Arco 'b' (N2 -> N1): Volvemos a la pantalla principal
        pressBack();
        onView(withId(R.id.card_reservas)).check(matches(isDisplayed()));

        // Y ahora realizamos el Arco 'e' (N1 -> N4): Entramos en reservas para asegurar que la jerarquía está bien
        onView(withId(R.id.card_reservas)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
    }
}