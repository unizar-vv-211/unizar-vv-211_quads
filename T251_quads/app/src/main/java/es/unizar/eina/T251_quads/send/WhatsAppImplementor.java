package es.unizar.eina.T251_quads.send;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Concrete implementor utilizando la aplicación de WhatsApp.
 * Se ha implementado esta clase para enviar mensajes a través de WhatsApp.
 * No funciona en el emulador si no se ha configurado previamente.
 * Se ha incluido un mecanismo de fallback a SMS si WhatsApp no está instalado.
 */
public class WhatsAppImplementor implements SendImplementor {

    /**
     * Actividad desde la cual se abrirá la aplicación de WhatsApp.
     */
    private Activity sourceActivity;

    /**
     * Constructor de la clase.
     * Se ha inicializado la actividad de origen.
     *
     * @param source Actividad desde la cual se abrirá la aplicación de WhatsApp.
     */
    public WhatsAppImplementor(Activity source) {
        setSourceActivity(source);
    }

    /**
     * Se ha actualizado la actividad desde la cual se abrirá la actividad de gestión de WhatsApp.
     *
     * @param source La actividad de origen.
     */
    public void setSourceActivity(Activity source) {
        sourceActivity = source;
    }

    /**
     * Se ha recuperado la actividad desde la cual se abrirá la aplicación de WhatsApp.
     *
     * @return La actividad de origen.
     */
    public Activity getSourceActivity() {
        return sourceActivity;
    }

    /**
     * Se ha implementado el método send utilizando la aplicación de WhatsApp.
     * Se ha utilizado la API oficial de WhatsApp (api.whatsapp.com/send).
     * Si WhatsApp no está instalado, se ha proporcionado un fallback automático a SMS.
     * El número de teléfono se ha limpiado de espacios y guiones.
     * El mensaje se ha codificado para URL.
     *
     * @param phone Teléfono (con código de país, ej: +34612345678).
     * @param message Cuerpo del mensaje que se ha de enviar.
     */
    public void send(String phone, String message) {
        try {
            // Se ha limpiado el número de teléfono (quitar espacios y guiones)
            String cleanPhone = phone.replaceAll("[\\s-]", "");
            
            // Se ha añadido el código de país de España si no está presente
            if (!cleanPhone.startsWith("+")) {
                cleanPhone = "+34" + cleanPhone;
            }

            // Se ha codificado el mensaje para URL
            String encodedMessage = android.net.Uri.encode(message);

            // Se ha utilizado la API oficial de WhatsApp
            String url = "https://api.whatsapp.com/send?phone=" + cleanPhone + "&text=" + encodedMessage;

            Intent sendIntent = new Intent(Intent.ACTION_VIEW);
            sendIntent.setData(android.net.Uri.parse(url));
            sendIntent.setPackage("com.whatsapp");

            // Se ha verificado si WhatsApp está instalado
            if (sendIntent.resolveActivity(sourceActivity.getPackageManager()) != null) {
                // WhatsApp está instalado, se ha lanzado la actividad
                sourceActivity.startActivity(sendIntent);
            } else {
                // WhatsApp no está instalado, se ha usado SMS como fallback
                android.widget.Toast.makeText(sourceActivity,
                        "WhatsApp no está instalado. Usando SMS...",
                        android.widget.Toast.LENGTH_SHORT).show();

                // Se ha lanzado SMS como alternativa
                android.net.Uri smsUri = android.net.Uri.parse("sms:" + phone);
                Intent smsIntent = new Intent(Intent.ACTION_VIEW, smsUri);
                smsIntent.putExtra("sms_body", message);
                sourceActivity.startActivity(smsIntent);
            }
        } catch (Exception e) {
            // Si ha habido algún error, se ha mostrado mensaje y se ha usado SMS
            android.widget.Toast.makeText(sourceActivity,
                    "Error al abrir WhatsApp. Abriendo SMS...",
                    android.widget.Toast.LENGTH_SHORT).show();

            android.net.Uri smsUri = android.net.Uri.parse("sms:" + phone);
            Intent smsIntent = new Intent(Intent.ACTION_VIEW, smsUri);
            smsIntent.putExtra("sms_body", message);
            sourceActivity.startActivity(smsIntent);
        }
    }
}
