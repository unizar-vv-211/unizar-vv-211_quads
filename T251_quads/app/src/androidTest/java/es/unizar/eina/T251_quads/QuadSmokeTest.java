package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class QuadSmokeTest {

    // Atributo scenarioRule
    @Rule
    public ActivityScenarioRule<QuadListActivity> scenarioRule =
            new ActivityScenarioRule<>(QuadListActivity.class);

    private QuadRepository repository;
    private Quad testQuad;

    @Before
    public void setUp() {
        // Accedemos a la actividad para obtener el repositorio
        scenarioRule.getScenario().onActivity(activity -> {
            repository = activity.getQuadRepository();
        });
        
        // Creamos un quad de prueba
        testQuad = new Quad("9999TST", "Deportivo", 45.5f, "Quad para pruebas de humo");
    }

    // Prueba de averiguar número, insertar y comprobar que hay uno más
    @Test
    public void testCreationIncreasesNumberOfQuads() {
        // Averiguar el número de quads
        int quadsIniciales = repository.getNumQuads();

        // Insertar un nuevo quad
        long insertId = repository.insert(testQuad);
        assertTrue("La inserción en la BD ha fallado", insertId >= 0);

        // Comprobar que el nuevo número es una unidad mayor
        int quadsFinales = repository.getNumQuads();
        assertEquals("El número total de quads no se ha incrementado en 1", 
                     quadsIniciales + 1, quadsFinales);
    }

    // Prueba más exhaustiva (inserción y recuperación de campos)
    @Test
    public void testInsertAndRetrieveQuadData() {
        // Insertar quad
        repository.insert(testQuad);

        // Recuperar el quad a través de su matrícula
        Quad quadRecuperado = repository.getQuadByMatricula(testQuad.getMatricula());

        // Comprobaciones exhaustivas de los campos
        assertEquals("El quad no coincide", testQuad, quadRecuperado);
    }

    // Método de finalización para limpiar la base de datos
    @After
    public void tearDown() {
        if (repository != null) {
            repository.delete(testQuad);
        }
    }
}