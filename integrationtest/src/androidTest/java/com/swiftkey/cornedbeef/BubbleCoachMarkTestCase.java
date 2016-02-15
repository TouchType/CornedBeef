package com.swiftkey.cornedbeef;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.SystemClock;
import android.test.ActivityInstrumentationTestCase2;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.WindowManager;
import android.widget.TextView;

import com.swiftkey.cornedbeef.test.R;
import com.swiftkey.cornedbeef.test.SpamActivity;

public class
        BubbleCoachMarkTestCase extends ActivityInstrumentationTestCase2<SpamActivity> {
    
    private static final int TIMEOUT = 1000;
    private static final int SLEEP = 100;
    
    private SpamActivity mActivity;
    private CoachMark mCoachMark;
    private View mAnchor;
    
    public BubbleCoachMarkTestCase() {
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
                mAnchor = mActivity.findViewById(R.id.coach_mark_test_anchor);   
            }
        });
        getInstrumentation().waitForIdleSync();
        waitUntilStatusBarHidden();
        
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(mActivity, mAnchor, "spam spam spam", Color.WHITE)
                .setBackgroundColor(Color.BLUE)
                .build();
    }
    
    public void tearDown() throws Exception {
        dismissCoachMark(mCoachMark);
        mCoachMark = null;
        mAnchor = null;
        mActivity = null;
        super.tearDown();
    }

    /**
     * Test that the popup is shown above the anchor when there is room to do so
     */
    public void testShowPopupAbove() {
        final View topArrow;
        final View bottomArrow;

        moveAnchor(0, 200);        
        showCoachMark(mCoachMark);
        
        ViewGroup content = (ViewGroup) mCoachMark.getContentView()
                .findViewById(R.id.coach_mark_content);
        TextView tv = (TextView) content.getChildAt(0);
        
        // Get anchor and content positions
        int[] anchorPos = new int[2];
        int[] contentPos = new int[2];
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);
        
        topArrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        bottomArrow = mCoachMark.getContentView().findViewById(R.id.bottom_arrow);

        assertEquals("spam spam spam", tv.getText());
        assertTrue(mCoachMark.isShowing());
        assertTrue(anchorPos[1] >= contentPos[1]+mAnchor.getHeight());
        assertEquals(View.GONE, topArrow.getVisibility());
        assertEquals(View.VISIBLE, bottomArrow.getVisibility());
    }

    /**
     * Test that the popup is shown below the anchor when there is no room above
     */
    public void testShowPopupBelow() {
        final View topArrow;
        final View bottomArrow;

        showCoachMark(mCoachMark);

        ViewGroup content = (ViewGroup) mCoachMark.getContentView()
                .findViewById(R.id.coach_mark_content);
        TextView tv = (TextView) content.getChildAt(0);
        
        // Get anchor and content positions
        int[] anchorPos = new int[2];
        int[] contentPos = new int[2];
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);
        
        topArrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        bottomArrow = mCoachMark.getContentView().findViewById(R.id.bottom_arrow);

        assertEquals("spam spam spam", tv.getText());
        assertTrue(mCoachMark.isShowing());
        assertTrue(anchorPos[1] <= contentPos[1]);
        assertEquals(View.VISIBLE, topArrow.getVisibility());
        assertEquals(View.GONE, bottomArrow.getVisibility());
    }
    
    /**
     * Test that the popup is shown below the anchor even if there is room above
     * but the showAboveAnchor variable is set
     */
    public void testShowPopupBelowRoomAbove() {
        final View topArrow;
        final View bottomArrow;
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam", Color.BLACK)
                .setShowBelowAnchor(true)
                .build();

        moveAnchor(0, 200);
        showCoachMark(mCoachMark);

        ViewGroup content = (ViewGroup) mCoachMark.getContentView()
                .findViewById(R.id.coach_mark_content);
        TextView tv = (TextView) content.getChildAt(0);
        
        // Get anchor and content positions
        int[] anchorPos = new int[2];
        int[] contentPos = new int[2];
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);
        
        topArrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        bottomArrow = mCoachMark.getContentView().findViewById(R.id.bottom_arrow);

        assertEquals("spam spam spam", tv.getText());
        assertTrue(mCoachMark.isShowing());
        assertTrue(anchorPos[1] <= contentPos[1]);
        assertEquals(View.VISIBLE, topArrow.getVisibility());
        assertEquals(View.GONE, bottomArrow.getVisibility());
    }
    
    /**
     * Test that the target arrow can be shown to the left of centre
     */
    public void testShowPopupTargetLeft() {
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam", Color.BLACK).setTargetOffset(0.25f).build();
        
        showCoachMark(mCoachMark);
        
        View arrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        MarginLayoutParams params = (MarginLayoutParams) arrow.getLayoutParams();

        assertTrue(mCoachMark.isShowing());
        assertTrue(params.leftMargin < mCoachMark.getContentView().getWidth()/2);
    }
    
    /**
     * Test that the target arrow can be shown to the right of centre
     */
    public void testShowPopupTargetRight() {
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam", Color.BLACK).setTargetOffset(0.75f).build();
        
        showCoachMark(mCoachMark);
        
        View arrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        MarginLayoutParams params = (MarginLayoutParams) arrow.getLayoutParams();

        assertTrue(mCoachMark.isShowing());
        assertTrue(params.leftMargin > mCoachMark.getContentView().getWidth()/2); 
    }

    /**
     * Test that the popup is removed when the user taps on it
     */
    public void testDismissOnTouch() {
        moveAnchor(0, 200);
        showCoachMark(mCoachMark);

        // Get coach mark location
        int[] contentPos = new int[2];
        mCoachMark.getContentView().getLocationOnScreen(contentPos);
        
        // Create up and down events
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        int x = contentPos[0]+mCoachMark.getContentView().getWidth()/2;
        int y = contentPos[1]+mCoachMark.getContentView().getHeight()/2;
        MotionEvent down = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_DOWN, x, y, 0);
        MotionEvent up = MotionEvent.obtain(downTime, eventTime,
                MotionEvent.ACTION_UP, x, y, 0);
        
        // Send up and down events
        getInstrumentation().sendPointerSync(down);
        getInstrumentation().sendPointerSync(up);

        assertFalse(mCoachMark.isShowing());
        
        down.recycle();
        up.recycle();
    }

    /**
     * Test that the position of the popup is updated when the anchor moves and
     * that the correct arrow is visible depending upon the coach mark position
     */
    public void testPopupMovesWhenAnchorMoves() {
        int[] anchorPos = new int[2];
        int[] contentPos = new int[2];
        final View topArrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        final View bottomArrow = mCoachMark.getContentView().findViewById(R.id.bottom_arrow);

        showCoachMark(mCoachMark);
        
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);
                
        assertTrue(mCoachMark.isShowing());
        assertEquals(View.VISIBLE, topArrow.getVisibility());
        assertEquals(View.GONE, bottomArrow.getVisibility());
        assertEquals(anchorPos[1]+mAnchor.getHeight(), contentPos[1]);
        
        moveAnchor(50, 200);
        
        int oldCoachMarkX = contentPos[0];
        int oldCoachMarkY = contentPos[1];
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);
                
        assertTrue(mCoachMark.isShowing());
        assertEquals(View.GONE, topArrow.getVisibility());
        assertEquals(View.VISIBLE, bottomArrow.getVisibility());
        assertEquals(anchorPos[1]-mCoachMark.getContentView().getHeight(), contentPos[1]);
        assertTrue(oldCoachMarkX != contentPos[0]);
        assertTrue(oldCoachMarkY != contentPos[1]);
    }
    
    /**
     * Verify that the popup is positioned correctly when a long message is set
     */
    public void testPositionedCorrectlyWithLongMessage() {
        int[] anchorPos = new int[2];
        int[] contentPos = new int[2];
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                mActivity,
                mAnchor,
                "This is a long message. We're using it to verify that when the popup content is" + 
                " wrapped over multiple lines, the popup is still positioned above the anchor." + 
                "Here is some more text to make sure that this the text spans multiple lines",
                Color.BLACK).build();
  
        moveAnchor(0, 600);
        showCoachMark(mCoachMark);
        
        final View topArrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        final View bottomArrow = mCoachMark.getContentView().findViewById(R.id.bottom_arrow);
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);

        // Verify that there is enough room above the anchor
        assertEquals(View.GONE, topArrow.getVisibility());
        assertEquals(View.VISIBLE, bottomArrow.getVisibility());
        
        // Bottom of coach mark should appear above the top of the anchor
        assertTrue(anchorPos[1] >= contentPos[1] + mCoachMark.getContentView().getHeight());
    }
    
    /**
     * Verify that the popup is positioned correctly when an internal anchor is set
     */
    public void testPositionedAboveInternalAnchor() {
        int[] anchorPos = new int[2];
        int[] contentPos = new int[2];
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam", Color.BLACK)
                .setInternalAnchor(0.25f, 0.5f, 0.5f, 0.5f).build();
        
        moveAnchor(0, 200);
        showCoachMark(mCoachMark);
        
        final View topArrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        final View bottomArrow = mCoachMark.getContentView().findViewById(R.id.bottom_arrow);
        final int popupHeight = mCoachMark.getContentView().getHeight();
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);

        // Verify that there is enough room above the anchor
        assertEquals(View.GONE, topArrow.getVisibility());
        assertEquals(View.VISIBLE, bottomArrow.getVisibility());
        
        // Verify that the coach mark is positioned within the anchor - not above
        assertTrue(contentPos[1] + popupHeight > anchorPos[1]);
        assertTrue(contentPos[1] + popupHeight < anchorPos[1] + mAnchor.getHeight());
    }
    
    /**
     * Verify that the target arrow is applied to the internal anchor rather
     * than the whole view when an internal anchor is specified
     */
    public void testTargetArrowPointsToInternalAnchor() {
        int[] anchorPos = new int[2];
        int[] contentPos = new int[2];
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                mActivity, mAnchor, "spam spam spam", Color.BLACK)
                .setTargetOffset(0.25f)
                .setInternalAnchor(0.5f, 0.5f, 0.5f, 0.5f)
                .build();
        
        showCoachMark(mCoachMark);
        
        mAnchor.getLocationOnScreen(anchorPos);
        mCoachMark.getContentView().getLocationOnScreen(contentPos);       
        View arrow = mCoachMark.getContentView().findViewById(R.id.top_arrow);
        MarginLayoutParams params = (MarginLayoutParams) arrow.getLayoutParams();

        assertTrue(mCoachMark.isShowing());
        assertTrue(contentPos[0]+params.leftMargin > anchorPos[0] + mAnchor.getWidth() * 0.50);
        assertTrue(contentPos[0]+params.leftMargin < anchorPos[0] + mAnchor.getWidth() * 0.75);
    }
    
    /**
     * Verify that coach mark padding is set correctly
     */
    public void testSetPadding() {
        int[] contentPos = new int[2];
        final int screenWidth = getInstrumentation().getTargetContext()
                .getResources().getDisplayMetrics().widthPixels;
        mCoachMark = new BubbleCoachMark.BubbleCoachMarkBuilder(
                mActivity, 
                mAnchor, 
                "This is a long coach mark, to which padding will be applied." +
                "For testing purposes it is important that this coach mark is" +
                " wider than the width of the screen, otherwise this will fail",
                Color.BLACK)
                .setPadding(10).build();
        
        showCoachMark(mCoachMark);
        
        mCoachMark.getContentView().getLocationOnScreen(contentPos);
        
        int paddingInPx = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 10, mActivity.getResources().getDisplayMetrics());

        assertEquals(paddingInPx, contentPos[0]);
        assertEquals(screenWidth-paddingInPx, contentPos[0]+mCoachMark.getContentView().getWidth());
    }
    
    /*
     * HELPERS
     */
    
    /**
     * Move the anchor view to the specified location
     */
    private void moveAnchor(final int x, final int y) {
        final ViewGroup.MarginLayoutParams params = 
                (MarginLayoutParams) mAnchor.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;
        
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mAnchor.setLayoutParams(params);
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
