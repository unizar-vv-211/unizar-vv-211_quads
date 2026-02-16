package es.unizar.eina.T251_quads.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.QuadRepository;
import es.unizar.eina.T251_quads.database.ReservaRepository;
import es.unizar.eina.T251_quads.database.UnitTests;

/**
 * Activity para ejecutar las pruebas unitarias de la aplicación.
 * Permite ejecutar todas las pruebas y ver los resultados en pantalla.
 * 
 * IMPORTANTE: Los resultados detallados se muestran en LogCat con el tag "Pruebas".
 */
public class TestActivity extends AppCompatActivity {

    private QuadRepository quadRepository;
    private ReservaRepository reservaRepository;
    private Button btnRunTests;
    private Button btnClearDatabase;
    private TextView tvStatus;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Inicializar repositorios
        quadRepository = new QuadRepository(getApplication());
        reservaRepository = new ReservaRepository(getApplication());

        // Inicializar vistas
        btnRunTests = findViewById(R.id.btn_run_tests);
        btnClearDatabase = findViewById(R.id.btn_clear_database);
        tvStatus = findViewById(R.id.tv_status);
        scrollView = findViewById(R.id.scroll_view);

        // Configurar botones
        btnRunTests.setOnClickListener(v -> runTests());
        btnClearDatabase.setOnClickListener(v -> clearDatabase());

        // Mensaje inicial
        tvStatus.setText("Presiona el botón para ejecutar las pruebas.\n\n" +
                "Se ejecutarán:\n" +
                "• 9 pruebas de inserción de Quads\n" +
                "• 3 pruebas de eliminación de Quads\n" +
                "• 4 pruebas de Reservas\n" +
                "• Prueba de volumen (100 Quads)\n" +
                "• Prueba de sobrecarga");
    }

    /**
     * Ejecuta todas las pruebas en un hilo secundario.
     */
    private void runTests() {
        // Deshabilitar botón durante la ejecución
        btnRunTests.setEnabled(false);
        
        // Actualizar estado
        runOnUiThread(() -> {
            tvStatus.setText("Ejecutando pruebas...\n\nEspera un momento.");
            scrollView.fullScroll(ScrollView.FOCUS_UP);
        });

        // Ejecutar pruebas en hilo secundario
        new Thread(() -> {
            try {
                // Crear instancia de UnitTests
                UnitTests tests = new UnitTests(quadRepository, reservaRepository);
                
                // Ejecutar todas las pruebas
                long startTime = System.currentTimeMillis();
                tests.runAllTests();
                long duration = System.currentTimeMillis() - startTime;

                // Actualizar UI con resultado
                runOnUiThread(() -> {
                    tvStatus.setText("Pruebas completadas\n\n" +
                            "Tiempo: " + duration + " ms\n\n" +
                            "Revisa LogCat para ver los detalles.");
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                    btnRunTests.setEnabled(true);
                    
                    Toast.makeText(TestActivity.this, 
                            "Completado en " + duration + " ms", 
                            Toast.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                // Manejar errores
                runOnUiThread(() -> {
                    tvStatus.setText("Error al ejecutar las pruebas\n\n" +
                            e.getClass().getSimpleName() + ": " + e.getMessage());
                    scrollView.fullScroll(ScrollView.FOCUS_UP);
                    btnRunTests.setEnabled(true);
                    
                    Toast.makeText(TestActivity.this, 
                            "Error en las pruebas", 
                            Toast.LENGTH_SHORT).show();
                });
                e.printStackTrace();
            }
        }).start();
    }
    
    /**
     * Limpia toda la base de datos.
     */
    private void clearDatabase() {
        // Deshabilitar botones durante la operación
        btnRunTests.setEnabled(false);
        btnClearDatabase.setEnabled(false);
        
        tvStatus.setText("Limpiando base de datos...\n\nEspera un momento.");
        
        new Thread(() -> {
            try {
                // Primero eliminar todas las reservas (por la FK)
                reservaRepository.deleteAll();
                Thread.sleep(500); // Esperar a que se complete
                
                // Luego eliminar todos los quads
                quadRepository.deleteAll();
                Thread.sleep(500);
                
                runOnUiThread(() -> {
                    tvStatus.setText("✅ Base de datos limpiada correctamente.\n\n" +
                            "Todos los Quads y Reservas han sido eliminados.\n\n" +
                            "Puedes ejecutar las pruebas de nuevo.");
                    btnRunTests.setEnabled(true);
                    btnClearDatabase.setEnabled(true);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvStatus.setText("❌ Error al limpiar la base de datos:\n" + e.getMessage());
                    btnRunTests.setEnabled(true);
                    btnClearDatabase.setEnabled(true);
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Limpiar referencias
        quadRepository = null;
        reservaRepository = null;
    }
}
