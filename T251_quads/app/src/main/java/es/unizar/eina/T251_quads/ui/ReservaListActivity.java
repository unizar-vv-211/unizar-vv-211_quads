package es.unizar.eina.T251_quads.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.stream.Collectors;

import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.Reserva;
import es.unizar.eina.T251_quads.database.ReservaConQuads;
import es.unizar.eina.T251_quads.send.SendAbstraction;
import es.unizar.eina.T251_quads.send.SendAbstractionImpl;

/**
 * Activity principal para la gestión del listado de Reservas.
 * Se ha implementado en esta clase la interfaz de usuario que permite visualizar,
 * crear, editar, eliminar y enviar (por WhatsApp/SMS) las reservas del sistema.
 * Se ha utilizado un RecyclerView para mostrar la lista de reservas con sus quads asociados.
 * Las acciones se han activado mediante un PopupMenu al hacer click corto en un item.
 */
public class ReservaListActivity extends AppCompatActivity {

    /** Código de solicitud para crear una nueva reserva. */
    public static final int NEW_RESERVA_REQUEST_CODE = 1;
    
    /** Código de solicitud para actualizar una reserva existente. */
    public static final int UPDATE_RESERVA_REQUEST_CODE = 2;

    /** ViewModel que gestiona el acceso a los datos de Reservas. */
    private ReservaViewModel mReservaViewModel;

    /**
     * Se ha inicializado la pantalla de lista de reservas.
     * Se han configurado el RecyclerView, el adaptador, el ViewModel,
     * y se ha establecido un observador de LiveData para actualizar la UI automáticamente.
     * Se ha configurado un listener de clic corto para mostrar un menú con las opciones disponibles.
     *
     * @param savedInstanceState El estado previo de la pantalla.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reserva_list);

        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Gestión de Reservas");
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerview);

        // Se ha configurado el listener para click corto que muestra el menú de opciones
        ReservaListAdapter.OnItemClickListener clickListener = (reservaConQuads, view) -> {
            mostrarMenuOpciones(view, reservaConQuads);
        };

        final ReservaListAdapter adapter = new ReservaListAdapter(new ReservaListAdapter.ReservaDiff(), clickListener);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mReservaViewModel = new ViewModelProvider(this).get(ReservaViewModel.class);
        mReservaViewModel.getAllReservasConQuads().observe(this, adapter::submitList);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(ReservaListActivity.this, ReservaEdit.class);
            startActivityForResult(intent, NEW_RESERVA_REQUEST_CODE);
        });
    }

    /**
     * Se ha mostrado un PopupMenu con las opciones disponibles para una reserva específica.
     * Las opciones incluidas son: Enviar por WhatsApp, Enviar por SMS, Modificar y Eliminar.
     *
     * @param view La vista desde la cual se ha de mostrar el menú popup.
     * @param reservaConQuads La reserva con sus quads asociados sobre la cual se han de aplicar las acciones.
     */
    private void mostrarMenuOpciones(View view, ReservaConQuads reservaConQuads) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.getMenuInflater().inflate(R.menu.menu_reserva_opciones, popup.getMenu());
        
        popup.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();
            
            if (id == R.id.action_enviar_whatsapp) {
                enviarReserva(reservaConQuads, "whatsapp");
                return true;
            } else if (id == R.id.action_enviar_sms) {
                enviarReserva(reservaConQuads, "sms");
                return true;
            } else if (id == R.id.action_modificar) {
                modificarReserva(reservaConQuads);
                return true;
            } else if (id == R.id.action_eliminar) {
                mReservaViewModel.delete(reservaConQuads.reserva);
                Toast.makeText(this, "Reserva eliminada", Toast.LENGTH_SHORT).show();
                return true;
            }
            return false;
        });
        
        popup.show();
    }

    /**
     * Se ha iniciado el flujo de modificación de una reserva existente.
     * Se han empaquetado todos los datos de la reserva y sus quads en un Intent
     * y se ha lanzado ReservaEdit en modo edición.
     *
     * @param reservaConQuads La reserva con sus quads asociados que se ha de editar.
     */
    private void modificarReserva(ReservaConQuads reservaConQuads) {
        Intent intent = new Intent(ReservaListActivity.this, ReservaEdit.class);

        Reserva reserva = reservaConQuads.reserva;
        ArrayList<String> quadIds = new ArrayList<>();
        if (reservaConQuads.quads != null) {
            quadIds = (ArrayList<String>) reservaConQuads.quads.stream()
                    .map(Quad::getMatricula)
                    .collect(Collectors.toList());
        }

        intent.putExtra(ReservaEdit.EXTRA_RESERVA_ID, reserva.getId());
        intent.putExtra(ReservaEdit.EXTRA_RESERVA_CLIENTE, reserva.getCliente());
        intent.putExtra(ReservaEdit.EXTRA_RESERVA_TELEFONO, reserva.getTelefono());
        intent.putExtra(ReservaEdit.EXTRA_RESERVA_FECHA_R, reserva.getFechaRecogida());
        intent.putExtra(ReservaEdit.EXTRA_RESERVA_FECHA_D, reserva.getFechaDevolucion());
        intent.putExtra(ReservaEdit.EXTRA_RESERVA_CASCOS, reserva.getCascos());
        intent.putExtra(ReservaEdit.EXTRA_RESERVA_PRECIO, reserva.getPrecioTotal());
        intent.putExtra(ReservaEdit.EXTRA_RESERVA_QUADS_SELECCIONADOS, quadIds);

        startActivityForResult(intent, UPDATE_RESERVA_REQUEST_CODE);
    }

    /**
     * Se ha generado un mensaje con los detalles de la reserva y se ha enviado mediante WhatsApp o SMS.
     * Se ha utilizado el patrón Bridge (SendAbstraction) para abstraer el método de envío.
     * El mensaje incluye: nombre del cliente, fechas, número de cascos y quads asociados.
     *
     * @param reservaConQuads La reserva con sus quads que se ha de enviar.
     * @param method El método de envío ("whatsapp" o "sms").
     */
    private void enviarReserva(ReservaConQuads reservaConQuads, String method) {
        Reserva reserva = reservaConQuads.reserva;
        
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Reserva confirmada para ").append(reserva.getCliente()).append("\n");
        mensaje.append("Fechas: ").append(reserva.getFechaRecogida());
        mensaje.append(" - ").append(reserva.getFechaDevolucion()).append("\n");
        mensaje.append("Cascos: ").append(reserva.getCascos()).append("\n");
        
        if (reservaConQuads.quads != null && !reservaConQuads.quads.isEmpty()) {
            mensaje.append("Quads: ");
            ArrayList<String> matriculas = new ArrayList<>();
            for (Quad quad : reservaConQuads.quads) {
                matriculas.add(quad.getMatricula());
            }
            mensaje.append(String.join(", ", matriculas));
        } else {
            mensaje.append("Quads: (Ninguno)");
        }
        
        SendAbstraction sendAbstraction = new SendAbstractionImpl(this, method);
        sendAbstraction.send(reserva.getTelefono(), mensaje.toString());
        
        String metodoNombre = method.equalsIgnoreCase("sms") ? "SMS" : "WhatsApp";
        Toast.makeText(this, "Abriendo " + metodoNombre + "...", Toast.LENGTH_SHORT).show();
    }

    /**
     * Se ha procesado el resultado devuelto por ReservaEdit.
     * Si el resultado es exitoso, se han extraído los datos del Intent y se ha procedido
     * a insertar una nueva reserva o actualizar una existente según el código de solicitud.
     *
     * @param requestCode El código que identifica la operación (crear o actualizar).
     * @param resultCode El código de resultado (RESULT_OK o RESULT_CANCELED).
     * @param data El Intent con los datos de la reserva.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            String cliente = data.getStringExtra(ReservaEdit.EXTRA_RESERVA_CLIENTE);
            String telefono = data.getStringExtra(ReservaEdit.EXTRA_RESERVA_TELEFONO);
            String fechaR = data.getStringExtra(ReservaEdit.EXTRA_RESERVA_FECHA_R);
            String fechaD = data.getStringExtra(ReservaEdit.EXTRA_RESERVA_FECHA_D);
            int cascos = data.getIntExtra(ReservaEdit.EXTRA_RESERVA_CASCOS, 0);
            double precio = data.getDoubleExtra(ReservaEdit.EXTRA_RESERVA_PRECIO, 0.0);
            ArrayList<String> quadIds = data.getStringArrayListExtra(ReservaEdit.EXTRA_RESERVA_QUADS_SELECCIONADOS);

            if (requestCode == NEW_RESERVA_REQUEST_CODE) {
                Reserva reserva = new Reserva(cliente, telefono, fechaR, fechaD, cascos, precio);
                mReservaViewModel.insert(reserva, quadIds);
                Toast.makeText(getApplicationContext(), "Reserva guardada", Toast.LENGTH_SHORT).show();
            } else if (requestCode == UPDATE_RESERVA_REQUEST_CODE) {
                int id = data.getIntExtra(ReservaEdit.EXTRA_RESERVA_ID, -1);
                if (id == -1) {
                    Toast.makeText(this, "Error al actualizar (ID no encontrado)", Toast.LENGTH_SHORT).show();
                    return;
                }
                Reserva reserva = new Reserva(cliente, telefono, fechaR, fechaD, cascos, precio);
                reserva.setId(id);
                mReservaViewModel.update(reserva, quadIds);
                Toast.makeText(getApplicationContext(), "Reserva actualizada", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Operación cancelada", Toast.LENGTH_SHORT).show();
        }
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

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_reserva_filtros, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.filter_todas) {
            mReservaViewModel.setFiltro(ReservaViewModel.FiltroReserva.TODAS);
            return true;
        } else if (id == R.id.filter_futuras) {
            mReservaViewModel.setFiltro(ReservaViewModel.FiltroReserva.FUTURAS);
            return true;
        } else if (id == R.id.filter_activas) {
            mReservaViewModel.setFiltro(ReservaViewModel.FiltroReserva.ACTIVAS);
            return true;
        } else if (id == R.id.filter_pasadas) {
            mReservaViewModel.setFiltro(ReservaViewModel.FiltroReserva.PASADAS);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}