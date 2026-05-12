package com.example.accidentreportingapp.controllers;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Locale;

public class DateTimePicker {

    public static void showDatePicker(Context context, TextView target) {

        Calendar calendar = Calendar.getInstance();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dialog = new DatePickerDialog(
                context,
                (view, y, m, d) -> {

                    String date = String.format(
                            Locale.getDefault(),
                            "%04d-%02d-%02d",
                            y, m + 1, d
                    );

                    target.setText(date);
                },
                year, month, day
        );

        dialog.show();
    }

    public static void showTimePicker(Context context, TextView target) {

        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog dialog = new TimePickerDialog(
                context,
                (view, h, m) -> {

                    String time = String.format(
                            Locale.getDefault(),
                            "%02d:%02d",
                            h, m
                    );

                    target.setText(time);
                },
                hour, minute,
                true
        );

        dialog.show();
    }
}
