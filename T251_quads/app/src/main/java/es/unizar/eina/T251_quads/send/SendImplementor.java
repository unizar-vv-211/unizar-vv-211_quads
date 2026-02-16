package es.unizar.eina.T251_quads.send;

import android.app.Activity;

/**
 * Define la interfaz para las clases de la implementación del patrón Bridge.
 * La interfaz no se tiene que corresponder directamente con la interfaz de la abstracción.
 * Se ha diseñado esta interfaz para permitir múltiples implementaciones concretas
 * (WhatsApp, SMS) que puedan ser intercambiables.
 */
public interface SendImplementor {

    /**
     * Se ha actualizado la actividad desde la cual se abrirá la actividad de envío.
     *
     * @param source La actividad de origen.
     */
    public void setSourceActivity(Activity source);

    /**
     * Se ha recuperado la actividad desde la cual se abrirá la actividad de envío.
     *
     * @return La actividad de origen.
     */
    public Activity getSourceActivity();

    /**
     * Se ha lanzado la actividad encargada de gestionar el envío del mensaje.
     *
     * @param phone El número de teléfono al que se ha de enviar el mensaje.
     * @param message El cuerpo del mensaje que se ha de enviar.
     */
    public void send(String phone, String message);
}
