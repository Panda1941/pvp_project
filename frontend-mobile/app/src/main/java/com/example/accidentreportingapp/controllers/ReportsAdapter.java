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
    // NOTE: We use `Serializable` for `AccidentReport` in this demo.
    // When passing objects via Intents consider switching to `Parcelable`
    // for better performance on Android IPC. The `dateFormat` below is
    // kept per-adapter to allow locale-sensitive formatting without
    // dealing with threading issues.
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public ReportsAdapter(List<AccidentReport> reports) {
        this.reports = reports;
    }

    @SuppressWarnings("unchecked")
    private static <T extends View> T vv(View root, int id) {
        return (T) root.findViewById(id);
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
            textLocation = vv(view, R.id.text_location);
            textTimestamp = vv(view, R.id.text_timestamp);
            textPlate = vv(view, R.id.text_plate);
            textDescription = vv(view, R.id.text_description);
            textStatus = vv(view, R.id.text_status);
        }
    }
}
