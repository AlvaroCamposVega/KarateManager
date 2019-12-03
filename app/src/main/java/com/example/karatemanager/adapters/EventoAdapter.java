package com.example.karatemanager.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatemanager.EventActivity;
import com.example.karatemanager.R;
import com.example.karatemanager.model.Evento;

import java.util.ArrayList;
import java.util.List;

public class EventoAdapter extends RecyclerView.Adapter<EventoAdapter.ListHolder>
{
  private List<Evento> eventoList;
  private Context ctx;
  private boolean wkf;
  private boolean stripped = false;
  private final int COD_EVENT = 20;

  public EventoAdapter(Context ctx, boolean wkf)
  {
    eventoList = new ArrayList<>();
    this.ctx = ctx;
    this.wkf = wkf;
  }

  public void setEventoList(List<Evento> eventos)
  {
    eventoList = eventos;
    notifyDataSetChanged();
  }

  public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
  {
    View view;
    // Inflate the layout used for the eventos
    view = LayoutInflater.from(ctx).inflate(
      R.layout.evento_layout,
      parent,
      false
    );

    return new ListHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ListHolder holder, int position)
  {
    holder.BindHolder(eventoList.get(position));
  }

  @Override
  public int getItemCount() { return eventoList.size(); }

  public class ListHolder extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener
  {
    private TextView name;
    private TextView city;
    private TextView startDate;
    private TextView endDate;
    // Layout
    private ConstraintLayout layout;

    public ListHolder(@NonNull View eventoView)
    {
      super(eventoView);
      // Stripped inflated layout
      if (stripped)
      {
        eventoView.setBackground(ctx.getDrawable(R.drawable.event_stripped_style));
        stripped = false;

      } else
      {
        stripped = true;
      }

      name = eventoView.findViewById(R.id.eventoName);
      city = eventoView.findViewById(R.id.eventoCity);
      startDate = eventoView.findViewById(R.id.eventoStartDate);
      endDate = eventoView.findViewById(R.id.eventoEndDate);
      // Only if it's the user eventos
      if (!wkf)
      {
        // Layout
        layout = eventoView.findViewById(R.id.eventoLayout);
        layout.setOnCreateContextMenuListener(this);
      }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
      int eventoId = eventoList.get(this.getAdapterPosition()).getId();

      menu.setHeaderTitle(ctx.getString(R.string.mnu_header));
      menu.add(0, eventoId, 0, ctx.getString(R.string.mnu_go));
      menu.add(1, eventoId, 0, ctx.getString(R.string.mnu_edit));
      menu.add(2, eventoId, 0, ctx.getString(R.string.mnu_delete));
    }

    public void BindHolder(final Evento evento)
    {
      name.setText(evento.getName());
      city.setText(evento.getCity());
      startDate.setText(ctx.getString(R.string.label_eventoStart) + evento.getStartDate());
      endDate.setText(ctx.getString(R.string.label_eventoEnd) + evento.getEndDate());
      // Only if it's the user eventos
      if (!wkf)
      {
        // Layout Listener
        super.itemView.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            Bundle bundle = new Bundle();
            bundle.putSerializable("_evento", evento);
            // Evento Activity
            Intent intent = new Intent(ctx, EventActivity.class);
            intent.putExtras(bundle);
            ((Activity)ctx).startActivityForResult(intent, COD_EVENT);
          }
        });
      }
    }
  }
}
