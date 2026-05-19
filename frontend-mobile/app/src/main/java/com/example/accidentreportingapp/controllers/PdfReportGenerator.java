package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;
import android.util.Base64;

import com.example.accidentreportingapp.models.AccidentReport;
import com.example.accidentreportingapp.models.Damage;
import com.example.accidentreportingapp.models.VehicleSection;
import com.example.accidentreportingapp.models.Witness;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Generates a unified English PDF report of the accident.
 * This class ensures that all labels and headers are in English for official processing,
 * regardless of the user's interface language.
 */
public class PdfReportGenerator {

    private static final int PAGE_WIDTH = 1200;
    private static final int PAGE_HEIGHT = 1800;
    private static final int START_X = 50;
    private static final int START_Y = 80;
    private static final int LINE_HEIGHT = 30;

    // Fixed English Date Format for official PDF
    private static final SimpleDateFormat PDF_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    public static File generate(Context context, AccidentReport report, List<String> photos) throws IOException {
        PdfDocument pdfDocument = new PdfDocument();

        // Setup Paints
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setTextSize(36);
        titlePaint.setFakeBoldText(true);

        Paint subTitlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        subTitlePaint.setTextSize(26);
        subTitlePaint.setFakeBoldText(true);

        Paint headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setTextSize(20);
        headerPaint.setFakeBoldText(true);

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(18);

        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextSize(18);
        labelPaint.setColor(android.graphics.Color.GRAY);

        Paint linePaint = new Paint();
        linePaint.setColor(android.graphics.Color.DKGRAY);
        linePaint.setStrokeWidth(2f);

        int pageNumber = 1;
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();

        int y = START_Y;

        // HEADER
        canvas.drawText("ACCIDENT REPORT / EUROPEAN ACCIDENT STATEMENT", START_X, y, titlePaint);
        y += 60;
        canvas.drawLine(START_X, y, PAGE_WIDTH - START_X, y, linePaint);
        y += 50;

        // 1. GENERAL INFORMATION
        canvas.drawText("1. GENERAL INFORMATION", START_X, y, subTitlePaint);
        y += 40;

        drawField(canvas, "Date & Time:", PDF_DATE_FORMAT.format(new Date(report.getTimestamp())), START_X, y, textPaint, labelPaint);
        y += LINE_HEIGHT;
        drawField(canvas, "Address:", report.getAddress() != null ? report.getAddress() : "N/A", START_X, y, textPaint, labelPaint);
        y += LINE_HEIGHT;
        
        if (report.getLatitude() != null && report.getLongitude() != null) {
            String coords = String.format(Locale.US, "%.6f, %.6f", report.getLatitude(), report.getLongitude());
            drawField(canvas, "Coordinates:", coords, START_X, y, textPaint, labelPaint);
            y += LINE_HEIGHT;
        }

        y += 10;
        canvas.drawText("Description of accident:", START_X, y, labelPaint);
        y += 25;
        String desc = report.getDescription();
        if (desc == null || desc.isEmpty()) desc = "No description provided.";
        y = drawWrappedText(canvas, desc, START_X + 20, y, PAGE_WIDTH - (START_X * 2) - 20, textPaint, LINE_HEIGHT);
        
        y += 40;
        canvas.drawLine(START_X, y, PAGE_WIDTH - START_X, y, linePaint);
        y += 50;

        // 2. VEHICLES (A and B side-by-side)
        int midX = PAGE_WIDTH / 2;
        int colWidth = (PAGE_WIDTH - (START_X * 2) - 60) / 2;
        int yVehiclesStart = y;
        
        int yA = drawVehicleColumn(canvas, "VEHICLE A", report.getVehicleA(), 0, report, START_X, yVehiclesStart, colWidth, textPaint, labelPaint, headerPaint);
        int yB = drawVehicleColumn(canvas, "VEHICLE B", report.getVehicleB(), 1, report, midX + 30, yVehiclesStart, colWidth, textPaint, labelPaint, headerPaint);
        
        y = Math.max(yA, yB) + 60;
        
        // Overflow check
        if (y > PAGE_HEIGHT - 400) {
            pdfDocument.finishPage(page);
            pageNumber++;
            pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
            page = pdfDocument.startPage(pageInfo);
            canvas = page.getCanvas();
            y = START_Y;
        }

        // 3. WITNESSES
        canvas.drawText("3. WITNESSES", START_X, y, subTitlePaint);
        y += 40;
        if (report.getWitnesses() == null || report.getWitnesses().isEmpty()) {
            canvas.drawText("No witnesses recorded.", START_X + 20, y, textPaint);
            y += LINE_HEIGHT;
        } else {
            for (Witness w : report.getWitnesses()) {
                String firstName = w.getFirstName() != null ? w.getFirstName() : "";
                String lastName = w.getLastName() != null ? w.getLastName() : "";
                String name = firstName + " " + lastName;
                String phone = w.getPhone() != null ? "Tel: " + w.getPhone() : "No contact";
                canvas.drawText("• " + name.trim() + " (" + phone + ")", START_X + 20, y, textPaint);
                y += LINE_HEIGHT;
            }
        }
        
        y += 30;
        canvas.drawLine(START_X, y, PAGE_WIDTH - START_X, y, linePaint);
        y += 50;

        // 4. FAULT & SIGNATURES
        canvas.drawText("4. FAULT & SIGNATURES", START_X, y, subTitlePaint);
        y += 40;

        String faultParty = "Not assigned";
        if ("A".equals(report.getAtFaultVehicle())) faultParty = "Vehicle A";
        else if ("B".equals(report.getAtFaultVehicle())) faultParty = "Vehicle B";
        else if ("BOTH".equals(report.getAtFaultVehicle())) faultParty = "Both Vehicles";
        
        drawField(canvas, "At-fault party:", faultParty, START_X, y, textPaint, labelPaint);
        y += 60;

        int sigBoxW = 350;
        int sigBoxH = 180;
        
        canvas.drawText("Signature Vehicle A", START_X, y, labelPaint);
        canvas.drawText("Signature Vehicle B", midX + 30, y, labelPaint);
        y += 15;
        
        drawSignatureBox(canvas, report.getSignatureA(), START_X, y, sigBoxW, sigBoxH);
        drawSignatureBox(canvas, report.getSignatureB(), midX + 30, y, sigBoxW, sigBoxH);
        
        pdfDocument.finishPage(page);

        // 5. PHOTOS
        if (photos != null && !photos.isEmpty()) {
            pageNumber++;
            pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
            page = pdfDocument.startPage(pageInfo);
            canvas = page.getCanvas();
            y = START_Y;

            canvas.drawText("5. ACCIDENT PHOTOS", START_X, y, subTitlePaint);
            y += 60;

            int photoWidth = (PAGE_WIDTH - (START_X * 2) - 40) / 2;
            int col = 0;
            
            for (String photoPath : photos) {
                Bitmap bmp = decodeSampledBitmap(photoPath, 800, 800);
                if (bmp == null) continue;
                
                bmp = rotateBitmapIfRequired(bmp, photoPath);
                
                float ratio = (float) bmp.getWidth() / bmp.getHeight();
                int h = (int) (photoWidth / ratio);
                
                if (y + h > PAGE_HEIGHT - 100) {
                    pdfDocument.finishPage(page);
                    pageNumber++;
                    pageInfo = new PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create();
                    page = pdfDocument.startPage(pageInfo);
                    canvas = page.getCanvas();
                    y = START_Y;
                    col = 0;
                }

                int x = START_X + (col * (photoWidth + 40));
                canvas.drawBitmap(Bitmap.createScaledBitmap(bmp, photoWidth, h, true), x, y, null);
                
                if (col == 1) {
                    y += h + 40;
                    col = 0;
                } else {
                    col = 1;
                }
            }
            pdfDocument.finishPage(page);
        }

        // SAVE FILE
        File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (!downloads.exists()) downloads.mkdirs();
        File file = new File(downloads, "Accident_Report_EN_" + System.currentTimeMillis() + ".pdf");
        FileOutputStream fos = new FileOutputStream(file);
        pdfDocument.writeTo(fos);
        pdfDocument.close();
        fos.close();

        return file;
    }

    private static int drawVehicleColumn(Canvas canvas, String title, VehicleSection v, int vIndex, AccidentReport report, int x, int y, int width, Paint textPaint, Paint labelPaint, Paint headerPaint) {
        int curY = y;
        canvas.drawText(title, x, curY, headerPaint);
        curY += 35;

        // Section: Insured
        canvas.drawText("Insured / Policy Holder", x, curY, textPaint);
        curY += 25;
        drawField(canvas, "Name:", v.insuredName, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        drawField(canvas, "Address:", v.insuredAddress, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        
        curY += 15;
        // Section: Vehicle
        canvas.drawText("Vehicle & Insurance", x, curY, textPaint);
        curY += 25;
        drawField(canvas, "Make/Type:", v.vehicleMakeType, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        drawField(canvas, "Plate No:", v.vehicleRegistration, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        if (v.hasTrailer) {
            drawField(canvas, "Trailer Plate:", v.trailerRegistration, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        }
        drawField(canvas, "Insurer:", v.insuranceName, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        drawField(canvas, "Policy No:", v.policyNumber, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        
        curY += 15;
        // Section: Driver
        canvas.drawText("Driver Information", x, curY, textPaint);
        curY += 25;
        drawField(canvas, "Name:", v.driverName, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        drawField(canvas, "DOB:", v.driverDob, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        drawField(canvas, "License No:", v.licenseNumber, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;
        drawField(canvas, "Contact:", v.driverContact, x + 10, curY, textPaint, labelPaint); curY += LINE_HEIGHT;

        curY += 20;
        // Section: Visual Diagram
        canvas.drawText("Visual Damage Assessment", x, curY, textPaint);
        curY += 20;
        drawDamageDiagram(canvas, x + 10, curY, width - 20, 300, vIndex, report);
        curY += 320;

        // Section: Circumstances
        canvas.drawText("Circumstances", x, curY, textPaint);
        curY += 25;
        List<String> circs = getEnglishCircumstances(v);
        if (circs.isEmpty()) {
            canvas.drawText("No circumstances selected.", x + 10, curY, textPaint);
            curY += LINE_HEIGHT;
        } else {
            for (String s : circs) {
                canvas.drawText("• " + s, x + 10, curY, textPaint);
                curY += LINE_HEIGHT;
            }
        }
        
        return curY;
    }

    private static void drawField(Canvas canvas, String label, String value, int x, int y, Paint textPaint, Paint labelPaint) {
        canvas.drawText(label, x, y, labelPaint);
        float labelW = labelPaint.measureText(label + " ");
        canvas.drawText(value != null && !value.isEmpty() ? value : "---", x + (int)labelW, y, textPaint);
    }

    private static int drawWrappedText(Canvas canvas, String text, int x, int y, int width, Paint paint, int lineHeight) {
        if (text == null) return y;
        String[] words = text.split(" ");
        StringBuilder line = new StringBuilder();
        int curY = y;
        for (String word : words) {
            if (paint.measureText(line.toString() + word) > width) {
                canvas.drawText(line.toString(), x, curY, paint);
                line = new StringBuilder(word + " ");
                curY += lineHeight;
            } else {
                line.append(word).append(" ");
            }
        }
        canvas.drawText(line.toString(), x, curY, paint);
        return curY + lineHeight;
    }

    private static void drawSignatureBox(Canvas canvas, String base64, int x, int y, int w, int h) {
        Paint boxPaint = new Paint();
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setColor(android.graphics.Color.GRAY);
        boxPaint.setStrokeWidth(1f);
        canvas.drawRect(x, y, x + w, y + h, boxPaint);
        
        if (base64 != null && !base64.isEmpty()) {
            try {
                byte[] decoded = Base64.decode(base64, Base64.DEFAULT);
                Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                if (bmp != null) {
                    canvas.drawBitmap(Bitmap.createScaledBitmap(bmp, w - 10, h - 10, true), x + 5, y + 5, null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            textPaint.setTextSize(14);
            textPaint.setColor(android.graphics.Color.LTGRAY);
            textPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("NO SIGNATURE", x + w/2, y + h/2, textPaint);
        }
    }

    private static void drawDamageDiagram(Canvas canvas, int x, int y, int w, int h, int vehicleIndex, AccidentReport report) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3f);
        paint.setColor(android.graphics.Color.BLACK);
        
        // Car Body Outline (Top-down)
        float carW = w * 0.4f;
        float carH = h * 0.8f;
        float carLeft = x + (w - carW) / 2;
        float carTop = y + (h - carH) / 2;
        RectF carRect = new RectF(carLeft, carTop, carLeft + carW, carTop + carH);
        canvas.drawRoundRect(carRect, 40, 40, paint);
        
        // Wheels
        float wheelW = carW * 0.2f;
        float wheelH = carH * 0.15f;
        canvas.drawRect(carLeft - wheelW, carTop + 20, carLeft, carTop + 20 + wheelH, paint);
        canvas.drawRect(carLeft + carW, carTop + 20, carLeft + carW + wheelW, carTop + 20 + wheelH, paint);
        canvas.drawRect(carLeft - wheelW, carTop + carH - 20 - wheelH, carLeft, carTop + carH - 20, paint);
        canvas.drawRect(carLeft + carW, carTop + carH - 20 - wheelH, carLeft + carW + wheelW, carTop + carH - 20, paint);
        
        // Labels
        Paint labelPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        labelPaint.setTextSize(14);
        labelPaint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText("FRONT", carLeft + carW/2, carTop - 10, labelPaint);
        
        // Damage markers from the data
        Paint markerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        markerPaint.setColor(android.graphics.Color.RED);
        markerPaint.setStyle(Paint.Style.FILL);
        markerPaint.setAlpha(180);
        
        if (report.getDamages() != null) {
            for (Damage d : report.getDamages()) {
                if (d.getVehicleTarget() == vehicleIndex) {
                    PointF p = getPointForZone(d.getArea(), carLeft, carTop, carW, carH);
                    if (p != null) {
                        canvas.drawCircle(p.x, p.y, 15, markerPaint);
                    }
                }
            }
        }
    }

    private static PointF getPointForZone(String zone, float left, float top, float w, float h) {
        if (zone == null) return null;
        float cx = left + w/2;
        float cy = top + h/2;
        
        // Match string names to physical coordinates on the diagram
        // Normalizes input to handle both English and Lithuanian data entry
        String z = zone.toLowerCase();
        if (z.contains("front") || z.contains("priekis")) {
            if (z.contains("left") || z.contains("kairė")) return new PointF(left + 10, top + 50);
            if (z.contains("right") || z.contains("dešinė")) return new PointF(left + w - 10, top + 50);
            return new PointF(cx, top + 20);
        }
        if (z.contains("rear") || z.contains("galas")) {
            if (z.contains("left") || z.contains("kairė")) return new PointF(left + 10, top + h - 50);
            if (z.contains("right") || z.contains("dešinė")) return new PointF(left + w - 10, top + h - 50);
            return new PointF(cx, top + h - 20);
        }
        if (z.contains("left") || z.contains("kairė")) return new PointF(left - 5, cy);
        if (z.contains("right") || z.contains("dešinė")) return new PointF(left + w + 5, cy);
        
        return new PointF(cx, cy);
    }

    private static List<String> getEnglishCircumstances(VehicleSection v) {
        List<String> list = new ArrayList<>();
        if (v.isParkedStopped) list.add("Parked/Stopped");
        if (v.isLeavingParking) list.add("Leaving parking space");
        if (v.isEnteringParking) list.add("Entering parking space");
        if (v.isReversing) list.add("Reversing");
        if (v.isOpeningDoor) list.add("Opening door");
        if (v.isStopping) list.add("Stopping");
        if (v.isStartingOff) list.add("Starting off");
        if (v.isEnteringRoundabout) list.add("Entering roundabout");
        if (v.isCirculatingRoundabout) list.add("Circulating roundabout");
        if (v.isRearEndSameDirection) list.add("Striking rear-end");
        if (v.isChangingLanes) list.add("Changing lanes");
        if (v.isOvertaking) list.add("Overtaking");
        if (v.isTurningRight) list.add("Turning right");
        if (v.isTurningLeft) list.add("Turning left");
        if (v.isEnteringOppositelane) list.add("Entering opposite lane");
        if (v.isFromRightAtIntersection) list.add("Coming from right");
        if (v.isFailedToPrioritize) list.add("Failed to give way");
        return list;
    }

    private static Bitmap decodeSampledBitmap(String path, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        
        int inSampleSize = 1;
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    private static Bitmap rotateBitmapIfRequired(Bitmap img, String path) {
        try {
            android.media.ExifInterface ei = new android.media.ExifInterface(path);
            int orientation = ei.getAttributeInt(android.media.ExifInterface.TAG_ORIENTATION, android.media.ExifInterface.ORIENTATION_NORMAL);
            android.graphics.Matrix matrix = new android.graphics.Matrix();
            switch (orientation) {
                case android.media.ExifInterface.ORIENTATION_ROTATE_90: matrix.postRotate(90); break;
                case android.media.ExifInterface.ORIENTATION_ROTATE_180: matrix.postRotate(180); break;
                case android.media.ExifInterface.ORIENTATION_ROTATE_270: matrix.postRotate(270); break;
                default: return img;
            }
            Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
            img.recycle();
            return rotatedImg;
        } catch (IOException e) { return img; }
    }

    private static Bitmap rotateImage(Bitmap img, int degree) {
        android.graphics.Matrix matrix = new android.graphics.Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }
}
