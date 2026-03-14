package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;

/**
 * BaseActivity provides shared functionality for all controllers (Activities),
 * such as runtime localization and common utility methods.
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("Settings", MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "en");
        
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        
        Configuration config = new Configuration();
        config.setLocale(locale);
        
        Context context = newBase.createConfigurationContext(config);
        super.attachBaseContext(context);
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
