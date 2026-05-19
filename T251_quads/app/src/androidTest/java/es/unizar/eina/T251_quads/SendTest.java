package es.unizar.eina.T251_quads;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import es.unizar.eina.T251_quads.send.SMSImplementor;
import es.unizar.eina.T251_quads.send.SendAbstraction;
import es.unizar.eina.T251_quads.send.SendAbstractionImpl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public class SendTest {

    private static final String PHONE = "612345678";
    private static final String MESSAGE = "Reserva confirmada";

    @Test
    public void testSMSImplementorCreatesSmsIntent() {
        CapturingContext activity = new CapturingContext(ApplicationProvider.getApplicationContext());

        new SMSImplementor(activity).send(PHONE, MESSAGE);

        assertSmsIntent(activity.startedIntent);
    }

    @Test
    public void testSendAbstractionSmsDelegatesToSmsImplementor() {
        CapturingContext activity = new CapturingContext(ApplicationProvider.getApplicationContext());

        SendAbstraction sendAbstraction = new SendAbstractionImpl(activity, "sms");
        sendAbstraction.send(PHONE, MESSAGE);

        assertSmsIntent(activity.startedIntent);
    }

    private void assertSmsIntent(Intent intent) {
        assertNotNull("Se debería lanzar un Intent de SMS", intent);
        assertEquals("La acción del Intent debería ser ACTION_VIEW",
                Intent.ACTION_VIEW, intent.getAction());
        assertNotNull("El Intent debería incluir la URI SMS", intent.getData());
        assertEquals("La URI del Intent debería apuntar al teléfono indicado",
                "sms:" + PHONE, intent.getData().toString());
        assertEquals("El cuerpo del SMS debería coincidir",
                MESSAGE, intent.getStringExtra("sms_body"));
    }

    private static class CapturingContext extends ContextWrapper {
        private Intent startedIntent;

        CapturingContext(Context base) {
            super(base);
        }

        @Override
        public void startActivity(Intent intent) {
            startedIntent = intent;
        }
    }
}
