package es.unizar.eina.T251_quads;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

@RunWith(AndroidJUnit4.class)
public class QuadSmokeTest {

    // Atributo scenarioRule
    @Rule
    public ActivityScenarioRule<QuadListActivity> scenarioRule =
            new ActivityScenarioRule<>(QuadListActivity.class);

    private Quad testQuad;

    @Before
    public void setUp() {
        // Creamos un quad de prueba
        testQuad = new Quad("9999TST", "Deportivo", 45.5f, "Quad para pruebas de humo");
    }

    // Prueba de averiguar número, insertar y comprobar que hay uno más
    @Test
    public void testCreationIncreasesNumberOfQuads() {
        // Averiguar el número de quads
        scenarioRule.getScenario().onActivity(activity -> {
            int quadsIniciales = activity.getQuadRepository().getNumQuads();

            // Insertar un nuevo quad
            long insertId = activity.getQuadRepository().insert(testQuad);
            assertTrue("La inserción en la BD ha fallado", insertId >= 0);

            // Comprobar que el nuevo número es una unidad mayor
            int quadsFinales = activity.getQuadRepository().getNumQuads();
            assertEquals("El número total de quads no se ha incrementado en 1",
                         quadsIniciales + 1, quadsFinales);
        });
    }

    // Prueba más exhaustiva (inserción y recuperación de campos)
    @Test
    public void testInsertAndRetrieveQuadData() {
        scenarioRule.getScenario().onActivity(activity -> {
            // Insertar quad
            activity.getQuadRepository().insert(testQuad);

            // Recuperar el quad a través de su matrícula
            Quad quadRecuperado = activity.getQuadRepository().getQuadByMatricula(testQuad.getMatricula());

            // Comprobaciones exhaustivas de los campos
            assertEquals("Los quads no coinciden", testQuad, quadRecuperado);
        });
    }

    // Método de finalización para limpiar la base de datos
    @After
    public void tearDown() {
        scenarioRule.getScenario().onActivity(activity -> {
            activity.getQuadRepository().delete(testQuad);
        });
    }
}