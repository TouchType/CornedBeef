package uk.co.lachie.cornedbeef;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import uk.co.lachie.cornedbeef.coachmark.BubbleCoachMark;
import uk.co.lachie.cornedbeef.coachmark.CoachMark;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_spam, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
