package com.uth.pm2examengrupo5.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.uth.pm2examengrupo5.R;
import com.uth.pm2examengrupo5.data.remote.dto.ContactoDto;

import java.util.ArrayList;
import java.util.List;

public class ContactoAdapter extends RecyclerView.Adapter<ContactoAdapter.Fila> {

    public interface AlTocar {
        void onContacto(ContactoDto contacto);
    }

    private final List<ContactoDto> contactos = new ArrayList<>();
    private final AlTocar alTocar;
    private int seleccionado = RecyclerView.NO_POSITION;

    public ContactoAdapter(AlTocar alTocar) {
        this.alTocar = alTocar;
    }

    public void mostrar(List<ContactoDto> nuevos) {
        Integer idAnterior = getSeleccionado() == null ? null : getSeleccionado().getId();
        contactos.clear();
        if (nuevos != null) contactos.addAll(nuevos);
        seleccionado = RecyclerView.NO_POSITION;
        if (idAnterior != null) {
            for (int i = 0; i < contactos.size(); i++) {
                if (contactos.get(i).getId() == idAnterior) seleccionado = i;
            }
        }
        notifyDataSetChanged();
    }

    @Nullable
    public ContactoDto getSeleccionado() {
        return seleccionado == RecyclerView.NO_POSITION || seleccionado >= contactos.size()
                ? null : contactos.get(seleccionado);
    }

    @NonNull
    @Override
    public Fila onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View vista = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contacto, parent, false);
        return new Fila(vista);
    }

    @Override
    public void onBindViewHolder(@NonNull Fila fila, int position) {
        ContactoDto contacto = contactos.get(position);
        fila.nombre.setText(contacto.getNombre());
        fila.telefono.setText(contacto.getTelefono());
        fila.itemView.setBackgroundColor(position == seleccionado
                ? ContextCompat.getColor(fila.itemView.getContext(), R.color.seleccion)
                : 0);
        fila.itemView.setOnClickListener(v -> {
            int posicion = fila.getBindingAdapterPosition();
            if (posicion == RecyclerView.NO_POSITION) return;
            int anterior = seleccionado;
            seleccionado = posicion;
            if (anterior != RecyclerView.NO_POSITION) notifyItemChanged(anterior);
            notifyItemChanged(posicion);
            alTocar.onContacto(contactos.get(posicion));
        });
    }

    @Override
    public int getItemCount() {
        return contactos.size();
    }

    static class Fila extends RecyclerView.ViewHolder {
        final TextView nombre;
        final TextView telefono;

        Fila(@NonNull View itemView) {
            super(itemView);
            nombre = itemView.findViewById(R.id.textNombre);
            telefono = itemView.findViewById(R.id.textTelefono);
        }
    }
}
