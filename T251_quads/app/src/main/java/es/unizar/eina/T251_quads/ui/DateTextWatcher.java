package es.unizar.eina.T251_quads.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * TextWatcher personalizado para formatear automáticamente fechas en formato DD-MM-YYYY.
 * Los guiones se añaden automáticamente después de 2 dígitos (día) y 2 dígitos (mes).
 * Al borrar, se elimina el guion automáticamente si es necesario.
 */
public class DateTextWatcher implements TextWatcher {

    private EditText editText;
    private boolean isUpdating = false;
    private String oldText = "";

    public DateTextWatcher(EditText editText) {
        this.editText = editText;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (!isUpdating) {
            oldText = s.toString();
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // No hacer nada aquí
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (isUpdating) {
            return;
        }

        isUpdating = true;

        String text = s.toString();
        // Eliminar todos los guiones para obtener solo los números
        String cleanText = text.replaceAll("[^\\d]", "");

        // Limitar a 8 dígitos (DDMMYYYY)
        if (cleanText.length() > 8) {
            cleanText = cleanText.substring(0, 8);
        }

        // Construir el texto formateado con guiones
        StringBuilder formatted = new StringBuilder();
        
        for (int i = 0; i < cleanText.length(); i++) {
            // Añadir guion después del día (posición 2)
            if (i == 2 || i == 4) {
                formatted.append("-");
            }
            formatted.append(cleanText.charAt(i));
        }

        // Establecer el texto formateado
        editText.setText(formatted.toString());
        
        // Colocar el cursor al final
        editText.setSelection(formatted.length());

        isUpdating = false;
    }
}
