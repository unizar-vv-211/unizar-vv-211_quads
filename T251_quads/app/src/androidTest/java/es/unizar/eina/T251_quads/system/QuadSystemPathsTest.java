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

    /**
     * Camino 1: Creación de Quad con Cancelación
     * Secuencia: T1 -> T7 -> T9 -> T7 -> T10 -> T2 -> T1 -> T2
     */
    @Test
    public void camino1CreacionQuadConCancelacionTest() {
        irAQuads(); // T1: Navegar a Quads

        pulsarCrear(); // T7: Abrir formulario creación
        cancelarFormulario(); // T9: Cancelar (Vuelta a lista)

        pulsarCrear(); // T7: Abrir formulario creación de nuevo
        rellenarYGuardarQuad("NIA8974", "Monoplaza", "45", "Prueba de creación con cancelación previa"); // T10: Guardar
                                                                                                         // Quad

        volverAtras(); // T2: Volver al menú principal
        irAQuads(); // T1: Volver a entrar a Quads
        volverAtras(); // T2: Volver al menú principal
    }

    /**
     * Camino 2: Edición y Borrado Múltiple de Quads
     * Secuencia: T1 -> T3 -> T3 -> T8 -> T9 -> T8 -> T10 -> T3 -> T2 -> T11
     */
    @Test
    public void camino2EdicionYBorradoMultipleQuadsTest() {
        irAQuads(); // T1: Navegar a Quads

        // Inyectamos datos para poder borrarlos
        pulsarCrear();
        rellenarYGuardarQuad("TMP1111", "Monoplaza", "30", "Temp 1");
        pulsarCrear();
        rellenarYGuardarQuad("TMP2222", "Biplaza", "30", "Temp 2");
        pulsarCrear();
        rellenarYGuardarQuad("TMP3333", "Monoplaza", "30", "Temp 3");

        pulsarEliminarEnLista(0); // T3: Eliminar primer quad
        pulsarEliminarEnLista(0); // T3: Eliminar segundo quad

        pulsarEditarQuadEnLista(0); // T8: Abrir formulario de modificación
        cancelarFormulario(); // T9: Cancelar (Vuelta a lista)

        pulsarEditarQuadEnLista(0); // T8: Abrir formulario de modificación de nuevo
        rellenarYGuardarQuad("MOD4444", "Biplaza", "80", "Prueba de modificación de Quad"); // T10: Guardar cambios

        pulsarEliminarEnLista(0); // T3: Eliminar el quad modificado
        volverAtras(); // T2: Volver al menú principal
        irAReservas(); // T11: Entrar a Reservas (Cruce de módulo)
    }

    /**
     * Camino 3: Ordenación Dinámica de Quads
     * Secuencia: T1 -> T4 -> T5 -> T6 -> T4 -> T7 -> T10 -> T6 -> T3 -> T2
     */
    @Test
    public void camino3OrdenacionDinamicaQuadsTest() {
        irAQuads(); // T1: Navegar a Quads

        seleccionarOrdenQuads(R.string.menu_ordenar_precio); // T4: Ordenar Precio
        seleccionarOrdenQuads(R.string.menu_ordenar_tipo); // T5: Ordenar Tipo
        seleccionarOrdenQuads(R.string.menu_ordenar_matricula); // T6: Ordenar Matrícula
        seleccionarOrdenQuads(R.string.menu_ordenar_precio); // T4: Ordenar Precio

        pulsarCrear(); // T7: Abrir formulario creación
        rellenarYGuardarQuad("ORD9999", "Biplaza", "100", "Inserción forzando reordenación"); // T10: Guardar Quad

        seleccionarOrdenQuads(R.string.menu_ordenar_matricula); // T6: Ordenar Matrícula
        pulsarEliminarEnLista(0); // T3: Eliminar quad
        volverAtras(); // T2: Volver al menú principal
    }
}