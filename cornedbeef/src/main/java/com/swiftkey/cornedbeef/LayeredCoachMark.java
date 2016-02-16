package com.swiftkey.cornedbeef;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

/**
 * The coach mark to layer for anchor view.
 */
public class LayeredCoachMark extends InternallyAnchoredCoachMark {

    public LayeredCoachMark(LayeredCoachMarkBuilder builder) {
        super(builder);
    }

    @Override
    protected View createContentView(View content) {
        final LinearLayout container = (LinearLayout) LayoutInflater.from(mContext)
                .inflate(R.layout.layered_coach_mark, null);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        container.addView(content, params);
        return container;
    }

    @Override
    protected PopupWindow createNewPopupWindow(View contentView) {
        PopupWindow popup = new PopupWindow(
                contentView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        popup.setTouchable(true);
        return popup;
    }

    @Override
    protected CoachMarkDimens<Integer> getPopupDimens(CoachMarkDimens<Integer> anchorDimens) {
        return anchorDimens;
    }

    @Override
    protected void updateView(CoachMarkDimens<Integer> popupDimens, CoachMarkDimens<Integer> anchorDimens) {
        mPopup.update(popupDimens.x, popupDimens.y, popupDimens.width, popupDimens.height);
    }

    public static class LayeredCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {

        public LayeredCoachMarkBuilder(Context context, View anchor, String message) {
            super(context, anchor, message);
        }

        public LayeredCoachMarkBuilder(Context context, View anchor, View content) {
            super(context, anchor, content);
        }

        public LayeredCoachMarkBuilder(Context context, View anchor, @LayoutRes int contentResId) {
            super(context, anchor, contentResId);
        }

        @Override
        public CoachMark build() {
            return new LayeredCoachMark(this);
        }
    }
}
