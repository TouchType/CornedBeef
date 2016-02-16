package com.swiftkey.cornedbeef;

import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * The coach mark for "punch hole" to present a transparent circle onto the given view.
 *
 * @author Dongyi Chun
 */
public class PunchHoleCoachMark extends InternallyAnchoredCoachMark {

    private final float GAP;

    private final View mTargetView;

    private PunchHoleView mPunchHoleView;

    private View mUpperGapView;
    private View mLowerGapView;

    protected PunchHoleCoachMark(PunchHoleCoachMarkBuilder builder) {
        super(builder);

        GAP = mContext.getResources().getDimension(R.dimen.punchhole_coach_mark_gap);

        mTargetView = builder.targetView;

        mPunchHoleView.setOnTargetClickListener(builder.targetClickListener);
        mPunchHoleView.setOnGlobalClickListener(builder.globalClickListener);
    }

    @Override
    protected View createContentView(String message) {
        final PunchHoleView contentView = (PunchHoleView) LayoutInflater.from(mContext)
                .inflate(R.layout.punchhole_coach_mark, null);
        final TextView textView = (TextView) contentView.findViewById(R.id.punchhole_coach_mark_message);
        textView.setText(message);

        mPunchHoleView = contentView;

        mUpperGapView = contentView.findViewById(R.id.punchhole_coach_mark_upper_gap);
        mLowerGapView = contentView.findViewById(R.id.punchhole_coach_mark_lower_gap);

        return contentView;
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
        final float relCircleRadius = (mTargetView.getHeight() + GAP) / 2;

        if (!mPunchHoleView.setCircle(relCircleX, relCircleY, relCircleRadius)) {
            return;
        }

        // Checking the vertical alignment of target
        if (relCircleY < (mAnchor.getHeight() / 2)) { // Circle in upper side
            mUpperGapView.setVisibility(View.VISIBLE);
            mUpperGapView.getLayoutParams().height = ((int) (relCircleY + relCircleRadius));
            mLowerGapView.setVisibility(View.GONE);
            mLowerGapView.getLayoutParams().height = 0;
        } else { // Circle in lower side
            mUpperGapView.getLayoutParams().height = 0;
            mUpperGapView.setVisibility(View.GONE);
            mLowerGapView.getLayoutParams().height = ((int) (relCircleY - relCircleRadius));
            mLowerGapView.setVisibility(View.VISIBLE);
        }
    }

    @VisibleForTesting
    public View getTargetView() {
        return mTargetView;
    }

    public static class PunchHoleCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {

        protected View targetView;
        protected CharSequence message;

        protected View.OnClickListener targetClickListener;
        protected View.OnClickListener globalClickListener;

        public PunchHoleCoachMarkBuilder(Context context, View anchor, String message) {
            super(context, anchor, message);
        }

        public PunchHoleCoachMarkBuilder(Context context, View anchor, int contentResId) {
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
         * Set a coach mark's message.
         *
         * @param message
         */
        public PunchHoleCoachMarkBuilder setMessage(String message) {
            this.message = message;
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
