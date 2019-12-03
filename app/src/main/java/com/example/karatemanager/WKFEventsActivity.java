package com.example.karatemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.karatemanager.adapters.EventoAdapter;
import com.example.karatemanager.model.Evento;
import com.example.karatemanager.services.APIClient;
import com.example.karatemanager.services.APIService;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WKFEventsActivity extends AppCompatActivity
{
  private FirebaseAuth fbAuth;
  private APIService apiService;

  private List<Evento> eventos;
  private EventoAdapter adapter;

  @BindView(R.id.wkfList)
  public RecyclerView recyclerView;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_wkfevents);

    ButterKnife.bind(this);
    // Set the title Uppercase
    TextView title = findViewById(R.id.wkfTitle);
    title.setText(title.getText().toString().toUpperCase());

    fbAuth = FirebaseAuth.getInstance();

    apiService = APIClient.getService("http://" + getString(R.string.app_ip) +
      ":8080/KarateAPI/");

    adapter = new EventoAdapter(this, true);

    recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
    recyclerView.setAdapter(adapter);

    getAllEventos();
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
        Intent intent = new Intent(WKFEventsActivity.this, LoginActivity.class);
        finish();
        startActivity(intent);

        return true;
    }

    return super.onOptionsItemSelected(item);
  }

  private void getAllEventos()
  {
    apiService.getAllEventos().enqueue(new Callback<List<Evento>>()
    {
      @Override
      public void onResponse(Call<List<Evento>> call, Response<List<Evento>> response)
      {
        if (response.isSuccessful())
        {
          eventos = response.body();

          adapter.setEventoList(eventos);
        }
      }

      @Override
      public void onFailure(Call<List<Evento>> call, Throwable t)
      {
        Log.i("KARATE_MANAGER:", "Respuesta SIN éxito!");
      }
    });
  }
}
