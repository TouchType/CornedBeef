package com.swiftkey.cornedbeef;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

import static com.swiftkey.cornedbeef.CoachMark.OnDismissListener;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
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
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_anchor);
            }
        });
        getInstrumentation().waitForIdleSync();
        
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(mActivity, mAnchor, "spam spam spam").build();
    }
    
    public void tearDown() throws Exception {
        dismissCoachMark(mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;
        super.tearDown();
    }
    
    /**
     * Test that the popup is removed when dismiss is called
     */
    public void testDismissMethodCall() {
        showCoachMark(mCoachMark);

        assertTrue(mCoachMark.isShowing());
        
        dismissCoachMark(mCoachMark);

        assertFalse(mCoachMark.isShowing());
    }
    
    /**
     * Test that the popup disappears after the specified timeout has passed
     */
    public void testDismissAfterTimeout() throws InterruptedException {
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam").setTimeout(1000).build();
        
        showCoachMark(mCoachMark);

        assertTrue(mCoachMark.isShowing());

        Thread.sleep(2000);
        
        assertFalse(mCoachMark.isShowing());
    }

    /**
     * Test that the popup disappears if the anchor is not visible
     */
    public void testDismissWhenAnchorNotVisible() {       
        showCoachMark(mCoachMark);
        
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
        
        showCoachMark(mCoachMark);
        dismissCoachMark(mCoachMark);
        
        verify(mockListener, times(1)).onDismiss();
    }
    
    /**
     * Verify that onShow is called when coach mark is shown
     * @throws InterruptedException 
     */
    public void testOnShowCalled() throws InterruptedException {
        final CoachMark.OnShowListener mockListener = mock(CoachMark.OnShowListener.class);
        mCoachMark = new TestCoachMark.TestCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam")
                .setOnShowListener(mockListener).build();
        
        showCoachMark(mCoachMark);
        
        verify(mockListener, times(1)).onShow();
    }
    
    /*
     * HELPERS
     */
    
    private static class TestCoachMark extends CoachMark {

        protected TestCoachMark(CoachMarkBuilder builder) {
            super(builder);
        }

        @Override
        protected View createContentView(String message) {
            return new TextView(mContext);
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
}
