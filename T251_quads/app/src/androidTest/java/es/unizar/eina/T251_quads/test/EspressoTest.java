package es.unizar.eina.T251_quads.test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

public class EspressoTest {

    // Configuramos los datos de prueba
    private static final String TEST_NAME  = "Espresso";
    private static final String TEST_MATRICULA_BASE = "ESP100"; // Cumple regex: 3 letras + 4 numeros (añadiremos un digito al final)
    private static final String TEST_DESC = TEST_NAME + " Quad Desc";
    private static final String TEST_UPDATED_DESC = TEST_NAME + " Updated Desc";

    private static final int TEST_NUMBER_OF_QUADS = 4;

    // Usamos la Activity de Quads[cite: 6]
    @Rule
    public ActivityScenarioRule<QuadListActivity> scenarioRule = new ActivityScenarioRule<>(QuadListActivity.class);

    @Test
    public void testAddAndUpdateQuads() {
        for (int i = 0; i < TEST_NUMBER_OF_QUADS; i++) {

            // 1. CREACIÓN
            // Clic en el botón flotante para añadir Quad[cite: 1, 6]
            onView(withId(R.id.fab)).perform(click());

            // Aseguramos que estamos en la pantalla de edición[cite: 6]
            onView(withId(R.id.edit_matricula)).check(matches(isDisplayed()));

            // Generamos matrícula válida (ej: ESP1000, ESP1001...)
            final String matricula = TEST_MATRICULA_BASE + i;
            onView(withId(R.id.edit_matricula)).perform(replaceText(matricula), closeSoftKeyboard());

            // Insertamos precio y descripción
            final String descripcion = TEST_DESC + " " + i;
            onView(withId(R.id.edit_precio_dia)).perform(replaceText("50.0"), closeSoftKeyboard());
            onView(withId(R.id.edit_descripcion)).perform(replaceText(descripcion), closeSoftKeyboard());

            // Guardamos
            goBackToQuadList(i, false);

            // Aserción en Interfaz: comprobación de que el quad se visualiza en el listado[cite: 6]
            onView(withId(R.id.recyclerview)).perform(scrollTo(hasDescendant(withText(matricula))));
            onView(withText(matricula)).check(matches(isDisplayed()));

            // Aserción en Base de datos[cite: 6]
            scenarioRule.getScenario().onActivity(activity -> {
                Quad actualQuad = activity.getQuadRepository().getQuadByMatricula(matricula);
                assertNotNull("El quad debe existir en la BD", actualQuad);
                assertQuadEquals(matricula, descripcion, actualQuad);
            });

            // 2. ACTUALIZACIÓN
            onView(withId(R.id.recyclerview)).perform(scrollTo(hasDescendant(withText(matricula))));

            // En QuadListActivity, el menú sale con clic CORTO[cite: 1]
            onView(withText(matricula)).perform(click());
            onView(withText("Editar")).perform(click());

            // Verificamos que se abre la edición
            onView(withId(R.id.edit_descripcion)).check(matches(isDisplayed()));

            // Cambiamos la descripción
            final String updatedDesc = TEST_UPDATED_DESC + " " + i;
            onView(withId(R.id.edit_descripcion)).perform(replaceText(updatedDesc), closeSoftKeyboard());
            goBackToQuadList(i, false);

            // Aserción en BD tras actualizar[cite: 6]
            scenarioRule.getScenario().onActivity(activity -> {
                Quad actualQuad = activity.getQuadRepository().getQuadByMatricula(matricula);
                assertNotNull("El quad actualizado debe existir en la BD", actualQuad);
                assertQuadEquals(matricula, updatedDesc, actualQuad);
            });
        }
    }

    private void goBackToQuadList(int quadNumber, boolean errorIntroduction) {
        if (errorIntroduction) {
            // En uno de cada dos, confirma; en otro cancela[cite: 6]
            if (quadNumber % 2 == 0) {
                onView(withId(R.id.button_save)).perform(click());
            } else {
                pressBack();
            }
        } else {
            onView(withId(R.id.button_save)).perform(click());
        }
    }

    @After
    public void borrarQuads() {
        for (int i = 0; i < TEST_NUMBER_OF_QUADS; i++) {
            String matricula = TEST_MATRICULA_BASE + i;

            // Buscamos y borramos usando el clic corto de la lista de quads[cite: 1, 6]
            try {
                onView(withId(R.id.recyclerview)).perform(scrollTo(hasDescendant(withText(matricula))));
                onView(withText(matricula)).perform(click());
                onView(withText("Eliminar")).perform(click());
            } catch (Exception e) {
                // Ignorar si el quad no existe por alguna cancelación en el test
            }
        }
    }

    private void assertQuadEquals(String matricula, String descripcion, Quad quad) {
        assertThat(quad.getMatricula(), is(equalTo(matricula)));
        assertThat(quad.getDescripcion(), is(equalTo(descripcion)));
    }
}