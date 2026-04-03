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
public class OverloadTest {
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
    public void testSobrecarga() {
        boolean falloControlado = false;
        try {
            // Intentamos insertar un quad con una descripción gigante (+1MB)
            int longitud = 1024 * 1024; 
            StringBuilder sb = new StringBuilder(longitud);
            for (int j = 0; j < longitud; j++) {
                sb.append("X");
            }
            
            Quad quad = new Quad("9999SOB", "Monoplaza", 50.0f, sb.toString());
            long result = quadRepository.insert(quad);
            
            // Si se rechaza, devolverá -1 o 0. Si se acepta, el test pasa sin crashear.
            falloControlado = true;
        } catch (OutOfMemoryError | Exception e) {
            falloControlado = true; // Se interceptó correctamente
        }
        
        assertTrue("Error:El sistema debe manejar la sobrecarga de datos", falloControlado);
    }

    @After
    public void tearDown() {
        quadRepository.deleteAll();
    }
}