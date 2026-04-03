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
public class VolumeTest {
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
    public void testVolumen() {
        int insertados = 0;
        for (int i = 0; i < 100; i++) {
            String matricula = String.format("%04d", i) + "AAA";
            Quad quad = new Quad(matricula, "Monoplaza", 50.0f, "Quad volumen " + i);
            long result = quadRepository.insert(quad);
            if (result > 0) insertados++;
        }
        assertTrue("Error: Se deberían insertar al menos 95 quads sin fallo", insertados >= 95);
    }

    @After
    public void tearDown() {
        quadRepository.deleteAll();
    }
}