package com.example.accidentreportingapp.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.example.accidentreportingapp.R;

import java.util.ArrayList;
import java.util.List;

public class DamageDiagramView extends View {

    private final List<PointF> damagedZones = new ArrayList<>();
    private PointF impactArrow = null;

    private final Paint damagePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint damageBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arrowHeadPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path headPath = new Path();

    private static final float TOUCH_TOLERANCE = 80f; // Pixels for hit testing

    public DamageDiagramView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        damagePaint.setColor(Color.argb(180, 255, 20, 147)); // Deep pink
        damagePaint.setStyle(Paint.Style.FILL);

        damageBorderPaint.setStyle(Paint.Style.STROKE);
        damageBorderPaint.setStrokeWidth(4f);
        damageBorderPaint.setColor(Color.WHITE);

        arrowPaint.setColor(Color.RED); 
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setStrokeWidth(16f);
        arrowPaint.setStrokeCap(Paint.Cap.ROUND);

        arrowHeadPaint.setColor(Color.RED);
        arrowHeadPaint.setStyle(Paint.Style.FILL);

        outlinePaint.setColor(Color.DKGRAY);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(8f);

        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(48f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setFakeBoldText(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) return;

        // Draw simple car outline (top-down)
        float carWidth = w * 0.5f;
        float carHeight = h * 0.7f;
        float left = (w - carWidth) / 2;
        float top = (h - carHeight) / 2;
        float right = left + carWidth;
        float bottom = top + carHeight;

        // Body
        canvas.drawRoundRect(left, top, right, bottom, 70, 70, outlinePaint);

        // Wheels
        float wheelW = carWidth * 0.18f;
        float wheelH = carHeight * 0.12f;
        float wheelOffset = carHeight * 0.12f;
        
        canvas.drawRect(left - wheelW, top + wheelOffset, left, top + wheelOffset + wheelH, outlinePaint);
        canvas.drawRect(right, top + wheelOffset, right + wheelW, top + wheelOffset + wheelH, outlinePaint);
        canvas.drawRect(left - wheelW, bottom - wheelOffset - wheelH, left, bottom - wheelOffset, outlinePaint);
        canvas.drawRect(right, bottom - wheelOffset - wheelH, right + wheelW, bottom - wheelOffset, outlinePaint);

        // Labels
        canvas.drawText(getContext().getString(R.string.label_front), w / 2f, top - 40, textPaint);
        canvas.drawText(getContext().getString(R.string.label_rear), w / 2f, bottom + 70, textPaint);

        // Draw damaged zones
        for (PointF p : damagedZones) {
            float cx = p.x * w;
            float cy = p.y * h;
            canvas.drawCircle(cx, cy, 40, damagePaint);
            canvas.drawCircle(cx, cy, 40, damageBorderPaint);
        }

        // Draw impact arrow LAST so it's on top
        if (impactArrow != null) {
            drawImpactArrow(canvas, impactArrow.x * w, impactArrow.y * h);
        }
    }

    private void drawImpactArrow(Canvas canvas, float x, float y) {
        float arrowLen = 120f;
        float headSize = 50f;
        
        // Pointing towards the car center
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        
        float dx = x - centerX;
        float dy = y - centerY;
        float mag = (float) Math.hypot(dx, dy);
        
        // If center is tapped, point down from top
        if (mag < 10) {
            dx = 0;
            dy = -1;
            mag = 1;
        }
        
        float ux = dx / mag;
        float uy = dy / mag;
        
        float startX = x + ux * arrowLen;
        float startY = y + uy * arrowLen;
        
        // Draw stem
        canvas.drawLine(startX, startY, x, y, arrowPaint);
        
        // Draw head at tip (x,y)
        headPath.reset();
        headPath.moveTo(x, y);
        
        float backX = ux * headSize;
        float backY = uy * headSize;
        float perpX = -uy * (headSize / 2f);
        float perpY = ux * (headSize / 2f);
        
        headPath.lineTo(x + backX + perpX, y + backY + perpY);
        headPath.lineTo(x + backX - perpX, y + backY - perpY);
        headPath.close();
        
        canvas.drawPath(headPath, arrowHeadPaint);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            performClick();
            float x = event.getX();
            float y = event.getY();
            float nx = x / getWidth();
            float ny = y / getHeight();

            // 1. Try to remove existing damage (Check newest first)
            for (int i = damagedZones.size() - 1; i >= 0; i--) {
                PointF p = damagedZones.get(i);
                if (Math.hypot(p.x * getWidth() - x, p.y * getHeight() - y) < TOUCH_TOLERANCE) {
                    damagedZones.remove(i);
                    invalidate();
                    return true;
                }
            }

            // 2. Try to remove arrow
            if (impactArrow != null) {
                if (Math.hypot(impactArrow.x * getWidth() - x, impactArrow.y * getHeight() - y) < TOUCH_TOLERANCE) {
                    impactArrow = null;
                    invalidate();
                    return true;
                }
            }

            // 3. Add new indicator
            if (impactArrow == null) {
                // First interaction sets the impact arrow
                impactArrow = new PointF(nx, ny);
            } else {
                // Subsequent interactions add damage zones
                damagedZones.add(new PointF(nx, ny));
            }

            invalidate();
        }
        return true;
    }

    public List<String> getDamagedZoneDescriptions() {
        List<String> descriptions = new ArrayList<>();
        for (PointF p : damagedZones) {
            String zone = getZoneName(p.x, p.y);
            if (!descriptions.contains(zone)) {
                descriptions.add(zone);
            }
        }
        return descriptions;
    }

    private String getZoneName(float x, float y) {
        Context c = getContext();
        if (y < 0.33) {
            if (x < 0.33) return c.getString(R.string.zone_front_left);
            if (x > 0.66) return c.getString(R.string.zone_front_right);
            return c.getString(R.string.zone_front_center);
        } else if (y > 0.66) {
            if (x < 0.33) return c.getString(R.string.zone_rear_left);
            if (x > 0.66) return c.getString(R.string.zone_rear_right);
            return c.getString(R.string.zone_rear_center);
        } else {
            if (x < 0.33) return c.getString(R.string.zone_left_side);
            if (x > 0.66) return c.getString(R.string.zone_right_side);
            return c.getString(R.string.zone_center);
        }
    }

    public void clearZones() {
        damagedZones.clear();
        impactArrow = null;
        invalidate();
    }
}
