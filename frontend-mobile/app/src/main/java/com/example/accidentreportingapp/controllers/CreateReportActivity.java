package com.example.accidentreportingapp.controllers;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accidentreportingapp.R;
import com.example.accidentreportingapp.models.AccidentReport;
import com.example.accidentreportingapp.models.VehicleSection;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

/**
 * CreateReportActivity implements a multi-step wizard for reporting a new accident.
 * This approach breaks down the complex form into bite-sized steps to prevent user fatigue.
 */
public class CreateReportActivity extends BaseActivity {

    private static final int STEP_GENERAL = 0;
    private static final int STEP_VEHICLE_A_INFO = 1;
    private static final int STEP_VEHICLE_A_INSURANCE = 2;
    private static final int STEP_VEHICLE_A_DRIVER = 3;
    private static final int STEP_VEHICLE_A_CIRCUMSTANCES = 4;
    private static final int STEP_VEHICLE_B_INFO = 5;
    private static final int STEP_VEHICLE_B_INSURANCE = 6;
    private static final int STEP_VEHICLE_B_DRIVER = 7;
    private static final int STEP_VEHICLE_B_CIRCUMSTANCES = 8;
    private static final int STEP_SUMMARY = 9;
    private static final int TOTAL_STEPS = 10;

    private int currentStep = STEP_GENERAL;
    private AccidentReport draftReport;

    private ImageButton btnBack;
    private TextView textTitle;
    private LinearProgressIndicator progressIndicator;
    private ViewGroup stepContainer;
    private MaterialButton btnPrevious, btnNext;

    // View caches for the steps
    private View viewGeneral, viewVehicleInfo, viewInsurance, viewDriver, viewCircumstances, viewSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_report);

        draftReport = new AccidentReport();
        initializeViews();
        updateStepUI();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        textTitle = findViewById(R.id.text_title);
        progressIndicator = findViewById(R.id.progress_indicator);
        stepContainer = findViewById(R.id.step_container);
        btnPrevious = findViewById(R.id.btn_previous);
        btnNext = findViewById(R.id.btn_next);

        // Pre-inflate step views for smooth transitions
        viewGeneral = getLayoutInflater().inflate(R.layout.step_general_info, stepContainer, false);
        viewVehicleInfo = getLayoutInflater().inflate(R.layout.step_vehicle_info, stepContainer, false);
        viewInsurance = getLayoutInflater().inflate(R.layout.step_insurance_info, stepContainer, false);
        viewDriver = getLayoutInflater().inflate(R.layout.step_driver_info, stepContainer, false);
        viewCircumstances = getLayoutInflater().inflate(R.layout.step_circumstances, stepContainer, false);
        viewSummary = getLayoutInflater().inflate(R.layout.step_summary, stepContainer, false);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> handleBack());
        btnPrevious.setOnClickListener(v -> goToPreviousStep());
        btnNext.setOnClickListener(v -> goToNextStep());
    }

    private void updateStepUI() {
        stepContainer.removeAllViews();
        
        // Update Progress (1-based for the bar)
        int progress = (int) (((float) (currentStep + 1) / TOTAL_STEPS) * 100);
        progressIndicator.setProgress(progress, true);

        // Update Navigation Buttons
        btnPrevious.setVisibility(currentStep == STEP_GENERAL ? View.GONE : View.VISIBLE);
        btnNext.setText(currentStep == TOTAL_STEPS - 1 ? R.string.btn_finish : R.string.btn_next);

        // Set Title (Step indicator)
        textTitle.setText(getString(R.string.step_indicator, currentStep + 1, TOTAL_STEPS));

        // Inject current step layout and load data
        switch (currentStep) {
            case STEP_GENERAL:
                textTitle.setText(R.string.title_create_report);
                stepContainer.addView(viewGeneral);
                loadGeneralData();
                break;
            case STEP_VEHICLE_A_INFO:
                setupStepTitle(viewVehicleInfo, R.string.header_vehicle_a);
                stepContainer.addView(viewVehicleInfo);
                loadVehicleInfo(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_A_INSURANCE:
                setupStepTitle(viewInsurance, R.string.header_vehicle_a);
                stepContainer.addView(viewInsurance);
                loadInsuranceInfo(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_A_DRIVER:
                setupStepTitle(viewDriver, R.string.header_vehicle_a);
                stepContainer.addView(viewDriver);
                loadDriverInfo(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_A_CIRCUMSTANCES:
                setupStepTitle(viewCircumstances, R.string.header_vehicle_a);
                stepContainer.addView(viewCircumstances);
                loadCircumstances(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_B_INFO:
                setupStepTitle(viewVehicleInfo, R.string.header_vehicle_b);
                stepContainer.addView(viewVehicleInfo);
                loadVehicleInfo(draftReport.getVehicleB());
                break;
            case STEP_VEHICLE_B_INSURANCE:
                setupStepTitle(viewInsurance, R.string.header_vehicle_b);
                stepContainer.addView(viewInsurance);
                loadInsuranceInfo(draftReport.getVehicleB());
                break;
            case STEP_VEHICLE_B_DRIVER:
                setupStepTitle(viewDriver, R.string.header_vehicle_b);
                stepContainer.addView(viewDriver);
                loadDriverInfo(draftReport.getVehicleB());
                break;
            case STEP_VEHICLE_B_CIRCUMSTANCES:
                setupStepTitle(viewCircumstances, R.string.header_vehicle_b);
                stepContainer.addView(viewCircumstances);
                loadCircumstances(draftReport.getVehicleB());
                break;
            case STEP_SUMMARY:
                textTitle.setText(R.string.title_summary);
                stepContainer.addView(viewSummary);
                loadSummary();
                break;
        }
    }

    private void setupStepTitle(View view, int titleResId) {
        TextView tv = view.findViewById(R.id.text_step_title);
        if (tv != null) tv.setText(titleResId);
    }

    private void goToNextStep() {
        saveCurrentStepData();
        if (currentStep < TOTAL_STEPS - 1) {
            currentStep++;
            updateStepUI();
        } else {
            finishWizard();
        }
    }

    private void goToPreviousStep() {
        saveCurrentStepData();
        if (currentStep > 0) {
            currentStep--;
            updateStepUI();
        }
    }

    private void saveCurrentStepData() {
        switch (currentStep) {
            case STEP_GENERAL:
                TextInputEditText editLoc = viewGeneral.findViewById(R.id.edit_location);
                TextInputEditText editDesc = viewGeneral.findViewById(R.id.edit_description);
                draftReport.setLocation(editLoc.getText().toString());
                draftReport.setDescription(editDesc.getText().toString());
                break;
            case STEP_VEHICLE_A_INFO:
                saveVehicleInfo(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_A_INSURANCE:
                saveInsuranceInfo(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_A_DRIVER:
                saveDriverInfo(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_A_CIRCUMSTANCES:
                saveCircumstances(draftReport.getVehicleA());
                break;
            case STEP_VEHICLE_B_INFO:
                saveVehicleInfo(draftReport.getVehicleB());
                break;
            case STEP_VEHICLE_B_INSURANCE:
                saveInsuranceInfo(draftReport.getVehicleB());
                break;
            case STEP_VEHICLE_B_DRIVER:
                saveDriverInfo(draftReport.getVehicleB());
                break;
            case STEP_VEHICLE_B_CIRCUMSTANCES:
                saveCircumstances(draftReport.getVehicleB());
                break;
        }
    }

    // Data Loading Helpers

    private void loadGeneralData() {
        ((TextInputEditText) viewGeneral.findViewById(R.id.edit_location)).setText(draftReport.getLocation());
        ((TextInputEditText) viewGeneral.findViewById(R.id.edit_description)).setText(draftReport.getDescription());
    }

    private void loadVehicleInfo(VehicleSection vehicle) {
        ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_insured_name)).setText(vehicle.insuredName);
        ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_insured_address)).setText(vehicle.insuredAddress);
        ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_vehicle_make)).setText(vehicle.vehicleMakeType);
        ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_vehicle_plate)).setText(vehicle.vehicleRegistration);
    }

    private void loadInsuranceInfo(VehicleSection vehicle) {
        ((TextInputEditText) viewInsurance.findViewById(R.id.edit_insurance_name)).setText(vehicle.insuranceName);
        ((TextInputEditText) viewInsurance.findViewById(R.id.edit_policy_number)).setText(vehicle.policyNumber);
        ((MaterialCheckBox) viewInsurance.findViewById(R.id.check_covers_damage)).setChecked(vehicle.coversDamage);
    }

    private void loadDriverInfo(VehicleSection vehicle) {
        ((TextInputEditText) viewDriver.findViewById(R.id.edit_driver_name)).setText(vehicle.driverName);
        ((TextInputEditText) viewDriver.findViewById(R.id.edit_driver_dob)).setText(vehicle.driverDob);
        ((TextInputEditText) viewDriver.findViewById(R.id.edit_driver_license)).setText(vehicle.licenseNumber);
    }

    private void loadCircumstances(VehicleSection vehicle) {
        ((MaterialCheckBox) viewCircumstances.findViewById(R.id.check_parked)).setChecked(vehicle.isParkedStopped);
        ((MaterialCheckBox) viewCircumstances.findViewById(R.id.check_leaving)).setChecked(vehicle.isLeavingParking);
        ((MaterialCheckBox) viewCircumstances.findViewById(R.id.check_reversing)).setChecked(vehicle.isReversing);
    }

    private void loadSummary() {
        TextView tvSummary = viewSummary.findViewById(R.id.summary_text);
        StringBuilder sb = new StringBuilder();
        
        sb.append("General Information:\n");
        sb.append("Location: ").append(draftReport.getLocation()).append("\n");
        sb.append("Description: ").append(draftReport.getDescription()).append("\n\n");

        appendVehicleSummary(sb, "Vehicle A", draftReport.getVehicleA());
        sb.append("\n");
        appendVehicleSummary(sb, "Vehicle B", draftReport.getVehicleB());

        tvSummary.setText(sb.toString());
    }

    private void appendVehicleSummary(StringBuilder sb, String label, VehicleSection vehicle) {
        sb.append(label).append(":\n");
        sb.append("- Plate: ").append(vehicle.vehicleRegistration).append("\n");
        sb.append("- Driver: ").append(vehicle.driverName).append("\n");
        sb.append("- Insurance: ").append(vehicle.insuranceName).append("\n");
        
        sb.append("- Circumstances: ");
        boolean any = false;
        if (vehicle.isParkedStopped) { sb.append("Parked"); any = true; }
        if (vehicle.isLeavingParking) { sb.append(any ? ", " : "").append("Leaving parking"); any = true; }
        if (vehicle.isReversing) { sb.append(any ? ", " : "").append("Reversing"); any = true; }
        if (!any) sb.append("None selected");
        sb.append("\n");
    }

    // Data Saving Helpers

    private void saveVehicleInfo(VehicleSection vehicle) {
        vehicle.insuredName = ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_insured_name)).getText().toString();
        vehicle.insuredAddress = ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_insured_address)).getText().toString();
        vehicle.vehicleMakeType = ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_vehicle_make)).getText().toString();
        vehicle.vehicleRegistration = ((TextInputEditText) viewVehicleInfo.findViewById(R.id.edit_vehicle_plate)).getText().toString();
    }

    private void saveInsuranceInfo(VehicleSection vehicle) {
        vehicle.insuranceName = ((TextInputEditText) viewInsurance.findViewById(R.id.edit_insurance_name)).getText().toString();
        vehicle.policyNumber = ((TextInputEditText) viewInsurance.findViewById(R.id.edit_policy_number)).getText().toString();
        vehicle.coversDamage = ((MaterialCheckBox) viewInsurance.findViewById(R.id.check_covers_damage)).isChecked();
    }

    private void saveDriverInfo(VehicleSection vehicle) {
        vehicle.driverName = ((TextInputEditText) viewDriver.findViewById(R.id.edit_driver_name)).getText().toString();
        vehicle.driverDob = ((TextInputEditText) viewDriver.findViewById(R.id.edit_driver_dob)).getText().toString();
        vehicle.licenseNumber = ((TextInputEditText) viewDriver.findViewById(R.id.edit_driver_license)).getText().toString();
    }

    private void saveCircumstances(VehicleSection vehicle) {
        vehicle.isParkedStopped = ((MaterialCheckBox) viewCircumstances.findViewById(R.id.check_parked)).isChecked();
        vehicle.isLeavingParking = ((MaterialCheckBox) viewCircumstances.findViewById(R.id.check_leaving)).isChecked();
        vehicle.isReversing = ((MaterialCheckBox) viewCircumstances.findViewById(R.id.check_reversing)).isChecked();
    }

    private void handleBack() {
        if (currentStep > 0) {
            goToPreviousStep();
        } else {
            finish();
        }
    }

    private void finishWizard() {
        showToast("Accident Report Created Successfully (Demo)");
        finish();
    }
}
