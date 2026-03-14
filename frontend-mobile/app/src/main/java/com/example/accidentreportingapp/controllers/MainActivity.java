package com.example.accidentreportingapp.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accidentreportingapp.R;
import com.google.android.material.button.MaterialButton;

/**
 * MainActivity serves as the primary dashboard for the ARA app.
 * It follows a classic MVC architecture where this Activity acts as the Controller,
 * managing the interactions between the UI (View) and the app's data/logic (Model).
 * Extends BaseActivity for shared localization and utility logic.
 */
public class MainActivity extends BaseActivity {

    // UI Elements
    private ImageButton settingsButton;
    private MaterialButton btnReportAccident;
    private MaterialButton btnViewReports;
    private MaterialButton btnVehicleInfo;
    private MaterialButton btnEmergencyContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Enable edge-to-edge display for a modern look
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        initializeViews();

        // Setup click listeners for navigation and actions
        setupClickListeners();

        // Handle system bar insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Initializes all the View components from the XML layout.
     */
    private void initializeViews() {
        settingsButton = findViewById(R.id.settings_button);
        btnReportAccident = findViewById(R.id.btn_report_accident);
        btnViewReports = findViewById(R.id.btn_view_reports);
        btnVehicleInfo = findViewById(R.id.btn_vehicle_info);
        btnEmergencyContacts = findViewById(R.id.btn_emergency_contacts);
    }

    /**
     * Sets up onClick listeners for all interactive elements.
     */
    private void setupClickListeners() {
        // Settings Button - Navigates to SettingsActivity
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        // Main Menu - Report New Accident
        btnReportAccident.setOnClickListener(v -> 
            showToast(getString(R.string.toast_report))
        );

        // Main Menu - View My Reports
        btnViewReports.setOnClickListener(v -> 
            showToast(getString(R.string.toast_view_reports))
        );

        // Main Menu - Vehicle Info
        btnVehicleInfo.setOnClickListener(v -> 
            showToast(getString(R.string.toast_vehicle))
        );

        // Main Menu - Emergency Contacts
        btnEmergencyContacts.setOnClickListener(v -> 
            showToast(getString(R.string.toast_emergency))
        );
    }
}
