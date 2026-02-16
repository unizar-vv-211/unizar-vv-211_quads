package es.unizar.eina.T251_quads;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import es.unizar.eina.T251_quads.ui.QuadListActivity;
import es.unizar.eina.T251_quads.ui.ReservaListActivity;
import es.unizar.eina.T251_quads.ui.TestActivity;

/**
 * Activity principal de la aplicación que sirve como pantalla de entrada.
 * Se ha diseñado esta clase para proporcionar un menú de navegación simple
 * con dos opciones principales: gestionar quads y gestionar reservas.
 * Se han utilizado CardViews para mejorar la experiencia visual del usuario.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * Se ha inicializado la pantalla principal.
     * Se han configurado dos CardViews con listeners de click que navegan
     * a las respectivas pantallas de gestión de quads y reservas.
     *
     * @param savedInstanceState El estado previo de la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CardView cardQuads = findViewById(R.id.card_quads);
        CardView cardReservas = findViewById(R.id.card_reservas);
        CardView cardTests = findViewById(R.id.card_tests);

        cardQuads.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QuadListActivity.class);
            startActivity(intent);
        });

        cardReservas.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ReservaListActivity.class);
            startActivity(intent);
        });
        
        cardTests.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TestActivity.class);
            startActivity(intent);
        });
    }
}
