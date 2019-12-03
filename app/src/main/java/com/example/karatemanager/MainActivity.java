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
import android.widget.TextView;

import com.example.karatemanager.adapters.EventoAdapter;
import com.example.karatemanager.model.Evento;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
{
  private FirebaseAuth fbAuth;
  private FirebaseDatabase fbDatabase;

  private List<Evento> eventosList;
  private EventoAdapter adapter;
  private int highestId;

  @BindView(R.id.mainUserList)
  public RecyclerView recyclerView;

  @BindView(R.id.mainCreateBtn)
  public FloatingActionButton createBtn;

  @BindView(R.id.mainInfo)
  public TextView infoText;

  private final int COD_EVENT = 20;
  private final int COD_CREATE = 30;
  private final int RESULT_DELETE_SUCCESS = 2;
  private final int RESULT_DELETE_FAIL = 3;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);
    // The adapter
    adapter = new EventoAdapter(this, false);
    // The RecyclerView
    recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    recyclerView.setAdapter(adapter);
    // Hide the back button of the action bar
    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
    // Get instances of FirebaseDatabase and FirebaseAuth
    fbDatabase = FirebaseDatabase.getInstance();
    fbAuth = FirebaseAuth.getInstance();

    final String uid = fbAuth.getCurrentUser().getUid();

    DatabaseReference eventosReference = fbDatabase.getReference().child("eventos/" + uid);

    eventosReference.addValueEventListener(new ValueEventListener()
    {
      @Override
      public void onDataChange(@NonNull DataSnapshot dataSnapshot)
      {
        eventosList = new ArrayList<Evento>();

        if (dataSnapshot.hasChildren())
        {
          // Add only the events this user owns to the adapter
          for (DataSnapshot eventoDS : dataSnapshot.getChildren())
          {
            Evento evento = eventoDS.getValue(Evento.class);

            eventosList.add(evento);
          }

          adapter.setEventoList(eventosList);

          infoText.setVisibility(View.INVISIBLE);

        } else
        {
          adapter.setEventoList(eventosList);
          // Set the information text
          infoText.setText(
            getString(R.string.info_noEvents1) +
              "\n\n" +
              getString(R.string.info_noEvents2)
          );
        }
      }

      @Override
      public void onCancelled(@NonNull DatabaseError databaseError)
      {

      }
    });

    createBtn.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent eventCreate = new Intent(MainActivity.this, EventActivity.class);

        Bundle bundleEC = new Bundle();
        bundleEC.putString("_mode", "create");

        eventCreate.putExtras(bundleEC);
        startActivityForResult(eventCreate, COD_CREATE);
      }
    });
  }

  @Override
  public boolean onContextItemSelected(@NonNull final MenuItem item)
  {
    switch (item.getGroupId())
    {
      case 0:
        // Go to Event info
        Intent eventInfo = new Intent(MainActivity.this, EventActivity.class);
        // Create the bundle
        Bundle bundleEI = new Bundle();
        bundleEI.putSerializable("_evento", getEventoById(item.getItemId()));
        // Put it in the Intent and start the Activity
        eventInfo.putExtras(bundleEI);
        startActivityForResult(eventInfo, COD_EVENT);

        return true;

      case 1:
        Intent eventEdit = new Intent(MainActivity.this, EventActivity.class);

        Bundle bundleEE = new Bundle();
        bundleEE.putString("_mode", "edit");
        bundleEE.putSerializable("_evento", getEventoById(item.getItemId()));

        eventEdit.putExtras(bundleEE);
        startActivityForResult(eventEdit, COD_EVENT);

        return true;

      case 2:
        new MaterialAlertDialogBuilder(
          MainActivity.this,
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

                    if (dbEvento.getId() == item.getItemId())
                    {
                      DatabaseReference eventoRef = eventoDS.getRef();

                      String eventoGlobalRef = eventoRef.toString().substring(
                        eventoRef.toString().lastIndexOf("/") + 1);

                      DatabaseReference strategyRef = fbDatabase.getReference()
                        .child("estrategias/" + eventoGlobalRef);

                      strategyRef.removeValue().addOnCompleteListener(
                        new OnCompleteListener<Void>()
                        {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {}
                        });

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
                                    item.getItemId(),
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
                              Snackbar.make(
                                recyclerView,
                                getResources().getText(R.string.error_delete),
                                Snackbar.LENGTH_LONG
                              ).show();

                            } else
                            {
                              Snackbar.make(
                                recyclerView,
                                getResources().getText(R.string.info_event_delete_success),
                                Snackbar.LENGTH_LONG
                              ).show();
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
      case R.id.mnuLogout:
        // Logout
        fbAuth.signOut();
        // Return to Login
        setResult(RESULT_OK);
        finish();

        return true;

      case R.id.mnuWKFEventos:
        // Go to WKF Events
        Intent intent = new Intent(MainActivity.this, WKFEventsActivity.class);
        startActivity(intent);

        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
  {
    if (requestCode == COD_EVENT)
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
            getResources().getText(R.string.info_event_delete_success),
            Snackbar.LENGTH_LONG
          ).show();

          break;
      }

    } else if (requestCode == COD_CREATE)
    {
      switch (resultCode)
      {
        case RESULT_OK:
          Snackbar.make(
            recyclerView,
            getResources().getText(R.string.info_event_create),
            Snackbar.LENGTH_LONG
          ).show();
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }

  public Evento getEventoById(int id)
  {
    for (Evento evento : eventosList)
    {
      if (evento.getId() == id)
      {
        return evento;
      }
    }

    return null;
  }
}
