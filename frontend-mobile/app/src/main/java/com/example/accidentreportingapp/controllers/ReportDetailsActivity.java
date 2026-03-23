package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accidentreportingapp.R;
import com.example.accidentreportingapp.models.AccidentReport;
import com.example.accidentreportingapp.models.VehicleSection;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * ReportDetailsActivity displays the full information of a specific accident report,
 * including details for both parties (Vehicle A and Vehicle B).
 */
public class ReportDetailsActivity extends BaseActivity {

    private ImageButton btnBack;
    private TextView textLocation, textTimestamp, textDescription, textStatus;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_report_details);

        initializeViews();
        
        AccidentReport report = (AccidentReport) getIntent().getSerializableExtra("REPORT_DATA");
        if (report != null) {
            displayReportDetails(report);
        }

        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        textLocation = findViewById(R.id.detail_location);
        textTimestamp = findViewById(R.id.detail_timestamp);
        textDescription = findViewById(R.id.detail_description);
        textStatus = findViewById(R.id.text_detail_status);
    }

    private void displayReportDetails(AccidentReport report) {
        textLocation.setText(report.getLocation());
        textDescription.setText(report.getDescription());
        textTimestamp.setText(dateFormat.format(new Date(report.getTimestamp())));
        
        // Set Status
        setStatusUI(textStatus, report.getStatus());

        // Display Vehicle A
        View layoutA = findViewById(R.id.layout_vehicle_a);
        bindVehicleData(layoutA, report.getVehicleA());

        // Display Vehicle B
        View layoutB = findViewById(R.id.layout_vehicle_b);
        bindVehicleData(layoutB, report.getVehicleB());
    }

    private void setStatusUI(TextView textView, String status) {
        Context context = textView.getContext();
        
        int color;
        String statusText;
        if (AccidentReport.STATUS_CONFIRMED.equals(status)) {
            color = ContextCompat.getColor(context, R.color.status_confirmed);
            statusText = getString(R.string.status_confirmed);
        } else if (AccidentReport.STATUS_ISSUE.equals(status)) {
            color = ContextCompat.getColor(context, R.color.status_issue);
            statusText = getString(R.string.status_issue);
        } else {
            color = ContextCompat.getColor(context, R.color.status_waiting);
            statusText = getString(R.string.status_waiting);
        }
        
        textView.setText(statusText);
        textView.setTextColor(color);
    }

    private void bindVehicleData(View root, VehicleSection vehicle) {
        TextView insuredName = root.findViewById(R.id.text_insured_name);
        TextView insuredAddress = root.findViewById(R.id.text_insured_address);
        TextView vehicleInfo = root.findViewById(R.id.text_vehicle_info);
        TextView insuranceInfo = root.findViewById(R.id.text_insurance_info);
        TextView driverName = root.findViewById(R.id.text_driver_name);
        TextView driverLicense = root.findViewById(R.id.text_driver_license);
        TextView circumstances = root.findViewById(R.id.text_circumstances);

        insuredName.setText(vehicle.insuredName);
        
        // Use formatted strings from resources for better localization
        String address = getString(R.string.format_address, 
                vehicle.insuredAddress, 
                vehicle.insuredPostalCode != null ? vehicle.insuredPostalCode : "", 
                vehicle.insuredCountry != null ? vehicle.insuredCountry : "");
        insuredAddress.setText(address);

        String vInfo = getString(R.string.format_vehicle_info, 
                vehicle.vehicleMakeType != null ? vehicle.vehicleMakeType : "---", 
                vehicle.vehicleRegistration != null ? vehicle.vehicleRegistration : "---");
        vehicleInfo.setText(vInfo);

        String iInfo = getString(R.string.format_insurance_info, 
                vehicle.insuranceName != null ? vehicle.insuranceName : "---", 
                vehicle.policyNumber != null ? vehicle.policyNumber : "---");
        insuranceInfo.setText(iInfo);

        String dInfo = getString(R.string.format_driver_info, 
                vehicle.driverName != null ? vehicle.driverName : "---", 
                vehicle.driverDob != null ? vehicle.driverDob : "---");
        driverName.setText(dInfo);

        String lInfo = getString(R.string.format_license_info, 
                getString(R.string.field_license_no), 
                vehicle.licenseNumber != null ? vehicle.licenseNumber : "---", 
                vehicle.licenseCategory != null ? vehicle.licenseCategory : "");
        driverLicense.setText(lInfo);

        StringBuilder circStr = new StringBuilder();
        if (vehicle.isParkedStopped) circStr.append("• ").append(getString(R.string.circ_parked)).append("\n");
        if (vehicle.isLeavingParking) circStr.append("• ").append(getString(R.string.circ_leaving)).append("\n");
        if (vehicle.isReversing) circStr.append("• ").append(getString(R.string.circ_reversing)).append("\n");
        
        circumstances.setText(circStr.length() > 0 ? circStr.toString().trim() : "---");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
