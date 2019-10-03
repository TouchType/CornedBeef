package com.swiftkey.cornedbeef;

import android.graphics.Color;
import android.view.View;
import android.view.WindowManager;

import androidx.test.rule.ActivityTestRule;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.swiftkey.cornedbeef.TestHelper.dismissCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.showCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.waitUntilStatusBarHidden;
import static org.junit.Assert.assertTrue;

public class HighlightCoachMarkTestCase {

    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;

    @Rule
    public ActivityTestRule<SpamActivity> mActivityRule =
            new ActivityTestRule<>(SpamActivity.class, false, true);

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
        
        getInstrumentation().runOnMainSync(new Runnable() {
            
            @Override
            public void run() {
                mActivity.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.setContentView(R.layout.coach_mark_test_activity);
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_target_wide);
            }
        });
        getInstrumentation().waitForIdleSync();
        waitUntilStatusBarHidden(mActivity);
        
        mCoachMark = new HighlightCoachMark.HighlightCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam").build();
    }

    @After
    public void tearDown() {
        dismissCoachMark(getInstrumentation(), mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;
    }

    /**
     * Verify that we can show the coach mark
     */
    @Test
    public void testShowCoachMark() {
        mCoachMark = new HighlightCoachMark.HighlightCoachMarkBuilder(
                mActivity,
                mAnchor,
                "spam spam spam")
                .setHighlightColor(Color.RED)
                .setStrokeWidth(20)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);

        assertTrue(mCoachMark.isShowing());
        // it is not possible to get information about the stroke so our testing is limited
    }
}
