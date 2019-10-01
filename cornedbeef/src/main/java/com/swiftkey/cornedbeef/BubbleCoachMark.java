package com.swiftkey.cornedbeef;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.RequiresApi;

/**
 * {@link BubbleCoachMark}s are displayed as speech bubble with a 'pointy mark'.
 * The speech bubble is implemented as an android {@link PopupWindow}. By
 * default the speech bubble is centred above the anchor view. If there is not
 * enough room above the anchor then the bubble will be moved below and the
 * direction of the pointy mark reversed. If the bubble would otherwise be
 * positioned off the side of the screen it is shifted left or right
 * accordingly. The pointy mark always points to the same location on the
 * anchor, regardless of the position of the speech bubble.
 * 
 * @author lachie
 * 
 */
public class BubbleCoachMark extends InternallyAnchoredCoachMark {
    
    private static final int MIN_ARROW_MARGIN = 10;

    private final float mTarget;
    private final boolean mShowBelowAnchor;
    private final int mMinArrowMargin;

    private int mMinWidth;
    private int mArrowWidth;
    private ImageView mTopArrow;
    private ImageView mBottomArrow;
    private ViewGroup mContentHolder;

    public BubbleCoachMark(BubbleCoachMarkBuilder builder) {
        super(builder);
        
        mTarget = builder.target;
        mShowBelowAnchor = builder.showBelowAnchor;
        mMinArrowMargin = (int) mContext.getResources()
                .getDimension(R.dimen.coach_mark_border_radius) + MIN_ARROW_MARGIN;

        // Set the bubble color, if possible. We could change the color in lower APIs but we'd
        // have to use the support library, increasing the size of the CornedBeef library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            try {
                mTopArrow.setImageTintList(ColorStateList.valueOf(builder.bubbleColor));
                mBottomArrow.setImageTintList(ColorStateList.valueOf(builder.bubbleColor));
                ((GradientDrawable) mContentHolder.getBackground().mutate()).setColor(builder.bubbleColor);
            } catch (Exception e) {
                Log.e("BubbleCoachMark", "Could not change the coach mark color");
            }
        }
    }
    
    @Override
    protected View createContentView(View content) {
        // Inflate the coach mark layout and add the content
        View view = LayoutInflater.from(mContext).inflate(R.layout.bubble_coach_mark, null);
        mContentHolder = view.findViewById(R.id.coach_mark_content);
        mContentHolder.addView(content);
        
        // Measure the coach mark to get the minimum width (constrained by screen width and padding) 
        final int maxWidth = mContext.getResources()
                .getDisplayMetrics().widthPixels - 2 * mPadding;
        view.measure(View.MeasureSpec.makeMeasureSpec(maxWidth, View.MeasureSpec.AT_MOST), 0);
        
        mMinWidth = view.getMeasuredWidth();
        mTopArrow = view.findViewById(R.id.top_arrow);
        mBottomArrow = view.findViewById(R.id.bottom_arrow);

        // Ensure that content holder expands to fill the coach mark
        mContentHolder.setLayoutParams(new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

        // It is assumed that the top and bottom arrows are identical
        mArrowWidth = mBottomArrow.getMeasuredWidth();

        return view;
    }
    
    @Override
    protected PopupWindow createNewPopupWindow(View contentView) {
        PopupWindow popup = new PopupWindow(
                contentView,
                LayoutParams.WRAP_CONTENT, 
                LayoutParams.WRAP_CONTENT);
        
        popup.setClippingEnabled(false); // We will handle clipping ourselves
        popup.setTouchInterceptor(new CoachMarkOnTouchListener());
        popup.setTouchable(true);
        return popup;
    }
    
    @Override
    protected CoachMarkDimens<Integer> getPopupDimens(CoachMarkDimens<Integer> anchorDimens) {
        final int screenWidth = mDisplayFrame.width();
        final int screenHeight = mDisplayFrame.height();
        
        final int popupWidth = CoachMarkUtils.getPopupWidth(mArrowWidth, 
                screenWidth, mMinWidth, anchorDimens.width, mTarget);
        
        final int popupHeight = getContentView().getMeasuredHeight();
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(anchorDimens, popupWidth, 
                popupHeight, screenWidth, screenHeight, mPadding, mShowBelowAnchor);
        
        return new CoachMarkDimens<Integer>(popupPos.x, popupPos.y, popupWidth, popupHeight);
    }
    
    @Override
    protected void updateView(CoachMarkDimens<Integer> popupDimens, CoachMarkDimens<Integer> anchorDimens) {
        int leftMargin;
        final View currentArrow;
        final MarginLayoutParams params; 
        
        // Check if the popup is being shown above or below the anchor
        if(popupDimens.getPos().y > anchorDimens.y) {
            currentArrow = mTopArrow;
            mTopArrow.setVisibility(View.VISIBLE);
            mBottomArrow.setVisibility(View.GONE);
        } else {
            currentArrow = mBottomArrow;
            mBottomArrow.setVisibility(View.VISIBLE);
            mTopArrow.setVisibility(View.GONE);
        }
        
        leftMargin = CoachMarkUtils.getArrowLeftMargin(mTarget,
                anchorDimens.width, mArrowWidth, anchorDimens.x,
                popupDimens.getPos().x, mMinArrowMargin, 
                popupDimens.width - mMinArrowMargin - mArrowWidth);

        params = (MarginLayoutParams) currentArrow.getLayoutParams();
        if(leftMargin != params.leftMargin) {
            params.leftMargin = leftMargin;
            currentArrow.setLayoutParams(params);
        }        
    }
    
    public static class BubbleCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {

        // Optional parameters with default values
        boolean showBelowAnchor = false;
        float target = 0.5f;
        @ColorInt int bubbleColor;
        
        public BubbleCoachMarkBuilder(Context context, View anchor, String message) {
            super(context, anchor, message);
            bubbleColor = CoachMarkUtils.resolveColor(context, R.color.default_colour);
        }

        public BubbleCoachMarkBuilder(Context context, View anchor, View content) {
            super(context, anchor, content);
            bubbleColor = CoachMarkUtils.resolveColor(context, R.color.default_colour);
        }

        public BubbleCoachMarkBuilder(Context context, View anchor, int contentResId) {
            super(context, anchor, contentResId);
            bubbleColor = CoachMarkUtils.resolveColor(context, R.color.default_colour);
        }
        
        /**
         * If possible, show this coach mark below the anchor rather than above
         * 
         * @param showBelowAnchor
         *      true if this coach mark should be shown below the anchor, false otherwise
         */
        public BubbleCoachMarkBuilder setShowBelowAnchor(boolean showBelowAnchor) {
            this.showBelowAnchor = showBelowAnchor;
            return this;
        }
        
        /**
         * Set the position of the pointy mark along the anchor
         * 
         * @param target
         *      a value between 0 and 1 indicating the focal point of the
         *      anchor (defaults to 0.5)
         */
        public BubbleCoachMarkBuilder setTargetOffset(float target) {
            this.target = target;
            return this;
        }

        /**
         * Set the coach mark's bubble color.
         * It has no effect if called from APIs lower than Lollipop (21).
         *
         * @param bubbleColor
         *      new bubble color
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public BubbleCoachMarkBuilder setBubbleColor(@ColorInt int bubbleColor) {
            this.bubbleColor = bubbleColor;
            return this;
        }

        @Override
        public CoachMark build() {
            return new BubbleCoachMark(this);
        }
    }
}
