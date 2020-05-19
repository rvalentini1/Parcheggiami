package com.example.parcheggiami;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //VOGLIAMO CREARE UNA PAGINA INIZIALE CON UN TEMA APPLICATO COME IMMAGINE E UN RITARDO DI UN SECONDO

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
        }

        //PASSIAMO ALLA SECONDA PAGINA DELLA NOSTRA APP
        startActivity(new Intent(this, Login.class));
        finish();

    }
}
