package com.example.accidentreportingapp.controllers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accidentreportingapp.R;
import com.google.android.material.button.MaterialButton;

/**
 * SettingsActivity allows the user to customize application preferences.
 * Currently supports runtime language switching between English and Lithuanian.
 * Extends BaseActivity for shared localization and utility logic.
 */
public class SettingsActivity extends BaseActivity {

    private ImageButton btnBack;
    private RadioGroup radioGroupLanguage;
    private RadioButton radioEn, radioLt;
    private MaterialButton btnSave;
    private String selectedLanguage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        initializeViews();
        loadSettings();
        setupClickListeners();

        // Standard edge-to-edge padding handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btn_back);
        radioGroupLanguage = findViewById(R.id.radio_group_language);
        radioEn = findViewById(R.id.radio_en);
        radioLt = findViewById(R.id.radio_lt);
        btnSave = findViewById(R.id.btn_save);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        selectedLanguage = prefs.getString("My_Lang", "en");

        if (selectedLanguage.equals("lt")) {
            radioLt.setChecked(true);
        } else {
            radioEn.setChecked(true);
        }
    }

    private void setupClickListeners() {
        // Go back without saving
        btnBack.setOnClickListener(v -> finish());

        // Update selected language variable when radio changes
        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radio_lt) {
                selectedLanguage = "lt";
            } else {
                selectedLanguage = "en";
            }
        });

        // Save and apply changes
        btnSave.setOnClickListener(v -> saveAndApplySettings());
    }

    private void saveAndApplySettings() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String currentLang = prefs.getString("My_Lang", "en");

        // Save preference
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("My_Lang", selectedLanguage);
        editor.apply();

        // If language changed, restart the app from MainActivity to apply everywhere
        if (!currentLang.equals(selectedLanguage)) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            // If no change, just go back
            finish();
        }
    }
}
