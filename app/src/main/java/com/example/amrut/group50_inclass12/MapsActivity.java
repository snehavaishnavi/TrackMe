package com.example.amrut.group50_inclass12;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager mLocationMngr;
    LocationListener mLocListener;
    ArrayList<LatLng> latLngList;
    boolean isTracking;
    PolylineOptions polylineOptions;
    private int flag=0;
Marker marker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mLocationMngr = (LocationManager) getSystemService(LOCATION_SERVICE);

        polylineOptions = new PolylineOptions();
        polylineOptions.width(7).color(Color.BLUE).geodesic(true);

    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    public void startTracking(){
        isTracking=true;
        if (!mLocationMngr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Log.d("demo", "gps not enabled");
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
            builder.setTitle("GPS not enabled")
                    .setMessage("Would u like to enable it?")
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                    finish();
                }
            });

            builder.create().show();

        } else {
            Log.d("demo","else part");
            latLngList=new ArrayList<>();
            mLocListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d("demo", location.getLatitude() + " , " + location.getLongitude());
                    LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                    latLngList.add(latLng);
                    Log.d("demo",flag+"");

                    if (flag==0){
                        drawMarker(location);
                    }
                    drawPath();

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {

                }
            };
            try {
                mLocationMngr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, mLocListener);
            } catch (SecurityException e) {
                Log.d("demo", "eoor"); // lets the user know there is a problem with the gps
            }

        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                if (isTracking){
                    Toast.makeText(getApplicationContext(),"Stopped Location Tracking",Toast.LENGTH_SHORT).show();
                    try {
                        LatLngBounds latLngBounds = new LatLngBounds(latLngList.get(0),latLngList.get(latLngList.size()-1));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds,11));
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),"Choose other tracking path",Toast.LENGTH_SHORT).show();
                    }

                    latLngList.clear();
                }else {
                    startTracking();
                    Toast.makeText(getApplicationContext(),"Started Location Tracking",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

private void drawMarker(Location location){
    mMap.clear();
    flag=1;
    LatLng currentPosition = new LatLng(location.getLatitude(),location.getLongitude());
    mMap.addMarker(new MarkerOptions()
            .position(currentPosition)
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
            .title("START"));
    float zoomlevel=16.0f;
    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition,zoomlevel));
}

    private void drawPath() {
        if (mMap != null) {
            polylineOptions = new PolylineOptions();
            Log.d("demo","latlonlist"+latLngList.toString());
            for(int i=0;i<latLngList.size();i++) {
                polylineOptions.add(latLngList.get(i));
                if (i==latLngList.size()-1){
                    if (marker!=null){
                        marker.remove();
                    }
                    marker=mMap.addMarker(new MarkerOptions()
                            .position(latLngList.get(i))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                            .title("END"));
                }
            }
            float zoomlevel=16.0f;
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngList.get(latLngList.size()-1), zoomlevel));
            polylineOptions.width(10).color(Color.BLUE).geodesic(true);
            mMap.addPolyline(polylineOptions);

        }
    }
}
