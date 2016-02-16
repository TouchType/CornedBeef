package com.swiftkey.cornedbeef;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.View;

public abstract class InternallyAnchoredCoachMark extends CoachMark {

    private final CoachMarkDimens<Float> mInternalAnchor;
    
    protected InternallyAnchoredCoachMark(InternallyAnchoredCoachMarkBuilder builder) {
        super(builder);
        mInternalAnchor = builder.internalAnchor;
    }

    @Override
    protected CoachMarkDimens<Integer> getAnchorDimens() {
        int[] anchorLoc = new int[2];
        int[] tokenViewLoc = new int[2];
        mAnchor.getLocationOnScreen(anchorLoc);
        
        // Workaround for SK-4652 - should be revisited when this is fixed
        View rootView = mTokenView.getRootView();
        if(rootView != mTokenView) {
            rootView.getLocationOnScreen(tokenViewLoc);
            anchorLoc[1] -= tokenViewLoc[1];
        }
        
        final int width = (int) (mAnchor.getMeasuredWidth() * mInternalAnchor.width);
        final int height = (int) (mAnchor.getMeasuredHeight() * mInternalAnchor.height);
        final int x = (int) (anchorLoc[0] + mInternalAnchor.x * mAnchor.getMeasuredWidth());
        final int y = (int) (anchorLoc[1] + mInternalAnchor.y * mAnchor.getMeasuredHeight());
        
        return new CoachMarkDimens<Integer>(x, y, width, height);
    }

    public abstract static class InternallyAnchoredCoachMarkBuilder extends CoachMarkBuilder {

        public InternallyAnchoredCoachMarkBuilder(Context context, View anchor, String message) {
            super(context, anchor, message);
        }

        public InternallyAnchoredCoachMarkBuilder(Context context, View anchor, View content) {
            super(context, anchor, content);
        }

        public InternallyAnchoredCoachMarkBuilder(Context context, View anchor, @LayoutRes int contentResId) {
            super(context, anchor, contentResId);
        }

        // Optional parameters with default values
        protected CoachMarkDimens<Float> internalAnchor = new CoachMarkDimens<Float>(0f, 0f, 1f, 1f);
        
        /**
         * Set the anchor to be a sub-region of the {@link CoachMarkBuilder#anchor}
         * All of the parameters should be set using relative values between 0 and 1
         * 
         * @param x
         *      the x coordinate of the top left corner of the internal anchor
         * @param y
         *      the y coordinate of the top left corner of the internal anchor
         * @param width
         *      the width of the internal anchor
         * @param height
         *      the height of the internal anchor
         */
        public InternallyAnchoredCoachMarkBuilder setInternalAnchor(float x, float y, float width, float height) {
            this.internalAnchor = new CoachMarkDimens<Float>(x, y, width, height);
            return this;
        }
    }

}
