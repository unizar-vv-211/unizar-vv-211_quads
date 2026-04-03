package es.unizar.eina.T251_quads;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.ui.QuadListActivity;

@RunWith(AndroidJUnit4.class)
public class QuadUpdateTest {

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

    // Prueba de actualización de precio y descripción de un quad existente
    @Test
    public void testUpdatePriceAndDescription() throws InterruptedException {
        // Insertar quad inicial
        Quad quad = new Quad("7777UPD", "Monoplaza", 40.0f, "Descripción original");
        quadRepository.insert(quad);

        // Modificar campos en memoria
        quad.setPrecioDia(99.99f);
        quad.setDescripcion("Descripción actualizada");

        // Ejecutar actualización
        quadRepository.update(quad);
        Thread.sleep(500);

        // Recuperar el quad y verificar los cambios
        Quad quadRecuperado = quadRepository.getQuadByMatricula("7777UPD");
        assertNotNull("El quad no fue encontrado tras la actualización", quadRecuperado);
        assertEquals("El precio no se actualizó correctamente",
                99.99f, quadRecuperado.getPrecioDia(), 0.01f);
        assertEquals("La descripción no se actualizó correctamente",
                "Descripción actualizada", quadRecuperado.getDescripcion());
    }

    @After
    public void tearDown() {
        quadRepository.deleteAll();
    }
}
