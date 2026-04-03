package es.unizar.eina.T251_quads.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.QuadRepository;

import static androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult;

/**
 * Activity principal para la gestión del listado de Quads.
 * Se ha implementado en esta clase la interfaz de usuario que permite visualizar,
 * crear, editar y eliminar quads del sistema.
 * Se ha utilizado un RecyclerView con un adaptador personalizado para mostrar la lista.
 * Las acciones de editar y eliminar se han activado mediante un PopupMenu al hacer clic corto en un item.
 */
public class QuadListActivity extends AppCompatActivity {

    /** ViewModel que gestiona el acceso a los datos de Quads. */
    private QuadViewModel mQuadViewModel;

    /** RecyclerView que contiene la lista de quads. */
    RecyclerView mRecyclerView;
    
    /** Adaptador personalizado para el RecyclerView. */
    QuadListAdapter mAdapter;
    
    /** Botón flotante para crear nuevos quads. */
    FloatingActionButton mFab;

    /**
     * Se ha inicializado la pantalla de lista de quads.
     * Se han configurado el RecyclerView, el adaptador, el ViewModel,
     * y se han establecido los observadores de LiveData para actualizar la UI automáticamente.
     * Se ha configurado un listener de clic corto para mostrar un menú de opciones.
     *
     * @param savedInstanceState El estado previo de la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quad);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Quads");
        }

        mRecyclerView = findViewById(R.id.recyclerview);

        // Se ha configurado el listener para click corto
        QuadListAdapter.OnItemClickListener clickListener = (quad, view, selectionMode) -> {
            if (selectionMode) {
                // En modo selección, alternar selección del item
                mAdapter.toggleSelection(quad.getMatricula());
                updateToolbarTitle();
                
                // Si no hay items seleccionados, salir del modo selección
                if (mAdapter.getSelectedCount() == 0) {
                    exitSelectionMode();
                }
            } else {
                // Modo normal: mostrar menú de opciones
                mostrarMenuOpciones(view, quad);
            }
        };
        
        // Configurar listener para long press
        QuadListAdapter.OnItemLongClickListener longClickListener = (quad, view) -> {
            if (!mAdapter.isSelectionMode()) {
                // Activar modo selección
                mAdapter.setSelectionMode(true);
                mAdapter.toggleSelection(quad.getMatricula());
                updateToolbarTitle();
                invalidateOptionsMenu(); // Refrescar menú
            }
        };

        mAdapter = new QuadListAdapter(new QuadListAdapter.QuadDiff(), clickListener);
        mAdapter.setOnItemLongClickListener(longClickListener);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mQuadViewModel = new ViewModelProvider(this).get(QuadViewModel.class);

        // Se ha observado la lista de quads para actualizar el adaptador automáticamente
        mQuadViewModel.getAllQuads().observe(this, quads -> {
            mAdapter.submitList(quads);
        });

        // Se ha observado el LiveData de eventos de Toast para mostrar mensajes asíncronos
        mQuadViewModel.getToastEvent().observe(this, message -> {
            if (message != null) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        });

        mFab = findViewById(R.id.fab);
        mFab.setOnClickListener(view -> createQuad());
    }
    
    /**
     * Actualiza el título de la toolbar según el modo.
     */
    private void updateToolbarTitle() {
        if (mAdapter.isSelectionMode()) {
            int count = mAdapter.getSelectedCount();
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(count + " seleccionado" + (count != 1 ? "s" : ""));
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle("Gestión de Quads");
            }
        }
    }
    
    /**
     * Sale del modo de selección.
     */
    private void exitSelectionMode() {
        mAdapter.clearSelection();
        updateToolbarTitle();
        invalidateOptionsMenu();
    }
    
    /**
     * Elimina los quads seleccionados.
     */
    private void deleteSelectedQuads() {
        java.util.Set<String> selectedMatriculas = mAdapter.getSelectedItems();
        int count = selectedMatriculas.size();
        
        if (count == 0) {
            Toast.makeText(this, "No hay quads seleccionados", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Obtener la lista actual de quads
        mQuadViewModel.getAllQuads().observe(this, quads -> {
            for (Quad quad : quads) {
                if (selectedMatriculas.contains(quad.getMatricula())) {
                    mQuadViewModel.delete(quad);
                }
            }
        });
        
        Toast.makeText(this, "Eliminando " + count + " quad" + (count != 1 ? "s" : ""), Toast.LENGTH_SHORT).show();
        exitSelectionMode();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mAdapter != null && mAdapter.isSelectionMode()) {
            getMenuInflater().inflate(R.menu.menu_quad_selection, menu);
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_selected) {
            deleteSelectedQuads();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public void onBackPressed() {
        if (mAdapter != null && mAdapter.isSelectionMode()) {
            exitSelectionMode();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Se ha mostrado un PopupMenu con las opciones disponibles para un quad específico.
     * Las opciones incluidas son: Editar y Eliminar.
     *
     * @param view La vista desde la cual se ha de mostrar el menú popup.
     * @param quad El quad sobre el cual se han de aplicar las acciones.
     */
    private void mostrarMenuOpciones(View view, Quad quad) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_quad_opciones, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.action_editar_quad) {
                editQuad(quad);
                return true;
            } else if (id == R.id.action_eliminar_quad) {
                Toast.makeText(getApplicationContext(), "Deleting " + quad.getMatricula(), Toast.LENGTH_LONG).show();
                mQuadViewModel.delete(quad);
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    /**
     * Se ha iniciado el flujo de creación de un nuevo quad.
     * Se ha lanzado la actividad QuadEdit en modo creación.
     */
    private void createQuad() {
        mStartCreateQuad.launch(new Intent(this, QuadEdit.class));
    }

    /**
     * Launcher que maneja el resultado de la creación de un quad.
     * Cuando se recibe un resultado exitoso, se ha extraído la información del Intent
     * y se ha insertado el nuevo quad mediante el ViewModel.
     */
    ActivityResultLauncher<Intent> mStartCreateQuad = newActivityResultLauncher(new ExecuteActivityResult() {
        @Override
        public void process(Bundle extras, Quad quad) {
            mQuadViewModel.insert(quad);
        }
    });

    /**
     * Se ha creado un ActivityResultLauncher genérico que recibe una interfaz funcional
     * para procesar el resultado de una actividad.
     * Este método factoriza el código común de creación de launchers.
     *
     * @param executable La interfaz funcional que se ha de ejecutar cuando se reciba un resultado exitoso.
     * @return El ActivityResultLauncher configurado.
     */
    private ActivityResultLauncher<Intent> newActivityResultLauncher(ExecuteActivityResult executable) {
        return registerForActivityResult(
                new StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Bundle extras = result.getData().getExtras();
                        String matricula = extras.getString(QuadEdit.EXTRA_REPLY_MATRICULA);
                        String tipo = extras.getString(QuadEdit.EXTRA_REPLY_TIPO);
                        float precioDia = extras.getFloat(QuadEdit.EXTRA_REPLY_PRECIO_DIA, 0.0f);
                        String descripcion = extras.getString(QuadEdit.EXTRA_REPLY_DESCRIPCION);

                        Quad quad = new Quad(matricula, tipo, precioDia, descripcion);
                        executable.process(extras, quad);
                    }
                });
    }

    /**
     * Se ha iniciado el flujo de edición de un quad existente.
     * Se han empaquetado los datos del quad actual en un Intent y se ha lanzado QuadEdit.
     *
     * @param current El quad que se ha de editar.
     */
    private void editQuad(Quad current) {
        Intent intent = new Intent(this, QuadEdit.class);

        intent.putExtra(QuadEdit.QUAD_MATRICULA, current.getMatricula());
        intent.putExtra(QuadEdit.QUAD_TIPO, current.getTipo());
        intent.putExtra(QuadEdit.QUAD_PRECIO_DIA, current.getPrecioDia());
        intent.putExtra(QuadEdit.QUAD_DESCRIPCION, current.getDescripcion());

        mStartUpdateQuad.launch(intent);
    }

    /**
     * Launcher que maneja el resultado de la edición de un quad.
     * Cuando se recibe un resultado exitoso, se ha actualizado el quad mediante el ViewModel.
     */
    ActivityResultLauncher<Intent> mStartUpdateQuad = newActivityResultLauncher(new ExecuteActivityResult() {
        @Override
        public void process(Bundle extras, Quad quad) {
            mQuadViewModel.update(quad);
        }
    });

    /**
     * Se ha manejado el clic en el botón "atrás" de la barra de herramientas.
     *
     * @return true si la navegación ha sido procesada.
     */
    @Override
    public boolean onSupportNavigateUp() {
        if (mAdapter != null && mAdapter.isSelectionMode()) {
            exitSelectionMode();
        } else {
            onBackPressed();
        }
        return true;
    }

    /**
     * Interfaz funcional utilizada para procesar el resultado de las actividades de creación/edición.
     * Se ha diseñado esta interfaz para permitir la reutilización del código de manejo de resultados.
     */
    interface ExecuteActivityResult {
        /**
         * Se ha procesado el resultado de la actividad.
         *
         * @param extras El Bundle con los datos extras del Intent.
         * @param quad El objeto Quad construido a partir de los datos del Intent.
         */
        void process(Bundle extras, Quad quad);
    }

    /**
     * Método específico para facilitar las pruebas instrumentadas.
     * Permite acceder al repositorio de Quads desde el entorno de pruebas.
     */
    public QuadRepository getQuadRepository() {
        // Como Room utiliza un Singleton para la BD, podemos instanciar el repositorio
        // directamente usando el contexto de la aplicación, o sacarlo del ViewModel si lo tuvieras expuesto.
        return new QuadRepository(getApplication());
    }
}