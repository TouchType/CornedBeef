package com.swiftkey.cornedbeef;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

/**
 * The coach mark for the message and button.
 *
 * Created by DongyiChun on 2/10/16.
 */
public class DialogCoachMark extends InternallyAnchoredCoachMark {

    private View mView;
    private ImageView mImageView;
    private Button mButton;

    public DialogCoachMark(DialogCoachMarkBuilder builder) {
        super(builder);

        mView.setOnClickListener(builder.globalClickListener);

        mImageView.setImageDrawable(builder.drawable);

        mButton.setText(builder.buttonText);
        mButton.setTextColor(Color.WHITE);
        mButton.setOnClickListener(builder.buttonClickListener);
    }

    @Override
    protected View createContentView(String message) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.dialog_coach_mark, null);
        final ImageView imageView = (ImageView) view.findViewById(R.id.dialog_coach_mark_image);
        final TextView textView = (TextView) view.findViewById(R.id.dialog_coach_mark_message);
        final Button button = (Button) view.findViewById(R.id.dialog_coach_mark_button);

        textView.setText(message);

        Drawable drawable = button.getBackground();
        drawable.setColorFilter(mContext.getResources()
                .getColor(R.color.bell_teal), PorterDuff.Mode.SRC_ATOP);
        button.setBackgroundDrawable(drawable); // For backward compatibility instaed of setBackground()

        mView = view;
        mImageView = imageView;
        mButton = button;

        return view;
    }

    @Override
    protected PopupWindow createNewPopupWindow(View contentView) {
        PopupWindow popup = new PopupWindow(
                contentView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
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

    public static class DialogCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {

        private String buttonText;
        private Drawable drawable;

        private View.OnClickListener buttonClickListener;
        private View.OnClickListener globalClickListener;

        public DialogCoachMarkBuilder(Context context, View anchor, String message) {
            super(context, anchor, message);
        }

        public DialogCoachMarkBuilder(Context context, View anchor, int contentResId) {
            super(context, anchor, contentResId);
        }

        /**
         * Set a branded image to coach mark.
         *
         * @param drawable
         */
        public DialogCoachMarkBuilder setDrawable(Drawable drawable) {
            this.drawable = drawable;
            return this;
        }

        /**
         * Set a button's text.
         *
         * @param text
         */
        public DialogCoachMarkBuilder setButtonText(String text) {
            this.buttonText = text;
            return this;
        }

        /**
         * Set a listener to be called when the button is clicked.
         *
         * @param listener
         */
        public DialogCoachMarkBuilder setButtonClickListener(View.OnClickListener listener) {
            this.buttonClickListener = listener;
            return this;
        }

        /**
         * Set a listener to be called when the coach mark is clicked.
         *
         * @param listener
         */
        public DialogCoachMarkBuilder setGlobalClickListener(View.OnClickListener listener) {
            this.globalClickListener = listener;
            return this;
        }

        @Override
        public CoachMark build() {
            return new DialogCoachMark(this);
        }
    }
}
