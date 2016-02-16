package com.swiftkey.cornedbeef;

import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.swiftkey.cornedbeef.TestHelper.dismissCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.moveTargetView;
import static com.swiftkey.cornedbeef.TestHelper.showCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.waitUntilStatusBarHidden;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PunchHoleCoachMarkTestCase extends ActivityInstrumentationTestCase2<SpamActivity> {

    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;
    private View mTargetView;

    @Mock
    private View.OnClickListener mMockTargetClickListener;
    @Mock
    private View.OnClickListener mMockCoachMarkClickListener;

    private TextView mTextView;
    private static final String MESSAGE = "spam spam spam";

    public PunchHoleCoachMarkTestCase() {
        super(SpamActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        MockitoAnnotations.initMocks(this);

        mActivity = getActivity();

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.setContentView(R.layout.coach_mark_test_activity);
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_layout_anchor);
                mTargetView = mActivity.findViewById(R.id.coach_mark_test_target);
            }
        });
        getInstrumentation().waitForIdleSync();
        waitUntilStatusBarHidden(mActivity);

        mTextView = (TextView) LayoutInflater.from(mActivity)
                .inflate(R.layout.sample_customised_punchhole_content, null);
        mTextView.setText(MESSAGE);
        mCoachMark = new PunchHoleCoachMark.PunchHoleCoachMarkBuilder(mActivity, mAnchor, mTextView)
                .setTargetView(mTargetView)
                .setOnTargetClickListener(mMockTargetClickListener)
                .setOnGlobalClickListener(mMockCoachMarkClickListener)
                .build();
    }

    public void tearDown() throws Exception {
        dismissCoachMark(getInstrumentation(), mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mTargetView = null;
        mActivity = null;

        mMockTargetClickListener = null;
        mMockCoachMarkClickListener = null;

        super.tearDown();
    }

    /**
     * Test the view creation and visibility.
     */
    public void testViewsCreatedAndVisible() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final View container = mCoachMark.getContentView();

        // Check the creation
        assertNotNull(getActivity());
        assertNotNull(mCoachMark);
        assertNotNull(container);
        assertNotNull(mTextView);

        // Check the visibility
        ViewAsserts.assertOnScreen(container, mTextView);
        ViewAsserts.assertHorizontalCenterAligned(container, mTextView);

        // Check the resources which passed by builder
        assertEquals(MESSAGE, mTextView.getText().toString());
    }

    /**
     * Test the target's click listener
     */
    public void testTargetClick() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final View container = mCoachMark.getContentView();
        final View target = container.findViewById(R.id.punch_hole_coach_mark_target);

        TouchUtils.tapView(this, mTargetView);

        // Touching textview should not propagated to global view.
        verify(mMockTargetClickListener, times(1)).onClick(container);
        verify(mMockCoachMarkClickListener, never()).onClick(container);
    }

    /**
     * Test the coachmark's click listener
     */
    public void testCoachMarkClick() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final View container = mCoachMark.getContentView();
        TouchUtils.tapView(this, container);

        verify(mMockTargetClickListener, never()).onClick(container);
        verify(mMockCoachMarkClickListener, times(1)).onClick(container);

        // Check whether global listener is working on tapping textview(message)
        // The container view should be checked whether it is clicked or not.
        // Because tap event propagated to parent view when target view don't have listener.
        TouchUtils.tapView(this, mTextView);

        verify(mMockTargetClickListener, never()).onClick(container);
        verify(mMockCoachMarkClickListener, times(2)).onClick(container);
    }

    /**
     * Test the location of punch hole to target view.
     */
    public void testCircleIsOverlayed() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final PunchHoleView container = (PunchHoleView) mCoachMark.getContentView();

        final float diameterGap = getActivity().getResources()
                .getDimension(R.dimen.punchhole_coach_mark_gap);

        // Get the coach mark and target view's coordinates of location on screen
        int[] containerScreenLoc = new int[2];
        container.getLocationOnScreen(containerScreenLoc);
        int[] targetScreenLoc = new int[2];
        mTargetView.getLocationOnScreen(targetScreenLoc);

        final int width = mTargetView.getWidth();
        final int height = mTargetView.getHeight();

        final int expectedCircleX = targetScreenLoc[0] + (width / 2) - containerScreenLoc[0];
        final int expectedCircleY = targetScreenLoc[1] + (height / 2) - containerScreenLoc[1];
        final float expectedCircleRadius = (height + diameterGap) / 2;

        assertFalse(container.setCircle(expectedCircleX, expectedCircleY, expectedCircleRadius));
    }

    /**
     * Test that the message is shown above when target view located in bottom side.
     */
    public void testMessageLoacatedAbove() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final PunchHoleView container = (PunchHoleView) mCoachMark.getContentView();

        moveTargetView(getInstrumentation(), mTargetView,
                0, container.getHeight() - mTargetView.getHeight()); // Move target view to bottom

        // Get the coach mark and textview's coordinates of location on screen
        int[] containerScreenLoc = new int[2];
        container.getLocationOnScreen(containerScreenLoc);
        int[] textScreenLoc = new int[2];
        mTextView.getLocationOnScreen(textScreenLoc);

        final int height = container.getHeight();
        final int relativeTextViewX = textScreenLoc[1] - containerScreenLoc[1] + mTextView.getHeight() / 2;

        assertTrue(relativeTextViewX < (height / 2));
    }

    /**
     * Test that the message is shown below when target view located in top side.
     */
    public void testMessageLoacatedBelow() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final PunchHoleView container = (PunchHoleView) mCoachMark.getContentView();

        moveTargetView(getInstrumentation(), mTargetView, 0, 0); // Move target view to top

        // Get the coach mark and textview's coordinates of location on screen
        int[] containerScreenLoc = new int[2];
        container.getLocationOnScreen(containerScreenLoc);
        int[] textScreenLoc = new int[2];
        mTextView.getLocationOnScreen(textScreenLoc);

        final int height = container.getHeight();
        final int relativeTextViewX = textScreenLoc[1] - containerScreenLoc[1] + mTextView.getHeight() / 2;

        assertTrue(relativeTextViewX > (height / 2));
    }
}
