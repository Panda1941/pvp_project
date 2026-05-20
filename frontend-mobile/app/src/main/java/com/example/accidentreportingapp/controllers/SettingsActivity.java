package com.example.accidentreportingapp.controllers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.accidentreportingapp.R;
import com.google.android.material.button.MaterialButtonToggleGroup;

/**
 * SettingsActivity allows the user to customize application preferences.
 * Supports runtime language switching and theme (Light/Dark/System) selection via a toggle group.
 */
public class SettingsActivity extends BaseActivity {

    private ImageButton btnBack;
    private RadioGroup radioGroupLanguage;
    private RadioButton radioEn, radioLt, radioRu;
    private MaterialButtonToggleGroup themeToggleGroup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);

        // NOTE: Changing language requires a restart of the
        // activity stack to apply a new locale cleanly across the app.
        // We handle this by saving the preference and restarting the activity stack.

        initializeViews();
        loadSettings();
        setupClickListeners();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void initializeViews() {
        btnBack = v(R.id.btn_back);
        radioGroupLanguage = v(R.id.radio_group_language);
        radioEn = v(R.id.radio_en);
        radioLt = v(R.id.radio_lt);
        radioRu = v(R.id.radio_ru);
        themeToggleGroup = v(R.id.theme_toggle_group);
    }

    private void loadSettings() {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        
        // Load Language - Default to English
        String selectedLanguage = prefs.getString("My_Lang", "en");
        if (selectedLanguage.equals("lt")) {
            radioLt.setChecked(true);
        } else if (selectedLanguage.equals("ru")) {
            radioRu.setChecked(true);
        } else {
            radioEn.setChecked(true);
        }

        // Load Theme - Default to System
        int themeMode = prefs.getInt("Theme_Mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (themeMode == AppCompatDelegate.MODE_NIGHT_YES) {
            themeToggleGroup.check(R.id.btn_theme_dark);
        } else if (themeMode == AppCompatDelegate.MODE_NIGHT_NO) {
            themeToggleGroup.check(R.id.btn_theme_light);
        } else {
            themeToggleGroup.check(R.id.btn_theme_system);
        }
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        // Language change - Saves immediately and restarts app
        radioGroupLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            String newLang;
            if (checkedId == R.id.radio_lt) {
                newLang = "lt";
            } else if (checkedId == R.id.radio_ru) {
                newLang = "ru";
            } else {
                newLang = "en";
            }
            saveLanguage(newLang);
        });

        // Theme change - Saves immediately and applies
        themeToggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                int themeMode;
                if (checkedId == R.id.btn_theme_dark) {
                    themeMode = AppCompatDelegate.MODE_NIGHT_YES;
                } else if (checkedId == R.id.btn_theme_light) {
                    themeMode = AppCompatDelegate.MODE_NIGHT_NO;
                } else {
                    themeMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                }
                saveTheme(themeMode);
            }
        });
    }

    private void saveLanguage(String lang) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String currentLang = prefs.getString("My_Lang", "en");

        if (!currentLang.equals(lang)) {
            prefs.edit().putString("My_Lang", lang).apply();
            
            // Restart the app to apply the language change globally.
            // We start MainActivity with CLEAR_TOP to refresh it, 
            // then immediately launch SettingsActivity again so the user stays here.
            Intent mainIntent = new Intent(this, MainActivity.class);
            mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            
            finish();
            // Use a smooth fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }

    private void saveTheme(int themeMode) {
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        int currentTheme = prefs.getInt("Theme_Mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        if (currentTheme != themeMode) {
            prefs.edit().putInt("Theme_Mode", themeMode).apply();
            AppCompatDelegate.setDefaultNightMode(themeMode);
        }
    }
}
