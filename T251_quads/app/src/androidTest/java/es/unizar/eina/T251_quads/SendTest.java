package es.unizar.eina.T251_quads;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.lang.reflect.Method;
import java.util.Arrays;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaConQuads;
import es.unizar.eina.T251_quads.ui.ReservaListActivity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SendTest {

    private static final String MATRICULA = "SMS0001";
    private static final String CLIENTE = "Raul";
    private static final String PHONE = "612345678";
    private static final String FECHA_RECOGIDA = "2026-05-10";
    private static final String FECHA_DEVOLUCION = "2026-05-11";

    @Test
    public void testEnviarSmsDesdeReserva() throws Exception {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        Intent[] startedIntent = new Intent[1];
        Instrumentation.ActivityMonitor monitor = new Instrumentation.ActivityMonitor(
                new IntentFilter(Intent.ACTION_VIEW),
                new Instrumentation.ActivityResult(Activity.RESULT_OK, null),
                true) {
            @Override
            public Instrumentation.ActivityResult onStartActivity(Intent intent) {
                startedIntent[0] = intent;
                return new Instrumentation.ActivityResult(Activity.RESULT_OK, null);
            }
        };
        instrumentation.addMonitor(monitor);

        ReservaConQuads reservaConQuads = new ReservaConQuads();
        reservaConQuads.reserva = new Reserva(
                CLIENTE, PHONE, FECHA_RECOGIDA, FECHA_DEVOLUCION, 1, 50.0);
        reservaConQuads.quads = Arrays.asList(
                new Quad(MATRICULA, "Monoplaza", 50.0f, "Quad para envío SMS"));

        Method enviarReserva = ReservaListActivity.class.getDeclaredMethod(
                "enviarReserva", ReservaConQuads.class, String.class);
        enviarReserva.setAccessible(true);

        try (ActivityScenario<ReservaListActivity> scenario =
                     ActivityScenario.launch(ReservaListActivity.class)) {
            scenario.onActivity(activity -> {
                try {
                    enviarReserva.invoke(activity, reservaConQuads, "sms");
                } catch (Exception e) {
                    throw new AssertionError(e);
                }
            });

            instrumentation.waitForMonitorWithTimeout(monitor, 5000);

            Intent intent = startedIntent[0];
            assertNotNull("Se debería lanzar un Intent de SMS", intent);
            assertEquals("La acción del Intent debería ser ACTION_VIEW",
                    Intent.ACTION_VIEW, intent.getAction());
            assertNotNull("El Intent debería incluir la URI SMS", intent.getData());
            assertEquals("La URI del Intent debería apuntar al teléfono indicado",
                    "sms:" + PHONE, intent.getData().toString());

            String smsBody = intent.getStringExtra("sms_body");
            assertNotNull("El cuerpo del SMS no debería ser null", smsBody);
            assertTrue("El SMS debería incluir el cliente", smsBody.contains(CLIENTE));
            assertTrue("El SMS debería incluir la fecha de recogida", smsBody.contains(FECHA_RECOGIDA));
            assertTrue("El SMS debería incluir la fecha de devolución", smsBody.contains(FECHA_DEVOLUCION));
            assertTrue("El SMS debería incluir los cascos", smsBody.contains("Cascos: 1"));
            assertTrue("El SMS debería incluir el quad asociado", smsBody.contains(MATRICULA));
        } finally {
            instrumentation.removeMonitor(monitor);
        }
    }
}
