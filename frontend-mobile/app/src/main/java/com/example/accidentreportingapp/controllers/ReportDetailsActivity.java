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

import java.text.DateFormat;
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
    // Shared date formatter for showing report timestamps. Kept static to
    // avoid allocating a new formatter per Activity instance. For more robust
    // timezone/locale handling consider using `java.time` APIs on newer SDKs.
    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    // Small null-safe helpers to shorten view-binding code and make intent clearer
    private String nn(String s) {
        return s != null ? s : "---";
    }

    private String nnEmpty(String s) {
        return s != null ? s : "";
    }

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
        btnBack = v(R.id.btn_back);
        textLocation = v(R.id.detail_location);
        textTimestamp = v(R.id.detail_timestamp);
        textDescription = v(R.id.detail_description);
        textStatus = v(R.id.text_detail_status);
    }

    private void displayReportDetails(AccidentReport report) {
        textLocation.setText(report.getLocation());
        textDescription.setText(report.getDescription());
        textTimestamp.setText(DATE_FORMAT.format(new Date(report.getTimestamp())));
        
        // Set Status
        setStatusUI(textStatus, report);

        // Display Vehicle A
        View layoutA = v(R.id.layout_vehicle_a);
        bindVehicleData(layoutA, report.getVehicleA());

        // Display Vehicle B
        View layoutB = v(R.id.layout_vehicle_b);
        bindVehicleData(layoutB, report.getVehicleB());
    }

    private void setStatusUI(TextView textView, AccidentReport report) {
        Context context = textView.getContext();
        int status = report.getStatus();
        
        int color;
        if (status == AccidentReport.STATUS_CONFIRMED) {
            color = ContextCompat.getColor(context, R.color.status_confirmed);
        } else if (status == AccidentReport.STATUS_ISSUE) {
            color = ContextCompat.getColor(context, R.color.status_issue);
        } else {
            color = ContextCompat.getColor(context, R.color.status_waiting);
        }
        
        textView.setText(getString(report.getStatusResourceId()));
        textView.setTextColor(color);
    }

    private void bindVehicleData(View root, VehicleSection vehicle) {
        TextView insuredName = v(root, R.id.text_insured_name);
        TextView insuredAddress = v(root, R.id.text_insured_address);
        TextView vehicleInfo = v(root, R.id.text_vehicle_info);
        TextView insuranceInfo = v(root, R.id.text_insurance_info);
        TextView driverName = v(root, R.id.text_driver_name);
        TextView driverLicense = v(root, R.id.text_driver_license);
        TextView circumstances = v(root, R.id.text_circumstances);

        insuredName.setText(nn(vehicle.insuredName));

        // Use formatted strings from resources for better localization
        String address = getString(R.string.format_address,
            nnEmpty(vehicle.insuredAddress),
            nnEmpty(vehicle.insuredPostalCode),
            nnEmpty(vehicle.insuredCountry));
        insuredAddress.setText(address.isEmpty() ? "---" : address);

        String vInfo = getString(R.string.format_vehicle_info,
            nn(vehicle.vehicleMakeType),
            nn(vehicle.vehicleRegistration));
        vehicleInfo.setText(vInfo);

        String iInfo = getString(R.string.format_insurance_info,
            nn(vehicle.insuranceName),
            nn(vehicle.policyNumber));
        insuranceInfo.setText(iInfo);

        String dInfo = getString(R.string.format_driver_info,
            nn(vehicle.driverName),
            nn(vehicle.driverDob));
        driverName.setText(dInfo);

        String lInfo = getString(R.string.format_license_info,
            getString(R.string.field_license_no),
            nn(vehicle.licenseNumber),
            nnEmpty(vehicle.licenseCategory));
        driverLicense.setText(lInfo);

        StringBuilder circStr = new StringBuilder();
        if (vehicle.isParkedStopped) circStr.append("• ").append(getString(R.string.circ_parked_short)).append("\n");
        if (vehicle.isLeavingParking) circStr.append("• ").append(getString(R.string.circ_leaving_parking_short)).append("\n");
        if (vehicle.isReversing) circStr.append("• ").append(getString(R.string.circ_reversing_short)).append("\n");

        circumstances.setText(circStr.length() > 0 ? circStr.toString().trim() : "---");
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
