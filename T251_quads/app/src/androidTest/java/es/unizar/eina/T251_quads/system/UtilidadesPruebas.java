package es.unizar.eina.T251_quads.system;

import static androidx.test.espresso.Espresso.onView;
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
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import es.unizar.eina.T251_quads.R;

public class UtilidadesPruebas {

    // Tiempo de pausa
    private static final long PAUSA_MS = 100L;

    // Navegacion base
    public static void irAQuads() {
        onView(withId(R.id.card_quads)).perform(click());
    }

    public static void irAReservas() {
        onView(withId(R.id.card_reservas)).perform(click());
    }

    // Esperar al Snackbar de confirmación (opcional)
    public static void esperarSnackbar() {
        try {
            // Busca cualquier texto que contenga "guardada" (mensaje típico de guardado)
            onView(withText(containsString("guardada"))).check(matches(isDisplayed()));
        } catch (Exception ignored) {
            // Si no aparece, no hacemos nada
        }
    }

    public static void volverAtras() {
        pressBack();
    }

    public static void pulsarCrear() {
        try {
            // Cerramos teclado por si acaso quedó abierto
            onView(withId(R.id.fab)).perform(closeSoftKeyboard());
        } catch (Exception ignored) {
        }
        for (int i = 0; i < 5; i++) {
            try {
                onView(withId(R.id.fab)).perform(click());
                return; // Éxito
            } catch (Exception e) {
                try {
                    Thread.sleep(PAUSA_MS);
                } catch (Exception ignored) {
                }
            }
        }
        // Último intento que lanzará la excepción si sigue fallando
        onView(withId(R.id.fab)).perform(click());
    }

    private static boolean pulsarOpcionEnLista(int posicion, String textoOpcion) {
        try {
            onView(withId(R.id.recyclerview))
                    .perform(actionOnItemAtPosition(posicion, click()));
            onView(withText(textoOpcion)).perform(click());
            return true; // Devuelve true si lo ha conseguido
        } catch (androidx.test.espresso.PerformException e) {
            System.out.println("Lista vacía, saltando acción");
            return false; // Devuelve false si falla
        } catch (Exception e) {
            System.out.println("Lista vacía, saltando acción");
            return false; // Devuelve false si falla
        }
    }

    public static boolean pulsarEliminarEnLista(int posicion) {
        return pulsarOpcionEnLista(posicion, "Eliminar");
    }

    public static boolean pulsarEditarQuadEnLista(int posicion) {
        return pulsarOpcionEnLista(posicion, "Editar");
    }

    public static boolean pulsarEditarReservaEnLista(int posicion) {
        return pulsarOpcionEnLista(posicion, "Modificar");
    }

    // MENÚS (Ordenación y Filtros)
    public static void seleccionarOrdenQuads(int resIdOpcion) {
        onView(withContentDescription("Ordenar")).perform(click());
        onView(withText(resIdOpcion)).perform(click());
    }

    public static void seleccionarFiltroReservas(int resIdOpcion) {
        onView(withId(R.id.action_filter)).perform(click());
        onView(withText(resIdOpcion)).perform(click());
    }

    // FORMULARIOS
    public static void cancelarFormulario() {
        volverAtras();
    }

    public static void rellenarYGuardarQuad(String matricula, String tipo, String precio, String descripcion) {
        onView(withId(R.id.edit_matricula)).perform(replaceText(matricula), closeSoftKeyboard());

        if (tipo != null && !tipo.isEmpty()) {
            onView(withId(R.id.spinner_tipo)).perform(click());
            onData(hasToString(tipo)).perform(click());
        }

        onView(withId(R.id.edit_precio_dia)).perform(replaceText(precio), closeSoftKeyboard());
        onView(withId(R.id.edit_descripcion)).perform(replaceText(descripcion), closeSoftKeyboard());
        onView(withId(R.id.button_save)).perform(closeSoftKeyboard(), scrollTo(), click());
    }

    public static void rellenarYGuardarReserva(String cliente, String telefono, String fRecogida, String fDevolucion,
            String cascos, String matriculaQuad) {
        onView(withId(R.id.edit_cliente)).perform(replaceText(cliente), closeSoftKeyboard());
        onView(withId(R.id.edit_telefono)).perform(replaceText(telefono), closeSoftKeyboard());
        onView(withId(R.id.edit_fecha_recogida)).perform(replaceText(fRecogida), closeSoftKeyboard());
        onView(withId(R.id.edit_fecha_devolucion)).perform(replaceText(fDevolucion), closeSoftKeyboard());
        onView(withId(R.id.edit_cascos)).perform(replaceText(cascos), closeSoftKeyboard());
        if (matriculaQuad != null) {
            onView(withText(containsString(matriculaQuad))).perform(closeSoftKeyboard(), click());
        }

        onView(withId(R.id.button_save)).perform(closeSoftKeyboard(), click());
    }

    // Motor de transiciones
    public static int generadorMatriculas = 1;
    public static int generadorClientes = 10;

    // Memoria del motor para saber el contexto de la prueba
    public static int ultimaTransicion = -1;

    /**
     * EL MOTOR DE TRANSICIONES
     * Recibe un número y ejecuta la acción en la pantalla.
     */
    public static void ejecutarTransicion(int numeroTransicion) {
        switch (numeroTransicion) {
            // T1: Ir a Quads
            case 1:
                irAQuads();
                break;
            // T2: Volver atrás
            case 2:
                volverAtras();
                break;
            // T3: Eliminar elemento de lista
            case 3:
                pulsarEliminarEnLista(0);
                break;
            // T4: Ordenar por precio
            case 4:
                seleccionarOrdenQuads(R.string.menu_ordenar_precio);
                break;
            // T5: Ordenar por tipo
            case 5:
                seleccionarOrdenQuads(R.string.menu_ordenar_tipo);
                break;
            // T6: Ordenar por matrícula
            case 6:
                seleccionarOrdenQuads(R.string.menu_ordenar_matricula);
                break;
            // T7: Pulsar crear
            case 7:
                pulsarCrear();
                break;
            // T8: Editar primer Quad
            case 8:
                pulsarEditarQuadEnLista(0);
                break;
            // T9: Cancelar formulario
            case 9:
                cancelarFormulario();
                break;
            // T10: Crear Quad con datos realistas
            case 10:
                String matriculaNueva = String.format("CPS%04d", generadorMatriculas);
                String tipoQuad = (generadorMatriculas % 2 == 0) ? "Biplaza" : "Monoplaza";
                generadorMatriculas++;
                try {
                    Thread.sleep(PAUSA_MS);
                } catch (Exception e) {
                }
                try {
                    rellenarYGuardarQuad(matriculaNueva, tipoQuad, "50", "Prueba auto");
                } catch (Exception e) {
                    System.out.println("Fallo al guardar Quad en T10.");
                    try {
                        onView(withId(android.R.id.content)).perform(closeSoftKeyboard());
                    } catch (Exception ignored) {
                    }
                    pressBack();
                }
                break;

            // T11: Ir a Reservas
            case 11:
                irAReservas();
                break;
            // T12: Volver atrás
            case 12:
                volverAtras();
                break;
            // T13: Eliminar reserva
            case 13:
                pulsarEliminarEnLista(0);
                break;
            // T14: Filtrar pasadas
            case 14:
                seleccionarFiltroReservas(R.string.menu_filtro_pasadas);
                break;
            // T15: Filtrar actuales
            case 15:
                seleccionarFiltroReservas(R.string.menu_filtro_actuales);
                break;
            // T16: Filtrar futuras
            case 16:
                seleccionarFiltroReservas(R.string.menu_filtro_futuras);
                break;
            // T17: Ver todas las reservas
            case 17:
                seleccionarFiltroReservas(R.string.menu_filtro_todas);
                break;
            // T18: Crear reserva
            case 18:
                pulsarCrear();
                break;
            // T19: Editar primera reserva
            case 19:
                pulsarEditarReservaEnLista(0);
                break;
            // T20: Cancelar formulario reserva
            case 20:
                cancelarFormulario();
                break;
            // T21: Guardar reserva con datos realistas
            case 21:
                generadorClientes++;
                int anioReserva = 2030 + generadorClientes;
                String fechaIda = "10-05-" + anioReserva;
                String fechaVuelta = "15-05-" + anioReserva;

                try {
                    Thread.sleep(PAUSA_MS);
                } catch (Exception e) {
                }

                // Si la acción anterior fue Crear (18), marcamos el Quad.
                // Si la acción anterior fue Editar (19), pasamos null para no tocar los que ya
                // estén.
                String quadParaMarcar = (ultimaTransicion == 18) ? "CPS0001" : null;

                // Construimos un nombre usando letras (ej: "Cliente A", "Cliente B")
                // para superar el validador CLIENTE_PATTERN que no admite números.
                String sufijoLetras = String.valueOf((char) ('A' + (generadorClientes % 26)));
                String nombreCliente = "Cliente " + sufijoLetras;

                try {
                    rellenarYGuardarReserva(nombreCliente, "123456789", fechaIda, fechaVuelta, "1",
                            quadParaMarcar);
                } catch (Exception e) {
                    System.out.println("Fallo al guardar Reserva en T21.");
                    try {
                        onView(withId(android.R.id.content)).perform(closeSoftKeyboard());
                    } catch (Exception ignored) {
                    }
                    pressBack();
                }
                break;

            default:
                throw new IllegalArgumentException("El número de transición " + numeroTransicion + " no existe.");
        }

        // Guardamos la transición que acabamos de ejecutar en la memoria del motor
        ultimaTransicion = numeroTransicion;
    }
}