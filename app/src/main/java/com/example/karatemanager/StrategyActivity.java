package com.example.karatemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.karatemanager.adapters.StrategyAdapter;
import com.example.karatemanager.model.Evento;
import com.example.karatemanager.model.Strategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StrategyActivity extends AppCompatActivity
{
  FirebaseAuth fbAuth;
  FirebaseDatabase fbDatabase;

  private Strategy strategy;
  private int strategyNewId;
  private int highestId;

  private final String EDIT_MODE = "edit";
  private final String INFO_MODE = "info";
  // Info layout
  public TextView infoTarget;
  public TextView infoCategory;
  public TextView infoDesc;
  public FloatingActionButton infoEditBtn;
  public FloatingActionButton infoDeleteBtn;
  // Edit layout
  public EditText editTarget;
  public Spinner editCategory;
  public EditText editDesc;
  public FloatingActionButton editOkBtn;
  public FloatingActionButton editCancelBtn;
  public FloatingActionButton strategyBackBtn;
  public ArrayAdapter<CharSequence> categoryAdapter;

  private Evento evento;

  private final int RESULT_DELETE_SUCCESS = 2;
  private final int RESULT_DELETE_FAIL = 3;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);

    Bundle extras = getIntent().getExtras();

    String mode = INFO_MODE;
    evento = null;

    if (extras != null)
    {
      evento = (Evento) extras.getSerializable("_evento");

      if (extras.getString("_mode") != null)
      {
        mode = extras.getString("_mode");
      }
      strategy = (Strategy) extras.getSerializable("_strategy");
    }

    fbAuth = FirebaseAuth.getInstance();
    fbDatabase = FirebaseDatabase.getInstance();

    setActivityLayout(mode);
  }

  public void setActivityLayout(String mode)
  {
    switch (mode)
    {
      case "info":
        setContentView(R.layout.strategy_info);

        infoTarget = findViewById(R.id.strategyInfoTarget);
        infoCategory = findViewById(R.id.strategyInfoCategory);
        infoDesc = findViewById(R.id.strategyInfoDesc);
        // Buttons
        infoEditBtn = findViewById(R.id.strategyInfoEditBtn);
        infoDeleteBtn = findViewById(R.id.strategyInfoDeleteBtn);

        infoTarget.setText(strategy.getTarget());
        infoCategory.setText(strategy.getCategory());
        infoDesc.setText(strategy.getDesc());
        // User UID
        final String uid = fbAuth.getUid();

        infoEditBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            setActivityLayout(EDIT_MODE);
          }
        });

        infoDeleteBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(final View v)
          {
            new MaterialAlertDialogBuilder(
              StrategyActivity.this,
              R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
            )
              .setTitle(R.string.dialog_title)
              .setMessage(R.string.dialog_msg)
              .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener()
              {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                  DatabaseReference eventoDbRef = fbDatabase.getReference().child("eventos/" + uid);

                  eventoDbRef.addListenerForSingleValueEvent(new ValueEventListener()
                  {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                      if (dataSnapshot.hasChildren())
                      {
                        for (DataSnapshot eventoDS : dataSnapshot.getChildren())
                        {
                          Evento dbEvento = eventoDS.getValue(Evento.class);

                          if (dbEvento.getId() == evento.getId())
                          {
                            DatabaseReference eventoRef = eventoDS.getRef();

                            String eventoGlobalRef = eventoRef.toString().substring(
                              eventoRef.toString().lastIndexOf("/") + 1
                            );

                            DatabaseReference dbRef = fbDatabase.getReference()
                              .child("estrategias/" + eventoGlobalRef);

                            dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                            {
                              @Override
                              public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
                              {
                                highestId = 1;

                                for (DataSnapshot strategyDs : dataSnapshot.getChildren())
                                {
                                  Strategy dbStrategy = strategyDs.getValue(Strategy.class);

                                  if (dbStrategy.getId() > highestId)
                                  {
                                    highestId = dbStrategy.getId();
                                  }
                                }

                                for (DataSnapshot strategyDS : dataSnapshot.getChildren())
                                {
                                  final Strategy dbStrategy = strategyDS.getValue(Strategy.class);

                                  if (dbStrategy.getId() == strategy.getId())
                                  {
                                    DatabaseReference strategyRef = strategyDS.getRef();

                                    strategyRef.removeValue()
                                      .addOnCompleteListener(new OnCompleteListener<Void>()
                                      {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                          if (!(dbStrategy.getId() == highestId))
                                          {
                                            for (
                                              DataSnapshot strategyDs2 : dataSnapshot.getChildren()
                                            )
                                            {
                                              Strategy strategyHighestId = strategyDs2
                                                .getValue(Strategy.class);

                                              if (strategyHighestId.getId() == highestId)
                                              {
                                                strategyDs2.getRef().setValue(new Strategy(
                                                  strategy.getId(),
                                                  strategyHighestId.getTarget(),
                                                  strategyHighestId.getCategory(),
                                                  strategyHighestId.getDesc()
                                                ));
                                              }
                                            }
                                          }

                                          if (!task.isSuccessful())
                                          {
                                            setResult(RESULT_DELETE_FAIL);
                                            finish();

                                            return;

                                          } else
                                          {
                                            setResult(RESULT_DELETE_SUCCESS);
                                            finish();

                                            return;
                                          }
                                        }
                                      });
                                  }
                                }
                              }

                              @Override
                              public void onCancelled(@NonNull DatabaseError databaseError)
                              {

                              }
                            });
                          }
                        }
                      }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                  });
                }
              })
              .setNegativeButton(R.string.label_no, null)
              .show();
          }
        });

        strategyBackBtn = findViewById(R.id.strategyBackBtn);
        strategyBackBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            Intent intent = new Intent(StrategyActivity.this, EventActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("_evento", evento);
            intent.putExtras(bundle);
            startActivity(intent);
          }
        });

        break;

      case "edit":
        setEditLayout();

        editTarget.setText(strategy.getTarget());
        editDesc.setText(strategy.getDesc());
        int i = 0;

        while (i < categoryAdapter.getCount() && i >= 0)
        {
          String item = categoryAdapter.getItem(i).toString();

          if (item.equals(strategy.getCategory()))
          {
            editCategory.setSelection(i);
            i = -2;
          }

          i++;
        }

        editOkBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            final String targetField = getField(editTarget);
            final String descField = getField(editDesc);
            final String categoryField = editCategory.getSelectedItem().toString();

            if (targetField.isEmpty() || descField.isEmpty() || categoryField.isEmpty())
            {
              Snackbar.make(
                v,
                getResources().getText(R.string.error_empty_fields),
                Snackbar.LENGTH_LONG
              ).show();

              return;
            }

            // User UID and database reference
            final String uid = fbAuth.getUid();
            final DatabaseReference eventoDbRef = fbDatabase.getReference().child("eventos/" + uid);

            eventoDbRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot)
              {
                if (dataSnapshot.hasChildren())
                {
                  for (DataSnapshot eventoDS : dataSnapshot.getChildren())
                  {
                    Evento dbEvento = eventoDS.getValue(Evento.class);

                    if (dbEvento.getId() == evento.getId())
                    {
                      DatabaseReference eventoRef = eventoDS.getRef();

                      String eventoGlobalRef = eventoRef.toString().substring(
                        eventoRef.toString().lastIndexOf("/") + 1
                      );

                      DatabaseReference dbRef = fbDatabase.getReference()
                        .child("estrategias/" + eventoGlobalRef);

                      dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                      {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                          Strategy updateStrategy = null;
                          DatabaseReference strategyRef = null;

                          for (DataSnapshot strategyDS : dataSnapshot.getChildren())
                          {
                            Strategy dbStrategy = strategyDS.getValue(Strategy.class);

                            if (dbStrategy.getId() == strategy.getId())
                            {
                              updateStrategy = new Strategy(
                                strategy.getId(),
                                targetField,
                                categoryField,
                                descField
                              );

                              strategyRef = strategyDS.getRef();
                            }
                          }

                          strategyRef.setValue(updateStrategy);

                          strategy = updateStrategy;
                          setActivityLayout(INFO_MODE);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                      });
                    }
                  }
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError)
              {

              }
            });
          }
        });

        editCancelBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            setActivityLayout(INFO_MODE);
          }
        });

        strategyBackBtn = findViewById(R.id.strategyBackBtn);
        strategyBackBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            Intent intent = new Intent(StrategyActivity.this, EventActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("_evento", evento);
            intent.putExtras(bundle);
            startActivity(intent);
          }
        });

        break;

      case "create" :
        setEditLayout();
        // Change the title so the user knows he's a the create page
        TextView title = findViewById(R.id.strategyEditTitle);
        title.setText(getString(R.string.label_strategy_new));

        editCancelBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            Intent eventIntent = new Intent(
              StrategyActivity.this,
              EventActivity.class
            );
            Bundle bundle = new Bundle();
            bundle.putSerializable("_evento", evento);
            eventIntent.putExtras(bundle);
            startActivity(eventIntent);
          }
        });

        editOkBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            final String targetField = getField(editTarget);
            final String descField = getField(editDesc);
            final String categoryField = editCategory.getSelectedItem().toString();

            if (targetField.isEmpty() || descField.isEmpty() || categoryField.isEmpty())
            {
              Snackbar.make(
                v,
                getResources().getText(R.string.error_empty_fields),
                Snackbar.LENGTH_LONG
              ).show();

              return;
            }

            // User UID and database reference
            final String uid = fbAuth.getUid();
            final DatabaseReference eventoDbRef = fbDatabase.getReference().child("eventos/" + uid);

            eventoDbRef.addListenerForSingleValueEvent(new ValueEventListener()
            {
              @Override
              public void onDataChange(@NonNull DataSnapshot dataSnapshot)
              {
                if (dataSnapshot.hasChildren())
                {
                  for (DataSnapshot eventoDS : dataSnapshot.getChildren())
                  {
                    Evento dbEvento = eventoDS.getValue(Evento.class);

                    if (dbEvento.getId() == evento.getId())
                    {
                      DatabaseReference eventoRef = eventoDS.getRef();

                      final String eventoGlobalRef = eventoRef.toString().substring(
                        eventoRef.toString().lastIndexOf("/") + 1
                      );

                      final DatabaseReference dbRef = fbDatabase.getReference()
                        .child("estrategias/");

                      strategyNewId = 1;

                      dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                      {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                          DatabaseReference newBdRef = dbRef.child(eventoGlobalRef);

                          for (
                            int i = 0;
                            i <= dataSnapshot.child(eventoGlobalRef).getChildrenCount();
                            i++
                          )
                          {
                            strategyNewId++;
                          }

                          Strategy newStrategy = new Strategy(
                            strategyNewId,
                            targetField,
                            categoryField,
                            descField
                          );

                          newBdRef.push().setValue(newStrategy);

                          setResult(RESULT_OK);
                          finish();

                          return;
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {

                        }
                      });
                    }
                  }
                }
              }

              @Override
              public void onCancelled(@NonNull DatabaseError databaseError)
              {

              }
            });
          }
        });

        strategyBackBtn = findViewById(R.id.strategyBackBtn);
        strategyBackBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            Intent intent = new Intent(StrategyActivity.this, EventActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("_evento", evento);
            intent.putExtras(bundle);
            startActivity(intent);
          }
        });

        break;
    }
  }

  public void setEditLayout()
  {
    setContentView(R.layout.strategy_edit);

    editTarget = findViewById(R.id.strategyEditTarget);
    editDesc = findViewById(R.id.strategyEditDesc);
    editCategory = findViewById(R.id.strategyEditCategory);
    // Spinner set up
    categoryAdapter = ArrayAdapter.createFromResource(
      StrategyActivity.this,
      R.array.strategyCategories,
      android.R.layout.simple_spinner_item
    );
    categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    editCategory.setAdapter(categoryAdapter);
    // Buttons
    editOkBtn = findViewById(R.id.strategyEditOkBtn);
    editCancelBtn = findViewById(R.id.strategyEditCancelBtn);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu)
  {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.menu.main_menu, menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item)
  {
    switch (item.getItemId())
    {
      case R.id.mnuWKFEventos:
        // Go to WKF Events
        Intent intentWKF = new Intent(
          StrategyActivity.this,
          WKFEventsActivity.class
        );
        startActivity(intentWKF);

        return true;

      case R.id.mnuLogout:
        // Logout
        fbAuth.signOut();
        // Return to Login
        setResult(RESULT_OK);
        Intent intentLogin = new Intent(StrategyActivity.this, LoginActivity.class);
        finish();
        startActivity(intentLogin);

        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private String getField(EditText field) { return field.getText().toString().trim(); }
}
