package com.example.parcheggiami.ui.fragmentmap;

import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.parcheggiami.Park;
import com.example.parcheggiami.R;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;
import java.util.Map;

public class FragmentMapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    private FragmentMapViewModel mViewModel;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    private static final int REQUEST_CODE = 101;
    private int REQUEST_LOCATION = 101;
    SearchView searchView;
    double Lat=0;
    double Long=0;
    double Latsel=0;
    double Longsel=0;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Park park;
    FirebaseAuth firebaseAuth;
    GoogleSignInClient googleSignInClient;
    private GoogleMap mMap;
    private String marker_id="";

    Park mypark;

    public static FragmentMapFragment newInstance() {
        return new FragmentMapFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_map_fragment, container, false);

        Button b = (Button) v.findViewById(R.id.buttonPark);
        b.setOnClickListener(this);

        Button b2 = (Button) v.findViewById(R.id.buttonLibera);
        b2.setOnClickListener(this);

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions( //Method of Fragment
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    101
            );
        }

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(FragmentMapViewModel.class);
        // TODO: Use the ViewModel

        googleSignInClient = GoogleSignIn.getClient( getActivity() , GoogleSignInOptions.DEFAULT_SIGN_IN);
        firebaseAuth = FirebaseAuth.getInstance();


        final TextView textView = (TextView) getView().findViewById(R.id.textView);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
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

        SupportMapFragment mapFragment = ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map));

        mapFragment.getMapAsync(this);

    }



    private void fetchLocation() {

        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {

                    currentLocation = location;
                    Toast.makeText(getActivity().getApplicationContext(), currentLocation.getLatitude() + "" + currentLocation.getLongitude(), Toast.LENGTH_SHORT).show();

                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    Lat=currentLocation.getLatitude();
                    Long=currentLocation.getLongitude();


                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Sei qui!");
                   // mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12.0f));
                    Marker marker= mMap.addMarker(markerOptions);


              }
            }
        });
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

         mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
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

    @Override
    public void onRequestPermissionsResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
            // Verifico se l'utente ha accettato i permessi richiesti
            if (grantResults.length > 0  && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
              // Rilevo la posizione attuale
               fetchLocation();
            }
    }

    //Leggo da firebase tutti i parcheggi memorizzati
    private void retrieve_marker(DataSnapshot dataSnapshot) {
         int count_liberi=0;
      //   for (DataSnapshot postSnapshot: dataSnapshot.getChildren()) {
            mypark=null;
            if(mMap != null) {
                mMap.clear();
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {

                    Double vLat = Double.parseDouble(String.valueOf(childDataSnapshot.child("latitude").getValue()));
                    Double vLong = Double.parseDouble(String.valueOf(childDataSnapshot.child("longitude").getValue()));
                    LatLng latLng = new LatLng(vLat, vLong);
                    if (childDataSnapshot.child("libero").getValue().toString().equals("1")) {
                        MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Posizione Libera").snippet("Clicca per impostare la posizione").icon(BitmapDescriptorFactory.fromResource(R.drawable.location));
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                        Marker marker=mMap.addMarker(markerOptions);
                        marker.setTag(childDataSnapshot.getKey());
                        count_liberi+=1
                        ;
                    }else if(childDataSnapshot.child("user").getValue().toString().equals(firebaseAuth.getCurrentUser().getUid().toString())){
                        mypark = childDataSnapshot.getValue(Park.class);
                        marker_id=childDataSnapshot.getKey();
                    }

               //     TextView textPosti = getActivity().findViewById(R.id.textPosti); //(TextView) getActivity().findViewById(R.id.textPosti);
                 //   textPosti.setText(String.valueOf(count_liberi));


             //**       calculate_distance(vLat,vLong);
                }
                if(currentLocation!=null){
                    LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

                    Lat = currentLocation.getLatitude();
                    Long = currentLocation.getLongitude();

                    MarkerOptions markerOptions = new MarkerOptions().position(latLng).title("Sei qui!");
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    mMap.addMarker(markerOptions);
                }

            }
        //}


    }

    //Calcolo la distanza tra due coordinate
    private void calculate_distance(Double vLat,Double vLong){
        if(currentLocation!=null){
            float[] results = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    vLat, vLong, results);

            final TextView textDistance = (TextView) getActivity().findViewById(R.id.textPosti);
          //  textDistance.setText(String.valueOf(results[0]));
            if (results[0] > 2000) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                final DatabaseReference myRef = database.getReference().child("parks");
                String uid = currentUser.getUid();
                //Aggiorno il nodo relativo al parcheggio con lo stato "libero"
                Park park = new Park(String.valueOf(vLat), String.valueOf(vLong), "1",uid);
               // myRef.child(uid).setValue(park);
            }
        }

    }
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
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.buttonPark:
                if(mypark != null) {
                    Toast.makeText(getActivity(), "Posizione parcheggio già memorizzata", Toast.LENGTH_LONG).show();
                    return;
                }
                if(marker_id!=""){
                    Toast.makeText(getActivity(), "Informazioni Memorizzate", Toast.LENGTH_LONG).show();


                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    final DatabaseReference myRef = database.getReference().child("parks");
                    //***    String uid = currentUser.getUid();
                    // Creo un nuovo nodo per la classe Park,che ritorna un id univoco
                    // Il nodo Park sarò /parks/$parkij/
                    // creo l'oggetto park
                    Park park = new Park(String.valueOf(Latsel), String.valueOf(Longsel),"0", currentUser.getUid());

                    // faccio il 'push' di park nel nodo usando parkid
                    myRef.child(marker_id).setValue(park);
                    marker_id="";
                    Latsel=0;
                    Longsel=0;
                } else  Toast.makeText(getActivity(), "Nessuna posizione selezionata", Toast.LENGTH_LONG).show();
                break;
            case R.id.buttonLibera:
                if(mypark != null) {
                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                    FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                    final DatabaseReference myRef = database.getReference().child("parks");
                    //Aggiorno il nodo relativo al parcheggio con lo stato "libero"
                    mypark.libera();
                    myRef.child(marker_id).setValue(mypark);
                    Toast.makeText(getActivity(), "Posizione liberata", Toast.LENGTH_LONG).show();
                    mypark=null;
                    marker_id="";
                    return;
                } else    Toast.makeText(getActivity(), "Nessun parcheggio memorizzato", Toast.LENGTH_LONG).show();
                break;
        }


    }





}
