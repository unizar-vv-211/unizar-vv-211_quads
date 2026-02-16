package es.unizar.eina.T251_quads.send;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Concrete implementor utilizando la actividad de envío de SMS.
 * Se ha implementado esta clase para enviar mensajes a través de la aplicación de SMS nativa de Android.
 * No funciona en el emulador si no se ha configurado previamente.
 */
public class SMSImplementor implements SendImplementor {

    /**
     * Actividad desde la cual se abrirá la actividad de envío de SMS.
     */
    private Activity sourceActivity;

    /**
     * Constructor de la clase.
     * Se ha inicializado la actividad de origen.
     *
     * @param source Actividad desde la cual se abrirá la actividad de envío de SMS.
     */
    public SMSImplementor(Activity source) {
        setSourceActivity(source);
    }

    /**
     * Se ha actualizado la actividad desde la cual se abrirá la actividad de envío de SMS.
     *
     * @param source La actividad de origen.
     */
    public void setSourceActivity(Activity source) {
        sourceActivity = source;
    }

    /**
     * Se ha recuperado la actividad desde la cual se abrirá la actividad de envío de SMS.
     *
     * @return La actividad de origen.
     */
    public Activity getSourceActivity() {
        return sourceActivity;
    }

    /**
     * Se ha implementado el método send utilizando la aplicación de envío de SMS nativa.
     * Se ha creado un Intent con ACTION_VIEW y un URI de tipo "sms".
     * El mensaje se ha pre-rellenado en el campo de texto de la aplicación de SMS.
     *
     * @param phone Número de teléfono al que se ha de enviar el mensaje.
     * @param message Cuerpo del mensaje que se ha de enviar.
     */
    public void send(String phone, String message) {
        // Se ha creado URI con el número de teléfono
        Uri smsUri = Uri.parse("sms:" + phone);
        // Se ha creado Intent para abrir la aplicación de SMS nativa
        Intent sendIntent = new Intent(Intent.ACTION_VIEW, smsUri);
        // Se ha añadido el mensaje pre-rellenado
        sendIntent.putExtra("sms_body", message);
        // Se ha lanzado la actividad
        sourceActivity.startActivity(sendIntent);
    }
}
