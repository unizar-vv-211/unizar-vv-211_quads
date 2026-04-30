package es.unizar.eina.T251_quads.test;

import androidx.test.rule.ActivityTestRule;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import es.unizar.eina.T251_quads.ui.QuadListActivity;
import es.unizar.eina.T251_quads.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

public class RunStepsDefinition {

    private ActivityTestRule<QuadListActivity> rule = new ActivityTestRule<>(QuadListActivity.class, true, false);

    @Before
    public void setup() {
        rule.launchActivity(null);
    }

    @After
    public void tearDown() {
        if (rule.getActivity() != null) {
            rule.getActivity().finish();
        }
    }

    @Given("Estoy en la pantalla principal de Quads")
    public void estoy_en_la_pantalla_principal_de_quads() {
        assertNotNull(rule.getActivity());
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    @When("Hago clic en crear un quad")
    public void hago_clic_en_crear_un_quad() {
        onView(withId(R.id.fab)).perform(click());
    }

    @When("Introduzco {string} como matricula")
    public void introduzco_matricula(final String matricula) {
        onView(withId(R.id.edit_matricula)).perform(replaceText(matricula), closeSoftKeyboard());
    }

    @When("Introduzco {string} como precio")
    public void introduzco_precio(final String precio) {
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(precio), closeSoftKeyboard());
    }

    @When("Introduzco {string} como descripcion")
    public void introduzco_descripcion(final String descripcion) {
        onView(withId(R.id.edit_descripcion)).perform(replaceText(descripcion), closeSoftKeyboard());
    }

    @When("Confirmo la creacion")
    public void confirmo_la_creacion() {
        onView(withId(R.id.button_save)).perform(click());
    }

    @Then("Deberia ver {string} en la lista")
    public void deberia_ver_resultado_en_la_lista(final String resultado) {
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withText(resultado)).check(matches(isDisplayed()));
    }
}