package com.example.parcheggiami;

import android.os.Bundle;

import com.google.android.gms.maps.MapView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    MapView m1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        m1=(MapView)findViewById(R.id.mapView);


    }

}
