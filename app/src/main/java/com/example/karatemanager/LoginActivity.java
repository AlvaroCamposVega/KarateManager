package com.example.karatemanager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.karatemanager.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity
{
  public final int COD_REGISTRO = 88;
  // Firebase Auth and Database
  private FirebaseAuth fbAuth;
  private FirebaseDatabase fbDatabase;
  // Buttons
  private Button btnLog;
  private Button btnSup;
  // Input texts
  private EditText email;
  private EditText passwd;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    // FirebaseAuth Instance
    fbAuth = FirebaseAuth.getInstance();
    // UI Components references
    btnLog = findViewById(R.id.logBtnLogin);
    btnSup = findViewById(R.id.logBtnSignup);
    email = findViewById(R.id.logUserName);
    passwd = findViewById(R.id.logPassword);

    email.setText("usuarioprueba@gmail.com");
    passwd.setText("123456");
    // Login button Listener
    btnLog.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        // Input values entered by the user
        String inputEmail = email.getText().toString();
        String inputPasswd = passwd.getText().toString();
        // Login
        fbAuth
          .signInWithEmailAndPassword(inputEmail, inputPasswd)
          .addOnCompleteListener(new OnCompleteListener<AuthResult>()
          {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
              // Login error
              if (!task.isSuccessful())
              {
                // Snackbar
                Snackbar.make(
                  btnLog,
                  getString(R.string.error_ema_or_pass),
                  Snackbar.LENGTH_LONG
                  // OK button in the Snackbar
                ).setAction(R.string.action_ok, new View.OnClickListener()
                {
                  @Override
                  public void onClick(View v)
                  {
                    // Dismiss the Snackbar
                  }

                }).show();
              } else
              {
                // UID of logged-in user
                String uid = fbAuth.getCurrentUser().getUid();
                // Firebase Instance
                fbDatabase = FirebaseDatabase.getInstance();
                // Reference to logged-in user
                DatabaseReference userReference = fbDatabase.getReference()
                  .child("usuarios/" + uid);

                userReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                  @Override
                  public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                  {
                    if (dataSnapshot.hasChildren())
                    {
                      // Get logged user data
                        /*User user = dataSnapshot.getValue(User.class);
                        // Crate a Bundle
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("_user", user);*/
                      // Main Activity
                      Intent intent = new Intent(
                        LoginActivity.this,
                        MainActivity.class
                      );
                      // Storage data in the Intent
                      /*intent.putExtras(bundle);*/
                      // Launch the intent
                      startActivity(intent);
                    }
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
    });

    // Sing up buton Listener
    btnSup.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        Intent intent = new Intent(
          LoginActivity.this,
          RegisterActivity.class
        );
        startActivityForResult(intent, COD_REGISTRO);
      }
    });

  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
  {
    if (requestCode == COD_REGISTRO)
    {
      if (resultCode == RESULT_OK)
      {
        Snackbar.make(
          btnLog,
          getString(R.string.info_signup_success),
          Snackbar.LENGTH_LONG
        ).show();

      } else
      {
        Snackbar.make(
          btnLog,
          getString(R.string.info_signup_cancel),
          Snackbar.LENGTH_LONG
        ).show();
      }
    }

    super.onActivityResult(requestCode, resultCode, data);
  }
}
