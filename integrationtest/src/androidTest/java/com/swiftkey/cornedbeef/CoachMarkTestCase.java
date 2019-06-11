package com.swiftkey.cornedbeef;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

import androidx.test.rule.ActivityTestRule;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static com.swiftkey.cornedbeef.CoachMark.OnDismissListener;
import static com.swiftkey.cornedbeef.CoachMark.OnShowListener;
import static com.swiftkey.cornedbeef.CoachMark.OnTimeoutListener;
import static com.swiftkey.cornedbeef.TestHelper.dismissCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.showCoachMark;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CoachMarkTestCase {
    
    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;

    @Rule
    public ActivityTestRule<SpamActivity> mActivityRule =
            new ActivityTestRule<>(SpamActivity.class, false, true);

    @Before
    public void setUp() {
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getAbsolutePath());

        mActivity = mActivityRule.getActivity();
        
        getInstrumentation().runOnMainSync(new Runnable() {
            
            @Override
            public void run() {
                mActivity.getWindow().setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.setContentView(R.layout.coach_mark_test_activity);
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_empty_anchor);
            }
        });
        getInstrumentation().waitForIdleSync();
        
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(mActivity, mAnchor, "spam spam spam").build();
    }

    @After
    public void tearDown() {
        dismissCoachMark(getInstrumentation(), mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;
    }
    
    /**
     * Test that the popup is removed when dismiss is called
     */
    @Test
    public void testDismissMethodCall() {
        showCoachMark(getInstrumentation(), mCoachMark);

        assertTrue(mCoachMark.isShowing());
        
        dismissCoachMark(getInstrumentation(), mCoachMark);

        assertFalse(mCoachMark.isShowing());
    }

    /**
     * Test that the popup disappears after the specified timeout has passed
     */
    @Test
    public void testDismissAfterTimeout() throws InterruptedException {
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam").setTimeout(1000).build();
        
        showCoachMark(getInstrumentation(), mCoachMark);

        assertTrue(mCoachMark.isShowing());

        Thread.sleep(2000);
        
        assertFalse(mCoachMark.isShowing());
    }

    /**
     * Test that the popup disappears if the anchor is not visible
     */
    @Test
    public void testDismissWhenAnchorNotVisible() {       
        showCoachMark(getInstrumentation(), mCoachMark);
        
        assertTrue(mCoachMark.isShowing());

        getInstrumentation().runOnMainSync(new Runnable() {
            
            @Override
            public void run() {
                mAnchor.setVisibility(View.GONE);
            }
        });
        getInstrumentation().waitForIdleSync();
        
        assertFalse(mCoachMark.isShowing());
    }

    /**
     * Test that the popup disappears after the anchor view detaches if specified in the builder
     */
    @Test
    public void testDismissAfterAnchorDetach() throws InterruptedException {
        checkSetDismissOnAnchorDetach(true);
        checkSetDismissOnAnchorDetach(false);
    }

    /**
     * Helper method used in testDismissAfterAnchorDetach
     *
     * @param shouldAutoDismissOnDetach whether we should auto dismiss the coach mark
     * @throws InterruptedException
     */
    private void checkSetDismissOnAnchorDetach(boolean shouldAutoDismissOnDetach) throws InterruptedException {
        final View anchorView = new View(mActivity);
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mActivity.setContentView(anchorView);
            }
        });

        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, anchorView, "spam spam spam")
                .setDismissOnAnchorDetach(shouldAutoDismissOnDetach).build();

        showCoachMark(getInstrumentation(), mCoachMark);

        assertTrue(mCoachMark.isShowing());

        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mActivity.setContentView(R.layout.coach_mark_test_activity);
            }
        });

        if (shouldAutoDismissOnDetach) {
            assertFalse(mCoachMark.isShowing());
        } else {
            assertTrue(mCoachMark.isShowing());
        }
    }

    /**
     * Verify that onDismiss is called when coach mark is dismissed
     * @throws InterruptedException 
     */
    @Test
    public void testOnDismissCalled() throws InterruptedException {
        final OnDismissListener mockListener = mock(OnDismissListener.class);
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam")
                .setOnDismissListener(mockListener).build();
        
        showCoachMark(getInstrumentation(), mCoachMark);
        dismissCoachMark(getInstrumentation(), mCoachMark);
        
        verify(mockListener).onDismiss();
    }

    /**
     * Verify that onTimeout is called when coach mark's timeout expired
     * @throws InterruptedException
     */
    @Test
    public void testOnTimeoutCalled() throws InterruptedException {
        final long timeout = 50;
        final OnTimeoutListener mockListener = mock(OnTimeoutListener.class);
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam")
                .setOnTimeoutListener(mockListener)
                .setTimeout(timeout)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);

        // Wait until timeout
        Thread.sleep(timeout);

        verify(mockListener).onTimeout();
    }

    /**
     * Verify that onTimeout and onDismiss is called when coach mark's timeout expired
     * and the coach mark dismissed
     * @throws InterruptedException
     */
    @Test
    public void testOnTimeoutAndOnDismissCalled() throws InterruptedException {
        final long timeout = 50;
        final OnTimeoutListener mockOnTimeoutListener = mock(OnTimeoutListener.class);
        final OnDismissListener mockOnDismissListener = mock(OnDismissListener.class);
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam")
                .setOnTimeoutListener(mockOnTimeoutListener)
                .setOnDismissListener(mockOnDismissListener)
                .setTimeout(timeout)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);

        // Wait until timeout
        Thread.sleep(timeout);

        verify(mockOnTimeoutListener).onTimeout();
        verify(mockOnDismissListener).onDismiss();
    }

    /**
     * Verify that onDismiss is called and onTimeout not called
     * when explicitly dismiss() method called before timeout expired
     * @throws InterruptedException
     */
    @Test
    public void testOnDismissCalledAndOnTimeoutNotCalled() throws InterruptedException {
        final long timeout = 1000; // Should give the enough time for dismiss
        final OnTimeoutListener mockOnTimeoutListener = mock(OnTimeoutListener.class);
        final OnDismissListener mockOnDismissListener = mock(OnDismissListener.class);
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam")
                .setOnTimeoutListener(mockOnTimeoutListener)
                .setOnDismissListener(mockOnDismissListener)
                .setTimeout(timeout)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);
        dismissCoachMark(getInstrumentation(), mCoachMark);

        verify(mockOnTimeoutListener, never()).onTimeout();
        verify(mockOnDismissListener).onDismiss();
    }

    /**
     * Verify that onShow is called when coach mark is shown
     * @throws InterruptedException 
     */
    @Test
    public void testOnShowCalled() throws InterruptedException {
        final OnShowListener mockListener = mock(OnShowListener.class);
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam")
                .setOnShowListener(mockListener).build();
        
        showCoachMark(getInstrumentation(), mCoachMark);
        
        verify(mockListener).onShow();
    }

    /**
     * Verify that onShow, onTimeout and onDismiss is called when coach mark's timeout expired
     * and the coach mark dismissed
     * @throws InterruptedException
     */
    @Test
    public void testAllListenerCalled() throws InterruptedException {
        final long timeout = 50;
        final OnShowListener mockOnShowListener = mock(OnShowListener.class);
        final OnTimeoutListener mockOnTimeoutListener = mock(OnTimeoutListener.class);
        final OnDismissListener mockOnDismissListener = mock(OnDismissListener.class);
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam")
                .setOnShowListener(mockOnShowListener)
                .setOnTimeoutListener(mockOnTimeoutListener)
                .setOnDismissListener(mockOnDismissListener)
                .setTimeout(timeout)
                .build();

        showCoachMark(getInstrumentation(), mCoachMark);

        // Wait until timeout
        Thread.sleep(timeout);

        verify(mockOnShowListener).onShow();
        verify(mockOnTimeoutListener).onTimeout();
        verify(mockOnDismissListener).onDismiss();
    }

    /**
     * Test coach mark focusable.
     */
    @Test
    public void testViewsFocusable() {
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam").build();
        showCoachMark(getInstrumentation(), mCoachMark);
        mCoachMark.setFocusable(true);
        assertTrue(mCoachMark.isFocusable());
        mCoachMark.setFocusable(false);
        assertFalse(mCoachMark.isFocusable());
    }

    /*
     * HELPERS
     */
    
    private static class TestCoachMark extends CoachMark {

        protected TestCoachMark(CoachMarkBuilder builder) {
            super(builder);
        }

        @Override
        protected View createContentView(View content) {
            return content;
        }

        @Override
        protected PopupWindow createNewPopupWindow(View contentView) {
            return new PopupWindow(contentView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        @Override
        protected CoachMarkDimens<Integer> getAnchorDimens() {
            return null;
        }

        @Override
        protected CoachMarkDimens<Integer> getPopupDimens(CoachMarkDimens<Integer> anchorDimens) {
            return new CoachMarkDimens<>(0, 0, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }

        @Override
        protected void updateView(CoachMarkDimens<Integer> popupDimens, CoachMarkDimens<Integer> anchorDimens) {
            
        }
        
        public static class TestCoachMarkBuilder extends CoachMarkBuilder {

            public TestCoachMarkBuilder(Context context, View anchor, String text) {
                super(context, anchor, text);
            }

            @Override
            public CoachMark build() {
                return new TestCoachMark(this);
            }
        }
    }
}
