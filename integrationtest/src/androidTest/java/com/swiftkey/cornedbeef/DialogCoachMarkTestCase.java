package com.swiftkey.cornedbeef;

import android.graphics.Rect;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.test.TouchUtils;
import android.test.ViewAsserts;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DialogCoachMarkTestCase extends ActivityInstrumentationTestCase2<SpamActivity> {

    private static final int TIMEOUT = 1000;
    private static final int SLEEP = 100;

    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;

    @Mock
    private View.OnClickListener mMockButtonClickListener;
    @Mock
    private View.OnClickListener mMockCoachMarkClickListener;

    private static final int IMAGE_RES_ID = R.drawable.sk_logo;
    private static final String MESSAGE = "spam spam spam";
    private static final String BUTTON_MESSAGE = "button";

    public DialogCoachMarkTestCase() {
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
            }
        });
        getInstrumentation().waitForIdleSync();
        waitUntilStatusBarHidden();

        mCoachMark = new DialogCoachMark.DialogCoachMarkBuilder(mActivity, mAnchor, MESSAGE)
                .setDrawable(mActivity.getResources().getDrawable(IMAGE_RES_ID))
                .setButtonText(BUTTON_MESSAGE)
                .setButtonClickListener(mMockButtonClickListener)
                .setGlobalClickListener(mMockCoachMarkClickListener)
                .build();
    }

    public void tearDown() throws Exception {
        dismissCoachMark(mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;

        mMockButtonClickListener = null;
        mMockCoachMarkClickListener = null;

        super.tearDown();
    }

    /**
     * Test the view creation and visibility.
     */
    public void testViewsCreatedAndVisible() {
        showCoachMark(mCoachMark);

        final View container = mCoachMark.getContentView();
        final ImageView image = (ImageView) container.findViewById(R.id.dialog_coach_mark_image);
        final TextView text = (TextView) container.findViewById(R.id.dialog_coach_mark_message);
        final Button button = (Button) container.findViewById(R.id.dialog_coach_mark_button);

        // Check the creation
        assertNotNull(getActivity());
        assertNotNull(mCoachMark);
        assertNotNull(container);
        assertNotNull(image);
        assertNotNull(text);
        assertNotNull(button);

        // Check the visibility
        ViewAsserts.assertOnScreen(container, image);
        ViewAsserts.assertOnScreen(container, text);
        ViewAsserts.assertOnScreen(container, button);

        // Check the resources which passed by builder
        assertEquals(image.getDrawable().getConstantState(),
                getActivity().getResources().getDrawable(IMAGE_RES_ID).getConstantState());
        assertEquals(MESSAGE, text.getText().toString());
        assertEquals(BUTTON_MESSAGE, button.getText().toString());
    }

    /**
     * Test the button's click listener
     */
    public void testButtonClick() {
        showCoachMark(mCoachMark);

        final View container = mCoachMark.getContentView();
        final Button button = (Button) container.findViewById(R.id.dialog_coach_mark_button);

        TouchUtils.tapView(this, button);

        // Clicking button should not propagated to global view.
        verify(mMockButtonClickListener, times(1)).onClick(button);
        verify(mMockCoachMarkClickListener, never()).onClick(button);
        verify(mMockCoachMarkClickListener, never()).onClick(container);
    }

    /**
     * Test the coachmark's click listener
     */
    public void testCoachMarkClick() {
        showCoachMark(mCoachMark);

        final View container = mCoachMark.getContentView();
        TouchUtils.tapView(this, container);

        verify(mMockButtonClickListener, never()).onClick(container);
        verify(mMockCoachMarkClickListener, times(1)).onClick(container);

        // Check whether global listener is working on tapping image(logo)
        // The container view should be checked whether it is clicked or not.
        // Because tap event propagated to parent view when target view don't have listener.
        final ImageView image = (ImageView) container.findViewById(R.id.dialog_coach_mark_image);
        TouchUtils.tapView(this, image);

        verify(mMockButtonClickListener, never()).onClick(container);
        verify(mMockCoachMarkClickListener, times(2)).onClick(container);

        // Check whether global listener is working on tapping text(message)
        final TextView text = (TextView) container.findViewById(R.id.dialog_coach_mark_message);
        TouchUtils.tapView(this, text);

        verify(mMockButtonClickListener, never()).onClick(container);
        verify(mMockCoachMarkClickListener, times(3)).onClick(container);
    }

    /*
     * HELPERS
     */

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
