package com.example.accidentreportingapp.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom view for capturing signatures.
 */
public class SignatureView extends View {

    private final Path drawPath = new Path();
    private final Paint drawPaint = new Paint();
    private final List<Path> paths = new ArrayList<>();

    public SignatureView(Context context) {
        super(context);
        init();
    }

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        drawPaint.setColor(Color.WHITE);
        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(4f);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        for (Path path : paths) {
            canvas.drawPath(path, drawPaint);
        }
        canvas.drawPath(drawPath, drawPaint);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float touchX = event.getX();
        float touchY = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                getParent().requestDisallowInterceptTouchEvent(true);
                performClick();
                drawPath.moveTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                drawPath.lineTo(touchX, touchY);
                break;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(true);
                paths.add(new Path(drawPath));
                drawPath.reset();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public boolean isEmpty() {
        return paths.isEmpty();
    }

    public void clear() {
        paths.clear();
        drawPath.reset();
        invalidate();
    }

    /**
     * Converts the current signature to a Base64 encoded PNG string.
     * Note: This performs image compression which can be slow.
     */
    @SuppressLint("WrongThread")
    public String toBase64Png() {
        int width = getWidth();
        int height = getHeight();

        //if (width <= 0 || height <= 0) return "";
        width = width > 0 ? width : 400;
        height = height > 0 ? height : 200;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(android.graphics.Color.WHITE);

        int originalColor = drawPaint.getColor();
        drawPaint.setColor(android.graphics.Color.BLACK);

        // Draw the current signature paths onto the bitmap
        for (Path path : paths) {
            canvas.drawPath(path, drawPaint);
        }

        drawPaint.setColor(originalColor);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // PNG is a lossless format, so quality 100 is standard.
        // We use @SuppressLint("WrongThread") because this is currently called from the UI thread during step saving.
        boolean success = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);

        if (!success) {
            bitmap.recycle();
            return "";
        }

        byte[] bytes = outputStream.toByteArray();
        String result = Base64.encodeToString(bytes, Base64.DEFAULT);

        bitmap.recycle();
        return result;
    }
}
