package com.example.accidentreportingapp.controllers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.os.Environment;

import com.example.accidentreportingapp.models.AccidentReport;
import com.example.accidentreportingapp.models.VehicleSection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PdfReportGenerator {

    private static final int PAGE_WIDTH = 1200;
    private static final int PAGE_HEIGHT = 1800;

    private static final int START_X = 40;
    private static final int START_Y = 60;

    private static final int LINE_HEIGHT = 30;

    public static File generate(Context context,
                                AccidentReport report,
                                List<String> photos) throws IOException {

        PdfDocument pdfDocument = new PdfDocument();

        Paint titlePaint = new Paint();
        titlePaint.setTextSize(32);
        titlePaint.setFakeBoldText(true);

        Paint textPaint = new Paint();
        textPaint.setTextSize(20);

        int pageNumber = 1;

        int columnGap = 40;

        int columnWidth = (PAGE_WIDTH - (START_X * 2) - columnGap) / 2;

        int leftX = START_X;
        int rightX = START_X + columnWidth + columnGap;

        PdfDocument.PageInfo pageInfo =
                new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        pageNumber
                ).create();

        PdfDocument.Page page = pdfDocument.startPage(pageInfo);

        Canvas canvas = page.getCanvas();

        Paint linePaint = new Paint();
        linePaint.setColor(android.graphics.Color.GRAY);
        linePaint.setStrokeWidth(2f);

        int y = START_Y;

        // TITLE
        canvas.drawText("Accident Report", START_X, y, titlePaint);

        y += 60;

        canvas.drawLine(START_X, y, PAGE_WIDTH - START_X, y, linePaint);

        y += 60;

        // GENERAL INFO
        canvas.drawText("Location: " + report.getLocation(), START_X, y, textPaint);

        y += LINE_HEIGHT;

        canvas.drawText("Description: " + report.getDescription(), START_X, y, textPaint);

        y += LINE_HEIGHT;

        canvas.drawText("Date: " + report.getTimestampAsDate(), START_X, y, textPaint);

        y += 60;

        canvas.drawLine(START_X, y, PAGE_WIDTH - START_X, y, linePaint);

        y += 60;

        int startY = y;

        int yA = drawVehicleSectionColumn(
                canvas,
                "Vehicle A",
                report.getVehicleA(),
                leftX,
                startY,
                columnWidth,
                textPaint,
                titlePaint
        );

        int yB = drawVehicleSectionColumn(
                canvas,
                "Vehicle B",
                report.getVehicleB(),
                rightX,
                startY,
                columnWidth,
                textPaint,
                titlePaint
        );

        y = Math.max(yA, yB) + 60;

        canvas.drawLine(START_X, y, PAGE_WIDTH - START_X, y, linePaint);

        // photos in new page
        pdfDocument.finishPage(page);

        pageNumber++;

        pageInfo = new PdfDocument.PageInfo.Builder(
                PAGE_WIDTH,
                PAGE_HEIGHT,
                pageNumber
        ).create();

        page = pdfDocument.startPage(pageInfo);
        canvas = page.getCanvas();

        y = START_Y;


        // PHOTOS TITLE
        canvas.drawText("Photos", START_X, y, titlePaint);

        y += 40;

        // DRAW PHOTOS
        int column = 0;

        int photoWidth = (PAGE_WIDTH - (START_X * 3)) / 2; // 2 columns + spacing
        int maxPhotoHeight = 500;

        float ratio;
        Bitmap bitmap;
        Bitmap scaledBitmap;

        for (String photoPath : photos) {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;

            bitmap = BitmapFactory.decodeFile(photoPath, options);
            if (bitmap == null) continue;

            // FIX ORIENTATION
            try {
                android.media.ExifInterface exif =
                        new android.media.ExifInterface(photoPath);

                int orientation = exif.getAttributeInt(
                        android.media.ExifInterface.TAG_ORIENTATION,
                        android.media.ExifInterface.ORIENTATION_NORMAL
                );

                int rotation = 0;

                if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_90)
                    rotation = 90;
                else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_180)
                    rotation = 180;
                else if (orientation == android.media.ExifInterface.ORIENTATION_ROTATE_270)
                    rotation = 270;

                if (rotation != 0) {
                    android.graphics.Matrix matrix = new android.graphics.Matrix();
                    matrix.postRotate(rotation);

                    bitmap = Bitmap.createBitmap(
                            bitmap, 0, 0,
                            bitmap.getWidth(),
                            bitmap.getHeight(),
                            matrix,
                            true
                    );
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            // SCALE TO FIT COLUMN
            ratio = (float) bitmap.getWidth() / bitmap.getHeight();

            int drawWidth = photoWidth;
            int drawHeight = (int) (drawWidth / ratio);

            if (drawHeight > maxPhotoHeight) {
                drawHeight = maxPhotoHeight;
                drawWidth = (int) (drawHeight * ratio);
            }

            scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    drawWidth,
                    drawHeight,
                    true
            );

            if (y + drawHeight > PAGE_HEIGHT - 100) {

                pdfDocument.finishPage(page);

                pageNumber++;

                pageInfo = new PdfDocument.PageInfo.Builder(
                        PAGE_WIDTH,
                        PAGE_HEIGHT,
                        pageNumber
                ).create();

                page = pdfDocument.startPage(pageInfo);
                canvas = page.getCanvas();

                y = START_Y;
                column = 0;
            }

            // X POSITION (2 columns)
            int x;

            if (column == 0) {
                x = START_X;
            } else {
                x = START_X + photoWidth + START_X;
            }

            canvas.drawBitmap(scaledBitmap, x, y, null);

            // move column
            if (column == 1) {
                y += drawHeight + 40;
                column = 0;
            } else {
                column = 1;
            }
        }

        pdfDocument.finishPage(page);

        // SAVE FILE
        File downloadsFolder =
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DOWNLOADS
                );

        if (!downloadsFolder.exists()) {
            downloadsFolder.mkdirs();
        }

        String fileName =
                "AccidentReport_" + System.currentTimeMillis() + ".pdf";

        File pdfFile = new File(
                downloadsFolder,
                fileName
        );

        FileOutputStream fos = new FileOutputStream(pdfFile);

        pdfDocument.writeTo(fos);

        pdfDocument.close();

        fos.close();

        return pdfFile;
    }

    private static int drawVehicleSectionColumn(
            Canvas canvas,
            String title,
            VehicleSection vehicle,
            int x,
            int y,
            int width,
            Paint textPaint,
            Paint titlePaint
    ) {

        int lineY = y;

        canvas.drawText(title, x, lineY, titlePaint);
        lineY += 40;


        canvas.drawText("Owner: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.insuredName, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("Address: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.insuredAddress, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("Vehicle: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.vehicleMakeType, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("Plate: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.vehicleRegistration, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("Insurance: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.insuranceName, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("Policy: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.policyNumber, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("Driver: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.driverName, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("DOB: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.driverDob, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        canvas.drawText("License: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(vehicle.licenseNumber, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);
        lineY += LINE_HEIGHT*1.5;

        String circumstances = "";

        if (vehicle.isParkedStopped) circumstances += "Parked ";
        if (vehicle.isLeavingParking) circumstances += "Leaving ";
        if (vehicle.isReversing) circumstances += "Reversing ";

        canvas.drawText("Circumstances: ", x, lineY, textPaint);
        lineY += LINE_HEIGHT;
        textPaint.setFakeBoldText(true);
        canvas.drawText(circumstances, x, lineY, textPaint);
        textPaint.setFakeBoldText(false);

        return lineY;
    }
}
