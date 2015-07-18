package com.swiftkey.cornedbeef;

import android.content.Context;
import android.view.View;
import android.widget.PopupWindow;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static com.swiftkey.cornedbeef.CoachMark.CoachMarkDimens;

@RunWith(RobolectricTestRunner.class)
public class InternallyAnchoredCoachMarkTestCase {

    private Context getContext() {
        return RuntimeEnvironment.application.getApplicationContext();
    }

    @Test
    public void testGetAnchorDimensNoInternalAnchor() {
        View mockAnchor = new MockView(getContext(), 0, 0, 300, 200);
        CoachMark coachMark = new TestInternallyAnchoredCoachMark
                .TestInternallyAnchoredCoachMarkBuilder(getContext(), mockAnchor, null).build();
        
        CoachMarkDimens<Integer> dimens = coachMark.getAnchorDimens();
        assertEquals((Integer) 0,   dimens.x);
        assertEquals((Integer) 0,   dimens.y);
        assertEquals((Integer) 300, dimens.width);
        assertEquals((Integer) 200, dimens.height);
    }

    @Test
    public void testGetAnchorDimenswithInternalAnchor() {
        View mockAnchor = new MockView(getContext(), 10, 40, 300, 200);
        CoachMark coachMark = new TestInternallyAnchoredCoachMark
                .TestInternallyAnchoredCoachMarkBuilder(getContext(), mockAnchor, null)
                     .setInternalAnchor(0.1f, 0.3f, 0.1f, 0.2f)
                     .build();
        
        CoachMarkDimens<Integer> dimens = coachMark.getAnchorDimens();
        assertEquals((Integer) (10+30), dimens.x);      // anchor x-loc + offset
        assertEquals((Integer) (40+60), dimens.y);      // anchor y-loc + offset
        assertEquals((Integer) 30,      dimens.width);
        assertEquals((Integer) 40,      dimens.height);
    }

    private static class MockView extends View {

        private final int xLoc;
        private final int yLoc;
        
        public MockView(Context context,  int x, int y, int measuredWidth, int measuredHeight) {
            super(context);
            xLoc = x;
            yLoc = y;
            setMeasuredDimension(measuredWidth, measuredHeight);
        }
        
        public void getLocationOnScreen(int[] loc) {
            loc[0] = xLoc;
            loc[1] = yLoc;
        }
    }
    
    private static class TestInternallyAnchoredCoachMark extends InternallyAnchoredCoachMark {

        protected TestInternallyAnchoredCoachMark(TestInternallyAnchoredCoachMarkBuilder builder) {
            super(builder);

        }
        
        public static class TestInternallyAnchoredCoachMarkBuilder extends InternallyAnchoredCoachMarkBuilder {
           
            public TestInternallyAnchoredCoachMarkBuilder(Context context, View anchor, String message) {
                super(context, anchor, message);
            }

            @Override
            public CoachMark build() {
                return new TestInternallyAnchoredCoachMark(this);
            }
        }

        @Override
        protected View createContentView(View content) {
            return null;
        }

        @Override
        protected PopupWindow createNewPopupWindow(View contentView) {
            return mock(PopupWindow.class);
        }

        @Override
        protected CoachMarkDimens<Integer> getPopupDimens(CoachMarkDimens<Integer> anchorDimens) {
            return null;
        }

        @Override
        protected void updateView(CoachMarkDimens<Integer> popupDimens, CoachMarkDimens<Integer> anchorDimens) {            
        
        }
    }
}
