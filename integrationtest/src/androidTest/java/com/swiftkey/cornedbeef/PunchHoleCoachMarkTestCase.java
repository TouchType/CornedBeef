package com.swiftkey.cornedbeef;

import android.graphics.Rect;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class PunchHoleCoachMarkTestCase extends ActivityInstrumentationTestCase2<SpamActivity> {

    private static final int TIMEOUT = 1000;
    private static final int SLEEP = 100;

    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;
    private View mTargetView;

    @Mock
    private View.OnClickListener mMockTargetClickListener;
    @Mock
    private View.OnClickListener mMockCoachMarkClickListener;

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
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_anchor2);
                mTargetView = mActivity.findViewById(R.id.coach_mark_test_target);
            }
        });
        getInstrumentation().waitForIdleSync();
        waitUntilStatusBarHidden();

        mCoachMark = new PunchHoleCoachMark.PunchHoleCoachMarkBuilder(mActivity, mAnchor, MESSAGE)
                .setMessage(MESSAGE)
                .setTargetView(mTargetView)
                .setOnTargetClickListener(mMockTargetClickListener)
                .setOnGlobalClickListener(mMockCoachMarkClickListener)
                .build();
    }

    public void tearDown() throws Exception {
        dismissCoachMark(mCoachMark);
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
        showCoachMark(mCoachMark);

        final View container = mCoachMark.getContentView();
        final TextView text = (TextView) container.findViewById(R.id.punchhole_coach_mark_message);

        // Check the creation
        assertNotNull(getActivity());
        assertNotNull(mCoachMark);
        assertNotNull(container);
        assertNotNull(text);

        // Check the visibility
        ViewAsserts.assertOnScreen(container, text);
        ViewAsserts.assertHorizontalCenterAligned(container, text);

        // Check the resources which passed by builder
        assertEquals(MESSAGE, text.getText().toString());
    }

    /**
     * Test the target's click listener
     */
    public void testTargetClick() {
        showCoachMark(mCoachMark);

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
        showCoachMark(mCoachMark);

        final View container = mCoachMark.getContentView();
        TouchUtils.tapView(this, container);

        verify(mMockTargetClickListener, never()).onClick(container);
        verify(mMockCoachMarkClickListener, times(1)).onClick(container);

        // Check whether global listener is working on tapping textview(message)
        // The container view should be checked whether it is clicked or not.
        // Because tap event propagated to parent view when target view don't have listener.
        final TextView text = (TextView) container.findViewById(R.id.punchhole_coach_mark_message);
        TouchUtils.tapView(this, text);

        verify(mMockTargetClickListener, never()).onClick(container);
        verify(mMockCoachMarkClickListener, times(2)).onClick(container);
    }

    /**
     * Test the location of punch hole to target view.
     */
    public void testCircleIsOverlayed() {
        showCoachMark(mCoachMark);

        final PunchHoleView container = (PunchHoleView) mCoachMark.getContentView();
        final View target = ((PunchHoleCoachMark) mCoachMark).getTargetView();

        final float diameterGap = getActivity().getResources()
                .getDimension(R.dimen.punchhole_coach_mark_gap);

        // Get the coach mark and target view's coordinates of location on screen
        int[] containerScreenLoc = new int[2];
        container.getLocationOnScreen(containerScreenLoc);
        int[] targetScreenLoc = new int[2];
        target.getLocationOnScreen(targetScreenLoc);

        final int width = target.getWidth();
        final int height = target.getHeight();

        final int expectedCircleX = targetScreenLoc[0] + width / 2;
        final int expectedCircleY = targetScreenLoc[1] + height / 2;
        final float expectedCircleRadius = (height + diameterGap) / 2;

        final int actualCircleX = container.getCircleCenterX() + containerScreenLoc[0];
        final int actualCircleY = container.getCircleCenterY() + containerScreenLoc[1];
        final float actualCircleRadius = container.getCircleRadius();

        assertEquals(expectedCircleX, actualCircleX);
        assertEquals(expectedCircleY, actualCircleY);
        assertEquals(expectedCircleRadius, actualCircleRadius);
    }

    /**
     * Test that the message is shown above when target view located in bottom side.
     */
    public void testMessageLoacatedAbove() {
        showCoachMark(mCoachMark);

        final PunchHoleView container = (PunchHoleView) mCoachMark.getContentView();
        final View target = ((PunchHoleCoachMark) mCoachMark).getTargetView();
        final View text = container.findViewById(R.id.punchhole_coach_mark_message);
        final View upperGap = container.findViewById(R.id.punchhole_coach_mark_upper_gap);
        final View lowerGap = container.findViewById(R.id.punchhole_coach_mark_lower_gap);

        moveTargetView(0, container.getHeight() - target.getHeight()); // Move to bottom

        // Get the coach mark and textview's coordinates of location on screen
        int[] containerScreenLoc = new int[2];
        container.getLocationOnScreen(containerScreenLoc);
        int[] textScreenLoc = new int[2];
        text.getLocationOnScreen(textScreenLoc);

        final int height = container.getHeight();
        final int relativeTextViewX = textScreenLoc[1] - containerScreenLoc[1] + text.getHeight() / 2;

        assertTrue(relativeTextViewX < (height / 2));
        assertTrue(upperGap.getLayoutParams().height == 0);
        assertTrue(lowerGap.getLayoutParams().height != 0);
    }

    /**
     * Test that the message is shown below when target view located in top side.
     */
    public void testMessageLoacatedBelow() {
        showCoachMark(mCoachMark);

        final PunchHoleView container = (PunchHoleView) mCoachMark.getContentView();
        final View text = container.findViewById(R.id.punchhole_coach_mark_message);
        final View upperGap = container.findViewById(R.id.punchhole_coach_mark_upper_gap);
        final View lowerGap = container.findViewById(R.id.punchhole_coach_mark_lower_gap);

        moveTargetView(0, 0); // Move to top

        // Get the coach mark and textview's coordinates of location on screen
        int[] containerScreenLoc = new int[2];
        container.getLocationOnScreen(containerScreenLoc);
        int[] textScreenLoc = new int[2];
        text.getLocationOnScreen(textScreenLoc);

        final int height = container.getHeight();
        final int relativeTextViewX = textScreenLoc[1] - containerScreenLoc[1] + text.getHeight() / 2;

        assertTrue(relativeTextViewX > (height / 2));
        assertTrue(upperGap.getLayoutParams().height != 0);
        assertTrue(lowerGap.getLayoutParams().height == 0);
    }

    /*
     * HELPERS
     */

    /**
     * Move the anchor view to the specified location
     */
    private void moveTargetView(final int x, final int y) {
        final ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) mTargetView.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mTargetView.setLayoutParams(params);
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    /**
     * Call {@link CoachMark#show()} on the given {@link CoachMark}
     */
    private void showCoachMark(final CoachMark coachMark) {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                coachMark.show();
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    /**
     * Call {@link CoachMark#dismiss()}
     * on the given {@link CoachMark}
     */
    private void dismissCoachMark(final CoachMark coachMark) {
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                coachMark.dismiss();
            }
        });
        getInstrumentation().waitForIdleSync();
    }

    /**
     * Wait until the status bar is fully hidden
     */
    private void waitUntilStatusBarHidden() {
        final Rect rect = new Rect();
        final long startTime = SystemClock.uptimeMillis();
        do {
            try {
                mActivity.getWindow().getDecorView()
                        .getWindowVisibleDisplayFrame(rect);
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                break;
            }
        } while (SystemClock.uptimeMillis() - startTime < TIMEOUT && rect.top != 0);
    }
}
