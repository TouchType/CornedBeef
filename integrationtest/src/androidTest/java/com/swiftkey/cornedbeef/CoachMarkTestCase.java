package com.swiftkey.cornedbeef;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import static com.swiftkey.cornedbeef.CoachMark.OnShowListener;
import static com.swiftkey.cornedbeef.CoachMark.OnDismissListener;
import static com.swiftkey.cornedbeef.CoachMark.OnTimeoutListener;
import static com.swiftkey.cornedbeef.TestHelper.dismissCoachMark;
import static com.swiftkey.cornedbeef.TestHelper.showCoachMark;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CoachMarkTestCase extends ActivityInstrumentationTestCase2<SpamActivity> {
    
    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;
    
    public CoachMarkTestCase() {
        super(SpamActivity.class);
    }
    
    public void setUp() throws Exception {
        super.setUp();
        System.setProperty("dexmaker.dexcache", getInstrumentation().getTargetContext().getCacheDir().getAbsolutePath());

        mActivity = getActivity();
        
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
    
    public void tearDown() throws Exception {
        dismissCoachMark(getInstrumentation(), mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;
        super.tearDown();
    }
    
    /**
     * Test that the popup is removed when dismiss is called
     */
    public void testDismissMethodCall() {
        showCoachMark(getInstrumentation(), mCoachMark);

        assertTrue(mCoachMark.isShowing());
        
        dismissCoachMark(getInstrumentation(), mCoachMark);

        assertFalse(mCoachMark.isShowing());
    }

    /**
     * Test that the popup disappears after the specified timeout has passed
     */
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
     * Verify that onDismiss is called when coach mark is dismissed
     * @throws InterruptedException 
     */
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
