package com.swiftkey.cornedbeef.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.swiftkey.cornedbeef.BubbleCoachMark;
import com.swiftkey.cornedbeef.CoachMark;
import com.swiftkey.cornedbeef.HighlightCoachMark;

public class SpamActivity extends Activity {

    private CoachMark mBubbleCoachMark;
    private CoachMark mHighlightCoachMark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_spam);

        final Context context = getApplicationContext();
        final View view = findViewById(R.id.hello_world);

        mBubbleCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(context, view, "This is a coach mark!")
                .setTargetOffset(0.25f)
                .setShowBelowAnchor(true)
                .setPadding(10)
                .setOnShowListener(new CoachMark.OnShowListener() {
                    @Override
                    public void onShow() {
                        Toast.makeText(context, "Coach mark shown!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setOnDismissListener(new CoachMark.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        Toast.makeText(context, "Coach mark dismissed!", Toast.LENGTH_SHORT).show();
                    }
                })
                .build();

        mHighlightCoachMark = new HighlightCoachMark.HighlightCoachMarkBuilder(context, view).build();

        view.post(new Runnable() {
            @Override
            public void run() {
                mBubbleCoachMark.show();
                mHighlightCoachMark.show();
            }
        });
    }

    @Override
    public void onDestroy() {
        mHighlightCoachMark.dismiss(CoachMark.CoachMarkUserResponse.OTHER);
        mBubbleCoachMark.dismiss(CoachMark.CoachMarkUserResponse.OTHER);
        super.onDestroy();
    }
}
