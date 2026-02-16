package es.unizar.eina.T251_quads.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import es.unizar.eina.T251_quads.R;
import es.unizar.eina.T251_quads.database.Quad;

/**
 * Adaptador personalizado para el RecyclerView que muestra la lista de Quads.
 * Se ha extendido de ListAdapter para aprovechar las ventajas de DiffUtil
 * (actualizaciones eficientes de la lista sin necesidad de notifyDataSetChanged).
 */
public class QuadListAdapter extends ListAdapter<Quad, QuadListAdapter.QuadViewHolder> {

    /** Quad seleccionado más recientemente. */
    private Quad mCurrent;
    
    /** Listener para manejar clicks en items de la lista. */
    private OnItemClickListener mClickListener;
    
    /** Listener para manejar long press en items. */
    private OnItemLongClickListener mLongClickListener;
    
    /** Modo de selección múltiple activado. */
    private boolean mSelectionMode = false;
    
    /** Lista de quads seleccionados. */
    private java.util.Set<String> mSelectedItems = new java.util.HashSet<>();

    /**
     * Constructor del adaptador.
     * Se ha inicializado el adaptador con un DiffCallback y un listener de clicks.
     *
     * @param diffCallback Callback que determina cómo se calculan las diferencias entre listas.
     * @param clickListener Listener que se ha de invocar cuando se hace click en un item.
     */
    public QuadListAdapter(@NonNull DiffUtil.ItemCallback<Quad> diffCallback, OnItemClickListener clickListener) {
        super(diffCallback);
        this.mClickListener = clickListener;
    }
    
    /**
     * Establece el listener para long press.
     */
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.mLongClickListener = listener;
    }
    
    /**
     * Activa o desactiva el modo de selección.
     */
    public void setSelectionMode(boolean enabled) {
        mSelectionMode = enabled;
        if (!enabled) {
            mSelectedItems.clear();
        }
        notifyDataSetChanged();
    }
    
    /**
     * Verifica si el modo de selección está activo.
     */
    public boolean isSelectionMode() {
        return mSelectionMode;
    }
    
    /**
     * Alterna la selección de un quad.
     */
    public void toggleSelection(String matricula) {
        if (mSelectedItems.contains(matricula)) {
            mSelectedItems.remove(matricula);
        } else {
            mSelectedItems.add(matricula);
        }
        notifyDataSetChanged();
    }
    
    /**
     * Obtiene la lista de quads seleccionados.
     */
    public java.util.Set<String> getSelectedItems() {
        return new java.util.HashSet<>(mSelectedItems);
    }
    
    /**
     * Obtiene el número de items seleccionados.
     */
    public int getSelectedCount() {
        return mSelectedItems.size();
    }
    
    /**
     * Limpia la selección.
     */
    public void clearSelection() {
        mSelectedItems.clear();
        mSelectionMode = false;
        notifyDataSetChanged();
    }

    /**
     * Se ha creado un nuevo ViewHolder cuando el RecyclerView lo necesita.
     * Se ha inflado el layout del item.
     *
     * @param parent El ViewGroup padre al cual se añadirá la nueva View.
     * @param viewType El tipo de vista del nuevo View.
     * @return Un nuevo ViewHolder que contiene la View del item.
     */
    @Override
    public QuadViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new QuadViewHolder(itemView);
    }

    /**
     * Se ha vinculado un ViewHolder existente con los datos del quad en la posición especificada.
     * Se ha actualizado el quad actual.
     *
     * @param holder El ViewHolder que se ha de actualizar.
     * @param position La posición del item en la lista.
     */
    @Override
    public void onBindViewHolder(QuadViewHolder holder, int position) {
        Quad current = getItem(position);
        boolean isSelected = mSelectedItems.contains(current.getMatricula());
        holder.bind(current, mClickListener, mLongClickListener, mSelectionMode, isSelected);
        this.mCurrent = current;
    }

    /**
     * Se ha obtenido el quad seleccionado más recientemente.
     *
     * @return El quad actual.
     */
    public Quad getCurrent() {
        return mCurrent;
    }

    /**
     * ViewHolder que representa un item individual de la lista de quads.
     * Se ha encapsulado la lógica de vinculación de datos con las vistas.
     */
    static class QuadViewHolder extends RecyclerView.ViewHolder {
        
        /** TextView que muestra la matrícula del quad. */
        private final TextView quadItemView;

        /**
         * Constructor del ViewHolder.
         * Se han inicializado las referencias a las vistas del item.
         *
         * @param itemView La vista del item completo.
         */
        private QuadViewHolder(View itemView) {
            super(itemView);
            quadItemView = itemView.findViewById(R.id.textViewMatricula);
        }

        /**
         * Se han vinculado los datos del quad con las vistas.
         */
        public void bind(Quad quad, OnItemClickListener clickListener, 
                        OnItemLongClickListener longClickListener, 
                        boolean selectionMode, boolean isSelected) {
            quadItemView.setText(quad.getMatricula());
            
            // Cambiar color de fondo si está seleccionado
            if (isSelected) {
                itemView.setBackgroundColor(0xFFE3F2FD); // Azul claro
            } else {
                itemView.setBackgroundColor(0xFFFFFFFF); // Blanco
            }
            
            // Click corto
            itemView.setOnClickListener(v -> {
                if (clickListener != null) {
                    clickListener.onItemClick(quad, v, selectionMode);
                }
            });
            
            // Long press
            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onItemLongClick(quad, v);
                    return true;
                }
                return false;
            });
        }
    }

    /**
     * Callback de DiffUtil para calcular diferencias entre listas de Quads.
     * Se ha implementado esta clase para mejorar el rendimiento de las actualizaciones de la lista.
     */
    static class QuadDiff extends DiffUtil.ItemCallback<Quad> {

        /**
         * Se ha determinado si dos items representan el mismo quad.
         * Se ha comparado por referencia de objeto.
         *
         * @param oldItem El item antiguo.
         * @param newItem El item nuevo.
         * @return true si representan el mismo quad.
         */
        @Override
        public boolean areItemsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            return oldItem == newItem;
        }

        /**
         * Se ha determinado si dos quads tienen el mismo contenido.
         * Se ha comparado por matrícula (clave primaria).
         *
         * @param oldItem El quad antiguo.
         * @param newItem El quad nuevo.
         * @return true si tienen el mismo contenido.
         */
        @Override
        public boolean areContentsTheSame(@NonNull Quad oldItem, @NonNull Quad newItem) {
            return oldItem.getMatricula().equals(newItem.getMatricula());
        }
    }

    /**
     * Interfaz funcional para manejar clicks en items de la lista.
     */
    public interface OnItemClickListener {
        void onItemClick(Quad quad, View view, boolean selectionMode);
    }
    
    /**
     * Interfaz funcional para manejar long press en items.
     */
    public interface OnItemLongClickListener {
        void onItemLongClick(Quad quad, View view);
    }
}