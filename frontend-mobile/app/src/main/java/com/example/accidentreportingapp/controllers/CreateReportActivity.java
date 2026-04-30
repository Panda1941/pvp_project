package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
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
import com.example.accidentreportingapp.models.VehicleSection;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.util.List;
import android.os.Handler;
import android.os.Looper;
import java.util.Locale;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;

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
    private static final int STEP_PHOTOS = 9;
    private static final int STEP_SUMMARY = 10;
    private static final int TOTAL_STEPS = 11;

    private int currentStep = STEP_GENERAL;
    private AccidentReport draftReport;

    private ImageButton btnBack;
    private TextView textTitle;
    private LinearProgressIndicator progressIndicator;
    private ViewGroup stepContainer;
    private MaterialButton btnPrevious, btnNext;
    private MaterialButton btnAddPhoto;
    private RecyclerView recyclerPhotos;
    private PhotoAdapter photoAdapter;
    private List<String> capturedPhotos = new java.util.ArrayList<>();
    private ImageView imgOverlay;
    private ImageView imgFullPreview;
    private int currentPhotoStep = 0;


    private ImageButton btnUseCurrentLocation;
    private static final int REQ_PERMISSION_LOCATION_ONLY = 1002;
    private static final int REQ_CAPTURE_PHOTO = 1003;
    private static final int REQ_PERMISSION_CAMERA = 1004;

    private ImageCapture imageCapture;
    // View caches for the steps
    private View viewGeneral, viewVehicleInfo, viewInsurance, viewDriver, viewCircumstances, viewPhotos, viewSummary;
    private PreviewView previewView;
    // Use shared helpers from BaseActivity: v(root, id) and safeText(...)
    // no automatic location fields
    private final int[] overlays = new int[] {
            R.drawable.truck_front_left,
            R.drawable.truck_front_right,
            R.drawable.truck_back_left,
            R.drawable.truck_back_right
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_report);
        // NOTE: we check whether permissions are already granted so we
        // can politely inform the user if device features (GPS/camera) are
        // unavailable. We intentionally do NOT force a permission request here
        // because we want users to be able to file a report manually even if
        // they don't grant permissions now. Camera capture will be added later
        // and should request permissions on-demand.
        String[] required = new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            android.Manifest.permission.CAMERA};



        if (!hasPermissions(required)) {
            showToast("Location/Camera permissions not granted — you can enter location manually.");
        }

        draftReport = new AccidentReport();
        initializeViews();
        updateStepUI();
        setupClickListeners();
        // Automatic location disabled
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

        // Pre-inflate step views for smooth transitions
        viewGeneral = getLayoutInflater().inflate(R.layout.step_general_info, stepContainer, false);
        viewVehicleInfo = getLayoutInflater().inflate(R.layout.step_vehicle_info, stepContainer, false);
        viewInsurance = getLayoutInflater().inflate(R.layout.step_insurance_info, stepContainer, false);
        viewDriver = getLayoutInflater().inflate(R.layout.step_driver_info, stepContainer, false);
        viewCircumstances = getLayoutInflater().inflate(R.layout.step_circumstances, stepContainer, false);
        viewPhotos = getLayoutInflater().inflate(R.layout.step_photos, stepContainer, false);
        viewSummary = getLayoutInflater().inflate(R.layout.step_summary, stepContainer, false);

        imgFullPreview = viewPhotos.findViewById(R.id.img_full_preview);
        previewView = viewPhotos.findViewById(R.id.previewView);
        imgOverlay = viewPhotos.findViewById(R.id.img_overlay);
        btnAddPhoto = v(viewPhotos, R.id.btn_add_photo);
        recyclerPhotos = v(viewPhotos, R.id.recycler_photos);
        photoAdapter = new PhotoAdapter(capturedPhotos);
        if (recyclerPhotos != null) {
            recyclerPhotos.setAdapter(photoAdapter);
        }

        // Find the "Use current location" button inside the pre-inflated general step.
        int btnId = getResources().getIdentifier("btn_use_current_location", "id", getPackageName());
        if (btnId != 0) {
            View maybeBtn = v(viewGeneral, btnId);
            if (maybeBtn instanceof ImageButton) {
                btnUseCurrentLocation = (ImageButton) maybeBtn;
            }
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
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture =
                ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();

                Preview preview = new Preview.Builder().build();
                preview.setSurfaceProvider(previewView.getSurfaceProvider());

                imageCapture = new ImageCapture.Builder().build();

                CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

                cameraProvider.unbindAll();
                cameraProvider.bindToLifecycle(
                        this,
                        cameraSelector,
                        preview,
                        imageCapture
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }
    private void setOverlayStep(int step) {
        if (imgOverlay == null) return;

        imgOverlay.setImageResource(overlays[step]);
    }
    private void takePhoto() {
        if (imageCapture == null) return;

        File photoFile = new File(
                getExternalFilesDir(null),
                System.currentTimeMillis() + ".jpg"
        );

        ImageCapture.OutputFileOptions outputOptions =
                new ImageCapture.OutputFileOptions.Builder(photoFile).build();

        imageCapture.takePicture(
                outputOptions,
                ContextCompat.getMainExecutor(this),
                new ImageCapture.OnImageSavedCallback() {

                    @Override
                    public void onImageSaved(ImageCapture.OutputFileResults output) {
                        runOnUiThread(() -> {
                            capturedPhotos.add(photoFile.getAbsolutePath());
                            photoAdapter.notifyItemInserted(capturedPhotos.size() - 1);
                            currentPhotoStep++;

                            if (currentPhotoStep < overlays.length) {
                                setOverlayStep(currentPhotoStep);
                            }
                        });
                    }
                    @Override
                    public void onError(ImageCaptureException exception) {
                        exception.printStackTrace();
                    }
                }
        );
    }

    private void onAddPhotoClicked() {
        takePhoto();
    }
    private void onUseCurrentLocationClicked() {
        String fine = android.Manifest.permission.ACCESS_FINE_LOCATION;
        if (hasPermissions(fine)) {
            fetchAndSetCurrentLocation();
        } else {
            requestAppPermissions(new String[]{fine}, REQ_PERMISSION_LOCATION_ONLY,
                    "Allow location to auto-fill your current accident location.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_PERMISSION_LOCATION_ONLY) {
            if (grantResults != null && grantResults.length > 0 && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                fetchAndSetCurrentLocation();
            } else {
                showToast("Location permission denied. You can enter location manually.");
            }
        }
    }

    // Automatic location methods removed; manual entry only
    private void fetchAndSetCurrentLocation() {
        try {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) {
                showToast("Location services unavailable");
                return;
            }

            if (!hasPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                showToast("Location permission not granted");
                return;
            }

            // Try last-known locations first (fast). If none available, request
            // a single fresh update from the providers and wait briefly.
            Location loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (loc == null) loc = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (loc != null) {
                applyLocationToForm(loc);
                showToast("Location filled from device GPS");
                return;
            }

            // No last-known location — request a single update and timeout after 10s
            showToast("Attempting to get current location — please wait...");
            final String[] providers = new String[]{LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER};
            final LocationListener listener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    applyLocationToForm(location);
                    try { lm.removeUpdates(this); } catch (Exception ignored) {}
                }

                @Override public void onProviderEnabled(String provider) {}
                @Override public void onProviderDisabled(String provider) {}
                @Override public void onStatusChanged(String provider, int status, android.os.Bundle extras) {}
            };

            for (String p : providers) {
                try {
                    lm.requestLocationUpdates(p, 0L, 0f, listener, Looper.getMainLooper());
                } catch (SecurityException se) {
                    // Shouldn't happen because we checked permissions, but guard anyway
                } catch (IllegalArgumentException iae) {
                    // provider not available on device; ignore
                }
            }

            // Timeout: stop listening after 10 seconds if no update
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                try { lm.removeUpdates(listener); } catch (Exception ignored) {}
                showToast("Could not determine current location. Enter manually.");
            }, 10_000);
        } catch (SecurityException se) {
            showToast("Permission error while accessing location");
        } catch (Exception e) {
            showToast("Error obtaining location: " + e.getMessage());
        }
    }

    /**
     * Attempt to get a single high-accuracy location via the fused provider.
     * Falls back to the legacy LocationManager flow if fused fails or returns null.
     */
    // Fused location logic removed

    private void requestContinuousFusedUpdates() {
        // continuous fused updates removed
    }

    /**
     * Resolve address by performing a network reverse-geocode only when online.
     * This keeps internet usage minimal: single fused request + at most one reverse-geocode.
     */
    private void resolveAddressIfOnline(double lat, double lon) {
        // network reverse-geocode removed (manual entry only)
    }

    private void applyLocationToForm(Location loc) {
        if (loc == null) return;
        TextInputEditText editLoc = v(viewGeneral, R.id.edit_location);
        String text = String.format(Locale.getDefault(), "Lat: %.5f, Lon: %.5f", loc.getLatitude(), loc.getLongitude());
        if (editLoc != null) editLoc.setText(text);
        draftReport.setLocation(text);
    }

    private void resolveAddressOffline(double lat, double lon) {
        // offline reverse-geocode disabled; keep manual coordinate entry
    }

    /**
     * Simple network availability check used to decide whether to call
     * the fused provider reverse-geocode and to avoid unnecessary network calls.
     */
    private boolean hasNetworkConnection() {
        // network checks removed — no network-based features
        return false;
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
            case STEP_PHOTOS:
                setupStepTitle(viewPhotos, R.string.header_photos);
                stepContainer.addView(viewPhotos);

                if (hasPermissions(android.Manifest.permission.CAMERA)) {
                    startCamera();
                    currentPhotoStep = 0;
                    setOverlayStep(currentPhotoStep);
                } else {
                    requestAppPermissions(
                            new String[]{android.Manifest.permission.CAMERA},
                            REQ_PERMISSION_CAMERA,
                            "Camera permission required"
                    );
                }
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
            case STEP_GENERAL:
                TextInputEditText editLoc = v(viewGeneral, R.id.edit_location);
                TextInputEditText editDesc = v(viewGeneral, R.id.edit_description);
                draftReport.setLocation(safeText(editLoc));
                draftReport.setDescription(safeText(editDesc));
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
            case STEP_PHOTOS:
                // savePhotosData(); // To be implemented
                break;
        }
    }

    // Data Loading Helpers

    private void loadGeneralData() {
        TextInputEditText el = v(viewGeneral, R.id.edit_location);
        TextInputEditText ed = v(viewGeneral, R.id.edit_description);
        if (el != null) el.setText(draftReport.getLocation());
        if (ed != null) ed.setText(draftReport.getDescription());
    }

    private void loadVehicleInfo(VehicleSection vehicle) {
        TextInputEditText in = v(viewVehicleInfo, R.id.edit_insured_name);
        TextInputEditText ia = v(viewVehicleInfo, R.id.edit_insured_address);
        TextInputEditText vm = v(viewVehicleInfo, R.id.edit_vehicle_make);
        TextInputEditText vp = v(viewVehicleInfo, R.id.edit_vehicle_plate);
        if (in != null) in.setText(vehicle.insuredName);
        if (ia != null) ia.setText(vehicle.insuredAddress);
        if (vm != null) vm.setText(vehicle.vehicleMakeType);
        if (vp != null) vp.setText(vehicle.vehicleRegistration);
    }

    private void loadInsuranceInfo(VehicleSection vehicle) {
        TextInputEditText in = v(viewInsurance, R.id.edit_insurance_name);
        TextInputEditText pn = v(viewInsurance, R.id.edit_policy_number);
        MaterialCheckBox cb = v(viewInsurance, R.id.check_covers_damage);
        if (in != null) in.setText(vehicle.insuranceName);
        if (pn != null) pn.setText(vehicle.policyNumber);
        if (cb != null) cb.setChecked(vehicle.coversDamage);
    }

    private void loadDriverInfo(VehicleSection vehicle) {
        TextInputEditText dn = v(viewDriver, R.id.edit_driver_name);
        TextInputEditText dd = v(viewDriver, R.id.edit_driver_dob);
        TextInputEditText dl = v(viewDriver, R.id.edit_driver_license);
        if (dn != null) dn.setText(vehicle.driverName);
        if (dd != null) dd.setText(vehicle.driverDob);
        if (dl != null) dl.setText(vehicle.licenseNumber);
    }

    private void loadCircumstances(VehicleSection vehicle) {
        MaterialCheckBox cp = v(viewCircumstances, R.id.check_parked);
        MaterialCheckBox cl = v(viewCircumstances, R.id.check_leaving);
        MaterialCheckBox cr = v(viewCircumstances, R.id.check_reversing);
        if (cp != null) cp.setChecked(vehicle.isParkedStopped);
        if (cl != null) cl.setChecked(vehicle.isLeavingParking);
        if (cr != null) cr.setChecked(vehicle.isReversing);
    }

    private void loadSummary() {
        TextView tvSummary = v(viewSummary, R.id.summary_text);
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
        TextInputEditText in = v(viewVehicleInfo, R.id.edit_insured_name);
        TextInputEditText ia = v(viewVehicleInfo, R.id.edit_insured_address);
        TextInputEditText vm = v(viewVehicleInfo, R.id.edit_vehicle_make);
        TextInputEditText vp = v(viewVehicleInfo, R.id.edit_vehicle_plate);
        vehicle.insuredName = safeText(in);
        vehicle.insuredAddress = safeText(ia);
        vehicle.vehicleMakeType = safeText(vm);
        vehicle.vehicleRegistration = safeText(vp);
    }

    private void saveInsuranceInfo(VehicleSection vehicle) {
        TextInputEditText in = v(viewInsurance, R.id.edit_insurance_name);
        TextInputEditText pn = v(viewInsurance, R.id.edit_policy_number);
        MaterialCheckBox cb = v(viewInsurance, R.id.check_covers_damage);
        vehicle.insuranceName = safeText(in);
        vehicle.policyNumber = safeText(pn);
        vehicle.coversDamage = cb != null && cb.isChecked();
    }

    private void saveDriverInfo(VehicleSection vehicle) {
        TextInputEditText dn = v(viewDriver, R.id.edit_driver_name);
        TextInputEditText dd = v(viewDriver, R.id.edit_driver_dob);
        TextInputEditText dl = v(viewDriver, R.id.edit_driver_license);
        vehicle.driverName = safeText(dn);
        vehicle.driverDob = safeText(dd);
        vehicle.licenseNumber = safeText(dl);
    }

    private void saveCircumstances(VehicleSection vehicle) {
        MaterialCheckBox cp = v(viewCircumstances, R.id.check_parked);
        MaterialCheckBox cl = v(viewCircumstances, R.id.check_leaving);
        MaterialCheckBox cr = v(viewCircumstances, R.id.check_reversing);
        vehicle.isParkedStopped = cp != null && cp.isChecked();
        vehicle.isLeavingParking = cl != null && cl.isChecked();
        vehicle.isReversing = cr != null && cr.isChecked();
    }

    private void handleBack() {
        if (currentStep > 0) {
            goToPreviousStep();
        } else {
            finish();
        }
    }

    private void finishWizard() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setTitle(R.string.dialog_finish_title)
                .setMessage(R.string.dialog_finish_message)
                .setPositiveButton(R.string.dialog_yes, (dialog, which) -> {
                    showToast(getString(R.string.toast_report_created));
                    finish();
                })
                .setNegativeButton(R.string.dialog_no, (dialog, which) -> dialog.dismiss())
                .show();
    }

    private class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
        private final java.util.List<String> photos;

        PhotoAdapter(java.util.List<String> photos) {
            this.photos = photos;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo_preview, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position)
        {
            String photoPath = photos.get(position);

            Glide.with(holder.itemView.getContext())
                    .load(new File(photoPath))
                    .into(holder.imageView);

            holder.imageView.setOnClickListener(v -> {

                String previewPhotoPath = photos.get(holder.getAdapterPosition());

                // show full preview
                imgFullPreview.setVisibility(View.VISIBLE);

                Glide.with(v.getContext())
                        .load(new File(previewPhotoPath))
                        .into(imgFullPreview);
            });

            imgFullPreview.setOnClickListener(v -> {
                imgFullPreview.setVisibility(View.GONE);
            });


        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imageView;
            ViewHolder(View v) {
                super(v);
                imageView = v.findViewById(R.id.image_preview);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // cleaned up location resources removed
    }
}
