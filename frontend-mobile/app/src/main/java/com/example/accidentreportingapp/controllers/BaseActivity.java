package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.view.View;
import com.google.android.material.textfield.TextInputEditText;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import android.content.pm.PackageManager;

/**
 * BaseActivity provides shared functionality for all controllers (Activities),
 * such as runtime localization, theme management, and common utility methods.
 */
public abstract class BaseActivity extends AppCompatActivity {

    // Permission request codes
    public static final int REQ_PERMISSIONS_LOCATION_CAMERA = 1001;

    /*
     * NOTE: This activity centralizes small but important app-wide
     * behaviors that many Activities need: applying a saved locale, applying
     * the user's theme preference, showing quick toasts, and a couple of
     * helper methods for runtime permission handling. Keeping these helpers
     * here avoids repeating the same logic in every Activity.
     *
    * Why permissions are here: permission checks and simple rationale
    * dialogs are cross-cutting concerns — putting them in `BaseActivity`
    * makes it convenient to call `hasPermissions(...)` or
    * `requestAppPermissions(...)` from any subclass without duplicating
    * boilerplate.
     */


    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply saved locale before activity is created
        SharedPreferences prefs = newBase.getSharedPreferences("Settings", MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "en");
        
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration();
        config.setLocale(locale);
        
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before calling super.onCreate
        applyTheme();
        super.onCreate(savedInstanceState);
    }

    /**
     * Loads the saved theme preference and applies it using AppCompatDelegate.
     */
    protected void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        int themeMode = prefs.getInt("Theme_Mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(themeMode);
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Check whether the given permissions are all granted.
     */
    protected boolean hasPermissions(String... permissions) {
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request the given permissions, showing a rationale dialog if needed.
     */
    protected void requestAppPermissions(final String[] permissions, final int requestCode, String rationale) {
        if (hasPermissions(permissions)) return;

        boolean shouldExplain = false;
        for (String p : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, p)) {
                shouldExplain = true;
                break;
            }
        }

        if (shouldExplain && rationale != null && !rationale.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setMessage(rationale)
                    .setPositiveButton(android.R.string.ok, (d, w) -> ActivityCompat.requestPermissions(this, permissions, requestCode))
                    .setNegativeButton(android.R.string.cancel, (d, w) -> d.dismiss())
                    .show();
        } else {
            ActivityCompat.requestPermissions(this, permissions, requestCode);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean anyDenied = false;
        for (int r : grantResults) {
            if (r != PackageManager.PERMISSION_GRANTED) { anyDenied = true; break; }
        }
        if (anyDenied) {
            showToast("Required permissions were not granted");
        }
    }

    /**
     * Helper: find a view by id from the current Activity root.
     * Saves a cast at call sites: `v(R.id.my_view)` returns the typed View.
     */
    @SuppressWarnings("unchecked")
    protected <T extends View> T v(int id) {
        return (T) findViewById(id);
    }

    /**
     * Helper: find a view by id from a specific root view (useful for
     * pre-inflated step views). Returns null if root is null.
     */
    @SuppressWarnings("unchecked")
    protected <T extends View> T v(View root, int id) {
        if (root == null) return null;
        return (T) root.findViewById(id);
    }

    /**
     * Helper: safely extract text from a TextInputEditText, returning
     * an empty string when the view or its text is null.
     */
    protected String safeText(TextInputEditText e) {
        if (e == null) return "";
        CharSequence cs = e.getText();
        return cs == null ? "" : cs.toString();
    }
}
