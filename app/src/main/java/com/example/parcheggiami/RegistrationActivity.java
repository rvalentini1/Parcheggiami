package com.example.parcheggiami;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
public class RegistrationActivity extends AppCompatActivity {
    private EditText emailTextView, passwordTextView;
    private Button Btn;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        //Istanza Firebase
        mAuth = FirebaseAuth.getInstance();

        // Inizializzo gli elementi della view
        emailTextView = findViewById(R.id.username);
        passwordTextView = findViewById(R.id.password);
        Btn = findViewById(R.id.btnregister);
        progressBar = findViewById(R.id.loading);

        // Setto l'evento onclick del pulsante
        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                registerNewUser();
            }
        });
    }
    private void registerNewUser()
    {


        progressBar.setVisibility(View.VISIBLE);


        String email, password;
        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString();

        // Validazione email e password
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                    "Inserisci la mail!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                    "Inserisci la password!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Creazione nuovo utente
        mAuth
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful()) {
                            Toast.makeText(getApplicationContext(),
                                    "Registrazione avvenuta con successo!",
                                    Toast.LENGTH_LONG)
                                    .show();


                            progressBar.setVisibility(View.GONE);

                            // Lancio l'activity map dopo la registrazione
                            Intent intent
                                    = new Intent(RegistrationActivity.this,
                                    MapsActivity2.class);
                            startActivity(intent);
                        }
                        else {

                            // Registrazione fallita
                            Toast.makeText(
                                    getApplicationContext(),
                                    "Registrazione fallita, riprova",
                                    Toast.LENGTH_LONG)
                                    .show();

                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
    }

}
