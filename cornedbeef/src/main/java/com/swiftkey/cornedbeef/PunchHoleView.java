package com.swiftkey.cornedbeef;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

/**
 * The helper view for the punch hole and listeners.
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
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // Punch a hole to target (x, y) position with given radius.
        canvas.drawCircle(mCircleCenterX, mCircleCenterY, mCircleRadius, mPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
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


    /**
     * Set the punch hole's coordinates and radius
     *
     * @param centerX circle's x coordinate
     * @param centerY circle's y coordinate
     * @param radius circle's radius
     * @return true if value is changed
     */
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

        postInvalidate();

        return true;
    }

    /**
     * Set the punch hole's x coordinate.
     *
     * This needs to be public to do the horizontal translation animation.
     *
     * @param centerX circle's x coordinate
     * @return true if value is changed
     */
    public boolean setCircleCenterX(int centerX) {
        if (this.mCircleCenterX != centerX) {
            this.mCircleCenterX = centerX;
            postInvalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the punch hole's y coordinate
     *
     * @param centerY circle's y coordinate
     * @return true if value is changed
     */
    private boolean setCircleCenterY(int centerY) {
        if (this.mCircleCenterY != centerY) {
            this.mCircleCenterY = centerY;
            postInvalidate();
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the punch hole's radius
     *
     * @param radius circle's radius
     * @return true if value is changed
     */
    private boolean setCircleRadius(float radius) {
        if (this.mCircleRadius != radius) {
            this.mCircleRadius = radius;
            postInvalidate();
            return true;
        } else {
            return false;
        }
    }

    public void setOnTargetClickListener(OnClickListener listener) {
        this.mPunchHoleClickListener = listener;
    }

    public void setOnGlobalClickListener(OnClickListener listener) {
        this.mGlobalClickListener = listener;
    }
}
