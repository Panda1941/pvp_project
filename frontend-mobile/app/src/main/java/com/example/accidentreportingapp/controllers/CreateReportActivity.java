package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;

import com.example.accidentreportingapp.R;
import com.example.accidentreportingapp.models.AccidentReport;
import com.example.accidentreportingapp.models.Damage;
import com.example.accidentreportingapp.models.VehicleSection;
import com.example.accidentreportingapp.models.Witness;
import com.example.accidentreportingapp.views.DamageDiagramView;
import com.example.accidentreportingapp.views.SignatureView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

import java.io.IOException;

import network.api.ReportApi;
import network.client.ApiClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * CreateReportActivity implements a multi-step wizard for reporting a new accident.
 * This approach breaks down the complex form into bite-sized steps to prevent user fatigue.
 */
public class CreateReportActivity extends BaseActivity {

    private static final int STEP_PLATES = 0;
    private static final int STEP_TIME_LOCATION = 1;
    private static final int STEP_WITNESSES = 2;
    private static final int STEP_PHOTOS = 3;
    private static final int STEP_DAMAGE = 4;
    private static final int STEP_CIRCUMSTANCES = 5;
    private static final int STEP_VEHICLE_INFO = 6;
    private static final int STEP_FAULT_SIGNATURE = 7;
    private static final int STEP_SUMMARY = 8;
    private static final int TOTAL_STEPS = 9;

    private int currentStep = STEP_PLATES;
    private int activeVehicleTab = 0; // 0 = A, 1 = B
    private int activeVehicleInfoTab = 0;
    private int activeDamageTab = 0;
    private int activeCircumstancesTab = 0;
    private int activeSignatureTab = 0;
    private AccidentReport draftReport;
    private List<Witness> witnesses = new ArrayList<>();

    private ImageButton btnBack;
    ImageButton btnPrevOverlay;
    ImageButton btnNextOverlay;
    private TextView textTitle;
    private LinearProgressIndicator progressIndicator;
    private ViewGroup stepContainer;
    private MaterialButton btnPrevious, btnNext;
    private MaterialButton btnAddPhoto;
    private MaterialButton btnDownloadReport;
    private RecyclerView recyclerPhotos;
    private PhotoAdapter photoAdapter;
    private List<String> capturedPhotos = new java.util.ArrayList<>();
    private ImageView imgOverlay;
    private ImageView imgFullPreview;
    private int currentPhotoStep = 0;
    private boolean overlayManualMode = false;
    private int selectedOverlayIndex = 0;
    private TextInputEditText editDate;
    private TextInputEditText editTime;
    private TextInputEditText editAddress;

    private DamageDiagramView diagramViewA;
    private DamageDiagramView diagramViewB;

    private SignatureView signatureViewA, signatureViewB;

    private ImageButton btnUseCurrentLocation;
    private static final int REQ_PERMISSION_LOCATION_ONLY = 1002;
    private static final int REQ_CAPTURE_PHOTO = 1003;
    private static final int REQ_PERMISSION_CAMERA = 1004;

    private ImageCapture imageCapture;
    // View caches for the steps
    private View viewPlates, viewTimeLocation, viewGeneral, viewVehicleInfo, viewCircumstances, viewPhotos, viewSummary, viewWitnesses, viewDamage, viewFaultSignature;
    private PreviewView previewView;
    // Use shared helpers from BaseActivity: v(root, id) and safeText(...)
    // no automatic location fields
    private final int[] overlays = new int[] {
            R.drawable.truck_front_left,
            R.drawable.truck_front_right,
            R.drawable.truck_back_left,
            R.drawable.truck_back_right
    };

    private final String[] countries = {"LT", "LV", "EE", "PL", "DE", "FR", "UK", "IT", "ES", "OTHER"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_report);
        
        String[] required = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA};

        if (!hasPermissions(required)) {
            showToast(getString(R.string.msg_permissions_not_granted));
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                handleBack();
            }
        });

        draftReport = new AccidentReport();
        initializeViews();
        updateStepUI();
        setupClickListeners();
        
        ViewCompat.setOnApplyWindowInsetsListener(v(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        btnBack = v(R.id.btn_back);
        textTitle = v(R.id.text_title);
        progressIndicator = v(R.id.progress_indicator);
        stepContainer = v(R.id.step_container);
        btnPrevious = v(R.id.btn_previous);
        btnNext = v(R.id.btn_next);

        // Pre-inflate step views
        viewPlates = getLayoutInflater().inflate(R.layout.step_plates, stepContainer, false);
        viewTimeLocation = getLayoutInflater().inflate(R.layout.step_time_location, stepContainer, false);
        viewGeneral = getLayoutInflater().inflate(R.layout.step_general_info, stepContainer, false);
        viewVehicleInfo = getLayoutInflater().inflate(R.layout.step_vehicle_info, stepContainer, false);
        viewCircumstances = getLayoutInflater().inflate(R.layout.step_circumstances, stepContainer, false);
        viewPhotos = getLayoutInflater().inflate(R.layout.step_photos, stepContainer, false);
        viewSummary = getLayoutInflater().inflate(R.layout.step_summary, stepContainer, false);
        viewWitnesses = getLayoutInflater().inflate(R.layout.step_witnesses, stepContainer, false);
        viewDamage = getLayoutInflater().inflate(R.layout.step_damage, stepContainer, false);
        viewFaultSignature = getLayoutInflater().inflate(R.layout.step_fault_signature, stepContainer, false);

        signatureViewA = new SignatureView(this);
        signatureViewB = new SignatureView(this);
        FrameLayout signatureContainer = viewFaultSignature.findViewById(R.id.signature_container);
        if (signatureContainer != null) {
            signatureContainer.addView(signatureViewA);
        }

        btnDownloadReport = viewSummary.findViewById(R.id.btn_download_report);
        btnPrevOverlay = viewPhotos.findViewById(R.id.btn_overlay_prev);
        btnNextOverlay = viewPhotos.findViewById(R.id.btn_overlay_next);


        imgFullPreview = viewPhotos.findViewById(R.id.img_full_preview);
        previewView = viewPhotos.findViewById(R.id.previewView);
        imgOverlay = viewPhotos.findViewById(R.id.img_overlay);
        btnAddPhoto = v(viewPhotos, R.id.btn_add_photo);
        recyclerPhotos = v(viewPhotos, R.id.recycler_photos);
        photoAdapter = new PhotoAdapter(capturedPhotos);

        editDate = viewTimeLocation.findViewById(R.id.edit_date);
        editTime = viewTimeLocation.findViewById(R.id.edit_time);
        editAddress = viewTimeLocation.findViewById(R.id.edit_address);
        btnUseCurrentLocation = viewTimeLocation.findViewById(R.id.btn_use_current_location);

        setupDateTimePickers();
        setupPlatesListeners();
        setupWitnessStep(viewWitnesses);
        setupDamageStep(viewDamage);
        setupCircumstancesStep(viewCircumstances);
        setupFaultSignatureStep(viewFaultSignature);
        setupVehicleInfoStep(viewVehicleInfo);

        if (recyclerPhotos != null) {
            recyclerPhotos.setAdapter(photoAdapter);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> handleBack());
        btnPrevious.setOnClickListener(v -> goToPreviousStep());
        btnNext.setOnClickListener(v -> goToNextStep());
        if (btnUseCurrentLocation != null) {
            btnUseCurrentLocation.setOnClickListener(v -> onUseCurrentLocationClicked());
        }
        if (btnAddPhoto != null) {
            btnAddPhoto.setOnClickListener(v -> onAddPhotoClicked());
        }

        if (btnDownloadReport != null) {
            btnDownloadReport.setOnClickListener(v -> {
                generateAndDownloadPdf();
            });
        }
        btnPrevOverlay.setOnClickListener(v -> {
            overlayManualMode = true;
            selectedOverlayIndex--;
            updateOverlay();
        });

        btnNextOverlay.setOnClickListener(v -> {
            overlayManualMode = true;
            selectedOverlayIndex++;
            updateOverlay();
        });
    }

    private void setupPlatesListeners() {
        MaterialCheckBox checkTrailerA = v(viewPlates, R.id.check_trailer_a);
        View layoutTrailerPlateA = v(viewPlates, R.id.layout_trailer_plate_a);
        if (checkTrailerA != null && layoutTrailerPlateA != null) {
            checkTrailerA.setOnCheckedChangeListener((buttonView, isChecked) -> 
                layoutTrailerPlateA.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        }

        MaterialCheckBox checkTrailerB = v(viewPlates, R.id.check_trailer_b);
        View layoutTrailerPlateB = v(viewPlates, R.id.layout_trailer_plate_b);
        if (checkTrailerB != null && layoutTrailerPlateB != null) {
            checkTrailerB.setOnCheckedChangeListener((buttonView, isChecked) -> 
                layoutTrailerPlateB.setVisibility(isChecked ? View.VISIBLE : View.GONE));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        Spinner spinnerA = v(viewPlates, R.id.spinner_country_a);
        if (spinnerA != null) spinnerA.setAdapter(adapter);
        
        Spinner spinnerB = v(viewPlates, R.id.spinner_country_b);
        if (spinnerB != null) spinnerB.setAdapter(adapter);
    }

    private void setupDateTimePickers() {
        editDate.setFocusable(false);
        editDate.setClickable(true);
        editTime.setFocusable(false);
        editTime.setClickable(true);

        editDate.setOnClickListener(v -> DateTimePicker.showDatePicker(this, editDate));
        editTime.setOnClickListener(v -> DateTimePicker.showTimePicker(this, editTime));
    }

    private void setupWitnessStep(View stepView) {
        MaterialButton btnAddWitness = stepView.findViewById(R.id.btn_add_witness);
        LinearLayout container = stepView.findViewById(R.id.witnesses_container);

        btnAddWitness.setOnClickListener(v -> {
            View itemView = getLayoutInflater().inflate(R.layout.item_witness, container, false);
            Witness witness = new Witness();
            witnesses.add(witness);

            MaterialButton btnRemove = itemView.findViewById(R.id.btn_remove_witness);
            btnRemove.setOnClickListener(v2 -> {
                int index = container.indexOfChild(itemView);
                if (index != -1) {
                    container.removeView(itemView);
                    witnesses.remove(index);
                }
            });

            container.addView(itemView);
        });
    }

    private void saveWitnessData(View stepView) {
        LinearLayout container = stepView.findViewById(R.id.witnesses_container);
        for (int i = 0; i < container.getChildCount(); i++) {
            View itemView = container.getChildAt(i);
            Witness witness = witnesses.get(i);

            TextInputEditText editFirstName = itemView.findViewById(R.id.edit_witness_first_name);
            TextInputEditText editLastName = itemView.findViewById(R.id.edit_witness_last_name);
            TextInputEditText editPhone = itemView.findViewById(R.id.edit_witness_phone);

            witness.setFirstName(safeText(editFirstName));
            witness.setLastName(safeText(editLastName));
            witness.setPhone(safeText(editPhone));
        }
        draftReport.setWitnesses(witnesses);
    }

    private void setupDamageStep(View stepView) {
        diagramViewA = new DamageDiagramView(this, null);
        diagramViewB = new DamageDiagramView(this, null);

        FrameLayout container = stepView.findViewById(R.id.damage_diagram_container);
        container.addView(diagramViewA);

        MaterialButton tabA = stepView.findViewById(R.id.tab_damage_a);
        MaterialButton tabB = stepView.findViewById(R.id.tab_damage_b);

        tabA.setOnClickListener(v -> {
            activeDamageTab = 0;
            container.removeAllViews();
            container.addView(diagramViewA);
            tabA.setAlpha(1.0f);
            tabB.setAlpha(0.6f);
        });

        tabB.setOnClickListener(v -> {
            activeDamageTab = 1;
            container.removeAllViews();
            container.addView(diagramViewB);
            tabA.setAlpha(0.6f);
            tabB.setAlpha(1.0f);
        });
    }

    private void saveDamageData() {
        List<Damage> combinedList = new ArrayList<>();
        for (String area : diagramViewA.getDamagedZoneDescriptions()) {
            combinedList.add(new Damage(area, "unknown", 0));
        }
        for (String area : diagramViewB.getDamagedZoneDescriptions()) {
            combinedList.add(new Damage(area, "unknown", 1));
        }
        draftReport.setDamages(combinedList);
    }

    private void setupCircumstancesStep(View stepView) {
        MaterialButton tabA = stepView.findViewById(R.id.tab_circ_a);
        MaterialButton tabB = stepView.findViewById(R.id.tab_circ_b);

        tabA.setOnClickListener(v -> {
            saveCircumstances(activeCircumstancesTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), stepView);
            activeCircumstancesTab = 0;
            loadCircumstances(draftReport.getVehicleA(), stepView);
            tabA.setAlpha(1.0f);
            tabB.setAlpha(0.6f);
        });

        tabB.setOnClickListener(v -> {
            saveCircumstances(activeCircumstancesTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), stepView);
            activeCircumstancesTab = 1;
            loadCircumstances(draftReport.getVehicleB(), stepView);
            tabA.setAlpha(0.6f);
            tabB.setAlpha(1.0f);
        });
    }

    private void setupVehicleInfoStep(View stepView) {
        MaterialButton tabA = stepView.findViewById(R.id.tab_vinfo_a);
        MaterialButton tabB = stepView.findViewById(R.id.tab_vinfo_b);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, countries);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner spinner = v(stepView, R.id.spinner_driver_country);
        if (spinner != null) spinner.setAdapter(adapter);

        tabA.setOnClickListener(v -> {
            saveVehicleInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), stepView);
            saveDriverInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), stepView);
            activeVehicleInfoTab = 0;
            loadVehicleInfo(draftReport.getVehicleA(), stepView);
            loadDriverInfo(draftReport.getVehicleA(), stepView);
            tabA.setAlpha(1.0f);
            tabB.setAlpha(0.6f);
        });

        tabB.setOnClickListener(v -> {
            saveVehicleInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), stepView);
            saveDriverInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), stepView);
            activeVehicleInfoTab = 1;
            loadVehicleInfo(draftReport.getVehicleB(), stepView);
            loadDriverInfo(draftReport.getVehicleB(), stepView);
            tabA.setAlpha(0.6f);
            tabB.setAlpha(1.0f);
        });
    }

    private void setupFaultSignatureStep(View stepView) {
        RadioGroup radioGroupFault = stepView.findViewById(R.id.radio_group_fault);
        TextView tvFaultSummary = stepView.findViewById(R.id.tv_fault_summary);
        RadioButton radioFaultA = stepView.findViewById(R.id.radio_fault_a);
        RadioButton radioFaultB = stepView.findViewById(R.id.radio_fault_b);

        String plateA = draftReport.getVehicleA().vehicleRegistration;
        String plateB = draftReport.getVehicleB().vehicleRegistration;
        if (plateA != null && !plateA.isEmpty()) radioFaultA.setText(getString(R.string.format_vehicle_info, getString(R.string.label_vehicle_a), plateA));
        if (plateB != null && !plateB.isEmpty()) radioFaultB.setText(getString(R.string.format_vehicle_info, getString(R.string.label_vehicle_b), plateB));

        radioGroupFault.setOnCheckedChangeListener((group, checkedId) -> {
            tvFaultSummary.setVisibility(View.VISIBLE);
            if (checkedId == R.id.radio_fault_a) {
                draftReport.setAtFaultVehicle("A");
                tvFaultSummary.setText(R.string.fault_summary_a);
            } else if (checkedId == R.id.radio_fault_b) {
                draftReport.setAtFaultVehicle("B");
                tvFaultSummary.setText(R.string.fault_summary_b);
            } else if (checkedId == R.id.radio_fault_both) {
                draftReport.setAtFaultVehicle("BOTH");
                tvFaultSummary.setText(R.string.fault_summary_both);
            }
        });

        MaterialButton tabSigA = stepView.findViewById(R.id.tab_sig_a);
        MaterialButton tabSigB = stepView.findViewById(R.id.tab_sig_b);
        FrameLayout signatureContainer = stepView.findViewById(R.id.signature_container);
        TextView tvSignHint = stepView.findViewById(R.id.tv_sign_hint);
        signatureContainer.removeAllViews();
        signatureContainer.addView(signatureViewA);
        signatureContainer.addView(signatureViewB);
        signatureContainer.addView(tvSignHint);
        signatureViewA.setVisibility(View.VISIBLE);
        signatureViewB.setVisibility(View.GONE);
        activeSignatureTab = 0;

        tabSigA.setOnClickListener(v -> {
            activeSignatureTab = 0;
            signatureViewA.setVisibility(View.VISIBLE);
            signatureViewB.setVisibility(View.GONE);
            tvSignHint.setVisibility(
                    signatureViewA.isEmpty() ? View.VISIBLE : View.GONE
            );
            tabSigA.setAlpha(1.0f);
            tabSigB.setAlpha(0.6f);
        });

        tabSigB.setOnClickListener(v -> {
            activeSignatureTab = 1;
            signatureViewA.setVisibility(View.GONE);
            signatureViewB.setVisibility(View.VISIBLE);
            tvSignHint.setVisibility(
                    signatureViewB.isEmpty() ? View.VISIBLE : View.GONE
            );
            tabSigA.setAlpha(0.6f);
            tabSigB.setAlpha(1.0f);
        });

        MaterialButton btnClear = stepView.findViewById(R.id.btn_clear_signature);
        btnClear.setOnClickListener(v -> {
            if (activeSignatureTab == 0) signatureViewA.clear();
            else signatureViewB.clear();
            tvSignHint.setVisibility(View.VISIBLE);
        });

        signatureViewA.setOnTouchListener((v, event) -> {
            tvSignHint.setVisibility(View.GONE);
            return false;
        });
        signatureViewB.setOnTouchListener((v, event) -> {
            tvSignHint.setVisibility(View.GONE);
            return false;
        });
    }

    private void saveFaultSignatureData() {
        if (!signatureViewA.isEmpty()) draftReport.setSignatureA(signatureViewA.toBase64Png());
        if (!signatureViewB.isEmpty()) draftReport.setSignatureB(signatureViewB.toBase64Png());
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());
                imageCapture = new ImageCapture.Builder().build();
                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;
                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void setOverlayStep() {
        if (imgOverlay == null) return;
        int index = overlayManualMode ? selectedOverlayIndex : currentPhotoStep;
        if (index >= 0 && index < overlays.length) {
            imgOverlay.setImageResource(overlays[index]);
        }
    }

    private void updateOverlay() {
        if (imgOverlay == null) return;
        int len = overlays.length;
        if (selectedOverlayIndex < 0) selectedOverlayIndex = len - 1;
        if (selectedOverlayIndex >= len) selectedOverlayIndex = 0;
        imgOverlay.setImageResource(overlays[selectedOverlayIndex]);
    }

    private void takePhoto() {
        if (imageCapture == null) return;
        File photoFile = new File(getExternalFilesDir(null), System.currentTimeMillis() + ".jpg");
        ImageCapture.OutputFileOptions outputOptions = new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        runOnUiThread(() -> {
                            capturedPhotos.add(photoFile.getAbsolutePath());
                            photoAdapter.notifyItemInserted(capturedPhotos.size() - 1);
                            currentPhotoStep++;
                            if (!overlayManualMode) { selectedOverlayIndex = currentPhotoStep; }
                            setOverlayStep();
                        });
                    }
                    @Override
                    public void onError(ImageCaptureException exception) {
                        exception.printStackTrace();
                    }
                }
        );
    }

    private void onAddPhotoClicked() { takePhoto(); }

    private void onUseCurrentLocationClicked() {
        String fine = android.Manifest.permission.ACCESS_FINE_LOCATION;
        if (hasPermissions(fine)) {
            fetchAndSetCurrentLocation();
        } else {
            requestAppPermissions(new String[]{fine}, REQ_PERMISSION_LOCATION_ONLY,
                    "Allow location to auto-fill your current accident location.");
        }
    }

    private void generateAndDownloadPdf() {
        if (btnDownloadReport != null) {
            btnDownloadReport.setEnabled(false);
            btnDownloadReport.setText(R.string.msg_generating_pdf);
        }

        new Thread(() -> {
            try {
                File pdfFile = PdfReportGenerator.generate(this, draftReport, capturedPhotos);
                runOnUiThread(() -> {
                    if (btnDownloadReport != null) {
                        btnDownloadReport.setEnabled(true);
                        btnDownloadReport.setText(R.string.btn_download_report);
                    }
                    showToast(getString(R.string.msg_saved_to_downloads));
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    if (btnDownloadReport != null) {
                        btnDownloadReport.setEnabled(true);
                        btnDownloadReport.setText(R.string.btn_download_report);
                    }
                    showToast(getString(R.string.msg_failed_pdf));
                });
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION_LOCATION_ONLY) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                fetchAndSetCurrentLocation();
            } else {
                showToast(getString(R.string.msg_location_denied));
            }
        }
    }

    private void fetchAndSetCurrentLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm == null || !hasPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)) return;

            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc != null) {
                applyLocationToForm(loc);
                return;
            }

            final LocationListener listener = new LocationListener() {
                @Override public void onLocationChanged(Location location) {
                    applyLocationToForm(location);
                    try { lm.removeUpdates(this); } catch (Exception ignored) {}
                }
                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
                @Override public void onStatusChanged(String p, int s, Bundle e) {}
            };

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, listener, Looper.getMainLooper());
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try { lm.removeUpdates(listener); } catch (Exception ignored) {}
            }, 10_000);
        } catch (SecurityException se) {
            showToast(getString(R.string.msg_location_error));
        }
    }

    private void applyLocationToForm(Location loc) {
        if (loc == null) return;
        TextInputEditText editLoc = v(viewTimeLocation, R.id.edit_address);
        String address = convertCoordsToAddress(loc.getLatitude(),loc.getLongitude());
        if (editLoc != null) editLoc.setText(address);
        draftReport.setLatitude(loc.getLatitude());
        draftReport.setLongitude(loc.getLongitude());
    }
    private String convertCoordsToAddress(double latitude, double longitude)
    {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty())
            {
                return addresses.get(0).getAddressLine(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    private void updateStepUI() {
        stepContainer.removeAllViews();
        int progress = (int) (((float) (currentStep + 1) / TOTAL_STEPS) * 100);
        progressIndicator.setProgress(progress, true);

        btnPrevious.setVisibility(currentStep == STEP_PLATES ? View.GONE : View.VISIBLE);
        btnNext.setText(currentStep == TOTAL_STEPS - 1 ? R.string.btn_finish : R.string.btn_next);
        textTitle.setText(getString(R.string.step_indicator, currentStep + 1, TOTAL_STEPS));

        switch (currentStep) {
            case STEP_PLATES:
                textTitle.setText(R.string.title_create_report);
                stepContainer.addView(viewPlates);
                loadPlatesData();
                break;
            case STEP_TIME_LOCATION:
                textTitle.setText(R.string.title_create_report);
                stepContainer.addView(viewTimeLocation);
                loadTimeLocationData();
                break;
            case STEP_WITNESSES:
                textTitle.setText(R.string.title_create_report);
                stepContainer.addView(viewWitnesses);
                break;
            case STEP_PHOTOS:
                setupStepTitle(viewPhotos, R.string.header_photos);
                stepContainer.addView(viewPhotos);
                if (hasPermissions(android.Manifest.permission.CAMERA)) {
                    startCamera();
                    currentPhotoStep = 0;
                    setOverlayStep();
                } else {
                    requestAppPermissions(new String[]{android.Manifest.permission.CAMERA}, REQ_PERMISSION_CAMERA, "Camera permission required");
                }
                break;
            case STEP_DAMAGE:
                stepContainer.addView(viewDamage);
                break;
            case STEP_CIRCUMSTANCES:
                setupStepTitle(viewCircumstances, R.string.header_circumstances);
                stepContainer.addView(viewCircumstances);
                loadCircumstances(activeCircumstancesTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), viewCircumstances);
                break;
            case STEP_VEHICLE_INFO:
                setupStepTitle(viewVehicleInfo, R.string.header_driver_vehicle);
                stepContainer.addView(viewVehicleInfo);
                loadVehicleInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), viewVehicleInfo);
                loadDriverInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), viewVehicleInfo);
                break;
            case STEP_FAULT_SIGNATURE:
                stepContainer.addView(viewFaultSignature);
                RadioButton radioFaultA = viewFaultSignature.findViewById(R.id.radio_fault_a);
                RadioButton radioFaultB = viewFaultSignature.findViewById(R.id.radio_fault_b);
                String plateA = draftReport.getVehicleA().vehicleRegistration;
                String plateB = draftReport.getVehicleB().vehicleRegistration;
                if (plateA != null && !plateA.isEmpty()) radioFaultA.setText(getString(R.string.format_vehicle_info, getString(R.string.label_vehicle_a), plateA));
                if (plateB != null && !plateB.isEmpty()) radioFaultB.setText(getString(R.string.format_vehicle_info, getString(R.string.label_vehicle_b), plateB));
                break;
            case STEP_SUMMARY:
                textTitle.setText(R.string.title_summary);
                stepContainer.addView(viewSummary);
                loadSummary();
                break;
        }
    }

    private void setupStepTitle(View view, int titleResId) {
        TextView tv = v(view, R.id.text_step_title);
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
            case STEP_PLATES:
                savePlatesData();
                break;
            case STEP_TIME_LOCATION:
                draftReport.setTimestamp(safeText(editDate), safeText(editTime));
                draftReport.setAddress(safeText(editAddress));
                break;
            case STEP_WITNESSES:
                saveWitnessData(viewWitnesses);
                break;
            case STEP_VEHICLE_INFO:
                saveVehicleInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), viewVehicleInfo);
                saveDriverInfo(activeVehicleInfoTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), viewVehicleInfo);
                break;
            case STEP_CIRCUMSTANCES:
                saveCircumstances(activeCircumstancesTab == 0 ? draftReport.getVehicleA() : draftReport.getVehicleB(), viewCircumstances);
                break;
            case STEP_DAMAGE:
                saveDamageData();
                break;
            case STEP_FAULT_SIGNATURE:
                saveFaultSignatureData();
                break;
        }
    }

    private void loadPlatesData() {
        VehicleSection va = draftReport.getVehicleA();
        VehicleSection vb = draftReport.getVehicleB();

        Spinner spA = v(viewPlates, R.id.spinner_country_a);
        TextInputEditText etA = v(viewPlates, R.id.edit_plate_a);
        MaterialCheckBox ctA = v(viewPlates, R.id.check_trailer_a);
        TextInputEditText etTA = v(viewPlates, R.id.edit_trailer_plate_a);
        MaterialCheckBox cnA = v(viewPlates, R.id.check_no_plate_a);

        if (spA != null) setSpinnerValue(spA, va.vehicleCountry);
        if (etA != null) etA.setText(va.vehicleRegistration);
        if (ctA != null) ctA.setChecked(va.hasTrailer);
        if (etTA != null) etTA.setText(va.trailerRegistration);
        if (cnA != null) cnA.setChecked(va.noLicencePlate);

        Spinner spB = v(viewPlates, R.id.spinner_country_b);
        TextInputEditText etB = v(viewPlates, R.id.edit_plate_b);
        MaterialCheckBox ctB = v(viewPlates, R.id.check_trailer_b);
        TextInputEditText etTB = v(viewPlates, R.id.edit_trailer_plate_b);
        MaterialCheckBox cnB = v(viewPlates, R.id.check_no_plate_b);

        if (spB != null) setSpinnerValue(spB, vb.vehicleCountry);
        if (etB != null) etB.setText(vb.vehicleRegistration);
        if (ctB != null) ctB.setChecked(vb.hasTrailer);
        if (etTB != null) etTB.setText(vb.trailerRegistration);
        if (cnB != null) cnB.setChecked(vb.noLicencePlate);
    }

    private void setSpinnerValue(Spinner spinner, String value) {
        if (value == null) return;
        for (int i = 0; i < countries.length; i++) {
            if (countries[i].equals(value)) {
                spinner.setSelection(i);
                break;
            }
        }
    }

    private void loadTimeLocationData() {
        if (editDate != null) editDate.setText(draftReport.getTimestampAsDate().split(" ")[0]);
        if (editTime != null) editTime.setText(draftReport.getTimestampAsDate().split(" ")[1]);
        if (editAddress != null) editAddress.setText(draftReport.getAddress());
    }

    private void loadVehicleInfo(VehicleSection vehicle, View root) {
        TextInputEditText vm = v(root, R.id.edit_vehicle_make);
        TextInputEditText vp = v(root, R.id.edit_vehicle_plate);
        TextInputEditText in = v(root, R.id.edit_insurance_name);
        TextInputEditText pn = v(root, R.id.edit_policy_number);
        MaterialCheckBox cb = v(root, R.id.check_covers_damage);

        if (vm != null) vm.setText(vehicle.vehicleMakeType);
        if (vp != null) vp.setText(vehicle.vehicleRegistration);
        if (in != null) in.setText(vehicle.insuranceName);
        if (pn != null) pn.setText(vehicle.policyNumber);
        if (cb != null) cb.setChecked(vehicle.coversDamage);
    }

    private void loadDriverInfo(VehicleSection vehicle, View root) {
        TextInputEditText fn = v(root, R.id.edit_driver_first_name);
        TextInputEditText ln = v(root, R.id.edit_driver_last_name);
        TextInputEditText dob = v(root, R.id.edit_driver_dob);
        Spinner sc = v(root, R.id.spinner_driver_country);
        TextInputEditText ad = v(root, R.id.edit_driver_address);
        TextInputEditText em = v(root, R.id.edit_driver_email);
        TextInputEditText co = v(root, R.id.edit_driver_contact);
        TextInputEditText li = v(root, R.id.edit_driver_license);
        TextInputEditText lc = v(root, R.id.edit_driver_license_category);
        TextInputEditText le = v(root, R.id.edit_driver_license_expiry);
        TextInputEditText pi = v(root, R.id.edit_driver_personal_id);

        if (fn != null) fn.setText(vehicle.driverFirstName);
        if (ln != null) ln.setText(vehicle.driverLastName);
        if (dob != null) dob.setText(vehicle.driverDob);
        if (sc != null) setSpinnerValue(sc, vehicle.driverCountry);
        if (ad != null) ad.setText(vehicle.driverStreet);
        if (em != null) em.setText(vehicle.driverEmail);
        if (co != null) co.setText(vehicle.driverContact);
        if (li != null) li.setText(vehicle.licenseNumber);
        if (lc != null) lc.setText(vehicle.licenseCategory);
        if (le != null) le.setText(vehicle.licenseExpiry);
        if (pi != null) pi.setText(vehicle.driverPersonalId);
    }

    private void loadCircumstances(VehicleSection vehicle, View root) {
        MaterialCheckBox cp = v(root, R.id.check_parked);
        MaterialCheckBox cs = v(root, R.id.check_stopping);
        MaterialCheckBox cso = v(root, R.id.check_starting_off);
        MaterialCheckBox cod = v(root, R.id.check_opening_door);
        MaterialCheckBox clp = v(root, R.id.check_leaving_parking);
        MaterialCheckBox cep = v(root, R.id.check_entering_parking);
        MaterialCheckBox cer = v(root, R.id.check_entering_roundabout);
        MaterialCheckBox ccr = v(root, R.id.check_circulating_roundabout);
        MaterialCheckBox cre = v(root, R.id.check_rear_end_same);
        MaterialCheckBox ccl = v(root, R.id.check_changing_lanes);
        MaterialCheckBox cot = v(root, R.id.check_overtaking);
        MaterialCheckBox ctr = v(root, R.id.check_turning_right);
        MaterialCheckBox ctl = v(root, R.id.check_turning_left);
        MaterialCheckBox crv = v(root, R.id.check_reversing);
        MaterialCheckBox ceo = v(root, R.id.check_entering_opposite);
        MaterialCheckBox cfr = v(root, R.id.check_from_right);
        MaterialCheckBox cfp = v(root, R.id.check_failed_priority);

        if (cp != null) cp.setChecked(vehicle.isParkedStopped);
        if (cs != null) cs.setChecked(vehicle.isStopping);
        if (cso != null) cso.setChecked(vehicle.isStartingOff);
        if (cod != null) cod.setChecked(vehicle.isOpeningDoor);
        if (clp != null) clp.setChecked(vehicle.isLeavingParking);
        if (cep != null) cep.setChecked(vehicle.isEnteringParking);
        if (cer != null) cer.setChecked(vehicle.isEnteringRoundabout);
        if (ccr != null) ccr.setChecked(vehicle.isCirculatingRoundabout);
        if (cre != null) cre.setChecked(vehicle.isRearEndSameDirection);
        if (ccl != null) ccl.setChecked(vehicle.isChangingLanes);
        if (cot != null) cot.setChecked(vehicle.isOvertaking);
        if (ctr != null) ctr.setChecked(vehicle.isTurningRight);
        if (ctl != null) ctl.setChecked(vehicle.isTurningLeft);
        if (crv != null) crv.setChecked(vehicle.isReversing);
        if (ceo != null) ceo.setChecked(vehicle.isEnteringOppositelane);
        if (cfr != null) cfr.setChecked(vehicle.isFromRightAtIntersection);
        if (cfp != null) cfp.setChecked(vehicle.isFailedToPrioritize);
    }

    private void loadSummary() {
        TextView tvSummary = v(viewSummary, R.id.summary_text);
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.summary_general_info)).append("\n");
        sb.append("============================\n");
        sb.append(getString(R.string.summary_date_time, draftReport.getTimestampAsDate())).append("\n");
        sb.append(getString(R.string.summary_address, draftReport.getAddress() != null ? draftReport.getAddress() : getString(R.string.summary_not_specified))).append("\n");
        if (draftReport.getLatitude() != null && draftReport.getLongitude() != null) {
            sb.append(getString(R.string.summary_coordinates, draftReport.getLatitude(), draftReport.getLongitude())).append("\n");
        }
        if (draftReport.getDescription() != null && !draftReport.getDescription().isEmpty()) {
            sb.append(getString(R.string.summary_description, draftReport.getDescription())).append("\n");
        }
        sb.append("\n");

        appendVehicleSummary(sb, getString(R.string.label_vehicle_a), draftReport.getVehicleA(), 0);
        sb.append("\n");
        appendVehicleSummary(sb, getString(R.string.label_vehicle_b), draftReport.getVehicleB(), 1);
        sb.append("\n");

        sb.append(getString(R.string.summary_witnesses)).append("\n");
        sb.append("============================\n");
        List<Witness> reportWitnesses = draftReport.getWitnesses();
        if (reportWitnesses == null || reportWitnesses.isEmpty()) {
            sb.append(getString(R.string.summary_no_witnesses)).append("\n");
        } else {
            for (Witness w : reportWitnesses) {
                sb.append("- ").append(w.getFirstName()).append(" ").append(w.getLastName());
                if (w.getPhone() != null && !w.getPhone().isEmpty()) {
                    sb.append(" (").append(w.getPhone()).append(")");
                }
                sb.append("\n");
            }
        }
        sb.append("\n");

        sb.append(getString(R.string.summary_fault_signatures)).append("\n");
        sb.append("============================\n");
        String fault = draftReport.getAtFaultVehicle();
        if (fault == null) sb.append(getString(R.string.summary_fault_not_assigned)).append("\n");
        else if (fault.equals("BOTH")) sb.append(getString(R.string.summary_fault_both)).append("\n");
        else sb.append(getString(R.string.summary_fault_vehicle, fault)).append("\n");

        sb.append(getString(R.string.summary_signature_a, draftReport.getSignatureA() != null ? getString(R.string.summary_signed) : getString(R.string.summary_missing))).append("\n");
        sb.append(getString(R.string.summary_signature_b, draftReport.getSignatureB() != null ? getString(R.string.summary_signed) : getString(R.string.summary_missing))).append("\n");

        tvSummary.setText(sb.toString());

        ImageView summaryPreview = viewSummary.findViewById(R.id.img_summary_full_preview);
        if (summaryPreview != null) summaryPreview.setOnClickListener(v -> summaryPreview.setVisibility(View.GONE));

        RecyclerView recyclerSummaryPhotos = v(viewSummary, R.id.recycler_summary_photos);
        if (recyclerSummaryPhotos != null) {
            SummaryPhotoAdapter summaryAdapter = new SummaryPhotoAdapter(capturedPhotos, summaryPreview);
            recyclerSummaryPhotos.setAdapter(summaryAdapter);
        }
    }

    private void appendVehicleSummary(StringBuilder sb, String label, VehicleSection vehicle, int vehicleIndex) {
        sb.append(label).append(":\n");
        sb.append("----------------------------\n");
        sb.append("- ").append(getString(R.string.summary_plate, vehicle.vehicleRegistration != null ? vehicle.vehicleRegistration : "-"));
        if (vehicle.vehicleCountry != null) sb.append(" (").append(vehicle.vehicleCountry).append(")");
        sb.append("\n");
        
        if (vehicle.hasTrailer) {
            sb.append("- ").append(getString(R.string.summary_trailer, vehicle.trailerRegistration != null ? vehicle.trailerRegistration : getString(R.string.summary_yes_no_plate))).append("\n");
        }
        
        sb.append("- ").append(getString(R.string.summary_vehicle_label, vehicle.vehicleMakeType != null ? vehicle.vehicleMakeType : getString(R.string.summary_na))).append("\n");
        
        sb.append("- ").append(getString(R.string.summary_driver, 
                vehicle.driverFirstName != null ? vehicle.driverFirstName : "",
                vehicle.driverLastName != null ? vehicle.driverLastName : "")).append("\n");
        if (vehicle.driverContact != null && !vehicle.driverContact.isEmpty()) {
            sb.append("  ").append(getString(R.string.summary_contact, vehicle.driverContact)).append("\n");
        }
        
        sb.append("- ").append(getString(R.string.summary_insurance, vehicle.insuranceName != null ? vehicle.insuranceName : getString(R.string.summary_na)));
        if (vehicle.policyNumber != null && !vehicle.policyNumber.isEmpty()) {
            sb.append(" ").append(getString(R.string.summary_policy_format, vehicle.policyNumber));
        }
        sb.append("\n");

        // Damages
        List<String> damageAreas = new ArrayList<>();
        if (draftReport.getDamages() != null) {
            for (Damage d : draftReport.getDamages()) {
                if (d.getVehicleTarget() == vehicleIndex) {
                    damageAreas.add(d.getArea());
                }
            }
        }
        sb.append("- ").append(getString(R.string.summary_damages, damageAreas.isEmpty() ? getString(R.string.summary_none_noted) : String.join(", ", damageAreas))).append("\n");

        sb.append("- ").append(getString(R.string.summary_circumstances, ""));
        List<String> circs = new ArrayList<>();
        if (vehicle.isParkedStopped) circs.add(getString(R.string.circ_parked_short));
        if (vehicle.isStopping) circs.add(getString(R.string.circ_stopping_short));
        if (vehicle.isStartingOff) circs.add(getString(R.string.circ_starting_off_short));
        if (vehicle.isOpeningDoor) circs.add(getString(R.string.circ_opening_door_short));
        if (vehicle.isLeavingParking) circs.add(getString(R.string.circ_leaving_parking_short));
        if (vehicle.isEnteringParking) circs.add(getString(R.string.circ_entering_parking_short));
        if (vehicle.isEnteringRoundabout) circs.add(getString(R.string.circ_entering_roundabout_short));
        if (vehicle.isCirculatingRoundabout) circs.add(getString(R.string.circ_in_roundabout_short));
        if (vehicle.isRearEndSameDirection) circs.add(getString(R.string.circ_rear_end_short));
        if (vehicle.isChangingLanes) circs.add(getString(R.string.circ_changing_lanes_short));
        if (vehicle.isOvertaking) circs.add(getString(R.string.circ_overtaking_short));
        if (vehicle.isTurningRight) circs.add(getString(R.string.circ_turning_right_short));
        if (vehicle.isTurningLeft) circs.add(getString(R.string.circ_turning_left_short));
        if (vehicle.isReversing) circs.add(getString(R.string.circ_reversing_short));
        if (vehicle.isEnteringOppositelane) circs.add(getString(R.string.circ_opposite_lane_short));
        if (vehicle.isFromRightAtIntersection) circs.add(getString(R.string.circ_from_right_short));
        if (vehicle.isFailedToPrioritize) circs.add(getString(R.string.circ_failed_priority_short));
        sb.append(circs.isEmpty() ? getString(R.string.summary_none_selected) : String.join(", ", circs)).append("\n");
    }

    private void savePlatesData() {
        VehicleSection va = draftReport.getVehicleA();
        VehicleSection vb = draftReport.getVehicleB();

        Spinner spA = v(viewPlates, R.id.spinner_country_a);
        TextInputEditText etA = v(viewPlates, R.id.edit_plate_a);
        MaterialCheckBox ctA = v(viewPlates, R.id.check_trailer_a);
        TextInputEditText etTA = v(viewPlates, R.id.edit_trailer_plate_a);
        MaterialCheckBox cnA = v(viewPlates, R.id.check_no_plate_a);

        if (spA != null) va.vehicleCountry = (String) spA.getSelectedItem();
        va.vehicleRegistration = safeText(etA);
        va.hasTrailer = ctA != null && ctA.isChecked();
        va.trailerRegistration = safeText(etTA);
        va.noLicencePlate = cnA != null && cnA.isChecked();

        Spinner spB = v(viewPlates, R.id.spinner_country_b);
        TextInputEditText etB = v(viewPlates, R.id.edit_plate_b);
        MaterialCheckBox ctB = v(viewPlates, R.id.check_trailer_b);
        TextInputEditText etTB = v(viewPlates, R.id.edit_trailer_plate_b);
        MaterialCheckBox cnB = v(viewPlates, R.id.check_no_plate_b);

        if (spB != null) vb.vehicleCountry = (String) spB.getSelectedItem();
        vb.vehicleRegistration = safeText(etB);
        vb.hasTrailer = ctB != null && ctB.isChecked();
        vb.trailerRegistration = safeText(etTB);
        vb.noLicencePlate = cnB != null && cnB.isChecked();
    }

    private void saveVehicleInfo(VehicleSection vehicle, View root) {
        TextInputEditText vm = v(root, R.id.edit_vehicle_make);
        TextInputEditText vp = v(root, R.id.edit_vehicle_plate);
        TextInputEditText in = v(root, R.id.edit_insurance_name);
        TextInputEditText pn = v(root, R.id.edit_policy_number);
        MaterialCheckBox cb = v(root, R.id.check_covers_damage);

        vehicle.vehicleMakeType = safeText(vm);
        vehicle.vehicleRegistration = safeText(vp);
        vehicle.insuranceName = safeText(in);
        vehicle.policyNumber = safeText(pn);
        vehicle.coversDamage = cb != null && cb.isChecked();
    }

    private void saveDriverInfo(VehicleSection vehicle, View root) {
        TextInputEditText fn = v(root, R.id.edit_driver_first_name);
        TextInputEditText ln = v(root, R.id.edit_driver_last_name);
        TextInputEditText dob = v(root, R.id.edit_driver_dob);
        Spinner sc = v(root, R.id.spinner_driver_country);
        TextInputEditText ad = v(root, R.id.edit_driver_address);
        TextInputEditText em = v(root, R.id.edit_driver_email);
        TextInputEditText co = v(root, R.id.edit_driver_contact);
        TextInputEditText li = v(root, R.id.edit_driver_license);
        TextInputEditText lc = v(root, R.id.edit_driver_license_category);
        TextInputEditText le = v(root, R.id.edit_driver_license_expiry);
        TextInputEditText pi = v(root, R.id.edit_driver_personal_id);

        vehicle.driverFirstName = safeText(fn);
        vehicle.driverLastName = safeText(ln);
        vehicle.driverDob = safeText(dob);
        if (sc != null) vehicle.driverCountry = (String) sc.getSelectedItem();
        vehicle.driverStreet = safeText(ad);
        vehicle.driverEmail = safeText(em);
        vehicle.driverContact = safeText(co);
        vehicle.licenseNumber = safeText(li);
        vehicle.licenseCategory = safeText(lc);
        vehicle.licenseExpiry = safeText(le);
        vehicle.driverPersonalId = safeText(pi);
        vehicle.driverName = vehicle.driverFirstName + " " + vehicle.driverLastName;
        
        // Sync insured info
        vehicle.insuredName = vehicle.driverName;
        vehicle.insuredAddress = vehicle.driverStreet;
    }

    private void saveCircumstances(VehicleSection vehicle, View root) {
        MaterialCheckBox cp = v(root, R.id.check_parked);
        MaterialCheckBox cs = v(root, R.id.check_stopping);
        MaterialCheckBox cso = v(root, R.id.check_starting_off);
        MaterialCheckBox cod = v(root, R.id.check_opening_door);
        MaterialCheckBox clp = v(root, R.id.check_leaving_parking);
        MaterialCheckBox cep = v(root, R.id.check_entering_parking);
        MaterialCheckBox cer = v(root, R.id.check_entering_roundabout);
        MaterialCheckBox ccr = v(root, R.id.check_circulating_roundabout);
        MaterialCheckBox cre = v(root, R.id.check_rear_end_same);
        MaterialCheckBox ccl = v(root, R.id.check_changing_lanes);
        MaterialCheckBox cot = v(root, R.id.check_overtaking);
        MaterialCheckBox ctr = v(root, R.id.check_turning_right);
        MaterialCheckBox ctl = v(root, R.id.check_turning_left);
        MaterialCheckBox crv = v(root, R.id.check_reversing);
        MaterialCheckBox ceo = v(root, R.id.check_entering_opposite);
        MaterialCheckBox cfr = v(root, R.id.check_from_right);
        MaterialCheckBox cfp = v(root, R.id.check_failed_priority);

        vehicle.isParkedStopped = cp != null && cp.isChecked();
        vehicle.isStopping = cs != null && cs.isChecked();
        vehicle.isStartingOff = cso != null && cso.isChecked();
        vehicle.isOpeningDoor = cod != null && cod.isChecked();
        vehicle.isLeavingParking = clp != null && clp.isChecked();
        vehicle.isEnteringParking = cep != null && cep.isChecked();
        vehicle.isEnteringRoundabout = cer != null && cer.isChecked();
        vehicle.isCirculatingRoundabout = ccr != null && ccr.isChecked();
        vehicle.isRearEndSameDirection = cre != null && cre.isChecked();
        vehicle.isChangingLanes = ccl != null && ccl.isChecked();
        vehicle.isOvertaking = cot != null && cot.isChecked();
        vehicle.isTurningRight = ctr != null && ctr.isChecked();
        vehicle.isTurningLeft = ctl != null && ctl.isChecked();
        vehicle.isReversing = crv != null && crv.isChecked();
        vehicle.isEnteringOppositelane = ceo != null && ceo.isChecked();
        vehicle.isFromRightAtIntersection = cfr != null && cfr.isChecked();
        vehicle.isFailedToPrioritize = cfp != null && cfp.isChecked();
    }

    private void handleBack() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_exit_title)
                .setMessage(R.string.dialog_exit_message)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> finish())
                .setNegativeButton(R.string.dialog_no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void finishWizard() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_finish_title)
                .setMessage(R.string.dialog_finish_message)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                    showToast(getString(R.string.toast_report_created));

                    createReportInDB(draftReport);

                    finish();
                })
                .setNegativeButton(R.string.dialog_no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private final java.util.List<String> photos;
        PhotoAdapter(java.util.List<String> photos) { this.photos = photos; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_preview, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String photoPath = photos.get(position);
            Glide.with(holder.itemView.getContext()).load(new File(photoPath)).into(holder.imageView);
            holder.imageView.setOnClickListener(v -> {
                imgFullPreview.setVisibility(View.VISIBLE);
                Glide.with(v.getContext()).load(new File(photos.get(holder.getAdapterPosition()))).into(imgFullPreview);
            });
            imgFullPreview.setOnClickListener(v -> imgFullPreview.setVisibility(View.GONE));
            holder.btnDelete.setOnClickListener(v -> {
                int currentPos = holder.getAdapterPosition();
                if (currentPos != RecyclerView.NO_POSITION) {
                    photos.remove(currentPos);
                    notifyItemRemoved(currentPos);
                    notifyItemRangeChanged(currentPos, photos.size());
                }
            });
        }
        @Override public int getItemCount() { return photos.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView; ImageButton btnDelete;
            ViewHolder(View v) { super(v); imageView = v.findViewById(R.id.image_preview); btnDelete = v.findViewById(R.id.btn_delete_photo); }
        }
    }

    private class SummaryPhotoAdapter extends RecyclerView.Adapter<SummaryPhotoAdapter.ViewHolder> {
        private final List<String> photos;
        private final ImageView localPreview;
        SummaryPhotoAdapter(List<String> photos, ImageView preview) { this.photos = photos; this.localPreview = preview; }

        @NonNull @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_preview, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            String path = photos.get(position);
            Glide.with(holder.itemView.getContext()).load(new java.io.File(path)).into(holder.imageView);
            holder.imageView.setOnClickListener(v -> {
                if (localPreview != null) {
                    localPreview.setVisibility(View.VISIBLE);
                    Glide.with(v.getContext()).load(new java.io.File(path)).into(localPreview);
                }
            });
            holder.btnDeleteContainer.setVisibility(View.GONE);
        }
        @Override public int getItemCount() { return photos.size(); }
        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView; View btnDeleteContainer;
            ViewHolder(View v) {
                super(v);
                imageView = v.findViewById(R.id.image_preview);
                btnDeleteContainer = v.findViewById(R.id.btn_delete_photo).getParent() instanceof View ?
                        (View) v.findViewById(R.id.btn_delete_photo).getParent() : v.findViewById(R.id.btn_delete_photo);
            }
        }
    }
    private static void createReportInDB(AccidentReport draftReport)
    {
        ReportApi api = ApiClient.getClient().create(ReportApi.class);
        Call<AccidentReport> call = api.createReport(draftReport);

        call.enqueue(new Callback<AccidentReport>() {
            @Override
            public void onResponse(Call<AccidentReport> call, Response<AccidentReport> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AccidentReport saved = response.body();
                    String id = saved.getId();
                }
            }
            @Override
            public void onFailure(Call<AccidentReport> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
