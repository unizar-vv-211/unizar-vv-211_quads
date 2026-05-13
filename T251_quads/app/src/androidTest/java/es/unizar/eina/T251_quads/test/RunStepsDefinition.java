package es.unizar.eina.T251_quads.test;

import androidx.test.rule.ActivityTestRule;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;
import android.content.Context;
import androidx.test.core.app.ApplicationProvider;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RunStepsDefinition {

    private ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    private void runOnBackgroundAndWait(Runnable dbOperation) {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                dbOperation.run();
            } finally {
                latch.countDown();
            }
        }).start();
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Before
    public void setup() {
        Context context = ApplicationProvider.getApplicationContext();
        runOnBackgroundAndWait(() -> {
            es.unizar.eina.T251_quads.database.ReservaRepository reservaRepo = new es.unizar.eina.T251_quads.database.ReservaRepository(
                    (android.app.Application) context);
            es.unizar.eina.T251_quads.database.QuadRepository quadRepo = new es.unizar.eina.T251_quads.database.QuadRepository(
                    (android.app.Application) context);
            reservaRepo.deleteAll();
            quadRepo.deleteAll();
        });
        rule.launchActivity(null);
    }

    @After
    public void tearDown() {
        if (rule.getActivity() != null) {
            rule.getActivity().finish();
        }
    }

    // Quads

    @Given("Estoy en la pantalla principal de Quads")
    public void estoy_en_la_pantalla_principal_de_quads() {
        assertNotNull(rule.getActivity());
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    @When("Hago clic en crear un quad")
    public void hago_clic_en_crear_un_quad() {
        onView(withId(R.id.fab)).perform(click());
    }

    @When("Introduzco {string} como matricula")
    public void introduzco_matricula(final String matricula) {
        String texto = (matricula == null) ? "" : matricula;
        onView(withId(R.id.edit_matricula)).perform(replaceText(texto), closeSoftKeyboard());
    }

    @When("Introduzco {string} como precio")
    public void introduzco_precio(final String precio) {
        String texto = (precio == null) ? "" : precio;
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(texto), closeSoftKeyboard());
    }

    @When("Introduzco {string} como descripcion")
    public void introduzco_descripcion(final String descripcion) {
        String texto = (descripcion == null) ? "" : descripcion;
        onView(withId(R.id.edit_descripcion)).perform(replaceText(texto), closeSoftKeyboard());
    }

    @When("Confirmo la creacion")
    public void confirmo_la_creacion() {
        onView(withId(R.id.button_save)).perform(click());
    }

    @When("Selecciono el tipo {string}")
    public void selecciono_el_tipo(String tipo) {
        if (tipo != null && !tipo.isEmpty()) {
            onView(withId(R.id.spinner_tipo)).perform(click());
            onView(withText(tipo)).perform(click());
        }
    }

    @Then("Deberia ver {string} en la lista")
    public void deberia_ver_resultado_en_la_lista(final String resultado) {
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withText(resultado)).check(matches(isDisplayed()));
    }

    @Then("El sistema debe mantenerme en la pantalla de creacion de Quads")
    public void sistema_mantiene_pantalla_creacion_quads() {
        onView(withId(R.id.edit_matricula)).check(matches(isDisplayed()));
    }

    // Reservas
    @Given("Existe un quad con matricula {string} y precio {string}")
    public void existe_un_quad_con_matricula_y_precio(String matricula, String precio) {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository repository = new es.unizar.eina.T251_quads.database.QuadRepository(
                (android.app.Application) context);

        es.unizar.eina.T251_quads.database.Quad nuevoQuad = new es.unizar.eina.T251_quads.database.Quad(
                matricula,
                "Monoplaza",
                Float.parseFloat(precio),
                "Quad de prueba cargado automáticamente");

        runOnBackgroundAndWait(() -> repository.insert(nuevoQuad));
    }

    @Given("Estoy en la pantalla principal de Reservas")
    public void estoy_en_la_pantalla_principal_de_reservas() {
        assertNotNull(rule.getActivity());
        onView(withId(R.id.card_reservas)).perform(click());
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    @When("Hago clic en crear una reserva")
    public void hago_clic_en_crear_una_reserva() {
        onView(withId(R.id.fab)).perform(click());
    }

    @When("Introduzco {string} como cliente")
    public void introduzco_como_cliente(String cliente) {
        String texto = (cliente == null) ? "" : cliente;
        onView(withId(R.id.edit_cliente)).perform(replaceText(texto), closeSoftKeyboard());
    }

    @When("Introduzco {string} como telefono")
    public void introduzco_como_telefono(String telefono) {
        String texto = (telefono == null) ? "" : telefono;
        onView(withId(R.id.edit_telefono)).perform(replaceText(texto), closeSoftKeyboard());
    }

    @When("Introduzco {string} como fecha de recogida")
    public void introduzco_como_fecha_recogida(String fecha) {
        String texto = (fecha == null) ? "" : fecha;
        onView(withId(R.id.edit_fecha_recogida)).perform(replaceText(texto), closeSoftKeyboard());
    }

    @When("Introduzco {string} como fecha de devolucion")
    public void introduzco_como_fecha_devolucion(String fecha) {
        String texto = (fecha == null) ? "" : fecha;
        onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText(texto), closeSoftKeyboard());
    }

    @When("Introduzco {string} como numero de cascos")
    public void introduzco_como_numero_cascos(String cascos) {
        String texto = (cascos == null) ? "" : cascos;
        // Hacemos scroll por si el teclado de campos anteriores oculta este input
        onView(withId(R.id.edit_cascos)).perform(androidx.test.espresso.action.ViewActions.scrollTo(),
                replaceText(texto), closeSoftKeyboard());
    }

    @When("Selecciono los quads {string}")
    public void selecciono_los_quads(String quad) {
        if (quad != null && !quad.isEmpty()) {
            // Busca el CheckBox dinámico del RecyclerView que contiene la matrícula
            onView(withText(org.hamcrest.Matchers.containsString(quad)))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
        }
    }

    @When("Confirmo la creacion de la reserva")
    public void confirmo_la_creacion_de_la_reserva() {
        // Hacemos scroll al botón porque en pantallas pequeñas puede quedar debajo del
        // formulario
        onView(withId(R.id.button_save)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
    }

    @Then("Deberia ver {string} en la lista de reservas")
    public void deberia_ver_en_la_lista_de_reservas(String cliente) {
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withText(org.hamcrest.Matchers.containsString(cliente))).check(matches(isDisplayed()));
    }

    @Then("El sistema debe mantenerme en la pantalla de creacion de Reservas")
    public void el_sistema_debe_mantenerme_en_la_pantalla_de_creacion_de_reservas() {
        // Verificamos que el formulario sigue abierto porque la validación ha fallado
        onView(withId(R.id.edit_cliente)).check(matches(isDisplayed()));
    }

    // Congelamiento de Precios

    @Given("Una reserva activa para {string} vinculada al quad {string} con un precio total pactado de {string}")
    public void reserva_activa_precio_pactado(String cliente, String quad, String precioTotal) {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.ReservaRepository repository = new es.unizar.eina.T251_quads.database.ReservaRepository(
                (android.app.Application) context);

        // Metemos la reserva directamente en la base de datos con el precio pactado
        es.unizar.eina.T251_quads.database.Reserva nuevaReserva = new es.unizar.eina.T251_quads.database.Reserva(
                cliente,
                "600999999", // Teléfono por defecto
                "01-01-2030", // Fechas irrelevantes para esta prueba
                "03-01-2030",
                0,
                Double.parseDouble(precioTotal));

        java.util.List<String> quads = new java.util.ArrayList<>();
        quads.add(quad);

        // Ejecutar en hilo secundario usando tu método seguro
        runOnBackgroundAndWait(() -> repository.insert(nuevaReserva, quads));
    }

    @When("Modifico el precio base del quad {string} en el inventario a {string} euros")
    public void modifico_precio_inventario(String matricula, String nuevoPrecio) {
        // 1. Entrar a la gestión de Quads
        onView(withId(R.id.card_quads)).perform(click());

        // 2. Clic en el quad específico para abrir el menú
        onView(withText(org.hamcrest.Matchers.containsString(matricula))).perform(click());

        // 3. Clic en "Editar"
        onView(withText("Editar")).perform(click());

        // 4. Cambiar el precio
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(nuevoPrecio), closeSoftKeyboard());

        // 5. Guardar
        onView(withId(R.id.button_save)).perform(click());

        // 6. Volver al menú principal
        androidx.test.espresso.Espresso.pressBack();
    }

    @When("Navego al listado de reservas")
    public void navego_listado_reservas() {
        onView(withId(R.id.card_reservas)).perform(click());
    }

    @Then("La reserva de {string} debe seguir mostrando un precio total de {string}")
    public void verifico_precio_congelado(String cliente, String precioTotalEsperado) {
        // 1. Añadimos una pequeña pausa para asegurar que Room/LiveData
        // han terminado de cargar y renderizar los datos en el RecyclerView.
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // 2. Extraemos solo la parte entera para evitar problemas con las comas/puntos
        // de los decimales
        String valorEntero = precioTotalEsperado.contains(".")
                ? precioTotalEsperado.substring(0, precioTotalEsperado.indexOf('.'))
                : precioTotalEsperado;

        // 3. Hacemos scroll seguro hasta el elemento del RecyclerView que contenga el
        // nombre del cliente
        onView(withId(R.id.recyclerview))
                .perform(androidx.test.espresso.contrib.RecyclerViewActions.scrollTo(
                        hasDescendant(withText(org.hamcrest.Matchers.containsString(cliente)))));

        // 4. Verificación robusta: Buscamos el textViewPrecio que tenga como "hermano"
        // al cliente
        onView(org.hamcrest.Matchers.allOf(
                withId(R.id.textViewPrecio),
                withText(org.hamcrest.Matchers.containsString(valorEntero)),
                androidx.test.espresso.matcher.ViewMatchers.hasSibling(
                        withText(org.hamcrest.Matchers.containsString(cliente)))))
                .check(matches(isDisplayed()));
    }
}