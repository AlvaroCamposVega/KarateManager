package com.example.karatemanager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.ConfigurationCompat;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.karatemanager.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends AppCompatActivity
{
  @BindView(R.id.regName)
  public EditText name;

  @BindView(R.id.regSurname)
  public EditText surname;

  @BindView(R.id.regCountry)
  public Spinner country;

  @BindView(R.id.regCity)
  public EditText city;

  @BindView(R.id.regUserName)
  public EditText userName;

  @BindView(R.id.regPassword)
  public EditText password;

  @BindView(R.id.regBtnSignup)
  public Button btnReg;

  @BindView(R.id.regBtnCancel)
  public Button btnCancel;

  private FirebaseAuth fbAuth;
  private FirebaseDatabase fbDatabase;

  private Locale locale;
  // Sign up flag
  private boolean signupBtnFlag = true;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    // Bind the activity with ButterKnife
    ButterKnife.bind(this);
    // Instantiate Firebase
    fbAuth = FirebaseAuth.getInstance();
    fbDatabase = FirebaseDatabase.getInstance();
    // Spinner set up
    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
      this,
      R.array.regCountries,
      android.R.layout.simple_spinner_item
    );
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    country.setAdapter(adapter);
    // We get the device locale
    Locale locale = ConfigurationCompat.getLocales(
      Resources.getSystem().getConfiguration()
    ).get(0);

    int i = 0;
    // Make the default option become the country where the user is
    while (i < adapter.getCount() && i >= 0)
    {
      String item = adapter.getItem(i).toString();

      if (item.equals(locale.getDisplayCountry()))
      {
        country.setSelection(i);
        i = -2;
      }

      i++;
    }

    btnReg.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(final View v)
      {
        if (signupBtnFlag)
        {
          signupBtnFlag = false;
          // Obtain the values from the form fields
          final String nameField = getField(name);
          final String surnameField = getField(surname);
          final String countryField = country.getSelectedItem().toString();
          final String cityField = getField(city);
          final String userNameField = getField(userName);
          final String passwordField = getField(password);

          if (nameField.isEmpty() || surnameField.isEmpty() || userNameField.isEmpty()
            || passwordField.isEmpty() || cityField.isEmpty()
          )
          {
            signupBtnFlag = true;
            Snackbar.make(
              v,
              getResources().getText(R.string.error_empty_fields),
              Snackbar.LENGTH_LONG
            ).show();

            return;
          }

          fbAuth.createUserWithEmailAndPassword(userNameField, passwordField)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
              @Override
              public void onComplete(@NonNull Task<AuthResult> task)
              {
                // If the user is successfully registered
                if (task.isSuccessful())
                {
                  // Get the user UID
                  String uid = fbAuth.getUid();
                  // Create the user
                  User user = new User(nameField, surnameField, countryField, cityField);
                  // Get the database reference
                  DatabaseReference dbRef = fbDatabase.getReference("usuarios");
                  // Create the new user in the database
                  dbRef.child(uid).setValue(user);
                  // Sign out as Firebase Logs in new registered users automatically
                  fbAuth.signOut();
                  // Return to the Login Activity
                  setResult(RESULT_OK);
                  finish();

                  return;

                } else
                {
                  signupBtnFlag = true;
                  Snackbar.make(
                    v,
                    getResources().getText(R.string.error_signup),
                    Snackbar.LENGTH_LONG
                  ).show();
                }
              }
            });
        }
      }
    });

    // Cancel Button
    btnCancel.setOnClickListener(new View.OnClickListener()
    {
      @Override
      public void onClick(View v)
      {
        setResult(RESULT_CANCELED);
        finish();

        return;
      }
    });
  }

  private String getField(EditText field)
  {
    return field.getText().toString().trim();
  }
}
