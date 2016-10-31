package com.swiftkey.cornedbeef;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.StyleRes;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.PopupWindow;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A CoachMark is a temporary popup that can be positioned above a {@link View}
 * to notify the user about a new feature, proposition or other information.
 * 
 * CoachMarks are dismissed in two ways: 
 *  1) A pre-set timeout passed 
 *  2) The {@link CoachMark#dismiss()} method is called
 * 
 * Coach marks can be very annoying to the user, SO PLEASE USE SPARINGLY!
 * 
 * @author lachie
 * 
 */
public abstract class CoachMark {

    @IntDef({COACHMARK_PUNCHHOLE, COACHMARK_LAYERED, COACHMARK_HIGHLIGHT, COACHMARK_BUBBLE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CoachmarkType {}

    public static final int COACHMARK_PUNCHHOLE = 0;
    public static final int COACHMARK_LAYERED = 1;
    public static final int COACHMARK_HIGHLIGHT = 2;
    public static final int COACHMARK_BUBBLE = 3;

    public static final int NO_ANIMATION = 0;

    /**
     * Interface used to allow the creator of a coach mark to run some code when the
     * coach mark is dismissed.
     */
    public interface OnDismissListener {
        /**
         * This method will be invoked when the coach mark is dismissed.
         */
        void onDismiss();
    }

    /**
     * Interface used to allow the creator of a coach mark to run some code when the
     * coach mark is shown.
     */
    public interface OnShowListener {
        /**
         * This method will be invoked when the coach mark is shown.
         */
        void onShow();
    }

    /**
     * Interface used to allow the creator of a coach mark to run some code when the
     * coach mark's given timeout is expired.
     */
    public interface OnTimeoutListener {
        /**
         * This method will be invoked when the coach mark's given timeout is expired.
         */
        void onTimeout();
    }
    
    protected final PopupWindow mPopup;
    protected final Context mContext;
    protected final View mTokenView;
    protected final View mAnchor;
    protected final int mPadding;

    private final OnPreDrawListener mPreDrawListener;
    private final OnDismissListener mDismissListener;
    private final OnShowListener mShowListener;
    private final OnTimeoutListener mTimeoutListener;
    private final long mTimeoutInMs;

    private Runnable mTimeoutDismissRunnable;

    protected Rect mDisplayFrame;
    
    protected CoachMark(CoachMarkBuilder builder) {
        mAnchor = builder.anchor;
        mContext = builder.context;
        mTimeoutInMs = builder.timeout;
        mDismissListener = builder.dismissListener;
        mShowListener = builder.showListener;
        mTimeoutListener = builder.timeoutListener;
        mTokenView = builder.tokenView != null ? builder.tokenView : mAnchor;
        mPadding = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, builder.padding, 
                mContext.getResources().getDisplayMetrics());

        // Create the coach mark view
        View view = createContentView(builder.content);
        
        // Create and initialise the PopupWindow
        mPopup = createNewPopupWindow(view);
        
        mPopup.setAnimationStyle(builder.animationStyle);
        mPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NOT_NEEDED);
        mPopup.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        mPreDrawListener = new CoachMarkPreDrawListener();
    }
    
    /**
     * Create the coach mark view
     */
    protected abstract View createContentView(View content);
    
    /**
     * Create and initialise a new {@link PopupWindow}
     */
    protected abstract PopupWindow createNewPopupWindow(View contentView);
    
    /**
     * Get the dimensions of the anchor view
     */
    protected abstract CoachMarkDimens<Integer> getAnchorDimens();
    
    /**
     * Get the current dimensions of the popup window
     */
    protected abstract CoachMarkDimens<Integer> getPopupDimens(CoachMarkDimens<Integer> anchorDimens);
    
    /**
     * Perform any necessary updates to the view when popupDimens or anchorDimens have changed
     */
    protected abstract void updateView(CoachMarkDimens<Integer> popupDimens, CoachMarkDimens<Integer> anchorDimens);
    
    /**
     * Show the coach mark and start listening for changes to the anchor view
     */
    public void show() {
        // It is assumed that the displayFrame will not change for as long as
        // the coach mark is visible - otherwise, the positioning may be off
        mDisplayFrame = getDisplayFrame(mAnchor);
        final CoachMarkDimens<Integer> anchorDimens = getAnchorDimens();
        final CoachMarkDimens<Integer> popupDimens = getPopupDimens(anchorDimens);
        updateView(popupDimens, anchorDimens);

        // Dismiss coach mark after the timeout has passed if it is greater than 0.
        if (mTimeoutInMs > 0) {
            mTimeoutDismissRunnable = new Runnable() {
                @Override
                public void run() {
                    if(mPopup.isShowing()) {
                        if (mTimeoutListener != null) {
                            mTimeoutListener.onTimeout();
                        }
                        dismiss();
                    }
                }
            };
            getContentView().postDelayed(mTimeoutDismissRunnable, mTimeoutInMs);
        }

        mPopup.setWidth(popupDimens.width);
        mPopup.showAtLocation(mTokenView, Gravity.NO_GRAVITY, popupDimens.x, popupDimens.y);

        mAnchor.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
        if (mShowListener != null) {
            mShowListener.onShow();
        }
    }

    /**
     * Dismiss the coach mark and stop listening for changes to the anchor view
     */
    public void dismiss() {
        mAnchor.destroyDrawingCache();
        mAnchor.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
        mPopup.getContentView().removeCallbacks(mTimeoutDismissRunnable);
        mPopup.dismiss();

        if (mDismissListener != null) {
            mDismissListener.onDismiss();
        }
    }

    /**
     * Exposes the {@link PopupWindow#getContentView()} method of {@link CoachMark#mPopup}
     */
    public View getContentView() {
        return mPopup.getContentView();
    }
    
    /**
     * Exposes the {@link PopupWindow#isShowing()} method of {@link CoachMark#mPopup}
     */
    public boolean isShowing() {
        return mPopup.isShowing();
    }
    
    /**
     * Get the visible display size of the window this view is attached to
     */
    private static Rect getDisplayFrame(View view) {
        final Rect displayFrame = new Rect();
        view.getWindowVisibleDisplayFrame(displayFrame);
        return displayFrame;
    }
    
    /**
     * Listener which is used to update the position of the coach mark when the
     * position of the anchor view is about to change
     */
    private class CoachMarkPreDrawListener implements OnPreDrawListener {
        
        @Override
        public boolean onPreDraw() {
            if(mAnchor != null && mAnchor.isShown()) {
                CoachMarkDimens<Integer> anchorDimens = getAnchorDimens();
                CoachMarkDimens<Integer> popupDimens = getPopupDimens(anchorDimens);
                updateView(popupDimens, anchorDimens);
                mPopup.update(popupDimens.x, popupDimens.y, popupDimens.width , popupDimens.height);
            } else {
                dismiss();
            }        
            return true;
        }
    }
    
    /**
     * Listener may be used to dismiss the coach mark when it is touched
     */
    protected class CoachMarkOnTouchListener implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                dismiss();
            case MotionEvent.ACTION_DOWN:
                return true;
            default:
                return false;
            }
        }
    }

    /**
     * An {@link android.view.View.OnClickListener} which wraps an
     * existing listener with a call to {@link CoachMark#dismiss()}
     *
     * @author lachie
     *
     */
    protected class CoachMarkOnClickListener implements View.OnClickListener {

        private final View.OnClickListener mListener;

        public CoachMarkOnClickListener(View.OnClickListener listener) {
            mListener = listener;
        }

        @Override
        public void onClick(View v) {
            dismiss();

            if (mListener != null) {
                mListener.onClick(v);
            }
        }
    }
    
    public static class CoachMarkDimens<T extends Number> {
        public final T width;
        public final T height;
        public final T x;
        public final T y;
                
        public CoachMarkDimens(T x, T y, T width, T height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
        public Point getPos() {
            return new Point(x.intValue(), y.intValue());
        }
    }
    
    public abstract static class CoachMarkBuilder {
        
        // Required parameters
        protected Context context;
        protected View anchor;
        protected View content;
        
        // Optional parameters with default values
        protected long timeout = 10000;
        protected OnDismissListener dismissListener;
        protected int padding = 0;
        protected int animationStyle = R.style.CoachMarkAnimation;
        protected View tokenView;
        protected OnShowListener showListener;
        protected OnTimeoutListener timeoutListener;

        public CoachMarkBuilder(Context context, View anchor, String message) {
            this(context, anchor, new TextView(context));
            ((TextView) content).setTextColor(Color.WHITE);
            ((TextView) content).setText(message);
        }

        public CoachMarkBuilder(Context context, View anchor, @LayoutRes int contentResId) {
            this(context, anchor, LayoutInflater.from(context).inflate(contentResId, null));
        }
        
        public CoachMarkBuilder(Context context, View anchor, View content) {
            this.context = context;
            this.anchor = anchor;
            this.content = content;            
        }
        
        /**
         * If the desired anchor view does not contain a valid window token then
         * the token of an alternative view may be used to display the coach mark
         * 
         * @param tokenView
         *      the view who's window token should be used
         */
        public CoachMarkBuilder setTokenView(View tokenView) {
            this.tokenView = tokenView;
            return this;
        }
       
        /**
         * Set the period of time after which the coach mark should be
         * automatically dismissed
         * 
         * @param timeoutInMs
         *      the time in milliseconds after which to dismiss the coach
         *      mark (defaults to 10 seconds)
         */
        public CoachMarkBuilder setTimeout(long timeoutInMs) {
            this.timeout = timeoutInMs;
            return this;
        }
        
        /**
         * Set how much padding there should be between the left and right edges
         * of the coach mark and the screen
         * 
         * @param padding
         *      the amount of left/right padding in px
         */
        public CoachMarkBuilder setPadding(int padding) {
            this.padding = padding;
            return this;
        }
        
        /**
         * Set an {@link CoachMark.OnDismissListener} to be called when the
         * coach mark is dismissed
         * 
         * @param listener
         */
        public CoachMarkBuilder setOnDismissListener(OnDismissListener listener) {
            this.dismissListener = listener;
            return this;
        }

        /**
         * Set an {@link CoachMark.OnTimeoutListener} to be called when the
         * coach mark's display timeout has been expired
         *
         * This listener will be called before the coach mark dismissed
         *
         * @param listener the timeout listener
         */
        public CoachMarkBuilder setOnTimeoutListener(OnTimeoutListener listener) {
            this.timeoutListener = listener;
            return this;
        }

        /**
         * Set which animation will be used when displaying/hiding the coach mark
         * 
         * @param animationStyle
         *      the resource ID of the Style to be used for showing and hiding the coach mark
         */
        public CoachMarkBuilder setAnimation(@StyleRes int animationStyle) {
            this.animationStyle = animationStyle;
            return this;
        }
        
        /**
         * Set an {@link CoachMark.OnShowListener} to be called when the
         * coach mark is shown
         * 
         * @param listener
         */
        public CoachMarkBuilder setOnShowListener(OnShowListener listener) {
            this.showListener = listener;
            return this;
        }

        public abstract CoachMark build();
    }
}
