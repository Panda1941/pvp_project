package com.example.accidentreportingapp.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.accidentreportingapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private Marker selectedMarker;
    private MaterialButton btnConfirm;
    private LatLng selectedLatLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        mapView = findViewById(R.id.map_view);
        btnConfirm = findViewById(R.id.btn_confirm_location);

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        btnConfirm.setOnClickListener(v -> {
            if (selectedLatLng != null) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("lat", selectedLatLng.latitude);
                resultIntent.putExtra("lng", selectedLatLng.longitude);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        
        // Move the zoom controls and Google logo up so they aren't hidden by the bottom button/system bar
        googleMap.setPadding(0, 0, 0, 250);

        double defaultLat = getIntent().getDoubleExtra("default_lat", 54.6872); // Vilnius
        double defaultLng = getIntent().getDoubleExtra("default_lng", 25.2797);
        LatLng startPos = new LatLng(defaultLat, defaultLng);
        
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPos, 15f));

        googleMap.setOnMapClickListener(latLng -> {
            if (selectedMarker != null) {
                selectedMarker.remove();
            }
            selectedLatLng = latLng;
            selectedMarker = googleMap.addMarker(new MarkerOptions().position(latLng));
            btnConfirm.setVisibility(View.VISIBLE);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}