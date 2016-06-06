package com.swiftkey.cornedbeef;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
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
    private final int OVERLAY_COLOR = Color.BLACK;

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
        mTextView.setTextColor(Color.WHITE); // to make visual debugging easier
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

    public void testViewsCreatedAndVisible_noAnimation() {
        setupCoachmark(false);
        checkViewsCreatedAndVisible();
    }

    public void testViewsCreatedAndVisible_animation() {
        setupCoachmark(true);
        checkViewsCreatedAndVisible();
    }

    public void testOverlayCorrectColor() {
        setupCoachmark(false);
        final View container = mCoachMark.getContentView();
        int color = ((ColorDrawable) container.getBackground()).getColor();
        assertEquals(OVERLAY_COLOR, color);
    }

    /**
     * Test the view creation and visibility.
     */
    private void checkViewsCreatedAndVisible() {
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

    public void testTargetClick_noAnimation() {
        setupCoachmark(false);
        checkTargetClick();
    }

    public void testTargetClick_animation() {
        setupCoachmark(true);
        checkTargetClick();
    }

    /**
     * Test the target's click listener
     */
    private void checkTargetClick() {
        showCoachMark(getInstrumentation(), mCoachMark);

        final View container = mCoachMark.getContentView();
        final View target = container.findViewById(R.id.punch_hole_coach_mark_target);

        TouchUtils.tapView(this, mTargetView);

        // Touching textview should not propagated to global view.
        verify(mMockTargetClickListener, times(1)).onClick(container);
        verify(mMockCoachMarkClickListener, never()).onClick(container);
    }

    public void testCoachMarkClick_noAnimation() {
        setupCoachmark(false);
        checkCoachMarkClick();
    }

    public void testCoachMarkClick_animation() {
        setupCoachmark(true);
        checkCoachMarkClick();
    }

    /**
     * Test the coachmark's click listener
     */
    private void checkCoachMarkClick() {
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

    public void testCircleIsOverlayed_noAnimation() {
        setupCoachmark(false);
        checkCircleIsOverlayed(false);
    }

    public void testCircleIsOverlayed_animationCannotHappen() {
        setupCoachmark(true);
        // the target view is too small for the animation to happen so we just
        // centre the punch hole on the target view
        checkCircleIsOverlayed(false);
    }

    public void testCircleIsOverlayed_animationCanHappen() {
        mTargetView = mActivity.findViewById(R.id.coach_mark_test_target_wide);
        setupCoachmark(true);
        // the target view is wide enough for the animation to happen
        checkCircleIsOverlayed(true);
    }

    /**
     * Test the location of punch hole to target view.
     */
    private void checkCircleIsOverlayed(final boolean animationShouldHappen) {
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

        final float expectedCircleRadius = (height + diameterGap) / 2;

        final int expectedCircleStartOffsetX = animationShouldHappen
                ?  targetScreenLoc[0] + (int) expectedCircleRadius
                : width / 2;
        final int expectedCircleStartX = targetScreenLoc[0] + expectedCircleStartOffsetX - containerScreenLoc[0];
        final int expectedCircleStartY = targetScreenLoc[1] + (height / 2) - containerScreenLoc[1];

        // When the animation happens, the circle should not be in the start
        // position. Unfortunately we can't test that it ever was in the right
        // start position in this case as it starts moving when it's shown.
        assertEquals(
                animationShouldHappen,
                container.setCircle(expectedCircleStartX, expectedCircleStartY, expectedCircleRadius));
    }

    public void testMessageLocatedAbove_noAnimation() {
        setupCoachmark(false);
        checkMessageLocatedAbove();
    }

    public void testMessageLocatedAbove_animation() {
        setupCoachmark(true);
        checkMessageLocatedAbove();
    }

    /**
     * Test that the message is shown above when target view located in bottom side.
     */
    private void checkMessageLocatedAbove() {
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

    public void testMessageLocatedBelow_noAnimation() {
        setupCoachmark(false);
        checkMessageLocatedBelow();
    }

    public void testMessageLocatedBelow_animation() {
        setupCoachmark(true);
        checkMessageLocatedBelow();
    }

    /**
     * Test that the message is shown below when target view located in top side.
     */
    private void checkMessageLocatedBelow() {
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

    private void setupCoachmark(final boolean animation) {
        mCoachMark = new PunchHoleCoachMark.PunchHoleCoachMarkBuilder(mActivity, mAnchor, mTextView)
                .setTargetView(mTargetView)
                .setHorizontalTranslationDuration(animation ? 1000 : 0)
                .setOnTargetClickListener(mMockTargetClickListener)
                .setOnGlobalClickListener(mMockCoachMarkClickListener)
                .setOverlayColor(OVERLAY_COLOR)
                .build();
    }
}
