package com.swiftkey.cornedbeef;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;

/**
 * Can be used to highlight part, or all of a given 'anchor' view
 * 
 * @author lachie
 */
public class HighlightCoachMark extends InternallyAnchoredCoachMark {
    private View mView;

    protected HighlightCoachMark(HighlightCoachMarkBuilder builder) {
        super(builder);

        try {
            ((GradientDrawable) mView.getBackground().mutate()).setStroke(
                    builder.strokeWidth, builder.highlightColor);
        } catch (Exception e) {
            Log.e("HighlightCoachMark", "Could not change the coach mark color and stroke width");
        }
    }

    @Override
    protected PopupWindow createNewPopupWindow(View contentView) {
        PopupWindow popup = new PopupWindow(
                contentView,
                LayoutParams.WRAP_CONTENT, 
                LayoutParams.WRAP_CONTENT);

        popup.setTouchable(false);
        return popup;
    }

    protected View createContentView(View content) {
        mView = LayoutInflater.from(mContext).inflate(R.layout.highlight_coach_mark, null);
        return mView;
    }

    @Override
    protected CoachMarkDimens<Integer> getPopupDimens(CoachMarkDimens<Integer> anchorDimens) {
        return anchorDimens;
    }

    @Override
    protected void updateView(CoachMarkDimens<Integer> popupDimens,  CoachMarkDimens<Integer> anchorDimens) {
        mPopup.update(popupDimens.x, popupDimens.y, popupDimens.width, popupDimens.height);
    }
    
    public static class HighlightCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {

        // Optional parameters with default values
        @ColorInt int highlightColor;
        int strokeWidth;

        public HighlightCoachMarkBuilder(Context context, View anchor) {
            super(context, anchor, new String());
            setDefaultValues(context);
        }

        public HighlightCoachMarkBuilder(Context context, View anchor, String message) {
            super(context, anchor, message);
            setDefaultValues(context);
        }

        public HighlightCoachMarkBuilder(Context context, View anchor, View content) {
            super(context, anchor, content);
            setDefaultValues(context);
        }

        public HighlightCoachMarkBuilder(Context context, View anchor, @LayoutRes int contentResId) {
            super(context, anchor, contentResId);
            setDefaultValues(context);
        }

        private void setDefaultValues(final Context context) {
            this.highlightColor = CoachMarkUtils.resolveColor(context, R.color.default_colour);
            this.strokeWidth = (int) context.getResources().getDimension(
                    R.dimen.highlight_coach_mark_stroke_width);
        }

        /**
         * Set the coach mark's highlight color.
         *
         * @param highlightColor
         *      new highlight color
         */
        public HighlightCoachMark.HighlightCoachMarkBuilder setHighlightColor(
                @ColorInt int highlightColor) {
            this.highlightColor = highlightColor;
            return this;
        }

        /**
         * Set the coach mark's stroke width.
         *
         * @param strokeWidth
         *      new stroke width
         */
        public HighlightCoachMark.HighlightCoachMarkBuilder setStrokeWidth(int strokeWidth) {
            this.strokeWidth = strokeWidth;
            return this;
        }
        
        @Override
        public CoachMark build() {
            return new HighlightCoachMark(this);
        }
    }
}
