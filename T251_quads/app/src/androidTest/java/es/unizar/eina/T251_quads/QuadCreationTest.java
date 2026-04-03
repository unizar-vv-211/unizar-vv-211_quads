package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

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
        Quad quad = new Quad("1234MON", "Monoplaza", 45.0f, "Quad monoplaza para prueba");
        long result = quadRepository.insert(quad);
        assertTrue("La inserción de un Monoplaza válido debería devolver un rowid > 0", result > 0);
    }

    // Prueba de inserción válida de un Biplaza
    @Test
    public void testInsertValidBiplaza() {
        Quad quad = new Quad("5678BIP", "Biplaza", 70.0f, "Quad biplaza para prueba");
        long result = quadRepository.insert(quad);
        assertTrue("La inserción de un Biplaza válido debería devolver un rowid > 0", result > 0);
    }

    // Prueba de inserción con matrícula duplicada (clave primaria repetida)
    @Test
    public void testInsertDuplicatePrimaryKey() {
        Quad primerQuad = new Quad("9999DUP", "Monoplaza", 50.0f, "Primer quad");
        quadRepository.insert(primerQuad);

        // Intentar insertar otro quad con la misma matrícula
        Quad segundoQuad = new Quad("9999DUP", "Biplaza", 80.0f, "Segundo quad con misma matrícula");
        long result = quadRepository.insert(segundoQuad);
        assertTrue("La inserción con matrícula duplicada debería devolver <= 0", result <= 0);
    }

    @After
    public void tearDown() {
        quadRepository.deleteAll();
    }
}
