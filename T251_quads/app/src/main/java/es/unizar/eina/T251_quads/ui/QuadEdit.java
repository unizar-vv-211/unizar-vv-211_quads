package es.unizar.eina.T251_quads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Pattern;

import es.unizar.eina.T251_quads.R;

/**
 * Activity (pantalla) utilizada para la creación de nuevos Quads o para la edición
 * de Quads existentes.
 * Se ha implementado en esta clase la lógica de validación de formato para la matrícula
 * y la selección del tipo de quad mediante un Spinner con opciones predefinidas.
 */
public class QuadEdit extends AppCompatActivity {

    /** Clave utilizada para devolver la matrícula del Quad. */
    public static final String EXTRA_REPLY_MATRICULA = "es.unizar.eina.T251_quads.REPLY_MATRICULA";
    
    /** Clave utilizada para devolver el tipo de Quad. */
    public static final String EXTRA_REPLY_TIPO = "es.unizar.eina.T251_quads.REPLY_TIPO";
    
    /** Clave utilizada para devolver el precio por día. */
    public static final String EXTRA_REPLY_PRECIO_DIA = "es.unizar.eina.T251_quads.REPLY_PRECIO_DIA";
    
    /** Clave utilizada para devolver la descripción. */
    public static final String EXTRA_REPLY_DESCRIPCION = "es.unizar.eina.T251_quads.REPLY_DESCRIPCION";

    /** Clave utilizada para recibir la matrícula del Quad a editar. */
    public static final String QUAD_MATRICULA = "es.unizar.eina.T251_quads.QUAD_MATRICULA";
    
    /** Clave utilizada para recibir el tipo del Quad a editar. */
    public static final String QUAD_TIPO = "es.unizar.eina.T251_quads.QUAD_TIPO";
    
    /** Clave utilizada para recibir el precio por día del Quad a editar. */
    public static final String QUAD_PRECIO_DIA = "es.unizar.eina.T251_quads.QUAD_PRECIO_DIA";
    
    /** Clave utilizada para recibir la descripción del Quad a editar. */
    public static final String QUAD_DESCRIPCION = "es.unizar.eina.T251_quads.QUAD_DESCRIPCION";

    /** Campo de texto para la matrícula. */
    private EditText mEditMatriculaView;
    
    /** Selector desplegable para el tipo de quad (Monoplaza/Biplaza). */
    private Spinner mSpinnerTipoView;
    
    /** Campo de texto para el precio por día. */
    private EditText mEditPrecioView;
    
    /** Campo de texto para la descripción. */
    private EditText mEditDescripcionView;

    /** Expresión regular utilizada para validar el formato de la matrícula (3 letras, 4 números). */
    private static final Pattern MATRICULA_PATTERN = Pattern.compile("^[A-Z]{3}\\d{4}$");

    /**
     * Se ha inicializado la pantalla.
     * Se han recuperado las referencias a las vistas y se ha configurado la lógica
     * del botón de guardado y la comprobación del modo (creación o edición).
     * El Spinner de tipo se ha configurado con un adaptador que utiliza el array de recursos.
     *
     * @param savedInstanceState El estado previo de la pantalla.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quadedit);

        mEditMatriculaView = findViewById(R.id.edit_matricula);
        mSpinnerTipoView = findViewById(R.id.spinner_tipo);
        mEditPrecioView = findViewById(R.id.edit_precio_dia);
        mEditDescripcionView = findViewById(R.id.edit_descripcion);

        final Button button = findViewById(R.id.button_save);

        final Intent intent = getIntent();
        if (intent.hasExtra(QUAD_MATRICULA)) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Editar Quad");
            }
            mEditMatriculaView.setText(intent.getStringExtra(QUAD_MATRICULA));
            mEditMatriculaView.setEnabled(false);

            String tipo = intent.getStringExtra(QUAD_TIPO);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                    R.array.quad_tipos, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            mSpinnerTipoView.setAdapter(adapter);
            
            if (tipo != null) {
                int spinnerPosition = adapter.getPosition(tipo);
                mSpinnerTipoView.setSelection(spinnerPosition);
            }

            mEditPrecioView.setText(String.valueOf(intent.getFloatExtra(QUAD_PRECIO_DIA, 0.0f)));
            mEditDescripcionView.setText(intent.getStringExtra(QUAD_DESCRIPCION));
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Nuevo Quad");
            }
        }

        button.setOnClickListener(view -> {
            Intent replyIntent = new Intent();

            if (TextUtils.isEmpty(mEditMatriculaView.getText()) ||
                    TextUtils.isEmpty(mEditPrecioView.getText()) ||
                    TextUtils.isEmpty(mEditDescripcionView.getText())) {

                Toast.makeText(this, "Rellene todos los campos", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED, replyIntent);

            } else if (!isMatriculaValida(mEditMatriculaView.getText().toString())) {
                Toast.makeText(this, "Formato de matrícula incorrecto (ej: AAA1111)", Toast.LENGTH_SHORT).show();
                setResult(RESULT_CANCELED, replyIntent);

            } else {
                String matricula = mEditMatriculaView.getText().toString();
                String tipo = mSpinnerTipoView.getSelectedItem().toString();
                float precioDia = Float.parseFloat(mEditPrecioView.getText().toString());
                String descripcion = mEditDescripcionView.getText().toString();

                replyIntent.putExtra(EXTRA_REPLY_MATRICULA, matricula);
                replyIntent.putExtra(EXTRA_REPLY_TIPO, tipo);
                replyIntent.putExtra(EXTRA_REPLY_PRECIO_DIA, precioDia);
                replyIntent.putExtra(EXTRA_REPLY_DESCRIPCION, descripcion);

                setResult(RESULT_OK, replyIntent);
                finish();
            }
        });
    }

    /**
     * Se ha implementado la lógica para comprobar si la matrícula cumple con el formato requerido.
     * Se ha utilizado una expresión regular pre-compilada.
     *
     * @param matricula El string de la matrícula que se ha de comprobar.
     * @return true si la matrícula es válida, false en caso contrario.
     */
    private boolean isMatriculaValida(String matricula) {
        if (matricula == null) {
            return false;
        }
        return MATRICULA_PATTERN.matcher(matricula).matches();
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
}