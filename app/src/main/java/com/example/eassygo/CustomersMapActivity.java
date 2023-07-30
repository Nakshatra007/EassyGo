package com.example.eassygo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.eassygo.databinding.ActivityCustomersMapBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class CustomersMapActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener{

    private GoogleMap mMap;
    GoogleApiClient googleApiClient;
    Location lastLocation;
    LocationRequest locationRequest;
    private ActivityCustomersMapBinding binding;
    Marker driverMarker;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private Button CustomerLogOutButton;
    private Button callCabCarButton;

    private String customerID;
    private LatLng customerPickUpLocation;

    private int radius = 1;
    private boolean driverFound = false, requestType = false;
    private String driverFoundID;

    private DatabaseReference CustomerDatabaseRef;
    private DatabaseReference driverAvailableRef;
    private DatabaseReference driversRef;
    private DatabaseReference driverLocationRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityCustomersMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        customerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        CustomerDatabaseRef = FirebaseDatabase.getInstance().getReference().child("Customer's request");
        driverAvailableRef = FirebaseDatabase.getInstance().getReference().child("Drivers Available");
        driverLocationRef = FirebaseDatabase.getInstance().getReference().child("Drivers Working");

        CustomerLogOutButton = findViewById(R.id.customer_logout_button);
        callCabCarButton = findViewById(R.id.customer_callcab_button);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        CustomerLogOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                LogOutCustomer();
            }
        });

      callCabCarButton.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View view) {



              GeoFire geoFire = new GeoFire(CustomerDatabaseRef);
              geoFire.setLocation(customerID, new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()));

              customerPickUpLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
              mMap.addMarker(new MarkerOptions().position(customerPickUpLocation).title("Pick Up Customer from here"));

            callCabCarButton.setText("Getting nearby drivers...");

            GetClosestCabDriver();

          }
      });

    }

    private void GetClosestCabDriver() {

        GeoFire geoFire = new GeoFire(driverAvailableRef);
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(customerPickUpLocation.latitude, customerPickUpLocation.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation geoLocation)
            {
                if(!driverFound)
                {
                    driverFound = true;
                    driverFoundID = key;

                    driversRef = FirebaseDatabase.getInstance().getReference().
                            child("Users").child("Drivers").child(driverFoundID);

                    HashMap driverMap = new HashMap();
                    driverMap.put("CustomerRideID", customerID);
                    driversRef.updateChildren(driverMap);

                    gettingDriverLocation();
                    callCabCarButton.setText("Fetching driver's location...");

                }

            }

            @Override
            public void onKeyExited(String s) {

            }

            @Override
            public void onKeyMoved(String s, GeoLocation geoLocation) {

            }

            @Override
            public void onGeoQueryReady()
            {
                if(!driverFound)
                {
                    radius++;
                    GetClosestCabDriver();
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError databaseError) {

            }
        });
    }

    private void gettingDriverLocation() {

        driverLocationRef.child(driverFoundID).child("l")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.exists())
                        {
                            List<Object> driverLocationMap = (List<Object>) dataSnapshot.getValue();
                            double locationLat = 0;
                            double locationLong = 0;
                            callCabCarButton.setText("Driver Found");

                            if(driverLocationMap.get(0) != null)
                            {
                                locationLat = Double.parseDouble(driverLocationMap.get(0).toString());
                            }

                            if(driverLocationMap.get(1) != null)
                            {
                                locationLong = Double.parseDouble(driverLocationMap.get(1).toString());
                            }

                            LatLng driverLatLng = new LatLng(locationLat, locationLong);
                            if(driverMarker != null)
                            {
                                driverMarker.remove();
                            }

                            Location location1= new Location("");
                            location1.setLatitude(customerPickUpLocation.latitude);
                            location1.setLongitude(customerPickUpLocation.longitude);

                            Location location2= new Location("");
                            location2.setLatitude(driverLatLng.latitude);
                            location2.setLongitude(driverLatLng.longitude);

                            float distance = location1.distanceTo(location2);

                            callCabCarButton.setText("Cab is "+distance+"m away.");
                            driverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Driver is here"));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        lastLocation = location;

        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(12));
    }

    protected synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    private void LogOutCustomer() {
        Intent welcomeIntent = new Intent(CustomersMapActivity.this, WelcomeActivity.class);
        welcomeIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(welcomeIntent);
        finish();
    }
}