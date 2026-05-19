package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

@RunWith(AndroidJUnit4.class)
public class QuadCreationTest {

    @Rule
    public ActivityScenarioRule<QuadListActivity> scenarioRule =
            new ActivityScenarioRule<>(QuadListActivity.class);

    private QuadRepository quadRepository;

    @Before
    public void setUp() throws InterruptedException {
        scenarioRule.getScenario().onActivity(activity -> {
            quadRepository = activity.getQuadRepository();
        });
        quadRepository.deleteAll();
        Thread.sleep(300);
    }

    // Prueba de inserción válida de un Monoplaza
    @Test
    public void testInsertValidMonoplaza() {
        Quad quad = new Quad("MON1234", "Monoplaza", 45.0f, "Quad monoplaza para prueba");
        long result = quadRepository.insert(quad);
        assertTrue("La inserción de un Monoplaza válido debería devolver un rowid > 0", result > 0);
    }

    // Prueba de inserción válida de un Biplaza
    @Test
    public void testInsertValidBiplaza() {
        Quad quad = new Quad("BIP5678", "Biplaza", 70.0f, "Quad biplaza para prueba");
        long result = quadRepository.insert(quad);
        assertTrue("La inserción de un Biplaza válido debería devolver un rowid > 0", result > 0);
    }

    // Prueba de inserción con matrícula duplicada
    @Test
    public void testInsertDuplicatePrimaryKey() {
        Quad primerQuad = new Quad("DUP9999", "Monoplaza", 50.0f, "Primer quad");
        quadRepository.insert(primerQuad);

        // Intentar insertar otro quad con la misma matrícula
        Quad segundoQuad = new Quad("DUP9999", "Biplaza", 80.0f, "Segundo quad con misma matrícula");
        long result = quadRepository.insert(segundoQuad);
        assertTrue("La inserción con matrícula duplicada debería devolver <= 0", result <= 0);
    }

    // Q8: precio negativo
    @Test
    public void testPrecioNegativoEsRechazado() {
        Quad quad = new Quad("YYY7777", "Monoplaza", -10.0f, "Quad precio negativo");

        long result = quadRepository.insert(quad);

        assertEquals("La inserción con precio negativo debería devolver -1", -1, result);
        assertEquals("No debería insertarse ningún quad con precio negativo",
                0, quadRepository.getNumQuads());
    }

    @After
    public void tearDown() {
        quadRepository.deleteAll();
    }
}
