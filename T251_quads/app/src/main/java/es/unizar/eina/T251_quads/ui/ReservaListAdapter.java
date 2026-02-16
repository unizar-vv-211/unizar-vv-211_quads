package es.unizar.eina.T251_quads.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import java.util.stream.Collectors;

import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.Quad;
import es.unizar.eina.T251_quads.database.ReservaConQuads;

/**
 * Adaptador personalizado para el RecyclerView que muestra la lista de Reservas con sus quads asociados.
 * Se ha extendido de ListAdapter para aprovechar DiffUtil y gestionar eficientemente los cambios en la lista.
 * Se ha diseñado para mostrar información completa de cada reserva: cliente, fechas y quads asociados.
 */
public class ReservaListAdapter extends ListAdapter<ReservaConQuads, ReservaListAdapter.ReservaViewHolder> {

    /** Listener para manejar clicks en items de la lista. */
    private final OnItemClickListener mOnClickListener;

    /**
     * Constructor del adaptador.
     * Se ha inicializado el adaptador con un DiffCallback y un listener de clicks.
     *
     * @param diffCallback Callback que determina cómo se calculan las diferencias entre listas.
     * @param listener Listener que se ha de invocar cuando se hace click en un item.
     */
    protected ReservaListAdapter(@NonNull DiffUtil.ItemCallback<ReservaConQuads> diffCallback,
                                 OnItemClickListener listener) {
        super(diffCallback);
        this.mOnClickListener = listener;
    }

    /**
     * Se ha creado un nuevo ViewHolder cuando el RecyclerView lo necesita.
     * Se ha inflado el layout específico para items de reserva.
     *
     * @param parent El ViewGroup padre al cual se añadirá la nueva View.
     * @param viewType El tipo de vista del nuevo View.
     * @return Un nuevo ViewHolder que contiene la View del item de reserva.
     */
    @NonNull
    @Override
    public ReservaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.reserva_recyclerview_item, parent, false);
        return new ReservaViewHolder(view, mOnClickListener);
    }

    /**
     * Se ha vinculado un ViewHolder existente con los datos de la reserva en la posición especificada.
     *
     * @param holder El ViewHolder que se ha de actualizar.
     * @param position La posición del item en la lista.
     */
    @Override
    public void onBindViewHolder(@NonNull ReservaViewHolder holder, int position) {
        ReservaConQuads current = getItem(position);
        holder.bind(current);
    }

    /**
     * ViewHolder que representa un item individual de la lista de reservas.
     * Se ha encapsulado la lógica de vinculación de datos complejos (reserva + quads) con las vistas.
     */
    static class ReservaViewHolder extends RecyclerView.ViewHolder {
        
        /** TextView que muestra el nombre del cliente. */
        private final TextView mClienteTextView;
        
        /** TextView que muestra las fechas de recogida y devolución. */
        private final TextView mFechasTextView;
        
        /** TextView que muestra la lista de matrículas de quads asociados. */
        private final TextView mQuadsTextView;
        
        /** Referencia a la reserva completa con sus quads. */
        private ReservaConQuads mReservaConQuads;

        /**
         * Constructor del ViewHolder.
         * Se han inicializado las referencias a las vistas del item y se ha configurado el listener de click.
         *
         * @param itemView La vista del item completo.
         * @param clickListener El listener que se ha de invocar al hacer click.
         */
        ReservaViewHolder(View itemView, OnItemClickListener clickListener) {
            super(itemView);
            mClienteTextView = itemView.findViewById(R.id.textViewCliente);
            mFechasTextView = itemView.findViewById(R.id.textViewFechas);
            mQuadsTextView = itemView.findViewById(R.id.textViewQuads);

            // Se ha configurado el click corto para mostrar el menú de opciones
            itemView.setOnClickListener(v -> {
                if (clickListener != null && mReservaConQuads != null) {
                    clickListener.onItemClick(mReservaConQuads, v);
                }
            });
        }

        /**
         * Se han vinculado los datos de la reserva con sus quads a las vistas.
         * Se ha generado una representación textual de las fechas y de los quads asociados.
         *
         * @param reservaConQuads La reserva con su lista de quads asociados.
         */
        public void bind(ReservaConQuads reservaConQuads) {
            this.mReservaConQuads = reservaConQuads;

            mClienteTextView.setText(reservaConQuads.reserva.getCliente());
            String fechas = "De: " + reservaConQuads.reserva.getFechaRecogida() +
                    "  A: " + reservaConQuads.reserva.getFechaDevolucion();
            mFechasTextView.setText(fechas);

            if (reservaConQuads.quads == null || reservaConQuads.quads.isEmpty()) {
                mQuadsTextView.setText("Quads: (Ninguno)");
            } else {
                String quadsStr = reservaConQuads.quads.stream()
                        .map(Quad::getMatricula)
                        .collect(Collectors.joining(", "));
                mQuadsTextView.setText("Quads: " + quadsStr);
            }
        }
    }

    /**
     * Callback de DiffUtil para calcular diferencias entre listas de ReservaConQuads.
     * Se ha implementado esta clase para optimizar las actualizaciones de la lista.
     */
    static class ReservaDiff extends DiffUtil.ItemCallback<ReservaConQuads> {
        
        /**
         * Se ha determinado si dos items representan la misma reserva.
         * Se ha comparado por ID de reserva.
         *
         * @param oldItem El item antiguo.
         * @param newItem El item nuevo.
         * @return true si representan la misma reserva.
         */
        @Override
        public boolean areItemsTheSame(@NonNull ReservaConQuads oldItem, @NonNull ReservaConQuads newItem) {
            return oldItem.reserva.getId() == newItem.reserva.getId();
        }

        /**
         * Se ha determinado si dos reservas tienen el mismo contenido.
         * Se ha comparado cliente, fecha de recogida y número de quads.
         *
         * @param oldItem La reserva antigua.
         * @param newItem La reserva nueva.
         * @return true si tienen el mismo contenido.
         */
        @Override
        public boolean areContentsTheSame(@NonNull ReservaConQuads oldItem, @NonNull ReservaConQuads newItem) {
            return oldItem.reserva.getCliente().equals(newItem.reserva.getCliente()) &&
                    oldItem.reserva.getFechaRecogida().equals(newItem.reserva.getFechaRecogida()) &&
                    oldItem.quads.size() == newItem.quads.size();
        }
    }

    /**
     * Interfaz funcional para manejar clicks en items de la lista de reservas.
     */
    public interface OnItemClickListener {
        /**
         * Se ha invocado cuando se hace click en un item de la lista.
         *
         * @param reservaConQuads La reserva con sus quads del item clickeado.
         * @param view La vista del item.
         */
        void onItemClick(ReservaConQuads reservaConQuads, View view);
    }
}