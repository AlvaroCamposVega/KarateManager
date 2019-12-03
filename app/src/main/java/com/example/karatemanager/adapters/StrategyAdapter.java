package com.example.karatemanager.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.karatemanager.EventActivity;
import com.example.karatemanager.R;
import com.example.karatemanager.StrategyActivity;
import com.example.karatemanager.model.Evento;
import com.example.karatemanager.model.Strategy;

import java.util.ArrayList;
import java.util.List;

public class StrategyAdapter extends RecyclerView.Adapter<StrategyAdapter.ListHolder>
{
  private List<Strategy> strategyList;
  private Context ctx;
  private Evento evento;
  private boolean stripped = false;
  private final int COD_STRATEGY = 60;

  public StrategyAdapter(Context ctx, Evento evento)
  {
    strategyList = new ArrayList<>();
    this.ctx = ctx;
    this.evento = evento;
  }

  public void setStrategyList(List<Strategy> strategies)
  {
    strategyList = strategies;
    notifyDataSetChanged();
  }

  public StrategyAdapter.ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
  {
    View view;
    // Inflate the layout used for the eventos
    view = LayoutInflater.from(ctx).inflate(
      R.layout.strategy_layout,
      parent,
      false
    );

    return new StrategyAdapter.ListHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull StrategyAdapter.ListHolder holder, int position)
  {
    holder.BindHolder(strategyList.get(position));
  }

  @Override
  public int getItemCount()
  {
    return strategyList.size();
  }

  public class ListHolder extends RecyclerView.ViewHolder
    implements View.OnCreateContextMenuListener
  {
    private TextView target;
    private TextView category;
    private TextView desc;
    // Layout
    private ConstraintLayout layout;

    public ListHolder(@NonNull View strategyView)
    {
      super(strategyView);
      // Stripped inflated layout
      if (stripped)
      {
        strategyView.setBackground(ctx.getDrawable(R.drawable.event_stripped_style));
        stripped = false;

      } else
      {
        stripped = true;
      }

      target = strategyView.findViewById(R.id.strategyTarget);
      category = strategyView.findViewById(R.id.strategyCategory);
      desc = strategyView.findViewById(R.id.strategyDesc);
      // Layout
      layout = strategyView.findViewById(R.id.strategyLayout);
      layout.setOnCreateContextMenuListener(this);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
      int strategyId = strategyList.get(this.getAdapterPosition()).getId();

      menu.setHeaderTitle(ctx.getString(R.string.mnu_header));
      menu.add(0, strategyId, 0, ctx.getString(R.string.mnu_go));
      menu.add(1, strategyId, 0, ctx.getString(R.string.mnu_edit));
      menu.add(2, strategyId, 0, ctx.getString(R.string.mnu_delete));
    }

    public void BindHolder(final Strategy strategy)
    {
      target.setText(strategy.getTarget());
      category.setText(strategy.getCategory());

      if (strategy.getDesc().length() > 36)
      {
        desc.setText(strategy.getDesc().substring(0, 36) + "...");

      } else
      {
        desc.setText(strategy.getDesc());
      }
      // Layout Listener
      super.itemView.setOnClickListener(new View.OnClickListener()
      {
        @Override
        public void onClick(View v)
        {
          Bundle bundle = new Bundle();
          bundle.putSerializable("_strategy", strategy);
          bundle.putSerializable("_evento", evento);
          // Evento Activity
          Intent intent = new Intent(ctx, StrategyActivity.class);
          intent.putExtras(bundle);
          ((Activity) ctx).startActivityForResult(intent, COD_STRATEGY);
        }
      });
    }
  }
}
