package com.swiftkey.cornedbeef.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.swiftkey.cornedbeef.BubbleCoachMark;
import com.swiftkey.cornedbeef.CoachMark;
import com.swiftkey.cornedbeef.DialogCoachMark;
import com.swiftkey.cornedbeef.HighlightCoachMark;
import com.swiftkey.cornedbeef.PunchHoleCoachMark;

public class SpamActivity extends Activity {
    private static final String TAG = SpamActivity.class.getSimpleName();

    private CoachMark mBubbleCoachMark;
    private CoachMark mHighlightCoachMark;
    private CoachMark mPunchHoleCoachMark;
    private CoachMark mDialogCoachMark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_spam);

        final Context context = getApplicationContext();

        final View messageCoachMarkTargetView = findViewById(R.id.message_coach_mark_target_layout);
        final View punchHoleCoachMarkTargetView = findViewById(R.id.punch_hole_coach_mark_target_container);

        mBubbleCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                context, messageCoachMarkTargetView, "This is a bubble coach mark!")
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
                context, messageCoachMarkTargetView).build();

        mPunchHoleCoachMark = new PunchHoleCoachMark.PunchHoleCoachMarkBuilder(
                context, punchHoleCoachMarkTargetView, "Enable the emoji predictions")
                .setTargetView(findViewById(R.id.punch_hole_coach_mark_target))
                .setOnTargetClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "The punch hole is clicked!");
                        Toast.makeText(context, "The target punch hole clicked!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnGlobalClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d(TAG, "The coach mark is clicked!");
                        Toast.makeText(context, "The coach mark clicked!", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        mDialogCoachMark = new DialogCoachMark.DialogCoachMarkBuilder(
                //context, messageCoachMarkTargetView, "The sample coach mark long message for multiline")
                context, messageCoachMarkTargetView, "Want to customise your keyboard?")
                .setDrawable(getResources().getDrawable(R.drawable.sk_logo))
                .setButtonText("EXPLORE")
                .setButtonClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "DialogCoachMark's button click listener fired!");
                    }
                })
                .setGlobalClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d(TAG, "DialogCoachMark's global click listener fired!");
                    }
                })
                .build();

        getWindow().getDecorView().getRootView().post(new Runnable() {
            @Override
            public void run() {
                mBubbleCoachMark.show();
                mHighlightCoachMark.show();
                mPunchHoleCoachMark.show();
                mDialogCoachMark.show();
            }
        });
    }

    @Override
    public void onDestroy() {
        mHighlightCoachMark.dismiss();
        mBubbleCoachMark.dismiss();
        mPunchHoleCoachMark.dismiss();
        mDialogCoachMark.dismiss();
        super.onDestroy();
    }
}
