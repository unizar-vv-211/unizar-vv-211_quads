package es.unizar.eina.T251_quads.test;

import androidx.test.rule.ActivityTestRule;

import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import es.unizar.eina.T251_quads.ui.QuadListActivity;
import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;
import android.content.Context;
import android.os.SystemClock;
import androidx.test.core.app.ApplicationProvider;

import androidx.test.espresso.NoMatchingViewException;
import androidx.test.espresso.contrib.RecyclerViewActions;

import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class RunStepsDefinition {

    private ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class, true, false);

    private String lastClienteVisualizado = ""; // Guarda el contexto para el step @Then

    // -------------------------------------------------------------------------
    // Helpers de sincronización
    // -------------------------------------------------------------------------

    /**
     * Ejecuta una operación de BD en un hilo secundario y espera hasta 10 segundos
     * a que finalice usando un CountDownLatch.
     * Esto evita el deadlock de llamar a future.get() desde el Main Thread.
     */
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
            boolean completed = latch.await(10, TimeUnit.SECONDS);
            if (!completed) {
                throw new RuntimeException("Timeout: la operación de BD tardó más de 10 segundos");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrumpido esperando operación de BD", e);
        }
    }

    /**
     * Espera a que un elemento con el texto dado aparezca en el RecyclerView,
     * haciendo reintentos durante hasta {@code timeoutMs} milisegundos.
     * Usa RecyclerViewActions.scrollTo() para forzar el scroll si el item no es
     * visible.
     * Esto reemplaza el antipatrón de Thread.sleep() y no requiere IdlingResources
     * externos.
     */
    private void waitForRecyclerViewItem(int recyclerViewId, String text, long timeoutMs) {
        long deadline = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < deadline) {
            try {
                onView(withId(recyclerViewId))
                        .perform(RecyclerViewActions.scrollTo(
                                hasDescendant(withText(containsString(text)))));
                return; // éxito
            } catch (Exception e) {
                SystemClock.sleep(200);
            }
        }
        throw new AssertionError(
                "El elemento '" + text + "' no apareció en el RecyclerView tras " + timeoutMs + "ms");
    }

    // -------------------------------------------------------------------------
    // Ciclo de vida del test
    // -------------------------------------------------------------------------

    @Before
    public void setup() {
        // 1. Limpiar la BD ANTES de lanzar la Activity para evitar estado residual.
        // La limpieza se hace en un BG thread para no bloquear el Main Thread.
        Context context = ApplicationProvider.getApplicationContext();

        runOnBackgroundAndWait(() -> {
            es.unizar.eina.T251_quads.database.ReservaRepository reservaRepo = new es.unizar.eina.T251_quads.database.ReservaRepository(
                    (android.app.Application) context);
            es.unizar.eina.T251_quads.database.QuadRepository quadRepo = new es.unizar.eina.T251_quads.database.QuadRepository(
                    (android.app.Application) context);
            reservaRepo.deleteAll();
            quadRepo.deleteAll();
        });

        // 2. Lanzar la Activity solo después de que la BD esté limpia.
        rule.launchActivity(null);
    }

    @After
    public void tearDown() {
        if (rule.getActivity() != null) {
            rule.getActivity().finish();
        }
    }

    // -------------------------------------------------------------------------
    // Steps de Quads
    // -------------------------------------------------------------------------

    @Given("Estoy en la pantalla principal de Quads")
    public void estoy_en_la_pantalla_principal_de_quads() {
        assertNotNull(rule.getActivity());
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.fab)).check(matches(isDisplayed()));
    }

    /**
     * Inserta un Quad directamente en la BD ejecutando la operación en un hilo
     * secundario.
     * Esto evita el deadlock que ocurría al llamar a future.get() desde el Main
     * Thread
     * (hilo en el que Cucumber ejecuta los steps).
     */
    @Given("Existe un quad con matricula {string} y precio {string}")
    public void existe_un_quad_con_matricula_y_precio(String matricula, String precio) {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.QuadRepository repository = new es.unizar.eina.T251_quads.database.QuadRepository(
                (android.app.Application) context);

        es.unizar.eina.T251_quads.database.Quad nuevoQuad = new es.unizar.eina.T251_quads.database.Quad(
                matricula,
                "Monoplaza",
                Float.parseFloat(precio),
                "Quad de prueba Cucumber");

        // Ejecutar en BG thread para que future.get() no bloquee el Main Thread.
        runOnBackgroundAndWait(() -> repository.insert(nuevoQuad));
    }

    /**
     * Inserta una Reserva con precio congelado directamente en la BD.
     * El precio (100.0) representa 2 días × 50€/día y es el que debe persistir
     * aunque el quad cambie de precio posteriormente.
     */
    @Given("Existe una reserva para {string} vinculada al quad {string}")
    public void existe_una_reserva_para_vinculada_al_quad(String cliente, String matricula) {
        Context context = ApplicationProvider.getApplicationContext();
        es.unizar.eina.T251_quads.database.ReservaRepository repository = new es.unizar.eina.T251_quads.database.ReservaRepository(
                (android.app.Application) context);

        // 2 días a 50€/día = 100.0€ (precio congelado en el momento de la reserva)
        es.unizar.eina.T251_quads.database.Reserva nuevaReserva = new es.unizar.eina.T251_quads.database.Reserva(
                cliente,
                "600111222",
                "10-05-2026",
                "12-05-2026",
                0,
                100.0);

        java.util.List<String> quads = new java.util.ArrayList<>();
        quads.add(matricula);

        // Ejecutar en BG thread para que future.get() no bloquee el Main Thread.
        runOnBackgroundAndWait(() -> repository.insert(nuevaReserva, quads));
    }

    // -------------------------------------------------------------------------
    // Steps de creación de Quads via UI
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Steps del escenario "Precio Congelado"
    // -------------------------------------------------------------------------

    /**
     * Cambia el precio de un quad navegando por la UI (sin inyección directa a BD).
     * Esto garantiza que el LiveData de QuadListActivity actualice el adaptador y
     * que la Activity quede en un estado limpio al volver atrás.
     */
    @When("Cambio el precio del quad {string} a {string} euros por dia")
    public void cambio_el_precio_del_quad_a_euros(String matricula, String nuevoPrecio) {
        // 1. Desde MainActivity, entrar a la Gestión de Quads
        onView(withId(R.id.card_quads)).perform(click());

        // 2. Esperar y hacer scroll al quad en el RecyclerView
        waitForRecyclerViewItem(R.id.recyclerview, matricula, 5000);

        // 3. Click corto sobre el item → abre PopupMenu
        onView(withId(R.id.recyclerview))
                .perform(RecyclerViewActions.scrollTo(
                        hasDescendant(withText(containsString(matricula)))));
        onView(withText(containsString(matricula))).perform(click());

        // 4. Seleccionar "Editar" del PopupMenu
        onView(withText("Editar")).perform(click());

        // 5. Cambiar el precio en el formulario de edición
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(nuevoPrecio), closeSoftKeyboard());

        // 6. Guardar y volver a la lista de Quads
        onView(withId(R.id.button_save)).perform(click());

        // 7. Volver a MainActivity con pressBack
        pressBack();
    }

    /**
     * Navega dentro del RecyclerView de reservas hasta el item del cliente
     * especificado
     * y hace click sobre él para abrir el PopupMenu (o la pantalla de detalle).
     *
     * Usa RecyclerViewActions.scrollTo() con hasDescendant() como matcher robusto:
     * - scrollTo() garantiza que el item esté visible antes de interactuar.
     * - hasDescendant(withText(...)) busca dentro de la estructura de vistas del
     * item,
     * evitando ambigüedades con otros textos de la pantalla.
     * - waitForRecyclerViewItem() hace reintentos mientras Room y DiffUtil terminan
     * de entregar los datos al adaptador (reemplaza Thread.sleep() y es más
     * fiable).
     */
    @When("Visualizo la reserva de {string}")
    public void visualizo_la_reserva_de(String cliente) {
        this.lastClienteVisualizado = cliente;

        waitForRecyclerViewItem(R.id.recyclerview, cliente, 5000);

        // Scroll al item del cliente para asegurar que está en pantalla.
        onView(withId(R.id.recyclerview))
                .perform(RecyclerViewActions.scrollTo(
                        hasDescendant(withText(containsString(cliente)))));
    }

    /**
     * Verifica que el precio total mostrado en el PopupMenu o en la vista de
     * detalle
     * de la reserva coincide con el precio congelado en el momento de la creación.
     *
     * El precio se formatea en ReservaListAdapter como:
     * "Precio Total: %.2f €" (con Locale.getDefault())
     * En locale español, 100.0 → "Precio Total: 100,00 €"
     *
     * Usamos containsString("100") para ser agnósticos al separador decimal del
     * locale.
     * El verificador busca el textViewPrecio dentro del item del RecyclerView que
     * aún
     * está visible (o en el PopupMenu si la Activity lo muestra así).
     */
    @Then("El precio de la reserva deberia ser {string}")
    public void el_precio_de_la_reserva_deberia_ser(String precioEsperado) {
        // Extraer la parte numérica sin separadores de locale para hacer la comparación
        // robusta.
        // "100.0" → buscamos "100" (presente tanto en "100.00" como en "100,00")
        String valorNumerico = precioEsperado.contains(".")
                ? precioEsperado.substring(0, precioEsperado.indexOf('.'))
                : precioEsperado;

        // El adaptador muestra el precio en textViewPrecio dentro de cada item.
        // Verificamos que el RecyclerView contiene un item (descendiente común)
        // que tiene tanto el texto del cliente como el textViewPrecio con el valor
        // esperado.
        onView(withId(R.id.recyclerview))
                .check(matches(hasDescendant(
                        allOf(
                                hasDescendant(withText(containsString(lastClienteVisualizado))),
                                hasDescendant(allOf(
                                        withId(R.id.textViewPrecio),
                                        withText(containsString(valorNumerico))))))));
    }
    // =================================================================
    // PASOS PARA EL MÓDULO DE RESERVAS Y PRICE FREEZE
    // =================================================================

    @Given("Existe un quad {string} disponible con precio {string}")
    public void existe_quad_disponible_con_precio(String matricula, String precio) {
        // Creamos el quad navegando por la UI para preparar el entorno
        onView(withId(R.id.card_quads)).perform(click());
        onView(withId(R.id.fab)).perform(click());
        onView(withId(R.id.edit_matricula)).perform(replaceText(matricula), closeSoftKeyboard());
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(precio), closeSoftKeyboard());
        onView(withId(R.id.edit_descripcion)).perform(replaceText("Quad para UAT"), closeSoftKeyboard());
        onView(withId(R.id.button_save)).perform(click());
        pressBack(); // Volvemos al menú principal
    }

    @Given("Existe un quad {string} disponible")
    public void existe_quad_disponible(String matricula) {
        // Reutilizamos el método anterior con un precio por defecto
        existe_quad_disponible_con_precio(matricula, "50.0");
    }

    @Given("Estoy en la pantalla de creacion de Reservas")
    public void estoy_en_pantalla_creacion_reservas() {
        onView(withId(R.id.card_reservas)).perform(click());
        onView(withId(R.id.fab)).perform(click());
    }

    @When("Introduzco los datos del cliente {string} con telefono {string}")
    public void introduzco_datos_cliente(String cliente, String telefono) {
        onView(withId(R.id.edit_cliente)).perform(replaceText(cliente), closeSoftKeyboard());
        onView(withId(R.id.edit_telefono)).perform(replaceText(telefono), closeSoftKeyboard());
    }

    @When("Solicito {string} cascos")
    public void solicito_cascos(String cascos) {
        onView(withId(R.id.edit_cascos)).perform(replaceText(cascos), closeSoftKeyboard());
    }

    @When("Selecciono fechas del {string} al {string}")
    public void selecciono_fechas(String inicio, String fin) {
        onView(withId(R.id.edit_fecha_recogida)).perform(replaceText(inicio), closeSoftKeyboard());
        onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText(fin), closeSoftKeyboard());
    }

    @When("Asigno el quad {string}")
    public void asigno_el_quad(String matricula) {
        // Hacemos scroll hasta el checkbox que contiene la matrícula y lo marcamos
        onView(withText(org.hamcrest.Matchers.containsString(matricula))).perform(scrollTo(), click());
    }

    @Then("Deberia ver la reserva de {string} en el listado principal")
    public void deberia_ver_reserva_listado(String cliente) {
        onView(withId(R.id.recyclerview)).check(matches(isDisplayed()));
        onView(withText(org.hamcrest.Matchers.containsString(cliente))).check(matches(isDisplayed()));
    }

    @Then("El sistema debe mantenerme en la pantalla de creacion de Reservas")
    public void sistema_mantiene_pantalla_reservas() {
        // Seguimos en el formulario, el guardado fue bloqueado por validación
        onView(withId(R.id.edit_cliente)).check(matches(isDisplayed()));
    }

    @When("Confirmo la reserva")
    public void confirmo_la_reserva() {
        onView(withId(R.id.button_save)).perform(click());
    }

    @Then("El sistema debe mantenerme en la pantalla de creacion de Quads")
    public void sistema_mantiene_pantalla_creacion_quads() {
        // Seguimos en el formulario de quads, el guardado fue bloqueado por validación
        onView(withId(R.id.edit_matricula)).check(matches(isDisplayed()));
    }

    @Given("Existe una reserva previa del quad {string} entre {string} y {string}")
    public void existe_reserva_previa(String quad, String inicio, String fin) {
        // Creamos una reserva previa para provocar el solapamiento en el siguiente step
        estoy_en_pantalla_creacion_reservas();
        introduzco_datos_cliente("Cliente Previo Solape", "600000000");
        solicito_cascos("0");
        selecciono_fechas(inicio, fin);
        asigno_el_quad(quad);
        onView(withId(R.id.button_save)).perform(click());
        pressBack(); // Volvemos al menú principal
    }

    @Given("Una reserva activa para {string} vinculada al quad {string} con un precio total pactado de {string}")
    public void reserva_activa_precio_pactado(String cliente, String quad, String precioTotal) {
        existe_quad_disponible_con_precio(quad, "50.0");
        estoy_en_pantalla_creacion_reservas();
        introduzco_datos_cliente(cliente, "600999999");
        solicito_cascos("0");
        selecciono_fechas("01-01-2030", "03-01-2030"); // 2 días a 50€ = 100€
        asigno_el_quad(quad);
        onView(withId(R.id.button_save)).perform(click());
        pressBack(); // Volvemos al menú principal
    }

    @When("Modifico el precio base del quad {string} en el inventario a {string} euros")
    public void modifico_precio_inventario(String matricula, String nuevoPrecio) {
        onView(withId(R.id.card_quads)).perform(click());
        // Clic en el quad para abrir el menú de opciones
        onView(withText(org.hamcrest.Matchers.containsString(matricula))).perform(click());
        // Clic en "Editar" del PopupMenu de Android
        onView(withText("Editar")).perform(click());
        // Cambiamos el precio y guardamos
        onView(withId(R.id.edit_precio_dia)).perform(replaceText(nuevoPrecio), closeSoftKeyboard());
        onView(withId(R.id.button_save)).perform(click());
        pressBack(); // Volvemos al menú principal
    }

    @When("Navego al listado de reservas")
    public void navego_listado_reservas() {
        onView(withId(R.id.card_reservas)).perform(click());
    }

    @Then("La reserva de {string} debe seguir mostrando un precio total de {string}")
    public void verifico_precio_congelado(String cliente, String precioTotalEsperado) {
        // El adaptador muestra "Precio Total: 100,00 €" (locale español) o "100.00 €" (otros).
        // Para no depender del separador decimal, buscamos solo la parte entera: "100.0" → "100"
        String valorEntero = precioTotalEsperado.contains(".")
                ? precioTotalEsperado.substring(0, precioTotalEsperado.indexOf('.'))
                : precioTotalEsperado;

        // Primero esperamos a que aparezca el item del cliente en el RecyclerView
        waitForRecyclerViewItem(R.id.recyclerview, cliente, 5000);

        // Luego comprobamos que ese item tiene un TextView de precio con el valor esperado
        onView(withId(R.id.recyclerview))
                .check(matches(hasDescendant(
                        allOf(
                                hasDescendant(withText(containsString(cliente))),
                                hasDescendant(allOf(
                                        withId(R.id.textViewPrecio),
                                        withText(containsString(valorEntero))))))));
    }
}