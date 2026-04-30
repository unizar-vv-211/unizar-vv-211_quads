package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class NavigationSystemTest {

    // Iniciamos la prueba en el nodo N1 (MainActivity)
    @Rule
    public ActivityScenarioRule<MainActivity> scenarioRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void testCaminoProfundidad2_FlujoCompletoQuads() {
        String matriculaTest = "NAV1234";

        // -------------------------------------------------------------
        // ARCO 1: Ir a la lista de Quads (N1 -> N2)
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // -------------------------------------------------------------
        // ARCO 2: Crear un nuevo Quad (N2 -> N3)
        // Cubre la situación de prueba 1-2
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.edit_matricula)).check(matches(isDisplayed()));

        // Rellenamos el formulario (usando replaceText como en Notepad)
        onView(withId(R.id.edit_matricula)).perform(replaceText(matriculaTest), closeSoftKeyboard());
        onView(withId(R.id.edit_precio_dia)).perform(replaceText("50.0"), closeSoftKeyboard());
        onView(withId(R.id.edit_descripcion)).perform(replaceText("Quad para test de navegacion"), closeSoftKeyboard());

        // -------------------------------------------------------------
        // ARCO 3: Guardar el Quad (N3 -> N2)
        // Cubre la situación de prueba 2-3
        onView(withId(R.id.button_save)).perform(click());
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // -------------------------------------------------------------
        // ARCO 4: Editar el Quad creado (N2 -> N3)
        // Cubre la situación de prueba 3-4
        // Click en el item para sacar el PopupMenu, luego click en "Editar"
        onView(withText(matriculaTest)).perform(click());
        onView(withText("Editar")).perform(click());
        onView(withId(R.id.edit_descripcion)).check(matches(isDisplayed()));

        // Modificamos algo leve
        onView(withId(R.id.edit_descripcion)).perform(replaceText("Descripcion modificada"), closeSoftKeyboard());

        // -------------------------------------------------------------
        // ARCO 5: Cancelar/Atrás desde la edición (N3 -> N2)
        // Cubre la situación de prueba 4-5
        pressBack();
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));

        // -------------------------------------------------------------
        // ARCO 6: Eliminar el Quad (N2 -> N2)
        // Cubre la situación de prueba 5-6
        // Click en el item para sacar el PopupMenu, luego click en "Eliminar"
        onView(withText(matriculaTest)).perform(click());
        onView(withText("Eliminar")).perform(click());

        // -------------------------------------------------------------
        // ARCO 7: Volver a la pantalla principal (N2 -> N1)
        // Cubre la situación de prueba 6-7
        pressBack();

        // Verificación final de que estamos en N1
        onView(withId(R.id.card_quads)).check(matches(isDisplayed()));
    }
}