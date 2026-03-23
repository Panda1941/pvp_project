package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Locale;

/**
 * BaseActivity provides shared functionality for all controllers (Activities),
 * such as runtime localization, theme management, and common utility methods.
 */
public abstract class BaseActivity extends AppCompatActivity {

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
}
