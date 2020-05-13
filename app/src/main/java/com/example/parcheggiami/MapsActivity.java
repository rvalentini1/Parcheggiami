package com.example.parcheggiami;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;

import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import java.util.Locale;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    SearchView searchView;
    double Lat=0;
    double Long=0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Park park;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Toast.makeText(MapsActivity.this, "bbb", Toast.LENGTH_LONG).show();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();


        final TextView textView = (TextView)findViewById(R.id.textView);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
      /*  SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
       // mapFragment.getMapAsync(this);*/
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLocation();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        textView.setText(String.format(Locale.ITALIAN, "%s -- %s", location.getLatitude(), location.getLongitude()));
                   }
                }
            }
        };




    }
    @Override
    public void onResume(){
        super.onResume();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final DatabaseReference myRef = database.getReference().child("parks");
        String uid = currentUser.getUid();
        myRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        retrieve_marker(dataSnapshot);
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("ERROREDB", "Failed to read value.", error.toException());
            }
        });

    }
  /*  public void onClickBtn(View v)
    {
        Toast.makeText(this, "Informazioni Memorizzate", Toast.LENGTH_LONG).show();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final DatabaseReference myRef = database.getReference().child("parks");
        String uid = currentUser.getUid();
        // Creo un nuovo nodo per la classe Park,che ritorna un id univoco
        // Il nodo Park sarò /parks/$parkij/
        // creo l'oggetto park
        Park park = new Park(String.valueOf(Lat), String.valueOf(Long),"0");

        // faccio il 'push' di park nel nodo usando parkid
        myRef.child(uid).setValue(park);
    }*/

    private void fetchLocation() {
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
            return;
        }

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {

                    currentLocation = location;
                    Toast.makeText(getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                    SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
                    assert supportMapFragment != null;
                    supportMapFragment.getMapAsync(MapsActivity.this);

                    calculate_distance(currentLocation.getLatitude(), currentLocation.getLongitude());
                }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;
        LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        Lat=currentLocation.getLatitude();
        Long=currentLocation.getLongitude();

        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Sei qui!");
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 5));
        googleMap.addMarker(markerOptions);
    }
    @Override
    //Permessi per la geolocalizzazione
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetchLocation();
                }
                break;
        }
    }

    //Leggo da firebase tutti i parcheggi memorizzati
    private void retrieve_marker(DataSnapshot dataSnapshot) {

        for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            if(mMap != null) {
                mMap.clear();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    Log.v("FIREBASELOG", "" + childDataSnapshot.child("libero").getValue());

                    Log.v("FIREBASELOG", "" + childDataSnapshot.getKey()); //displays the key for the node
                    Log.v("FIREBASELOG", "" + childDataSnapshot.child("latitude").getValue());   //gives the value for given keyname
                    Double vLat = Double.parseDouble(String.valueOf(childDataSnapshot.child("latitude").getValue()));
                    Double vLong = Double.parseDouble(String.valueOf(childDataSnapshot.child("longitude").getValue()));
                    LatLng latLng = new LatLng(vLat, vLong);
                    if (childDataSnapshot.child("libero").getValue().toString().equals("1")) {
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("P1").icon(BitmapDescriptorFactory.fromResource(R.drawable.location));
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        mMap.addMarker(markerOptions);
                    }
                    calculate_distance(vLat,vLong);
                }
                LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                Lat = currentLocation.getLatitude();
                Long = currentLocation.getLongitude();

                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Sei qui!");
                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.addMarker(markerOptions);
            }
        }


    }
    //Calcolo la distanza tra due coordinate
    private void calculate_distance(Double vLat,Double vLong){
        float[] results = new float[1];
        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                vLat, vLong, results);

        final TextView textDistance = (TextView) findViewById(R.id.textPosti);
        textDistance.setText(String.valueOf(results[0]));
        if (results[0] > 2000) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            FirebaseUser currentUser = firebaseAuth.getCurrentUser();
            final DatabaseReference myRef = database.getReference().child("parks");
            String uid = currentUser.getUid();
            //Aggiorno il nodo relativo al parcheggio con lo stato "libero"
           // Park park = new Park(String.valueOf(vLat), String.valueOf(vLong), "1");
           // myRef.child(uid).setValue(park);
        }
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    /*@Override
    public void onMapReady_(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }*/
}
