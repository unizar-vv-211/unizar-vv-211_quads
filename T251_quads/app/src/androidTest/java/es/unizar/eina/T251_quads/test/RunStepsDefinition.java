package es.unizar.eina.T251_quads.test;

import androidx.test.rule.ActivityTestRule;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;
import android.app.Activity;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.NoMatchingViewException;
import androidx.test.platform.app.InstrumentationRegistry;

import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItem;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RunStepsDefinition {

    private ActivityTestRule<MainActivity> rule;
    private String lastReservaCliente;
    private String lastReservaTelefono;
    private String lastReservaPrecioTotal;
    private String lastReservaFechaRecogida;
    private String lastReservaFechaDevolucion;
    private String lastReservaQuad;
    private Intent lastSendIntent;

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

    private void waitForUi() {
        try {
            Thread.sleep(800);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void waitUntilDisplayed(int viewId) {
        long deadline = System.currentTimeMillis() + 5000;
        RuntimeException lastError = null;
        do {
            try {
                onView(withId(viewId)).check(matches(isDisplayed()));
                return;
            } catch (RuntimeException | AssertionError e) {
                lastError = e instanceof RuntimeException
                        ? (RuntimeException) e
                        : new RuntimeException(e);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw lastError;
                }
            }
        } while (System.currentTimeMillis() < deadline);
        throw lastError;
    }

    private String convertDDMMYYYYtoYYYYMMDD(String fecha) {
        String[] parts = fecha.split("-");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }

    private void clickRecyclerItemWithText(String text) {
        onView(withId(R.id.recyclerview))
                .perform(actionOnItem(hasDescendant(withText(containsString(text))), click()));
        waitForUi();
    }

    private long insertReservaDirecta(String cliente, String telefono, String fechaRecogida,
                                      String fechaDevolucion, int cascos, double precioTotal, String quad) {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.ReservaRepository repository =
                new es.unizar.eina.T251_quads.database.ReservaRepository((android.app.Application) context);
        es.unizar.eina.T251_quads.database.Reserva reserva =
                new es.unizar.eina.T251_quads.database.Reserva(
                        cliente,
                        telefono,
                        convertDDMMYYYYtoYYYYMMDD(fechaRecogida),
                        convertDDMMYYYYtoYYYYMMDD(fechaDevolucion),
                        cascos,
                        precioTotal);

        long[] insertedId = {-1};
        runOnBackgroundAndWait(() -> insertedId[0] = repository.insert(reserva, Arrays.asList(quad)));
        waitForUi();
        return insertedId[0];
    }

    private String menuTitleForSort(String criterio) {
        if ("matricula".equalsIgnoreCase(criterio)) {
            return "Matrícula";
        }
        if ("tipo".equalsIgnoreCase(criterio)) {
            return "Tipo";
        }
        return "Precio";
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

        rule = new ActivityTestRule<>(MainActivity.class, true, false);
        rule.launchActivity(null);
        lastReservaCliente = null;
        lastReservaTelefono = null;
        lastReservaPrecioTotal = null;
        lastSendIntent = null;
        waitForUi();
    }

    @After
    public void tearDown() {
        if (rule != null) {
            try {
                rule.finishActivity();
            } finally {
                rule = null;
                waitForUi();
            }
        }
    }

    // Quads

    @Given("Estoy en la pantalla principal de Quads")
    public void estoy_en_la_pantalla_principal_de_quads() {
        assertNotNull(rule.getActivity());
        onView(withId(R.id.card_quads)).perform(click());
        waitUntilDisplayed(R.id.fab);
    }

    @When("Hago clic en crear un quad")
    public void hago_clic_en_crear_un_quad() {
        onView(withId(R.id.fab)).perform(click());
        waitUntilDisplayed(R.id.edit_matricula);
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

    @Given("Existen quads registrados en el inventario")
    public void existen_quads_registrados_en_el_inventario() {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository repository =
                new es.unizar.eina.T251_quads.database.QuadRepository((android.app.Application) context);

        runOnBackgroundAndWait(() -> {
            repository.insert(new es.unizar.eina.T251_quads.database.Quad(
                    "AAA1111", "Monoplaza", 10.0f, "Quad de consulta A"));
            repository.insert(new es.unizar.eina.T251_quads.database.Quad(
                    "BBB1111", "Biplaza", 30.0f, "Quad de consulta B"));
            repository.insert(new es.unizar.eina.T251_quads.database.Quad(
                    "CCC1111", "Monoplaza", 20.0f, "Quad de consulta C"));
        });
        waitForUi();
    }

    @Then("El sistema muestra el inventario de quads")
    public void sistema_muestra_inventario_quads() {
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withText("AAA1111")).check(matches(isDisplayed()));
        onView(withText("BBB1111")).check(matches(isDisplayed()));
        onView(withText("CCC1111")).check(matches(isDisplayed()));
    }

    @When("Solicito ordenar los quads por {string}")
    public void solicito_ordenar_los_quads_por(String criterio) {
        try {
            onView(withContentDescription("Ordenar")).perform(click());
        } catch (NoMatchingViewException e) {
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
            onView(withText("Ordenar")).perform(click());
        }
        onView(withText(menuTitleForSort(criterio))).perform(click());
        waitForUi();
    }

    @Then("El listado de quads se actualiza segun {string}")
    public void listado_quads_actualiza_segun(String criterio) {
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        sistema_muestra_inventario_quads();
    }

    @When("Selecciono el quad {string}")
    public void selecciono_el_quad(String matricula) {
        clickRecyclerItemWithText(matricula);
    }

    @When("Elijo editar el quad")
    public void elijo_editar_el_quad() {
        onView(withText("Editar")).perform(click());
        waitForUi();
        onView(withId(R.id.edit_matricula)).check(matches(isDisplayed()));
    }

    @When("Actualizo el precio a {string} y la descripcion a {string}")
    public void actualizo_precio_y_descripcion(String precio, String descripcion) {
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(precio), closeSoftKeyboard());
        onView(withId(R.id.edit_descripcion)).perform(replaceText(descripcion), closeSoftKeyboard());
    }

    @Then("El quad {string} debe tener precio {string} y descripcion {string}")
    public void quad_debe_tener_precio_y_descripcion(String matricula, String precio, String descripcion) {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository repository =
                new es.unizar.eina.T251_quads.database.QuadRepository((android.app.Application) context);
        waitForUi();
        es.unizar.eina.T251_quads.database.Quad quad = repository.getQuadByMatricula(matricula);
        assertNotNull(quad);
        assertEquals(Float.parseFloat(precio), quad.getPrecioDia(), 0.01f);
        assertEquals(descripcion, quad.getDescripcion());
    }

    @When("Elijo eliminar el quad")
    public void elijo_eliminar_el_quad() {
        onView(withText("Eliminar")).perform(click());
        waitForUi();
    }

    @Then("No deberia ver {string} en la lista de quads")
    public void no_deberia_ver_en_lista_quads(String matricula) {
        waitForUi();
        onView(withId(R.id.recyclerview))
                .check(matches(not(hasDescendant(withText(containsString(matricula))))));
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
        waitForUi();
    }

    @Given("Creo desde la interfaz un quad {string} de tipo {string} con precio {string} y descripcion {string}")
    public void creo_desde_la_interfaz_un_quad(String matricula, String tipo, String precio, String descripcion) {
        estoy_en_la_pantalla_principal_de_quads();
        hago_clic_en_crear_un_quad();
        introduzco_matricula(matricula);
        selecciono_el_tipo(tipo);
        introduzco_precio(precio);
        introduzco_descripcion(descripcion);
        confirmo_la_creacion();
        waitForUi();
        deberia_ver_resultado_en_la_lista(matricula);
    }

    @Given("Existe una reserva previa para {string} con el quad {string} del {string} al {string}")
    public void existe_una_reserva_previa(String cliente, String quad, String fechaRecogida, String fechaDevolucion) {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.ReservaRepository repository =
                new es.unizar.eina.T251_quads.database.ReservaRepository((android.app.Application) context);

        es.unizar.eina.T251_quads.database.Reserva reserva =
                new es.unizar.eina.T251_quads.database.Reserva(
                        cliente,
                        "123456789",
                        convertDDMMYYYYtoYYYYMMDD(fechaRecogida),
                        convertDDMMYYYYtoYYYYMMDD(fechaDevolucion),
                        1,
                        10.0);

        runOnBackgroundAndWait(() -> repository.insert(reserva, Arrays.asList(quad)));
        waitForUi();
    }

    @Given("Existe una reserva registrada para {string} con el quad {string} del {string} al {string} y precio {string}")
    public void existe_una_reserva_registrada(String cliente, String quad, String fechaRecogida,
                                              String fechaDevolucion, String precioTotal) {
        insertReservaDirecta(
                cliente,
                "123456789",
                fechaRecogida,
                fechaDevolucion,
                1,
                Double.parseDouble(precioTotal),
                quad);
        lastReservaCliente = cliente;
        lastReservaTelefono = "123456789";
        lastReservaPrecioTotal = precioTotal;
    }

    @Given("Existen reservas previstas, vigentes y caducadas")
    public void existen_reservas_previstas_vigentes_y_caducadas() {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository quadRepository =
                new es.unizar.eina.T251_quads.database.QuadRepository((android.app.Application) context);
        es.unizar.eina.T251_quads.database.ReservaRepository reservaRepository =
                new es.unizar.eina.T251_quads.database.ReservaRepository((android.app.Application) context);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        String hoy = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        String manana = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, 4);
        String futuraFin = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -8);
        String pasadaFin = sdf.format(calendar.getTime());
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        String pasadaInicio = sdf.format(calendar.getTime());

        runOnBackgroundAndWait(() -> {
            quadRepository.insert(new es.unizar.eina.T251_quads.database.Quad(
                    "AAA1111", "Monoplaza", 10.0f, "Quad para filtros"));
            reservaRepository.insert(new es.unizar.eina.T251_quads.database.Reserva(
                    "Vigente", "123456789", hoy, manana, 1, 10.0), Arrays.asList("AAA1111"));
            reservaRepository.insert(new es.unizar.eina.T251_quads.database.Reserva(
                    "Prevista", "123456789", manana, futuraFin, 1, 10.0), Arrays.asList("AAA1111"));
            reservaRepository.insert(new es.unizar.eina.T251_quads.database.Reserva(
                    "Caducada", "123456789", pasadaInicio, pasadaFin, 1, 10.0), Arrays.asList("AAA1111"));
        });
        waitForUi();
    }

    @Given("Estoy en la pantalla principal de Reservas")
    public void estoy_en_la_pantalla_principal_de_reservas() {
        assertNotNull(rule.getActivity());
        try {
            onView(withId(R.id.card_reservas)).perform(click());
        } catch (NoMatchingViewException e) {
            onView(withId(R.id.fab)).check(matches(isDisplayed()));
        }
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
            // Busca el CheckBox dinámico que contiene la matrícula
            onView(withText(org.hamcrest.Matchers.containsString(quad)))
                    .perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
        }
    }

    @When("Confirmo la creacion de la reserva")
    public void confirmo_la_creacion_de_la_reserva() {
        // Hacemos scroll al botón porque en pantallas pequeñas puede quedar debajo del
        // formulario
        onView(withId(R.id.button_save)).perform(androidx.test.espresso.action.ViewActions.scrollTo(), click());
        waitForUi();
    }

    @Then("Deberia ver {string} en la lista de reservas")
    public void deberia_ver_en_la_lista_de_reservas(String cliente) {
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withText(containsString(cliente))).check(matches(isDisplayed()));
    }

    @Then("No deberia ver {string} en la lista de reservas")
    public void no_deberia_ver_en_la_lista_de_reservas(String cliente) {
        onView(withId(R.id.recyclerview)).check(matches(not(hasDescendant(withText(containsString(cliente))))));
    }

    @Then("El sistema debe mantenerme en la pantalla de creacion de Reservas")
    public void el_sistema_debe_mantenerme_en_la_pantalla_de_creacion_de_reservas() {
        // Verificamos que el formulario sigue abierto porque la validación ha fallado
        onView(withId(R.id.edit_cliente)).check(matches(isDisplayed()));
    }

    @When("Selecciono la reserva de {string}")
    public void selecciono_la_reserva_de(String cliente) {
        clickRecyclerItemWithText(cliente);
    }

    @When("Elijo modificar la reserva")
    public void elijo_modificar_la_reserva() {
        onView(withText("Modificar")).perform(click());
        waitForUi();
        onView(withId(R.id.edit_cliente)).check(matches(isDisplayed()));
    }

    @When("Elijo eliminar la reserva")
    public void elijo_eliminar_la_reserva() {
        onView(withText("Eliminar")).perform(click());
        waitForUi();
    }

    @Given("Creo desde la interfaz una reserva para {string} con telefono {string} del {string} al {string}, {string} casco y quad {string}")
    public void creo_desde_la_interfaz_una_reserva(String cliente, String telefono, String fechaRecogida,
                                                   String fechaDevolucion, String cascos, String quad) {
        pressBack();
        waitForUi();
        estoy_en_la_pantalla_principal_de_reservas();
        hago_clic_en_crear_una_reserva();
        introduzco_como_cliente(cliente);
        introduzco_como_telefono(telefono);
        introduzco_como_fecha_recogida(fechaRecogida);
        introduzco_como_fecha_devolucion(fechaDevolucion);
        introduzco_como_numero_cascos(cascos);
        selecciono_los_quads(quad);
        confirmo_la_creacion_de_la_reserva();
        deberia_ver_en_la_lista_de_reservas(cliente);
    }

    @Given("Vuelvo a la pantalla principal")
    public void vuelvo_a_la_pantalla_principal() {
        pressBack();
        waitForUi();
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

        // Ejecutar en hilo secundario 
        runOnBackgroundAndWait(() -> repository.insert(nuevaReserva, quads));
    }

    @When("Modifico el precio base del quad {string} en el inventario a {string} euros")
    public void modifico_precio_inventario(String matricula, String nuevoPrecio) {
        // 1. Entrar a la gestión de Quads
        onView(withId(R.id.card_quads)).perform(click());

        // 2. Clic en el quad específico para abrir el menú
        onView(withText(containsString(matricula))).perform(click());

        // 3. Clic en "Editar"
        onView(withText("Editar")).perform(click());

        // 4. Cambiar el precio
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(nuevoPrecio), closeSoftKeyboard());

        // 5. Guardar
        onView(withId(R.id.button_save)).perform(click());
        waitForUi();

        // 6. Volver al menú principal
        pressBack();
        waitForUi();
    }

    @When("Navego al listado de reservas")
    public void navego_listado_reservas() {
        onView(withId(R.id.card_reservas)).perform(click());
    }

    @Then("La reserva de {string} debe seguir mostrando un precio total de {string}")
    public void verifico_precio_congelado(String cliente, String precioTotalEsperado) {
        // 1. Añadimos una pequeña pausa para asegurar que 
        // han terminado de cargar y renderizar los datos.
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
                .perform(scrollTo(
                        hasDescendant(withText(containsString(cliente)))));

        // 4. Verificación: Buscamos el textViewPrecio que tenga como "hermano"
        // al cliente
        onView(org.hamcrest.Matchers.allOf(
                withId(R.id.textViewPrecio),
                withText(containsString(valorEntero)),
                androidx.test.espresso.matcher.ViewMatchers.hasSibling(
                        withText(containsString(cliente)))))
                .check(matches(isDisplayed()));
    }

    @When("Selecciono el filtro de reservas {string}")
    public void selecciono_el_filtro_de_reservas(String filtro) {
        try {
            onView(withContentDescription("Filtrar")).perform(click());
        } catch (NoMatchingViewException e) {
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext());
            onView(withText("Filtrar")).perform(click());
        }
        onView(withText(filtro)).perform(click());
        waitForUi();
    }

    // Envio de mensajes

    @Given("Existe una reserva de envio para {string} con telefono {string} y precio total {string}")
    public void existe_reserva_envio(String cliente, String telefono, String precioTotal) {
        Application application = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository quadRepository =
                new es.unizar.eina.T251_quads.database.QuadRepository(application);
        es.unizar.eina.T251_quads.database.ReservaRepository reservaRepository =
                new es.unizar.eina.T251_quads.database.ReservaRepository(application);

        lastReservaCliente = cliente;
        lastReservaTelefono = telefono;
        lastReservaPrecioTotal = precioTotal;
        lastReservaFechaRecogida = "2026-05-10";
        lastReservaFechaDevolucion = "2026-05-11";
        lastReservaQuad = "SMS0001";

        runOnBackgroundAndWait(() -> {
            quadRepository.insert(new es.unizar.eina.T251_quads.database.Quad(
                    lastReservaQuad,
                    "Monoplaza",
                    50.0f,
                    "Quad para envío SMS"));
            reservaRepository.insert(new es.unizar.eina.T251_quads.database.Reserva(
                    cliente,
                    telefono,
                    lastReservaFechaRecogida,
                    lastReservaFechaDevolucion,
                    1,
                    Double.parseDouble(precioTotal)), Arrays.asList(lastReservaQuad));
        });
        waitForUi();
    }

    @When("Envio por SMS la reserva de {string}")
    public void envio_por_sms_la_reserva_de(String cliente) {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        CapturingActivityMonitor monitor = new CapturingActivityMonitor();
        instrumentation.addMonitor(monitor);

        try {
            onView(withId(R.id.card_reservas)).perform(click());
            waitForUi();
            onView(withId(R.id.recyclerview))
                    .perform(actionOnItem(hasDescendant(withText(containsString(cliente))), click()));
            onView(withText("Enviar por SMS")).perform(click());
            instrumentation.waitForMonitorWithTimeout(monitor, 5000);
            lastSendIntent = monitor.getStartedIntent();
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }

    @Then("Se debe generar un SMS con los datos de la reserva")
    public void se_debe_generar_sms_con_datos_reserva() {
        assertNotNull(lastSendIntent);
        assertEquals(Intent.ACTION_VIEW, lastSendIntent.getAction());
        assertNotNull(lastSendIntent.getData());
        assertEquals("sms:" + lastReservaTelefono, lastSendIntent.getData().toString());
        String smsBody = lastSendIntent.getStringExtra("sms_body");
        assertNotNull(smsBody);
        assertTrue(smsBody.contains(lastReservaCliente));
        assertTrue(smsBody.contains(lastReservaFechaRecogida));
        assertTrue(smsBody.contains(lastReservaFechaDevolucion));
        assertTrue(smsBody.contains("Cascos: 1"));
        assertTrue(smsBody.contains(lastReservaQuad));
    }

    private static class CapturingActivityMonitor extends Instrumentation.ActivityMonitor {
        private Intent startedIntent;

        CapturingActivityMonitor() {
            super(new IntentFilter(Intent.ACTION_VIEW),
                    new Instrumentation.ActivityResult(Activity.RESULT_OK, null),
                    true);
        }

        @Override
        public Instrumentation.ActivityResult onStartActivity(Intent intent) {
            startedIntent = intent;
            return new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
        }

        Intent getStartedIntent() {
            return startedIntent;
        }
    }
}
