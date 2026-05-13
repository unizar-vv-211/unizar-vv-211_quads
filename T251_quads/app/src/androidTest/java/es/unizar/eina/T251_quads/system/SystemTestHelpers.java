package es.unizar.eina.T251_quads.system;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.contrib.RecyclerViewActions.actionOnItemAtPosition;
import static androidx.test.espresso.Espresso.onData;
import static org.hamcrest.Matchers.hasToString;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static org.hamcrest.Matchers.containsString;
import static androidx.test.espresso.action.ViewActions.scrollTo;

import androidx.test.platform.app.InstrumentationRegistry;
import es.unizar.eina.T251_quads.R;

public class SystemTestHelpers {

    // NAVEGACIÓN BASE
    public static void irAQuads() {
        onView(withId(R.id.card_quads)).perform(click());
    }

    public static void irAReservas() {
        onView(withId(R.id.card_reservas)).perform(click());
    }

    public static void volverAtras() {
        pressBack();
    }

    public static void pulsarCrear() {
        onView(withId(R.id.fab)).perform(click());
    }

    private static void pulsarOpcionEnLista(int posicion, String textoOpcion) {
        onView(withId(R.id.recyclerview))
                .perform(actionOnItemAtPosition(posicion, click()));
        onView(withText(textoOpcion)).perform(click());
    }

    public static void pulsarEliminarEnLista(int posicion) {
        pulsarOpcionEnLista(posicion, "Eliminar");
    }

    public static void pulsarEditarQuadEnLista(int posicion) {
        pulsarOpcionEnLista(posicion, "Editar");
    }

    public static void pulsarEditarReservaEnLista(int posicion) {
        pulsarOpcionEnLista(posicion, "Modificar");
    }

    // MENÚS (Ordenación y Filtros)
    public static void seleccionarOrdenQuads(int resIdOpcion) {
        // Buscamos el botón directamente por su descripción en la barra superior
        onView(withContentDescription("Ordenar")).perform(click());
        // Pulsamos la opción del desplegable
        onView(withText(resIdOpcion)).perform(click());
    }

    public static void seleccionarFiltroReservas(int resIdOpcion) {
        // En reservas, usamos el ID del botón de filtro
        onView(withId(R.id.action_filter)).perform(click());
        // Pulsamos la opción del desplegable
        onView(withText(resIdOpcion)).perform(click());
    }

    // FORMULARIOS
    public static void cancelarFormulario() {
        volverAtras();
    }

    public static void rellenarYGuardarQuad(String matricula, String tipo, String precio, String descripcion) {
        onView(withId(R.id.edit_matricula)).perform(replaceText(matricula), closeSoftKeyboard());

        if (tipo != null && !tipo.isEmpty()) {
            // 1. Hacemos clic en el Spinner para que se despliegue
            onView(withId(R.id.spinner_tipo)).perform(click());

            // 2. Usamos onData para buscar el texto exacto DENTRO del menú desplegable
            onData(hasToString(tipo)).perform(click());
        } else {
            System.out.println("Tipo no proporcionado, se mantiene el valor por defecto del spinner.");
        }

        onView(withId(R.id.edit_precio_dia)).perform(replaceText(precio), closeSoftKeyboard());
        onView(withId(R.id.edit_descripcion)).perform(replaceText(descripcion), closeSoftKeyboard());
        onView(withId(R.id.button_save)).perform(click());
    }

    public static void rellenarYGuardarReserva(String cliente, String telefono, String fRecogida, String fDevolucion,
            String cascos, String matriculaQuad) {
        onView(withId(R.id.edit_cliente)).perform(replaceText(cliente), closeSoftKeyboard());
        onView(withId(R.id.edit_telefono)).perform(replaceText(telefono), closeSoftKeyboard());
        onView(withId(R.id.edit_fecha_recogida)).perform(replaceText(fRecogida), closeSoftKeyboard());
        onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText(fDevolucion), closeSoftKeyboard());
        onView(withId(R.id.edit_cascos)).perform(replaceText(cascos), closeSoftKeyboard());

        // Solo pulsamos el CheckBox si hemos especificado una matrícula (Creación)
        // Si pasamos null (Edición), dejamos los que ya estén marcados.
        if (matriculaQuad != null) {
            onView(withText(containsString(matriculaQuad))).perform(scrollTo(), click());
        }

        onView(withId(R.id.button_save)).perform(scrollTo(), click());
    }
}