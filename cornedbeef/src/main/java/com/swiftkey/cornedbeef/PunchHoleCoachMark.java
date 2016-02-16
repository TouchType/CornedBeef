package com.swiftkey.cornedbeef;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;

/**
 * The coach mark for a "punch hole" to present a transparent circle onto the given view.
 */
public class PunchHoleCoachMark extends InternallyAnchoredCoachMark {

    private final float mGap;

    private final View mTargetView;

    private PunchHoleView mPunchHoleView;

    protected PunchHoleCoachMark(PunchHoleCoachMarkBuilder builder) {
        super(builder);

        mGap = mContext.getResources().getDimension(R.dimen.punchhole_coach_mark_gap);

        mTargetView = builder.targetView;

        mPunchHoleView.setOnTargetClickListener(builder.targetClickListener);
        mPunchHoleView.setOnGlobalClickListener(builder.globalClickListener);
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

        int[] anchorViewLoc = new int[2];
        int[] targetViewLoc = new int[2];
        mAnchor.getLocationOnScreen(anchorViewLoc);
        mTargetView.getLocationOnScreen(targetViewLoc);

        final int relCircleX = targetViewLoc[0] - anchorViewLoc[0] + (mTargetView.getWidth() / 2);
        final int relCircleY = targetViewLoc[1] - anchorViewLoc[1] + (mTargetView.getHeight() / 2);
        final float relCircleRadius = (mTargetView.getHeight() + mGap) / 2;

        if (!mPunchHoleView.setCircle(relCircleX, relCircleY, relCircleRadius)) {
            return;
        }

        // Calculating the proper padding of layout
        int upperGap = 0;
        int lowerGap = 0;
        if (relCircleY < (mAnchor.getHeight() / 2)) { // Circle in upper side
            upperGap = (int) (relCircleY + relCircleRadius);
        } else { // Circle in lower side
            lowerGap = (int) (relCircleY - relCircleRadius);
        }

        int horizontalPadding = (int) mContext.getResources().getDimension(R.dimen.punchhole_coach_mark_horizontal_padding);
        int verticalPadding = (int) mContext.getResources().getDimension(R.dimen.punchhole_coach_mark_vertical_padding);
        mPunchHoleView.setPadding(
                horizontalPadding, verticalPadding + upperGap,
                horizontalPadding, verticalPadding + lowerGap);
    }

    public static class PunchHoleCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {

        protected View targetView;

        protected View.OnClickListener targetClickListener;
        protected View.OnClickListener globalClickListener;

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

        @Override
        public CoachMark build() {
            return new PunchHoleCoachMark(this);
        }
    }
}
