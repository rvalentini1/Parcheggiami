package com.example.parcheggiami;



import android.content.Context;
import android.content.Intent;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final String ARG_NAME = "username";

    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;

    public static void startActivity(Context context, String username) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(ARG_NAME, username);
        context.startActivity(intent);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.textViewWelcome);
        if (getIntent().hasExtra(ARG_NAME)) {
            textView.setText(String.format("Benvenuto - %s", getIntent().getStringExtra(ARG_NAME)));
        }
        findViewById(R.id.buttonLogout).setOnClickListener(this);
        findViewById(R.id.buttonDisconnect).setOnClickListener(this);
        findViewById(R.id.buttonMap).setOnClickListener(this);
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.buttonLogout:
                signOut();
                break;
            case R.id.buttonDisconnect:

                 revokeAccess();
                break;
            case R.id.buttonMap:
                Intent myIntent = new Intent(MainActivity.this, MapsActivity.class);

                MainActivity.this.startActivity(myIntent);

                break;
        }
    }

    private void signOut() {
        // Firebase Logout
        firebaseAuth.signOut();
        // Google Logout
        googleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Google Login fallita,  aggiorno  UI
                        Log.w(TAG, "Signed out of google");
                        Intent myIntent = new Intent(MainActivity.this, Login.class);
                        MainActivity.this.startActivity(myIntent);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase Logout
        firebaseAuth.signOut();

        // Google revoke access
        googleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // Google Login fallita,  aggiorno  UI
                        Log.w(TAG, "Revoked Access");
                    }
                });
    }
}

