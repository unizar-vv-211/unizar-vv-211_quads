package es.unizar.eina.T251_quads.send;

/**
 * Define la interfaz de la abstracción del patrón Bridge.
 * Se ha diseñado esta interfaz para desacoplar la lógica de alto nivel (envío de mensajes)
 * de las implementaciones concretas (WhatsApp, SMS).
 */
public interface SendAbstraction {

    /**
     * Se ha enviado un mensaje a un número de teléfono.
     *
     * @param phone El número de teléfono al que se ha de enviar el mensaje.
     * @param message El cuerpo del mensaje que se ha de enviar.
     */
    public void send(String phone, String message);
}
