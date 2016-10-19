package com.swiftkey.cornedbeef.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swiftkey.cornedbeef.BubbleCoachMark;
import com.swiftkey.cornedbeef.CoachMark;
import com.swiftkey.cornedbeef.HighlightCoachMark;
import com.swiftkey.cornedbeef.LayeredCoachMark;
import com.swiftkey.cornedbeef.PunchHoleCoachMark;

public class SpamActivity extends Activity {
    private static final String TAG = SpamActivity.class.getSimpleName();

    private CoachMark mBubbleCoachMark;
    private CoachMark mHighlightCoachMark;
    private CoachMark mPunchHoleCoachMark;
    private CoachMark mLayeredCoachMark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_spam);

        final Context context = getApplicationContext();

        final View anchorTextView = findViewById(R.id.hello_world);
        final View anchorTextViewForHighlight = findViewById(R.id.highlight_target);
        final View anchorLinearLayoutHoldButton = findViewById(R.id.anchor_with_button);
        final View anchorEmptyLinearLayout = findViewById(R.id.empty_anchor);

        mBubbleCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                context, anchorTextView, "This is a bubble coach mark!")
                .setTargetOffset(0.25f)
                .setShowBelowAnchor(true)
                .setPadding(10)
                .setOnShowListener(new CoachMark.OnShowListener() {
                    @Override
                    public void onShow() {
                        Toast.makeText(context, "Bubble coach mark shown!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnDismissListener(new CoachMark.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(context, "Bubble coach mark dismissed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        mHighlightCoachMark = new HighlightCoachMark.HighlightCoachMarkBuilder(
                context, anchorTextViewForHighlight).build();

        // Prepare the sample PunchHoleCoachMark
        TextView punchholeContent = (TextView) LayoutInflater.from(context).inflate(R.layout.sample_customised_punchhole_content, null);
        punchholeContent.setText(R.string.punchhole_message_text);
        mPunchHoleCoachMark = new PunchHoleCoachMark.PunchHoleCoachMarkBuilder(
                context, anchorLinearLayoutHoldButton, punchholeContent)
                .setTargetView(findViewById(R.id.punch_hole_coach_mark_target))
                .setOnTargetClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "The target punch hole clicked!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnGlobalClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(context, "The coach mark clicked!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setTimeout(0)
                .build();

        // Prepare the sample custom LayeredCoachMark
        LinearLayout layeredContent = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.sample_customised_layered_content, null);
        ImageView layeredImageView = (ImageView) layeredContent.findViewById(R.id.layered_coach_mark_image);
        TextView layeredTextView = (TextView) layeredContent.findViewById(R.id.layered_coach_mark_message);
        Button layeredButton = (Button) layeredContent.findViewById(R.id.layered_coach_mark_button);

        layeredImageView.setImageDrawable(getResources().getDrawable(R.drawable.sk_logo));
        layeredTextView.setText(R.string.layered_message_text);
        layeredButton.setText(R.string.layered_button_text);
        layeredButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "The button clicked!", Toast.LENGTH_SHORT).show();
            }
        });

        mLayeredCoachMark = new LayeredCoachMark.LayeredCoachMarkBuilder(
                context, anchorEmptyLinearLayout, layeredContent)
                .setTimeout(0)
                .build();

        getWindow().getDecorView().getRootView().post(new Runnable() {
            @Override
            public void run() {
                mBubbleCoachMark.show();
                mHighlightCoachMark.show();
                mPunchHoleCoachMark.show();
                mLayeredCoachMark.show();
            }
        });
    }

    @Override
    public void onDestroy() {
        mHighlightCoachMark.dismiss();
        mBubbleCoachMark.dismiss();
        mPunchHoleCoachMark.dismiss();
        mLayeredCoachMark.dismiss();
        super.onDestroy();
    }
}
