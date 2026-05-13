package es.unizar.eina.T251_quads.system;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import es.unizar.eina.T251_quads.MainActivity;
import es.unizar.eina.T251_quads.R;
import static es.unizar.eina.T251_quads.system.SystemTestHelpers.*;

@RunWith(AndroidJUnit4.class)
public class QuadSystemPathsTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void camino1CreacionQuadConCancelacionTest() {
        // T1 -> T7 -> T9 -> T7 -> T10 -> T2 -> T1 -> T2
        irAQuads();

        pulsarCrear();
        cancelarFormulario();

        pulsarCrear();
        // CORRECCIÓN: 3 letras y 4 números
        rellenarYGuardarQuad("NIA8974", "Monoplaza", "45", "Prueba de creación con cancelación previa");

        volverAtras();
        irAQuads();
        volverAtras();
    }

    @Test
    public void camino2EdicionYBorradoMultipleQuadsTest() {
        // T1 -> T3 -> T3 -> T8 -> T9 -> T8 -> T10 -> T3 -> T2 -> T11
        irAQuads();

        pulsarCrear();
        // CORRECCIÓN: 4 números
        rellenarYGuardarQuad("TMP1111", "Monoplaza", "30", "Temp 1");
        pulsarCrear();
        rellenarYGuardarQuad("TMP2222", "Biplaza", "30", "Temp 2");
        pulsarCrear();
        rellenarYGuardarQuad("TMP3333", "Monoplaza", "30", "Temp 3");

        pulsarEliminarEnLista(0);
        pulsarEliminarEnLista(0);

        pulsarEditarQuadEnLista(0);
        cancelarFormulario();

        pulsarEditarQuadEnLista(0);
        // CORRECCIÓN: 4 números
        rellenarYGuardarQuad("MOD4444", "Biplaza", "80", "Prueba de modificación de Quad");

        pulsarEliminarEnLista(0);
        volverAtras();
        irAReservas();
    }

    @Test
    public void camino3OrdenacionDinamicaQuadsTest() {
        // T1 -> T4 -> T5 -> T6 -> T4 -> T7 -> T10 -> T6 -> T3 -> T2
        irAQuads();

        seleccionarOrdenQuads(R.string.menu_ordenar_precio);
        seleccionarOrdenQuads(R.string.menu_ordenar_tipo);
        seleccionarOrdenQuads(R.string.menu_ordenar_matricula);
        seleccionarOrdenQuads(R.string.menu_ordenar_precio);

        pulsarCrear();
        rellenarYGuardarQuad("ORD9999", "Biplaza", "100", "Inserción forzando reordenación");

        seleccionarOrdenQuads(R.string.menu_ordenar_matricula);
        pulsarEliminarEnLista(0);
        volverAtras();
    }
}