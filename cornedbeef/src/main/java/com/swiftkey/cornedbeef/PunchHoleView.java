package com.swiftkey.cornedbeef;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.support.annotation.VisibleForTesting;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * The helper view for the punch hole and listeners.
 *
 * Created by DongyiChun on 2/5/16.
 */
public class PunchHoleView extends LinearLayout {

    // Helpers to punch a hole
    private final Paint mPaint;

    private int mCircleCenterX;
    private int mCircleCenterY;
    private float mCircleRadius;
    private Rect mRect; // Contains target view's rect

    private View.OnClickListener mPunchHoleClickListener;
    private View.OnClickListener mGlobalClickListener;

    public PunchHoleView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Punch a hole to target (x, y) position with given radius.
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("PunchHoleView", String.format("Circle(%d, %d, %f)", mCircleCenterX, mCircleCenterY, mCircleRadius));
        Log.d("PunchHoleView", "onTouchEvent() " + event);
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_UP:
                if (mRect.contains((int) event.getX(), (int) event.getY())) {
                    if (mPunchHoleClickListener != null) {
                        mPunchHoleClickListener.onClick(this);
                        return true;
                    }
                } else {
                    if (mGlobalClickListener != null) {
                        mGlobalClickListener.onClick(this);
                        return true;
                    }
                }
                return false;
            default:
                return false;
        }
    }
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        Log.e("PunchHoleView", Log.getStackTraceString(new Exception()));
//        Log.e("PunchHoleView", String.format("onLayout(%s, %d, %d, %d, %d)", String.valueOf(changed), l, t, r, b));
//        super.onLayout(changed, l, t, r, b);
//    }

    public boolean setCircle(int centerX, int centerY, float radius) {
        boolean changed = false;
        changed |= setCircleCenterX(centerX);
        changed |= setCircleCenterY(centerY);
        changed |= setCircleRadius(radius);

        if (!changed) {
            return false;
        }

        mRect = new Rect(
                centerX - (int) radius, centerY - (int) radius,
                centerX + (int) radius, centerY + (int) radius);

        return true;
    }

    public boolean setCircleCenterX(int centerX) {
        if (this.mCircleCenterX != centerX) {
            this.mCircleCenterX = centerX;
            return true;
        } else {
            return false;
        }
    }

    public boolean setCircleCenterY(int centerY) {
        if (this.mCircleCenterY != centerY) {
            this.mCircleCenterY = centerY;
            return true;
        } else {
            return false;
        }
    }

    public boolean setCircleRadius(float radius) {
        if (this.mCircleRadius != radius) {
            this.mCircleRadius = radius;
            return true;
        } else {
            return false;
        }
    }

    @VisibleForTesting
    public int getCircleCenterX() {
        return mCircleCenterX;
    }

    @VisibleForTesting
    public int getCircleCenterY() {
        return mCircleCenterY;
    }

    @VisibleForTesting
    public float getCircleRadius() {
        return mCircleRadius;
    }

    public void setOnTargetClickListener(OnClickListener listener) {
        this.mPunchHoleClickListener = listener;
    }

    public void setOnGlobalClickListener(OnClickListener listener) {
        this.mGlobalClickListener = listener;
    }
}
