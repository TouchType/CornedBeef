package uk.co.lachie.cornedbeef.test;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import uk.co.lachie.cornedbeef.BubbleCoachMark;
import uk.co.lachie.cornedbeef.CoachMark;

public class SpamActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_spam);

        final Context context = getApplicationContext();
        final View view = findViewById(R.id.hello_world);
        view.post(new Runnable() {
            @Override
            public void run() {
                new BubbleCoachMark.BubbleCoachMarkBuilder(context, view, "This is a coach mark!")
                        .setTargetOffset(0.25f)
                        .setShowBelowAnchor(true)
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
                        .build()
                        .show();
            }
        });
    }
}
