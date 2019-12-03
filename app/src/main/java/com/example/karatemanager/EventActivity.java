package com.example.karatemanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.karatemanager.adapters.EventoAdapter;
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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;


public class EventActivity extends AppCompatActivity
{
  FirebaseAuth fbAuth;
  FirebaseDatabase fbDatabase;

  private Evento evento;
  private int eventoNewId;
  private int highestId;
  private String eventoGlobalRef;

  private final String EDIT_MODE = "edit";
  private final String INFO_MODE = "info";
  // Info layout
  public TextView infoName;
  public TextView infoCity;
  public TextView infoStartDate;
  public TextView infoEndDate;
  public Button infoEditBtn;
  public Button infoDeleteBtn;
  // Strategies
  public RecyclerView recyclerView;
  public FloatingActionButton createStrategyBtn;
  public TextView infoStrategyText;
  private int highestStratId;

  private List<Strategy> strategiesList;
  private StrategyAdapter adapter;
  // Edit layout
  public EditText editName;
  public EditText editCity;
  public EditText editStartDate;
  public EditText editEndDate;
  public Button editOkBtn;
  public Button editCancelBtn;

  public boolean createBtnFlag = true;
  public boolean editBtnFlag = true;
  public boolean deleteBtnFlag = true;

  private final int RESULT_DELETE_SUCCESS = 2;
  private final int RESULT_DELETE_FAIL = 3;
  private final int COD_STRATEGY = 60;
  private final int COD_EVENT = 20;
  private final int COD_CREATE = 30;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    Bundle extras = getIntent().getExtras();

    String mode = INFO_MODE;

    if (extras != null)
    {
      if (extras.getString("_mode") != null)
      {
        mode = extras.getString("_mode");
      }

      evento = (Evento) extras.getSerializable("_evento");
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
        setContentView(R.layout.event_info);

        infoName = findViewById(R.id.eventoInfoName);
        infoCity = findViewById(R.id.eventoInfoCity);
        infoStartDate = findViewById(R.id.eventoInfoStartDate);
        infoEndDate = findViewById(R.id.eventoInfoEndDate);
        // Buttons
        infoEditBtn = findViewById(R.id.eventoInfoEditBtn);
        infoDeleteBtn = findViewById(R.id.eventoInfoDeleteBtn);
        // Strategies
        recyclerView = findViewById(R.id.eventoInfoStrategiesList);
        createStrategyBtn = findViewById(R.id.eventoInfoStrategiesCreateBtn);
        infoStrategyText = findViewById(R.id.eventoInfoStrategiesInfo);

        infoName.setText(evento.getName());
        infoCity.setText(evento.getCity());
        infoStartDate.setText(getString(R.string.label_eventoStart) + evento.getStartDate());
        infoEndDate.setText(getString(R.string.label_eventoEnd) + evento.getEndDate());
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

        // Strategies adapter
        adapter = new StrategyAdapter(this, evento);
        // The RecyclerView
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);

        DatabaseReference eventosRef = fbDatabase.getReference().child("eventos/" + uid);
        // Get all strategies from this event
        eventosRef.addListenerForSingleValueEvent(new ValueEventListener()
        {
          @Override
          public void onDataChange(@NonNull DataSnapshot dataSnapshot)
          {
            strategiesList = new ArrayList<Strategy>();

            if (dataSnapshot.hasChildren())
            {
              // Add only the strategies this user owns to the adapter
              for (DataSnapshot eventoDS : dataSnapshot.getChildren())
              {
                Evento dbEvento = eventoDS.getValue(Evento.class);

                if (dbEvento.getId() == evento.getId())
                {
                  DatabaseReference eventoRef = eventoDS.getRef();

                  eventoGlobalRef = eventoRef.toString().substring(
                    eventoRef.toString().lastIndexOf("/") + 1
                  );

                  DatabaseReference strategiesRef = fbDatabase.getReference()
                    .child("estrategias/" + eventoGlobalRef);

                  strategiesRef.addListenerForSingleValueEvent(new ValueEventListener()
                  {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                      if (dataSnapshot.hasChildren())
                      {
                        for (DataSnapshot strategyDS : dataSnapshot.getChildren())
                        {
                          Strategy strategy = strategyDS.getValue(Strategy.class);

                          strategiesList.add(strategy);
                        }

                        adapter.setStrategyList(strategiesList);

                        infoStrategyText.setVisibility(View.INVISIBLE);

                      } else
                      {
                        // Strategies info text
                        infoStrategyText.setText(
                          getString(R.string.info_noStrategies1) +
                            "\n\n" +
                            getString(R.string.info_noStrategies2)
                        );
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

        infoDeleteBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(final View v)
          {
            new MaterialAlertDialogBuilder(
              EventActivity.this,
              R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
            )
              .setTitle(R.string.dialog_title)
              .setMessage(R.string.dialog_msg)
              .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener()
              {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                  if (deleteBtnFlag)
                  {
                    deleteBtnFlag = false;
                    // Database reference
                    final DatabaseReference dbRef = fbDatabase.getReference()
                      .child("eventos/" + uid);

                    dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                      @Override
                      public void onDataChange(@NonNull final DataSnapshot dataSnapshot)
                      {
                        highestId = 1;

                        for (DataSnapshot eventoDs : dataSnapshot.getChildren())
                        {
                          Evento dbEvento = eventoDs.getValue(Evento.class);

                          if (dbEvento.getId() > highestId)
                          {
                            highestId = dbEvento.getId();
                          }
                        }

                        for (DataSnapshot eventoDS : dataSnapshot.getChildren())
                        {
                          final Evento dbEvento = eventoDS.getValue(Evento.class);

                          if (dbEvento.getId() == evento.getId())
                          {
                            DatabaseReference eventoRef = eventoDS.getRef();

                            String eventoGlobalRef = eventoRef.toString().substring(
                              eventoRef.toString().lastIndexOf("/") + 1);

                            final DatabaseReference strategyRef = fbDatabase.getReference()
                              .child("estrategias/" + eventoGlobalRef);

                            eventoRef.removeValue()
                              .addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                              @Override
                              public void onComplete(@NonNull Task<Void> task)
                              {
                                if (!(dbEvento.getId() == highestId))
                                {
                                  for (DataSnapshot eventoDs2 : dataSnapshot.getChildren())
                                  {
                                    Evento eventoHighestId = eventoDs2.getValue(Evento.class);

                                    if (eventoHighestId.getId() == highestId)
                                    {
                                      eventoDs2.getRef().setValue(new Evento(
                                        evento.getId(),
                                        eventoHighestId.getName(),
                                        eventoHighestId.getCity(),
                                        eventoHighestId.getStartDate(),
                                        eventoHighestId.getEndDate()
                                      ));
                                    }
                                  }
                                }

                                if (!task.isSuccessful())
                                {
                                  Intent intent = new Intent(
                                    EventActivity.this,
                                    MainActivity.class
                                  );
                                  startActivity(intent);
                                  setResult(RESULT_DELETE_FAIL);
                                  finish();

                                  return;

                                } else
                                {
                                  strategyRef.removeValue();

                                  Intent intent = new Intent(
                                    EventActivity.this,
                                    MainActivity.class
                                  );
                                  startActivity(intent);
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
              })
              .setNegativeButton(R.string.label_no, null)
              .show();
          }
        });
        // Create strategy button
        createStrategyBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            Intent strategyCreate = new Intent(
              EventActivity.this,
              StrategyActivity.class
            );

            Bundle bundleSC = new Bundle();
            bundleSC.putSerializable("_evento", evento);
            bundleSC.putString("_mode", "create");

            strategyCreate.putExtras(bundleSC);
            startActivityForResult(strategyCreate, COD_CREATE);
          }
        });

        break;

      case "edit":
        setEditLayout();
        editBtnFlag = true;

        editName.setText(evento.getName());
        editCity.setText(evento.getCity());
        editStartDate.setText(evento.getStartDate());
        editEndDate.setText(evento.getEndDate());

        editOkBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            if (editBtnFlag)
            {
              editBtnFlag = false;

              final String nameField = getField(editName);
              final String cityField = getField(editCity);
              final String startDateField = getField(editStartDate);
              final String endDateField = getField(editEndDate);

              if (nameField.isEmpty() || cityField.isEmpty() || startDateField.isEmpty()
                || endDateField.isEmpty())
              {
                editBtnFlag = true;

                Snackbar.make(
                  v,
                  getResources().getText(R.string.error_empty_fields),
                  Snackbar.LENGTH_LONG
                ).show();

                return;
              }
              // User UID and database reference
              final String uid = fbAuth.getUid();
              final DatabaseReference dbRef = fbDatabase.getReference().child("eventos/" + uid);

              dbRef.addListenerForSingleValueEvent(new ValueEventListener()
              {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                  Evento updateEvento = null;
                  DatabaseReference eventoRef = null;

                  for (DataSnapshot eventoDS : dataSnapshot.getChildren())
                  {
                    Evento dbEvento = eventoDS.getValue(Evento.class);

                    if (dbEvento.getId() == evento.getId())
                    {
                      updateEvento = new Evento(
                        evento.getId(),
                        nameField,
                        cityField,
                        startDateField,
                        endDateField
                      );
                      eventoRef = eventoDS.getRef();
                    }
                  }

                  eventoRef.setValue(updateEvento);

                  evento = updateEvento;
                  setActivityLayout(INFO_MODE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {

                }
              });
            }
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

        break;

      case "create":
        setEditLayout();
        // Change the title so the user knows he's a the create page
        TextView title = findViewById(R.id.eventoEditTitle);
        title.setText(getString(R.string.label_event_new));
        // Hide the back button of the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        editCancelBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            Intent mainIntent = new Intent(EventActivity.this, MainActivity.class);
            startActivity(mainIntent);
          }
        });

        editOkBtn.setOnClickListener(new View.OnClickListener()
        {
          @Override
          public void onClick(View v)
          {
            if (createBtnFlag)
            {
              createBtnFlag = false;

              final String nameField = getField(editName);
              final String cityField = getField(editCity);
              final String startDateField = getField(editStartDate);
              final String endDateField = getField(editEndDate);

              if (nameField.isEmpty() || cityField.isEmpty() || startDateField.isEmpty()
                || endDateField.isEmpty())
              {
                createBtnFlag = true;

                Snackbar.make(
                  v,
                  getResources().getText(R.string.error_empty_fields),
                  Snackbar.LENGTH_LONG
                ).show();

                return;
              }
              // User UID and database reference
              final String uid = fbAuth.getUid();
              final DatabaseReference dbRef = fbDatabase.getReference().child("eventos/");

              eventoNewId = 1;

              dbRef.addListenerForSingleValueEvent(new ValueEventListener()
              {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                  DatabaseReference newBdRef = dbRef.child(uid);

                  for (int i = 0; i <= dataSnapshot.child(uid).getChildrenCount(); i++)
                  {
                    eventoNewId++;
                  }

                  Evento newEvento = new Evento(
                    eventoNewId,
                    nameField,
                    cityField,
                    startDateField,
                    endDateField
                  );

                  newBdRef.push().setValue(newEvento);

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
        });
    }
  }

  public void setEditLayout()
  {
    setContentView(R.layout.event_edit);

    editName = findViewById(R.id.eventoEditName);
    editCity = findViewById(R.id.eventoEditCity);
    editStartDate = findViewById(R.id.eventoEditStartDate);
    editEndDate = findViewById(R.id.eventoEditEndDate);
    // Buttons
    editOkBtn = findViewById(R.id.eventoEditOkBtn);
    editCancelBtn = findViewById(R.id.eventoEditCancelBtn);
  }

  @Override
  public boolean onContextItemSelected(@NonNull final MenuItem item)
  {
    switch (item.getGroupId())
    {
      case 0:
        // Go to Strategy info
        Intent strategyInfo = new Intent(EventActivity.this, StrategyActivity.class);
        // Create the bundle
        Bundle bundleSI = new Bundle();
        bundleSI.putSerializable("_strategy", getStrategyById(item.getItemId()));
        bundleSI.putSerializable("_evento", evento);
        // Put it in the Intent and start the Activity
        strategyInfo.putExtras(bundleSI);
        startActivityForResult(strategyInfo, COD_EVENT);

        return true;

      case 1:
        Intent strategyEdit = new Intent(EventActivity.this, StrategyActivity.class);

        Bundle bundleSE = new Bundle();
        bundleSE.putString("_mode", "edit");
        bundleSE.putSerializable("_strategy", getStrategyById(item.getItemId()));
        bundleSE.putSerializable("_evento", evento);

        strategyEdit.putExtras(bundleSE);
        startActivityForResult(strategyEdit, COD_EVENT);

        return true;

      case 2:
        new MaterialAlertDialogBuilder(
          EventActivity.this,
          R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog
        )
          .setTitle(R.string.dialog_title)
          .setMessage(R.string.dialog_msg)
          .setPositiveButton(R.string.label_yes, new DialogInterface.OnClickListener()
          {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
              // User UID and database reference
              final String uid = fbAuth.getUid();
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
                            highestStratId = 1;

                            for (DataSnapshot strategyDs : dataSnapshot.getChildren())
                            {
                              final Strategy dbStrategy = strategyDs.getValue(Strategy.class);

                              if (dbStrategy.getId() > highestStratId)
                              {
                                highestStratId = dbStrategy.getId();
                              }
                            }

                            for (DataSnapshot strategyDS : dataSnapshot.getChildren())
                            {
                              final Strategy dbStrategy = strategyDS.getValue(Strategy.class);

                              if (dbStrategy.getId() == item.getItemId())
                              {
                                DatabaseReference strategyRef = strategyDS.getRef();

                                strategyRef.removeValue()
                                  .addOnCompleteListener(new OnCompleteListener<Void>()
                                  {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task)
                                    {
                                      if (!(dbStrategy.getId() == highestStratId))
                                      {
                                        for (
                                          DataSnapshot strategyDs2 : dataSnapshot.getChildren()
                                        )
                                        {
                                          Strategy strategyHighestId = strategyDs2
                                            .getValue(Strategy.class);

                                          if (strategyHighestId.getId() == highestStratId)
                                          {
                                            strategyDs2.getRef().setValue(new Strategy(
                                              item.getItemId(),
                                              strategyHighestId.getTarget(),
                                              strategyHighestId.getCategory(),
                                              strategyHighestId.getDesc()
                                            ));
                                          }
                                        }
                                      }

                                      if (!task.isSuccessful())
                                      {
                                        Snackbar.make(
                                          recyclerView,
                                          getResources().getText(R.string.error_delete),
                                          Snackbar.LENGTH_LONG
                                        ).show();

                                      } else
                                      {
                                        Snackbar.make(
                                          recyclerView,
                                          getResources()
                                            .getText(R.string.info_strategy_delete_success),
                                          Snackbar.LENGTH_LONG
                                        ).show();

                                        Intent intent = new Intent(
                                          EventActivity.this,
                                          EventActivity.class
                                        );
                                        Bundle bundle = new Bundle();
                                        bundle.putSerializable("_evento", evento);
                                        intent.putExtras(bundle);
                                        startActivity(intent);
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

        return true;

      default:
        return super.onContextItemSelected(item);
    }
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
          EventActivity.this,
          WKFEventsActivity.class
        );
        startActivity(intentWKF);

        return true;

      case R.id.mnuLogout:
        // Logout
        fbAuth.signOut();
        // Return to Login
        setResult(RESULT_OK);
        Intent intentLogin = new Intent(EventActivity.this, LoginActivity.class);
        finish();
        startActivity(intentLogin);

        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
  {
    if (requestCode == COD_STRATEGY)
    {
      switch (resultCode)
      {
        case RESULT_DELETE_FAIL:
          Snackbar.make(
            recyclerView,
            getResources().getText(R.string.error_delete),
            Snackbar.LENGTH_LONG
          ).show();

          break;

        case RESULT_DELETE_SUCCESS:
          Snackbar.make(
            recyclerView,
            getResources().getText(R.string.info_strategy_delete_success),
            Snackbar.LENGTH_LONG
          ).show();

          Intent intent = new Intent(EventActivity.this, EventActivity.class);
          Bundle bundle = new Bundle();
          bundle.putSerializable("_evento", evento);
          intent.putExtras(bundle);
          startActivity(intent);

          break;
      }

    } else if (requestCode == COD_CREATE)
    {
      switch (resultCode)
      {
        case RESULT_OK:
          Snackbar.make(
            recyclerView,
            getResources().getText(R.string.info_strategy_create),
            Snackbar.LENGTH_LONG
          ).show();
      }

      Intent intent = new Intent(EventActivity.this, EventActivity.class);
      Bundle bundle = new Bundle();
      bundle.putSerializable("_evento", evento);
      intent.putExtras(bundle);
      startActivity(intent);
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  public Strategy getStrategyById(int id)
  {
    for (Strategy strategy : strategiesList)
    {
      if (strategy.getId() == id)
      {
        return strategy;
      }
    }

    return null;
  }

  private String getField(EditText field) { return field.getText().toString().trim(); }
}
