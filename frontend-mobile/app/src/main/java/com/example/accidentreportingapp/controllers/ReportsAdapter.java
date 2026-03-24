package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accidentreportingapp.R;
import com.example.accidentreportingapp.models.AccidentReport;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for the RecyclerView in ViewReportsActivity.
 * Binds AccidentReport data to the card layout and handles click events.
 */
public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {

    private final List<AccidentReport> reports;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public ReportsAdapter(List<AccidentReport> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ReportsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_accident_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportsAdapter.ViewHolder holder, int position) {
        AccidentReport report = reports.get(position);
        
        holder.textLocation.setText(report.getLocation());
        holder.textPlate.setText(report.getVehicleA().vehicleRegistration);
        holder.textDescription.setText(report.getDescription());
        
        String dateString = dateFormat.format(new Date(report.getTimestamp()));
        holder.textTimestamp.setText(dateString);

        // Set localized status text and color
        setStatusUI(holder.textStatus, report);

        // Handle item click to open details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ReportDetailsActivity.class);
            intent.putExtra("REPORT_DATA", report);
            v.getContext().startActivity(intent);
        });
    }

    /**
     * Helper to set status text and color based on the report status.
     */
    private void setStatusUI(TextView textView, AccidentReport report) {
        Context context = textView.getContext();
        int status = report.getStatus();
        
        // Use localized string from resources
        textView.setText(context.getString(report.getStatusResourceId()));
        
        int color;
        if (status == AccidentReport.STATUS_CONFIRMED) {
            color = ContextCompat.getColor(context, R.color.status_confirmed);
        } else if (status == AccidentReport.STATUS_ISSUE) {
            color = ContextCompat.getColor(context, R.color.status_issue);
        } else {
            color = ContextCompat.getColor(context, R.color.status_waiting);
        }
        textView.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final TextView textLocation, textTimestamp, textPlate, textDescription, textStatus;

        public ViewHolder(View view) {
            super(view);
            textLocation = view.findViewById(R.id.text_location);
            textTimestamp = view.findViewById(R.id.text_timestamp);
            textPlate = view.findViewById(R.id.text_plate);
            textDescription = view.findViewById(R.id.text_description);
            textStatus = view.findViewById(R.id.text_status);
        }
    }
}
