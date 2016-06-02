package com.swiftkey.cornedbeef;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.PopupWindow;

/**
 * The coach mark for a "punch hole" to present a transparent circle onto the given view.
 */
public class PunchHoleCoachMark extends InternallyAnchoredCoachMark {

    private final float mGap;
    private final boolean mHorizontalTranslation;

    private final View mTargetView;
    private final int[] mTargetViewLoc = new int[2];
    private final int[] mAnchorViewLoc = new int[2];
    private float mRelCircleRadius;

    private PunchHoleView mPunchHoleView;
    private AccelerateDecelerateInterpolator INTERPOLATOR = new AccelerateDecelerateInterpolator();
    private ObjectAnimator mHorizontalAnimator;

    protected PunchHoleCoachMark(PunchHoleCoachMarkBuilder builder) {
        super(builder);

        mGap = mContext.getResources().getDimension(R.dimen.punchhole_coach_mark_gap);

        mTargetView = builder.targetView;

        mPunchHoleView.setOnTargetClickListener(builder.targetClickListener);
        mPunchHoleView.setOnGlobalClickListener(builder.globalClickListener);
        mPunchHoleView.setBackgroundColor(builder.overlayColor);

        mHorizontalTranslation = builder.horizontalTranslation;
    }

    @Override
    protected View createContentView(View content) {
        final PunchHoleView view = (PunchHoleView) LayoutInflater.from(mContext)
                .inflate(R.layout.punchhole_coach_mark, null);
        view.addView(content);

        mPunchHoleView = view;

        return view;
    }

    @Override
    protected PopupWindow createNewPopupWindow(View contentView) {
        PopupWindow popup = new PopupWindow(
                contentView,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        popup.setTouchable(true);
        return popup;
    }

    @Override
    protected CoachMarkDimens<Integer> getPopupDimens(CoachMarkDimens<Integer> anchorDimens) {
        return anchorDimens;
    }

    @Override
    protected void updateView(CoachMarkDimens<Integer> popupDimens,  CoachMarkDimens<Integer> anchorDimens) {
        mPopup.update(popupDimens.x, popupDimens.y, popupDimens.width, popupDimens.height);

        mTargetView.getLocationOnScreen(mTargetViewLoc);
        mAnchor.getLocationOnScreen(mAnchorViewLoc);
        mRelCircleRadius = (mTargetView.getHeight() + mGap) / 2;

        // If the coachmark has an horizontal translation animation, draw the
        // circle on the start of the target view (it will move to the end).
        // However, if the width of the target view is smaller than the diameter
        // of the punch hole, just center the circle (no point in animating).
        final int startOffsetX = mHorizontalTranslation && canHaveHorizontalTranslation()
                ?  isRtlConfig()
                        ? mTargetViewLoc[0] + mTargetView.getWidth() - (int) mRelCircleRadius
                        : mTargetViewLoc[0] + (int) mRelCircleRadius
                : (mTargetView.getWidth() / 2);
        final int relCircleX = mTargetViewLoc[0] - mAnchorViewLoc[0] + startOffsetX;
        final int relCircleY = mTargetViewLoc[1] - mAnchorViewLoc[1] + (mTargetView.getHeight() / 2);

        if (!mPunchHoleView.setCircle(relCircleX, relCircleY, mRelCircleRadius)) {
            return;
        }

        if (mHorizontalTranslation) {
            animateHorizontalTranslation();
        }

        // Calculating the proper padding of layout
        int upperGap = 0;
        int lowerGap = 0;
        if (relCircleY < (mAnchor.getHeight() / 2)) { // Circle in upper side
            upperGap = (int) (relCircleY + mRelCircleRadius);
        } else {
            // Circle in lower side
            lowerGap = (int) (relCircleY - mRelCircleRadius);
        }

        int horizontalPadding = (int) mContext.getResources().getDimension(R.dimen.punchhole_coach_mark_horizontal_padding);
        int verticalPadding = (int) mContext.getResources().getDimension(R.dimen.punchhole_coach_mark_vertical_padding);
        mPunchHoleView.setPadding(
                horizontalPadding, verticalPadding + upperGap,
                horizontalPadding, verticalPadding + lowerGap);
    }

    /**
     * Move the punch hole from left to right of the target view, unless the
     * width of the target view is smaller than the diameter of the punch hole
     * in which case the circle will be centered and the animation is pointless.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void animateHorizontalTranslation() {
        if (canHaveHorizontalTranslation() && mHorizontalAnimator == null) {
            final int leftMostPosition = mTargetViewLoc[0] + (int) mRelCircleRadius;
            final int rightMostPosition = mTargetViewLoc[0] + mTargetView.getWidth() - (int) mRelCircleRadius;
            final int animationStartX = isRtlConfig() ? rightMostPosition : leftMostPosition;
            final int animationEndX = isRtlConfig() ? leftMostPosition : rightMostPosition;
            mHorizontalAnimator = ObjectAnimator.ofInt(
                    mPunchHoleView,
                    "circleCenterX",
                    animationStartX,
                    animationEndX);
            mHorizontalAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            mPunchHoleView.invalidate();
                        }
                    });
            // the animation lasts as long as the coachmark is on screen
            mHorizontalAnimator.setDuration(mTimeoutInMs);
            mHorizontalAnimator.setInterpolator(INTERPOLATOR);
            mHorizontalAnimator.start();
        }
    }

    /**
     * Check if we can animate the circle from left to right, i.e. if the
     * width of the target view is bigger than the diameter of the circle
     * (otherwise there's no space to perform the animation).
     * @return  whether we can display the animation
     */
    private boolean canHaveHorizontalTranslation() {
        return mTargetView.getWidth() > 2 * mRelCircleRadius;
    }

    /**
     * For APIs above JELLY_BEAN_MR1, we can check if the locale of a device is
     * LTR or RTL. Below that API, we assume it's LTR.
     * We need this to show the horizontal translation from start to end
     * (from left to right in LTR and from right to left in LTR), if possible.
     * @return  whether the device has a RTL locale
     */
    private boolean isRtlConfig() {
        final Configuration config = mAnchor.getResources().getConfiguration();
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                && config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static class PunchHoleCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {

        protected View targetView;
        protected int overlayColor = 0xBF000000;

        protected View.OnClickListener targetClickListener;
        protected View.OnClickListener globalClickListener;

        protected boolean horizontalTranslation;

        public PunchHoleCoachMarkBuilder(Context context, View anchor, String message) {
            super(context, anchor, message);
        }

        public PunchHoleCoachMarkBuilder(Context context, View anchor, View content) {
            super(context, anchor, content);
        }

        public PunchHoleCoachMarkBuilder(Context context, View anchor, @LayoutRes int contentResId) {
            super(context, anchor, contentResId);
        }

        /**
         * Set a target view where the "punch hole" will display.
         *
         * @param view
         */
        public PunchHoleCoachMarkBuilder setTargetView(View view) {
            this.targetView = view;
            return this;
        }

        /**
         * Set a listener to be called when the target view is clicked.
         *
         * @param listener
         */
        public PunchHoleCoachMarkBuilder setOnTargetClickListener(View.OnClickListener listener) {
            this.targetClickListener = listener;
            return this;
        }

        /**
         * Set a listener to be called when the coach mark is clicked.
         *
         * @param listener
         */
        public PunchHoleCoachMarkBuilder setOnGlobalClickListener(View.OnClickListener listener) {
            this.globalClickListener = listener;
            return this;
        }

        /**
         * Set the color of this coach mark.
         *
         * @param overlayColor the color to set
         */
        public PunchHoleCoachMarkBuilder setOverlayColor(int overlayColor) {
            this.overlayColor = overlayColor;
            return this;
        }

        /**
         * Set whether the punch hole animates going over the target view from
         * left to right.
         *
         * @param horizontalTranslation
         */
        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        public PunchHoleCoachMarkBuilder setHorizontalTranslation(boolean horizontalTranslation) {
            this.horizontalTranslation = horizontalTranslation;
            return this;
        }

        @Override
        public CoachMark build() {
            return new PunchHoleCoachMark(this);
        }
    }
}
