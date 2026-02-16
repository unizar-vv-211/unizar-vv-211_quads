package es.unizar.eina.T251_quads.send;

import android.app.Activity;

/**
 * Implementa la interfaz de la abstracción utilizando (delegando a) una referencia
 * a un objeto de tipo implementor.
 * Se ha aplicado el patrón Bridge para desacoplar la abstracción de la implementación,
 * permitiendo seleccionar dinámicamente el método de envío (WhatsApp o SMS).
 */
public class SendAbstractionImpl implements SendAbstraction {

    /**
     * Objeto delegado que facilita la implementación del método send.
     * Se ha utilizado la composición en lugar de la herencia para mayor flexibilidad.
     */
    private SendImplementor implementor;

    /**
     * Constructor de la clase.
     * Se ha inicializado el objeto delegado según el método especificado.
     *
     * @param sourceActivity Actividad desde la cual se abrirá la actividad encargada de realizar el envío.
     * @param method Parámetro utilizado para instanciar el objeto delegado apropiado ("whatsapp" o "sms").
     */
    public SendAbstractionImpl(Activity sourceActivity, String method) {
        // Se ha seleccionado el implementador según el método especificado
        if ("sms".equalsIgnoreCase(method)) {
            implementor = new SMSImplementor(sourceActivity);
        } else {
            // Por defecto (o si method es "whatsapp"), se ha usado WhatsApp
            implementor = new WhatsAppImplementor(sourceActivity);
        }
    }

    /**
     * Se ha enviado el mensaje con el teléfono y cuerpo que se reciben como parámetros
     * a través de un objeto delegado.
     *
     * @param phone El número de teléfono al que se ha de enviar el mensaje.
     * @param message El cuerpo del mensaje que se ha de enviar.
     */
    public void send(String phone, String message) {
        implementor.send(phone, message);
    }
}
