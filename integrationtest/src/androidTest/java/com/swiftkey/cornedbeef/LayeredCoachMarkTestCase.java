package com.swiftkey.cornedbeef;

import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import static com.swiftkey.cornedbeef.TestHelper.dismissCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.showCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.waitUntilStatusBarHidden;

public class LayeredCoachMarkTestCase extends ActivityInstrumentationTestCase2<SpamActivity> {

    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;

    private static final String MESSAGE = "spam spam spam";

    public LayeredCoachMarkTestCase() {
        super(SpamActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        mActivity = getActivity();

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.setContentView(R.layout.coach_mark_test_activity);
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_layout_anchor);
            }
        });
        getInstrumentation().waitForIdleSync();
        waitUntilStatusBarHidden(mActivity);

        mCoachMark = new LayeredCoachMark.LayeredCoachMarkBuilder(mActivity, mAnchor, MESSAGE)
                .build();
    }

    public void tearDown() throws Exception {
        dismissCoachMark(getInstrumentation(), mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;

        super.tearDown();
    }

    /**
     * Test the view creation.
     */
    public void testViewsCreatedAndVisible() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final View container = mCoachMark.getContentView();
        final ViewGroup content = (ViewGroup) container.findViewById(R.id.coach_mark_content);
        TextView tv = (TextView) content.getChildAt(0);

        // Check the creation
        assertNotNull(getActivity());
        assertNotNull(mCoachMark);
        assertNotNull(container);
        assertNotNull(content);

        // Check the visibility
        assertEquals(MESSAGE, tv.getText());
        assertTrue(mCoachMark.isShowing());
    }
}
