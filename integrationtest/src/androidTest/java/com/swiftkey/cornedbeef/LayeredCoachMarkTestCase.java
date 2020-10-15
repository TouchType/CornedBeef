package com.swiftkey.cornedbeef;

import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class LayeredCoachMarkTestCase {

    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;

    private static final String MESSAGE = "spam spam spam";

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
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_layout_anchor);
            }
        });
        getInstrumentation().waitForIdleSync();
        waitUntilStatusBarHidden(mActivity);
    }

    @After
    public void tearDown() {
        dismissCoachMark(getInstrumentation(), mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;
    }

    /**
     * Test the view creation.
     */
    @Test
    public void testViewsCreatedAndVisible() {
        mCoachMark = new LayeredCoachMark.LayeredCoachMarkBuilder(mActivity, mAnchor, MESSAGE)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);

        final View container = mCoachMark.getContentView();
        final ViewGroup content = container.findViewById(R.id.coach_mark_content);
        TextView tv = (TextView) content.getChildAt(0);

        // Check the creation
        assertNotNull(mActivity);
        assertNotNull(mCoachMark);
        assertNotNull(container);
        assertNotNull(content);

        // Check the visibility
        assertEquals(MESSAGE, tv.getText());
        assertTrue(mCoachMark.isShowing());
    }

    /**
     * Test that the coach mark text color is set correctly
     */
    @Test
    public void testSetTextColor() {
        final @ColorInt int color = Color.RED;
        mCoachMark = new LayeredCoachMark.LayeredCoachMarkBuilder(mActivity, mAnchor, MESSAGE)
                .setTextColor(color)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);

        final TextView tv = (TextView) ((ViewGroup) mCoachMark.getContentView()).getChildAt(0);

        // Check the text, text color and visibility
        assertTrue(mCoachMark.isShowing());
        assertEquals(MESSAGE, tv.getText());
        assertEquals(color, tv.getCurrentTextColor());
    }

    /**
     * Verify that setting the coach mark text color on a non-text coach mark throws exception
     */
    @Test(expected = IllegalStateException.class)
    public void testSetTextColorOnNonTextCoachMark() {
        mCoachMark = new LayeredCoachMark.LayeredCoachMarkBuilder(mActivity, mAnchor, new ImageView(mActivity))
                .setTextColor(Color.RED)
                .build();
    }

    /**
     * Verify that a non-text coach mark is shown correctly
     */
    @Test
    public void testNonTextCoachMark() {
        final ImageView imageView = new ImageView(mActivity);
        imageView.setImageResource(R.drawable.ic_pointy_mark_up);
        mCoachMark = new LayeredCoachMark.LayeredCoachMarkBuilder(mActivity, mAnchor, imageView)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);

        assertTrue(mCoachMark.isShowing());

        final ViewGroup content = mCoachMark.getContentView().findViewById(R.id.coach_mark_content);
        assertTrue(content.getChildAt(0) instanceof ImageView);
    }
}
