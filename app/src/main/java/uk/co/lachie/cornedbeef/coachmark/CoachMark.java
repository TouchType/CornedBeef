package uk.co.lachie.cornedbeef.coachmark;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.PopupWindow;
import android.widget.TextView;

import uk.co.lachie.cornedbeef.R;

/**
 * A CoachMark is a temporary popup that can be positioned above a {@link View}
 * to notify the user about a new feature, proposition or other information.
 * 
 * CoachMarks are dismissed in two ways: 
 *  1) A pre-set timeout passed 
 *  2) The {@link CoachMark#dismiss(CoachMarkUserResponse response)} method is called
 * 
 * Coach marks can be very annoying to the user, SO PLEASE USE SPARINGLY!
 * 
 * @author lachie
 * 
 */
public abstract class CoachMark {

    // ways the users can respond to the coach marks
    public enum CoachMarkUserResponse {
        POSITIVE,
        NEUTRAL,
        NEGATIVE,
        TIMEOUT,
        OTHER
    }
    
    public static final int NO_ANIMATION = 0;
    
    public interface OnDismissListener {
        void onDismiss();
    }
    
    public interface OnShowListener{
        void onShow();
    }
    
    protected final PopupWindow mPopup;
    protected final Context mContext;
    protected final View mTokenView;
    protected final View mAnchor;
    protected final int mPadding;

    private final OnPreDrawListener mPreDrawListener;
    private final OnDismissListener mDismissListener;
    private final OnShowListener mShowListener;
    private final long mTimeout;
    
    protected Rect mDisplayFrame;
    
    protected CoachMark(CoachMarkBuilder builder) {
        mAnchor = builder.anchor;
        mContext = builder.context;
        mTimeout = builder.timeout;
        mDismissListener = builder.dismissListener;
        mShowListener = builder.showListener;
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
        
        // Dismiss coach mark after the timeout has passed
        getContentView().postDelayed(new Runnable() {
            
            @Override
            public void run() {
                if(mPopup.isShowing()) {
                    dismiss(CoachMarkUserResponse.TIMEOUT);
                }
            }
        }, mTimeout);

        mPopup.setWidth(popupDimens.width);
        mPopup.showAtLocation(mTokenView, Gravity.NO_GRAVITY, popupDimens.x, popupDimens.y);
        
        mAnchor.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
        if (mShowListener != null) {
            mShowListener.onShow();
        }
    }

    /**
     * Dismiss the coach mark and stop listening for changes to the anchor view
     * @param userResponse  how the coach mark was dismissed
     */
    public void dismiss(CoachMarkUserResponse userResponse) {
        mAnchor.destroyDrawingCache();
        mAnchor.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
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
                dismiss(CoachMarkUserResponse.OTHER);
            }        
            return true;
        }
    }
    
    /**
     * Listener may be used to dismiss the coach mark when it is touched
     */
    protected class CoachMarkOnTouchListener implements OnTouchListener {
        private final CoachMarkUserResponse mUserResponse;

        public CoachMarkOnTouchListener(CoachMarkUserResponse userResponse) {
            mUserResponse = userResponse;
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                dismiss(mUserResponse);
            case MotionEvent.ACTION_DOWN:
                return true;
            default:
                return false;
            }
        }
    }

    /**
     * An {@link android.view.View.OnClickListener} which wraps an
     * existing listener with a call to {@link CoachMark#dismiss(CoachMarkUserResponse response)}
     *
     * @author lachie
     *
     */
    protected class CoachMarkOnClickListener implements View.OnClickListener {

        private final View.OnClickListener mListener;
        private final CoachMarkUserResponse mUserResponse;

        public CoachMarkOnClickListener(View.OnClickListener listener,
                CoachMarkUserResponse userResponse) {
            mListener = listener;
            mUserResponse = userResponse;
        }

        @Override
        public void onClick(View v) {
            dismiss(mUserResponse);

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
        
        public CoachMarkBuilder(Context context, View anchor, String message) {
            this(context, anchor, new TextView(context));
            ((TextView) content).setTextColor(Color.WHITE);
            ((TextView) content).setText(message);
        }

        public CoachMarkBuilder(Context context, View anchor, int contentResId) {
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
         * @param timeout
         *      the time in milliseconds after which to dismiss the coach
         *      mark (defaults to 10 seconds)
         */
        public CoachMarkBuilder setTimeout(long timeout) {
            this.timeout = timeout;
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
         * Set which animation will be used when displaying/hiding the coach mark
         * 
         * @param animationStyle
         *      the resource ID of the animation to be shown
         */
        public CoachMarkBuilder setAnimation(int animationStyle) {
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
