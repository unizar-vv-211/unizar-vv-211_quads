package es.unizar.eina.T251_quads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.QuadRoomDatabase;

/**
 * Activity utilizada para la creación y edición de Reservas.
 * Se ha implementado en esta clase una lógica compleja de validación que incluye:
 * - Validación de formato de teléfono (9 dígitos)
 * - Validación de formato de fechas (DD-MM-YYYY)
 * - Validación de fechas reales (días del mes, años bisiestos)
 * - Validación de rango de fechas (devolución posterior a recogida)
 * - Validación de número de cascos según tipos de quads seleccionados
 * - Validación asíncrona de disponibilidad de quads en el rango de fechas
 * 
 * Se han utilizado dos ViewModels: QuadViewModel para obtener la lista de quads disponibles,
 * y ReservaViewModel para verificar la disponibilidad de quads en las fechas seleccionadas.
 */
public class ReservaEdit extends AppCompatActivity {

    /** Clave utilizada para comunicar el ID de la reserva. */
    public static final String EXTRA_RESERVA_ID = "es.unizar.eina.T251_quads.RESERVA_ID";
    
    /** Clave utilizada para comunicar el nombre del cliente. */
    public static final String EXTRA_RESERVA_CLIENTE = "es.unizar.eina.T251_quads.RESERVA_CLIENTE";
    
    /** Clave utilizada para comunicar el teléfono del cliente. */
    public static final String EXTRA_RESERVA_TELEFONO = "es.unizar.eina.T251_quads.RESERVA_TELEFONO";
    
    /** Clave utilizada para comunicar la fecha de recogida. */
    public static final String EXTRA_RESERVA_FECHA_R = "es.unizar.eina.T251_quads.RESERVA_FECHA_R";
    
    /** Clave utilizada para comunicar la fecha de devolución. */
    public static final String EXTRA_RESERVA_FECHA_D = "es.unizar.eina.T251_quads.RESERVA_FECHA_D";
    
    /** Clave utilizada para comunicar el número de cascos. */
    public static final String EXTRA_RESERVA_CASCOS = "es.unizar.eina.T251_quads.RESERVA_CASCOS";
    
    /** Clave utilizada para comunicar la lista de quads seleccionados. */
    public static final String EXTRA_RESERVA_QUADS_SELECCIONADOS = "es.unizar.eina.T251_quads.RESERVA_QUADS_SELECCIONADOS";

    /** Campo de texto para el nombre del cliente. */
    private EditText mEditClienteView;
    
    /** Campo de texto para el teléfono del cliente. */
    private EditText mEditTelefonoView;
    
    /** Campo de texto para la fecha de recogida (formato DD-MM-YYYY). */
    private EditText mEditFechaRView;
    
    /** Campo de texto para la fecha de devolución (formato DD-MM-YYYY). */
    private EditText mEditFechaDView;
    
    /** Campo de texto para el número de cascos. */
    private EditText mEditCascosView;
    
    /** Contenedor donde se han añadido dinámicamente los checkboxes de quads. */
    private LinearLayout mQuadsContainer;
    
    /** Botón para guardar la reserva. */
    private Button mButtonSave;

    /** ViewModel utilizado para obtener la lista de quads disponibles. */
    private QuadViewModel mQuadViewModel;
    
    /** ViewModel utilizado para verificar disponibilidad de quads. */
    private ReservaViewModel mReservaViewModel;

    /** ID de la reserva en modo edición, o -1 en modo creación. */
    private int mId = -1;
    
    /** Mapa que asocia matrículas con sus checkboxes correspondientes. */
    private HashMap<String, CheckBox> mQuadCheckBoxes = new HashMap<>();
    
    /** Mapa que asocia matrículas con los objetos Quad completos. */
    private HashMap<String, Quad> mQuadData = new HashMap<>();
    
    /** Lista de quads que estaban seleccionados inicialmente (en modo edición). */
    private ArrayList<String> mQuadsSeleccionadosIniciales = new ArrayList<>();

    /** Patrón utilizado para validar el formato del teléfono (9 dígitos). */
    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\d{9}$");
    
    /** Patrón utilizado para validar el formato de fecha DD-MM-YYYY. */
    private static final Pattern FECHA_PATTERN = Pattern.compile("^\\d{2}-\\d{2}-\\d{4}$");

    /**
     * Se ha inicializado la pantalla de creación/edición de reservas.
     * Se han recuperado las referencias a las vistas, se han inicializado los ViewModels,
     * se ha poblado la lista de quads mediante observación de LiveData,
     * y se han configurado los TextWatchers para auto-formateo de fechas.
     *
     * @param savedInstanceState El estado previo de la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva_edit);

        mEditClienteView = findViewById(R.id.edit_cliente);
        mEditTelefonoView = findViewById(R.id.edit_telefono);
        mEditFechaRView = findViewById(R.id.edit_fecha_recogida);
        mEditFechaDView = findViewById(R.id.edit_fecha_devolucion);
        mEditCascosView = findViewById(R.id.edit_cascos);
        mQuadsContainer = findViewById(R.id.quad_checkbox_container);
        mButtonSave = findViewById(R.id.button_save);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);
        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);

        final Intent intent = getIntent();

        if (intent.hasExtra(EXTRA_RESERVA_ID)) {
            getSupportActionBar().setTitle("Editar Reserva");
            mId = intent.getIntExtra(EXTRA_RESERVA_ID, -1);
            mEditClienteView.setText(intent.getStringExtra(EXTRA_RESERVA_CLIENTE));
            mEditTelefonoView.setText(intent.getStringExtra(EXTRA_RESERVA_TELEFONO));
            
            String fechaRBD = intent.getStringExtra(EXTRA_RESERVA_FECHA_R);
            String fechaDBD = intent.getStringExtra(EXTRA_RESERVA_FECHA_D);
            mEditFechaRView.setText(convertYYYYMMDDtoDDMMYYYY(fechaRBD));
            mEditFechaDView.setText(convertYYYYMMDDtoDDMMYYYY(fechaDBD));
            
            int cascos = intent.getIntExtra(EXTRA_RESERVA_CASCOS, 0);
            mEditCascosView.setText(String.valueOf(cascos));

            if (intent.hasExtra(EXTRA_RESERVA_QUADS_SELECCIONADOS)) {
                mQuadsSeleccionadosIniciales = intent.getStringArrayListExtra(EXTRA_RESERVA_QUADS_SELECCIONADOS);
            }
        } else {
            getSupportActionBar().setTitle("Nueva Reserva");
        }

        mQuadViewModel.getAllQuads().observe(this, this::poblarListaDeQuads);

        mEditFechaRView.addTextChangedListener(new DateTextWatcher(mEditFechaRView));
        mEditFechaDView.addTextChangedListener(new DateTextWatcher(mEditFechaDView));

        mButtonSave.setOnClickListener(view -> guardarReserva());
    }

    /**
     * Se ha poblado dinámicamente el contenedor de quads con checkboxes.
     * Para cada quad disponible en el sistema, se ha creado un CheckBox que muestra
     * la matrícula y el tipo. En modo edición, se han marcado los quads previamente seleccionados.
     *
     * @param quads Lista de quads disponibles en el sistema.
     */
    private void poblarListaDeQuads(List<Quad> quads) {
        mQuadsContainer.removeAllViews();
        mQuadCheckBoxes.clear();
        mQuadData.clear();

        if (quads == null || quads.isEmpty()) {
            TextView noQuadsText = new TextView(this);
            noQuadsText.setText("No hay quads registrados en el sistema.");
            mQuadsContainer.addView(noQuadsText);
            return;
        }

        for (Quad quad : quads) {
            CheckBox cb = new CheckBox(this);
            cb.setText(quad.getMatricula() + " (" + quad.getTipo() + ")");
            cb.setTag(quad.getMatricula());

            if (mId != -1 && mQuadsSeleccionadosIniciales.contains(quad.getMatricula())) {
                cb.setChecked(true);
            }

            mQuadsContainer.addView(cb);
            mQuadCheckBoxes.put(quad.getMatricula(), cb);
            mQuadData.put(quad.getMatricula(), quad);
        }
    }

    /**
     * Se ha implementado la lógica completa de validación y guardado de la reserva.
     * El proceso incluye múltiples validaciones en cascada:
     * 1. Validación de campos vacíos
     * 2. Validación de formato de teléfono
     * 3. Validación de formato de fechas
     * 4. Validación de fechas reales (días del mes, años bisiestos)
     * 5. Validación de rango de fechas (devolución > recogida)
     * 6. Validación de selección de al menos un quad
     * 7. Validación de formato numérico de cascos
     * 8. Validación de cascos según tipos de quads
     * 9. Validación asíncrona de disponibilidad de quads
     *
     * Si todas las validaciones son exitosas, se ha devuelto el resultado a la Activity anterior.
     * La validación de disponibilidad se ejecuta en un thread en background para no bloquear la UI.
     */
    private void guardarReserva() {
        Intent replyIntent = new Intent();

        String cliente = mEditClienteView.getText().toString();
        String telefono = mEditTelefonoView.getText().toString();
        String fechaR = mEditFechaRView.getText().toString();
        String fechaD = mEditFechaDView.getText().toString();
        String cascosStr = mEditCascosView.getText().toString();

        if (TextUtils.isEmpty(cliente) || TextUtils.isEmpty(telefono) ||
                TextUtils.isEmpty(fechaR) || TextUtils.isEmpty(fechaD) || TextUtils.isEmpty(cascosStr)) {
            Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }

        if (!isTelefonoValido(telefono)) {
            Toast.makeText(this, "Formato de teléfono incorrecto (debe tener 9 dígitos)", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }

        if (!isFechaValida(fechaR) || !isFechaValida(fechaD)) {
            Toast.makeText(this, "Formato de fecha incorrecto (debe ser DD-MM-YYYY)", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }
        
        if (!esFechaReal(fechaR)) {
            Toast.makeText(this, "La fecha de recogida no es válida", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }
        
        if (!esFechaReal(fechaD)) {
            Toast.makeText(this, "La fecha de devolución no es válida", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }
        
        String fechaRBD = convertDDMMYYYYtoYYYYMMDD(fechaR);
        String fechaDBD = convertDDMMYYYYtoYYYYMMDD(fechaD);
        
        if (!esFechaPosterior(fechaRBD, fechaDBD)) {
            Toast.makeText(this, "La fecha de devolución debe ser posterior a la de recogida", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }

        ArrayList<String> quadsSeleccionados = new ArrayList<>();
        for (String matricula : mQuadCheckBoxes.keySet()) {
            CheckBox cb = mQuadCheckBoxes.get(matricula);
            if (cb != null && cb.isChecked()) {
                quadsSeleccionados.add((String) cb.getTag());
            }
        }

        if (quadsSeleccionados.isEmpty()) {
            Toast.makeText(this, "Debe seleccionar al menos un quad", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }

        int cascos;
        try {
            cascos = Integer.parseInt(cascosStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "El número de cascos debe ser un número válido", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }

        if (cascos < 0) {
            Toast.makeText(this, "El número de cascos no puede ser negativo", Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }

        int maxCascos = calcularMaximoCascos(quadsSeleccionados);
        if (cascos > maxCascos) {
            Toast.makeText(this, "Máximo de cascos para los quads seleccionados: " + maxCascos, Toast.LENGTH_SHORT).show();
            setResult(RESULT_CANCELED, replyIntent);
            return;
        }

        // Se ha ejecutado la validación de disponibilidad en un thread en background
        final int reservaId = (mId != -1) ? mId : -1;
        final String fechaInicioFinal = fechaRBD;
        final String fechaFinFinal = fechaDBD;
        final ArrayList<String> quadsSeleccionadosFinal = new ArrayList<>(quadsSeleccionados);
        final int cascosFinal = cascos;
        
        QuadRoomDatabase.databaseWriteExecutor.execute(() -> {
            List<String> quadsNoDisponibles = mReservaViewModel.comprobarDisponibilidad(
                quadsSeleccionadosFinal, fechaInicioFinal, fechaFinFinal, reservaId);
            
            runOnUiThread(() -> {
                if (quadsNoDisponibles != null && !quadsNoDisponibles.isEmpty()) {
                    String mensaje = "Los siguientes quads no están disponibles en esas fechas: " +
                            String.join(", ", quadsNoDisponibles);
                    Toast.makeText(this, mensaje, Toast.LENGTH_LONG).show();
                    setResult(RESULT_CANCELED, replyIntent);
                } else {
                    replyIntent.putExtra(EXTRA_RESERVA_CLIENTE, cliente);
                    replyIntent.putExtra(EXTRA_RESERVA_TELEFONO, telefono);
                    replyIntent.putExtra(EXTRA_RESERVA_FECHA_R, fechaInicioFinal);
                    replyIntent.putExtra(EXTRA_RESERVA_FECHA_D, fechaFinFinal);
                    replyIntent.putExtra(EXTRA_RESERVA_CASCOS, cascosFinal);
                    replyIntent.putExtra(EXTRA_RESERVA_QUADS_SELECCIONADOS, quadsSeleccionadosFinal);

                    if (mId != -1) {
                        replyIntent.putExtra(EXTRA_RESERVA_ID, mId);
                    }

                    setResult(RESULT_OK, replyIntent);
                    finish();
                }
            });
        });
    }

    /**
     * Se ha calculado el número máximo de cascos permitido según los tipos de quads seleccionados.
     * La lógica implementada suma las capacidades individuales:
     * - Monoplaza: 1 casco
     * - Biplaza: 2 cascos
     *
     * @param matriculas Lista de matrículas de quads seleccionados.
     * @return El número máximo de cascos permitido (suma de capacidades).
     */
    private int calcularMaximoCascos(ArrayList<String> matriculas) {
        int total = 0;
        for (String matricula : matriculas) {
            Quad quad = mQuadData.get(matricula);
            if (quad != null) {
                if ("Monoplaza".equals(quad.getTipo())) {
                    total += 1;
                } else if ("Biplaza".equals(quad.getTipo())) {
                    total += 2;
                }
            }
        }
        return total;
    }

    /**
     * Se ha validado que el teléfono cumple con el formato requerido (9 dígitos).
     *
     * @param telefono El string del teléfono que se ha de validar.
     * @return true si el formato es válido, false en caso contrario.
     */
    private boolean isTelefonoValido(String telefono) {
        if (telefono == null) return false;
        return TELEFONO_PATTERN.matcher(telefono).matches();
    }

    /**
     * Se ha validado que la fecha cumple con el formato DD-MM-YYYY.
     *
     * @param fecha El string de la fecha que se ha de validar.
     * @return true si el formato es válido, false en caso contrario.
     */
    private boolean isFechaValida(String fecha) {
        if (fecha == null) return false;
        return FECHA_PATTERN.matcher(fecha).matches();
    }

    /**
     * Se ha manejado el clic en el botón "atrás" de la barra de herramientas.
     *
     * @return true si la navegación ha sido procesada.
     */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
    
    /**
     * Se ha convertido una fecha del formato DD-MM-YYYY al formato YYYY-MM-DD.
     * Esta conversión se ha implementado para almacenar las fechas en la base de datos
     * en un formato que permite ordenamiento y comparación directa de strings.
     *
     * @param fecha La fecha en formato DD-MM-YYYY.
     * @return La fecha en formato YYYY-MM-DD, o la fecha original si no cumple el formato esperado.
     */
    private String convertDDMMYYYYtoYYYYMMDD(String fecha) {
        if (fecha == null || !FECHA_PATTERN.matcher(fecha).matches()) {
            return fecha;
        }
        String[] parts = fecha.split("-");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }
    
    /**
     * Se ha convertido una fecha del formato YYYY-MM-DD al formato DD-MM-YYYY.
     * Esta conversión se ha implementado para mostrar las fechas en la interfaz
     * de usuario en un formato más familiar para el usuario español.
     *
     * @param fecha La fecha en formato YYYY-MM-DD.
     * @return La fecha en formato DD-MM-YYYY, o la fecha original si no cumple el formato esperado.
     */
    private String convertYYYYMMDDtoDDMMYYYY(String fecha) {
        if (fecha == null || fecha.isEmpty()) {
            return "";
        }
        if (!fecha.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return fecha;
        }
        String[] parts = fecha.split("-");
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }
    
    /**
     * Se ha validado que la fecha corresponde a un día real del calendario.
     * La validación incluye:
     * - Verificación de que el mes está en el rango 1-12
     * - Verificación de que el día está en el rango válido para ese mes
     * - Consideración de años bisiestos para el mes de febrero
     *
     * @param fecha La fecha en formato DD-MM-YYYY que se ha de validar.
     * @return true si la fecha es real, false en caso contrario.
     */
    private boolean esFechaReal(String fecha) {
        if (fecha == null || !FECHA_PATTERN.matcher(fecha).matches()) {
            return false;
        }
        
        try {
            String[] parts = fecha.split("-");
            int dia = Integer.parseInt(parts[0]);
            int mes = Integer.parseInt(parts[1]);
            int anio = Integer.parseInt(parts[2]);
            
            if (mes < 1 || mes > 12) {
                return false;
            }
            
            int[] diasPorMes = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
            
            if (esBisiesto(anio)) {
                diasPorMes[1] = 29;
            }
            
            if (dia < 1 || dia > diasPorMes[mes - 1]) {
                return false;
            }
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Se ha determinado si un año es bisiesto.
     * Un año es bisiesto si es divisible por 4, excepto los años divisibles por 100,
     * a menos que también sean divisibles por 400.
     *
     * @param anio El año que se ha de comprobar.
     * @return true si el año es bisiesto, false en caso contrario.
     */
    private boolean esBisiesto(int anio) {
        return (anio % 4 == 0 && anio % 100 != 0) || (anio % 400 == 0);
    }
    
    /**
     * Se ha comparado dos fechas para verificar que la segunda es posterior a la primera.
     * La comparación se ha realizado mediante comparación alfanumérica de strings
     * en formato YYYY-MM-DD, que garantiza el orden correcto.
     *
     * @param fecha1 La fecha de inicio en formato YYYY-MM-DD.
     * @param fecha2 La fecha de fin en formato YYYY-MM-DD.
     * @return true si fecha2 es posterior a fecha1, false en caso contrario.
     */
    private boolean esFechaPosterior(String fecha1, String fecha2) {
        try {
            return fecha2.compareTo(fecha1) > 0;
        } catch (Exception e) {
            return false;
        }
    }
}