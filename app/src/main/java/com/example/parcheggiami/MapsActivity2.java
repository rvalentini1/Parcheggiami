package com.example.parcheggiami;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;


import android.util.Log;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class MapsActivity2 extends FragmentActivity implements LocationListener,
        OnMapReadyCallback, GoogleApiClient
                .ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    GoogleSignInClient googleSignInClient;
    private static final int PERMISSION_FINE_LOCATION =101 ;
    private GoogleMap mMap;
    private final int MY_LOCATION_REQUEST_CODE = 100;
    private Handler handler;
    private Marker m;
    float zoomLevel = 10.0f;
    float distanceInMeters;
//    private GoogleApiClient googleApiClient;

    public final static int SENDING = 1;
    public final static int CONNECTING = 2;
    public final static int ERROR = 3;
    public final static int SENT = 4;
    public final static int SHUTDOWN = 5;

    private static final String TAG = "LocationActivity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    Button btnFusedLocation;
    TextView tvLocation;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    String mLastUpdateTime;
    private Location previousLocation;
    Location currentLocation;
    FirebaseAuth firebaseAuth;
    Park mypark;
    private double Long=0;
    private double Lat=0;
    private double Latsel=0;
    private double Longsel=0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private String marker_id="";
    Geocoder geocoder;
    Toolbar toolbar;
    TextView tv_address,tv_lat,tv_long,tv_latitudine,tv_longitudine,tv_posizione_attuale,tv_pos_park_lat,tv_pos_park_long;
    TextView tv_distanza,tv_dist;
    Location pos_park,pos_att;


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
//DICHIARAZIONI VARIABILI UI
        tv_address=findViewById(R.id.tv_address);
        tv_lat=findViewById(R.id.tv_lat);
        tv_long=findViewById(R.id.tv_long);
        tv_latitudine=findViewById(R.id.tv_lat);
        tv_longitudine=findViewById(R.id.tv_long);
        tv_posizione_attuale=findViewById(R.id.tv_pos_park);
        tv_pos_park_lat=findViewById(R.id.tv_pos_park_lat);
        tv_pos_park_long=findViewById(R.id.tv_pos_park_long);
        tv_distanza=findViewById(R.id.tv_distanza);
        tv_dist=findViewById(R.id.tv_dist);


//CONTROLLO RICHIESTA PERMESSI
        createLocationRequest();
//GESIONE API GOOGLE
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        //RICHIAMO DEL FIREBASE
        firebaseAuth = FirebaseAuth.getInstance();
        googleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



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

    //QUANDO LA MAPPA E PRONTA LA VISUALIZZO
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(40.991734, 14.135251);

        m = mMap.addMarker(new MarkerOptions().position(sydney).title("My pos"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, zoomLevel));


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);

        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_FINE_LOCATION);

                // Show rationale and request permission.

            }
        }
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                //Gestisco il click sul Marker, memorizzando l'id per poter poi memorizzare il parcheggio
                marker.showInfoWindow();
                Object m=marker.getTag();
                String tags;
                if(m!=null) marker_id=m.toString();
                else marker_id="";

                Longsel=marker.getPosition().longitude;
                Latsel=marker.getPosition().latitude;
                //Toast.makeText(getActivity(),  marker_id  , Toast.LENGTH_LONG).show();

                return true;
            }
        });

    }
//ANIMAZIONE MARKER
/*
    public void rotateMarker(final Marker marker, final float toRotation, final float st) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = st;
        final long duration = 1555;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;

                marker.setRotation(-rot > 180 ? rot / 2 : rot);
                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                }
            }
        });
    }
    */

    //ANIMAZIONE MARKER
/*
    public void animateMarker(final LatLng toPosition, final boolean hideMarke) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = mMap.getProjection();
        Point startPoint = proj.toScreenLocation(m.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);
        final long duration = 5000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startLatLng.latitude;
                m.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarke) {
                        m.setVisible(false);
                    } else {
                        m.setVisible(true);
                    }
                }
            }
        });
    }
    */
    //ANIMAZIONE MARKER
/*
    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }
    */
    //GESTIONE DELLA RICHIESTA PERMESSI
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_FINE_LOCATION:
                if (grantResults[0]==PackageManager.PERMISSION_GRANTED){

                }
                else {
                    Toast.makeText(this,"QUESTA APP HA BISOGNO DEI PERMESSI PER FUNZIONARE", Toast.LENGTH_SHORT).show();


                    finish();;

                }
                break;
        }
    }

    // METODI PER LA CONNESSIONE ALLE API DI GOOGLE
    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.d(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        mGoogleApiClient.disconnect();
        Log.d(TAG, "isConnected ...............: " + mGoogleApiClient.isConnected());
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi
                .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location update started ..............: ");
    }


    //GESIONE DEL CAMBIO POSIZIONE GPS
    LatLng previouslatLng;

    @Override
    public void onLocationChanged(Location location) {
        previouslatLng = new LatLng(location.getLatitude(), location.getLongitude());

        //ANIMAZIONE MARKER
      /*  double rota = 0.0;
        double startrota = 0.0;
        if (previousLocation != null) {

            rota = bearingBetweenLocations(previouslatLng, new LatLng(location.getLatitude
                    (), location.getLongitude()));
        }


        rotateMarker(m, (float) rota, (float) startrota);
*/

        previousLocation = location;
        Log.d(TAG, "Firing onLocationChanged..........................");
        Log.d(TAG, "lat :" + location.getLatitude() + "long :" + location.getLongitude());
        Log.d(TAG, "bearing :" + location.getBearing());
        currentLocation = location;
        updateValue(location);



        //Gestisco gli aggiornamento di firebase, in modo da aggiornare la mappa
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        //  FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        final DatabaseReference myRef = database.getReference().child("parks");
        //String uid = currentUser.getUid();
        myRef.addValueEventListener(new ValueEventListener() {
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

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.d(TAG, "Location update stopped .......................");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
            Log.d(TAG, "Location update resumed .....................");

        }

    }


    //Leggo da firebase tutti i parcheggi memorizzati
    private void retrieve_marker(DataSnapshot dataSnapshot) {



        int count_liberi=0;
        //   for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
        mypark=null;
        mMap.clear();
        for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

            Double vLat = Double.parseDouble(String.valueOf(childDataSnapshot.child("latitude").getValue()));
            Double vLong = Double.parseDouble(String.valueOf(childDataSnapshot.child("longitude").getValue()));
            LatLng latLng = new LatLng(vLat, vLong);
            //     m = mMap.addMarker(new MarkerOptions().position(latLng).title("Posizione libera"));
            //  m = mMap.addMarker(new MarkerOptions().position(latLng).title("Posizione libera").icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
            //SE IL PARCHEGGIO è LIBERO, VISUALIZZO IL MARKER BLUE,ALTRIMENTI LA MIA POSIZIONE DI PARCHEGGIO (IN ROSSO)

            if (childDataSnapshot.child("libero").getValue().toString().equals("1")) {
                MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Posizione Libera").snippet("Clicca per impostare la posizione").icon(BitmapDescriptorFactory.fromResource(R.drawable.location));
                //   mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                //   m=mMap.addMarker(markerOptions);
                m = mMap.addMarker(new MarkerOptions().position(latLng).title("Posizione libera").icon(BitmapDescriptorFactory.fromResource(R.drawable.location)));
                m.setTag(childDataSnapshot.getKey());
                count_liberi+=1;
            }else if(childDataSnapshot.child("user").getValue().toString().equals(firebaseAuth.getCurrentUser().getUid().toString())){
                mypark = childDataSnapshot.getValue(Park.class);
                marker_id=childDataSnapshot.getKey();
                m.setTag(childDataSnapshot.getKey());
                //  m = mMap.addMarker(new MarkerOptions().position(latLng).title("Hai parcheggiato qui"));


                //CALCOLO DELLA DISTANZA DAL PARCHEGGIO + VISUALIZZAZIONE UI
                distanceInMeters = pos_park.distanceTo(pos_att);
                tv_dist.setText(String.valueOf(distanceInMeters));
                m = mMap.addMarker(new MarkerOptions().position(latLng).title("hai parcheggiato qui").snippet("Clicca per impostare la posizione").icon(BitmapDescriptorFactory.fromResource(R.drawable.locationp)));
                //Aggiungo la posizione di parcheggio memorizzata
                //  MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Hai parcheggiato qui").icon(BitmapDescriptorFactory.fromResource(R.drawable.locationp));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel));
                //  m=mMap.addMarker(markerOptions);
            }
        }
        if(currentLocation!=null){
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

            Lat = currentLocation.getLatitude();
            Long = currentLocation.getLongitude();

            MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Sei qui!");
            //     mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
            //      mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
            mMap.addMarker(markerOptions);
        }



    }

    //GESTIONE DEL CLICK SULLA UI
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonPark:
                //Se ho già memorizzato un parcheggio , non posso memorizzarne un altro!
                if(mypark != null) {

                    alertView("Posizione parcheggio già memorizzata");
                    return;
                }
                //Se ho selezionato il parcheggio, memorizzo i dati in firebaase
                if(marker_id!=""){
                    //Ottengo l'istanza di firebase
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child("parks");
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    // Creo un nuovo nodo per la classe Park,che ritorna un id univoco
                    // Il nodo Park sarò /parks/$i/
                    // creo l'oggetto park
                    pos_park=new Location("");
                    pos_park.setLatitude(Latsel);
                    pos_park.setLongitude(Longsel);
                    Park park = new Park(String.valueOf(Latsel), String.valueOf(Longsel),"0", currentUser.getUid());
                    tv_pos_park_lat.setText(String.valueOf(Latsel));
                    tv_pos_park_long.setText(String.valueOf(Longsel));
                    // faccio il 'push' di park nel nodo usando parkid
                    myRef.child(marker_id).setValue(park);
                    //Resetto le variabili globali relative al click del parcheggio (id, latitutide e longitude)
                    marker_id="";
                    Latsel=0;
                    Longsel=0;
                    alertView("Informazioni Memorizzate");
                } else      alertView("Nessuna posizione selezionata"); //Toast.makeText(getActivity(), "Nessuna posizione selezionata", Toast.LENGTH_LONG).show();
                break;
            case R.id.buttonLibera:
                if(mypark != null) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    DatabaseReference myRef = database.getReference().child("parks");
                    //Aggiorno il nodo relativo al parcheggio con lo stato "libero"
                    mypark.libera();
                    myRef.child(marker_id).setValue(mypark);
                    alertView("Posizione liberata");
                    tv_pos_park_lat.setText("");
                    tv_pos_park_long.setText("");
                    tv_dist.setText("");
                    //Resetto le variabili globali, (parcheggio memorizzato, idclick)
                    mypark=null;
                    marker_id="";
                    return;
                } else   alertView("Nessun parcheggio memorizzato");//  Toast.makeText(getActivity(), "Nessun parcheggio memorizzato", Toast.LENGTH_LONG).show();
                break;
            case R.id.button_logout:
                // Firebase Logout
                firebaseAuth.signOut();
                // Google Logout
                googleSignInClient.signOut().addOnCompleteListener(this,
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Intent myIntent = new Intent(MapsActivity2.this, Login.class);
                                MapsActivity2.this.startActivity(myIntent);
                            }
                        });
                break;
        }
    }
    //Visualizza a video Un messaggio
    private void alertView(String message){
        AlertDialog ad = new AlertDialog.Builder(this)
                .create();
        ad.setCancelable(false);
        ad.setTitle("");
        ad.setMessage(message);
        ad.setButton("OK", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        ad.show();
    }
    private void updateValue(Location location) {

        //AGGIORNAMENTO VALORI UI
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_long.setText(String.valueOf(location.getLongitude()));
        pos_att=new Location("");
        //AGGIORNAMENTO POSIZIONE ATTUALE PER CALCOLO DISTANZA
        pos_att.setLatitude(location.getLatitude());
        pos_att.setLongitude(location.getLongitude());

        //INVOCO IL GEOCODER PER RITROVARE L'INDIRIZZO DALLE COORDINATE
        Geocoder geocoder = new Geocoder(this);
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            tv_address.setText(addresses.get(0).getAddressLine(0));
        } catch (Exception e) {
            tv_address.setText("NOME STRADA NON AVVIABILE");
        }
    }



}