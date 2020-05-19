package com.example.parcheggiami;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Login extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int RC_SIGN_IN = 1001;

    GoogleSignInClient googleSignInClient;
    private Button Btn;
    private Button RegisterButton;
    private FirebaseAuth firebaseAuth;
    ActionBar actionbar;

    private EditText emailTextView, passwordTextView;
    private Button Btn2;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {

                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
            }
        });


        // Configuro il cliente di google
        configureGoogleClient();


        // inizilizzo gli elementi della view
        emailTextView = findViewById(R.id.username);
        passwordTextView = findViewById(R.id.password);
        Btn2 = findViewById(R.id.login);
        progressBar = findViewById(R.id.progressBar);

        // Onclick listner per il pulsante di login
        Btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                loginUserAccount();
            }
        });
    }


    public void onClickRegister(View v) {

        Intent myIntent = new Intent(Login.this, RegistrationActivity.class);

        Login.this.startActivity(myIntent);

    }

    private void loginUserAccount()
    {


        progressBar.setVisibility(View.VISIBLE);


        String email, password;
        email = emailTextView.getText().toString();
        password = passwordTextView.getText().toString();

        // VALIDAZIONE EMAIL E PASSWWORD
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getApplicationContext(),
                    "Inserisci la mail!!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(getApplicationContext(),
                    "Inserisci la password !!",
                    Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // Login utente già registrato
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(
                                    @NonNull Task<AuthResult> task)
                            {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getApplicationContext(),
                                            "Login Riuscito!!",
                                            Toast.LENGTH_LONG)
                                            .show();


                                    progressBar.setVisibility(View.INVISIBLE);

                                    // Se la login ha successo, vado alla Map Activity

                                    Intent intent
                                            = new Intent(Login.this,
                                            MapsActivity2.class);
                                    startActivity(intent);
                                }

                                else {

                                    // Login Fallita
                                    Toast.makeText(getApplicationContext(),
                                            "Login non riuscita!!",
                                            Toast.LENGTH_LONG)
                                            .show();

                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            }
                        });
    }

    private void configureGoogleClient() {
        // Configuro La Login Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                // Il token id è generato da google-services.json
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Pulsante LogIN , setto le dimnesioni
        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_WIDE);

        // Inizializzo Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Check se l'utente è loggato (non-null) e  aggiorno UI.
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            showToastMessage("Attualemente loggato con: " + currentUser.getEmail());
            launchMainActivity(currentUser);
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // requestCode ritorna da  GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Login riuscita, auttentico l'utente con Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                showToastMessage("Google Login Eseguita");
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Login fallita,  aggiorno  UI
                Log.w(TAG, "Google Login Fallita", e);
                showToastMessage("Google Login Fallita" + e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {

        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Login riuscita
                            FirebaseUser user = firebaseAuth.getCurrentUser();

                           // showToastMessage("Firebase OK ");
                            launchMainActivity(user);
                        } else {
                            // Se la login fallisce, avviso l'intente

                            showToastMessage("Autenticazione firebase fallita:" + task.getException());
                        }
                    }
                });
    }

    private void showToastMessage(String message) {
        Toast.makeText(Login.this, message, Toast.LENGTH_LONG).show();
    }

    private void launchMainActivity(FirebaseUser user) {

        if (user != null) {

            Intent myIntent = new Intent(Login.this, MapsActivity2.class);

            Login.this.startActivity(myIntent);
           //*** MainActivity.startActivity(this, user.getDisplayName());
            finish();
        }
    }
}