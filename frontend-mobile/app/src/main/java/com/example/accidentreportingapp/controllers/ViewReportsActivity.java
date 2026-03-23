package com.example.accidentreportingapp.controllers;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accidentreportingapp.R;
import com.example.accidentreportingapp.models.AccidentReport;
import com.example.accidentreportingapp.models.VehicleSection;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewReportsActivity displays a list of submitted and draft accident reports.
 * Populates the list with detailed example data for both parties.
 */
public class ViewReportsActivity extends BaseActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private ReportsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_reports);

        initializeViews();
        setupRecyclerView();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        recyclerView = findViewById(R.id.recycler_view_reports);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        List<AccidentReport> mockReports = new ArrayList<>();

        // Report 1: WAITING (Amber) - Kaunas
        AccidentReport r1 = new AccidentReport(getString(R.string.mock_loc_kaunas), "ABC-123");
        r1.setStatus(AccidentReport.STATUS_WAITING);
        r1.setDescription(getString(R.string.mock_desc_1));
        
        VehicleSection v1A = r1.getVehicleA();
        v1A.insuredName = "Jonas Jonaitis";
        v1A.insuredAddress = "Laisvės al. 5";
        v1A.insuredPostalCode = "LT-44239";
        v1A.insuredCountry = getString(R.string.mock_country_lithuania);
        v1A.vehicleMakeType = "Volvo FH16";
        v1A.insuranceName = "Lietuvos Draudimas";
        v1A.policyNumber = "POL-998877";
        v1A.driverName = "Jonas Jonaitis";
        v1A.driverDob = "1985-05-20";
        v1A.licenseNumber = "123456789";
        v1A.licenseCategory = "C, CE";
        v1A.isLeavingParking = true;

        VehicleSection v1B = r1.getVehicleB();
        v1B.insuredName = "Petras Petraitis";
        v1B.insuredAddress = "Taikos pr. 88";
        v1B.insuredPostalCode = "LT-51223";
        v1B.insuredCountry = getString(R.string.mock_country_lithuania);
        v1B.vehicleMakeType = "Volkswagen Passat";
        v1B.vehicleRegistration = "XYZ-789";
        v1B.insuranceName = "Ergo";
        v1B.policyNumber = "ERG-112233";
        v1B.driverName = "Antanas Antanaitis";
        v1B.driverDob = "1992-11-05";
        v1B.licenseNumber = "987654321";
        v1B.licenseCategory = "B";
        v1B.isParkedStopped = true;

        mockReports.add(r1);

        // Report 2: CONFIRMED (Green) - Vilnius
        AccidentReport r2 = new AccidentReport(getString(R.string.mock_loc_vilnius), "REG-555");
        r2.setStatus(AccidentReport.STATUS_CONFIRMED);
        r2.setDescription(getString(R.string.mock_desc_2));
        
        VehicleSection v2A = r2.getVehicleA();
        v2A.insuredName = "Mantas Mantaitis";
        v2A.insuredAddress = "Sodų g. 12";
        v2A.insuredCountry = getString(R.string.mock_country_lithuania);
        v2A.vehicleMakeType = "Scania R450";
        v2A.insuranceName = "BTA";
        v2A.policyNumber = "BTA-554433";
        v2A.driverName = "Mantas Mantaitis";
        v2A.licenseNumber = "L00112233";
        v2A.licenseCategory = "C, CE";
        v2A.isReversing = true;

        VehicleSection v2B = r2.getVehicleB();
        v2B.insuredName = "Karolis Karolaitis";
        v2B.vehicleRegistration = "GHT-001";
        v2B.vehicleMakeType = "Toyota Corolla";
        v2B.insuranceName = "Gjensidige";
        v2B.driverName = "Karolis Karolaitis";
        v2B.isParkedStopped = true;
        
        mockReports.add(r2);

        // Report 3: ISSUE (Red) - Klaipėda
        AccidentReport r3 = new AccidentReport(getString(R.string.mock_loc_klaipeda), "LTU-555");
        r3.setStatus(AccidentReport.STATUS_ISSUE);
        r3.setDescription(getString(R.string.mock_desc_3));
        
        VehicleSection v3A = r3.getVehicleA();
        v3A.insuredName = "UAB TransLogistics";
        v3A.insuredAddress = "Mainų g. 4";
        v3A.vehicleMakeType = "Mercedes-Benz Actros";
        v3A.insuranceName = "If P&C Insurance";
        v3A.driverName = "Tomas Tomaitis";
        v3A.licenseCategory = "C, CE";
        v3A.isParkedStopped = true;

        VehicleSection v3B = r3.getVehicleB();
        v3B.vehicleRegistration = "GHE-666";
        v3B.vehicleMakeType = "BMW 520d";
        v3B.driverName = "Nežinomas Vairuotojas";
        v3B.insuranceName = getString(R.string.mock_insurance_missing);
        v3B.isLeavingParking = true;

        mockReports.add(r3);

        adapter = new ReportsAdapter(mockReports);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }
}
