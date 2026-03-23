package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

@RunWith(AndroidJUnit4.class)
public class QuadDeletionTest {

    @Rule
    public ActivityScenarioRule<QuadListActivity> scenarioRule =
            new ActivityScenarioRule<>(QuadListActivity.class);

    private QuadRepository quadRepository;

    @Before
    public void setUp() {
        scenarioRule.getScenario().onActivity(activity -> {
            quadRepository = activity.getQuadRepository();
        });
        quadRepository.deleteAll();
    }

    @Test
    public void testCPD1_DeleteValidQuad() {
        Quad quad = new Quad("1111AAA", "Monoplaza", 50.0f, "Quad para borrar");
        quadRepository.insert(quad);
        
        int deleteResult = quadRepository.delete(quad);
        assertEquals("Error: Debería haberse borrado una fila", 1, deleteResult);
    }

    @Test
    public void testCPD2_DeleteNonExistentQuad() {
        Quad quadInexistente = new Quad("9999ZZZ", "Monoplaza", 50.0f, "No existe");
        
        int deleteResult = quadRepository.delete(quadInexistente);
        assertEquals("Error: Debería devolver 0 filas afectadas", 0, deleteResult);
    }

    @After
    public void tearDown() {
        quadRepository.deleteAll();
    }
}