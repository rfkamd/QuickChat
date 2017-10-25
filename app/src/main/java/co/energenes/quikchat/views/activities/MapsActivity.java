package co.energenes.quikchat.views.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import co.energenes.quikchat.R;
import co.energenes.quikchat.Utilities.Utils;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LatLng location;
    private AppCompatButton btnSelectLocation;
    private boolean permissionGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            askPermission();
            return;
        } else {
            permissionGranted = true;
        }

        btnSelectLocation = (AppCompatButton) findViewById(R.id.btnSelectLocation);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void askPermission(){
        if (Utils.shouldAskPermission()) {
            // Here, thisActivity is the current activity
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    permissionGranted = true;
                }
                return;
            }
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
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (permissionGranted) {
            mMap = googleMap;
            mMap.setMyLocationEnabled(true);
            mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    location = latLng;
                    mMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            });

            btnSelectLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String _Location = "";
                    //check if users has not clicked on map but got current location
                    if (location == null) {
                        if (mMap.getMyLocation() != null) {
                            location = new LatLng(mMap.getMyLocation().getLatitude(), mMap.getMyLocation().getLongitude());
                        }
                    }
                    //get location address or name
                    Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        List<Address> listAddresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
                        if (null != listAddresses && listAddresses.size() > 0) {
                            _Location = listAddresses.get(0).getAddressLine(0);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //set result and finish()
                    Intent intent = new Intent();
                    intent.putExtra("latitude", location.latitude);
                    intent.putExtra("longitude", location.longitude);
                    intent.putExtra("location", _Location);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }

    }
}
