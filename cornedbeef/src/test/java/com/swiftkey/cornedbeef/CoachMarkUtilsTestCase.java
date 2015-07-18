package com.swiftkey.cornedbeef;

import android.graphics.Point;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static junit.framework.Assert.assertEquals;
import static com.swiftkey.cornedbeef.CoachMark.CoachMarkDimens;

@RunWith(RobolectricTestRunner.class)
public class CoachMarkUtilsTestCase {
    
    private static final float TARGET_LEFT = 0.25f;
    private static final float TARGET_RIGHT = 0.75f;
    private static final float TARGET_CENTRE = 0.5f;
    private static final int SCREEN_WIDTH = 1080;
    private static final int SCREEN_HEIGHT = 1920;
    private static final int NO_PADDING = 0;
    private static final int MAX_POPUP_WIDTH = SCREEN_WIDTH;
    private static final int FULL_ANCHOR_WIDTH = SCREEN_WIDTH;
    private static final int ANCHOR_HEIGHT = 10;
    private static final int POPUP_HEIGHT = 10;
    private static final int ARROW_WIDTH = 10;
    private static final int MIN_ARROW_MARGIN = 0;
    private static final int MAX_ARROW_MARGIN = SCREEN_WIDTH;
    private static final boolean SHOW_BELOW = true;
    private static final boolean SHOW_ABOVE = false;

    /**
     * If the desired width of the popup is greater than the width of the
     * screen, then ensure that the popup is clipped to fit on-screen
     */
    @Test
    public void testGetPopupWidth_contentWiderThanScreen() {
        final int minWidth = 15;
        final int desiredWidth = SCREEN_WIDTH + 10;

        final int popupWidth = CoachMarkUtils.getPopupWidth(
                minWidth,
                MAX_POPUP_WIDTH,
                desiredWidth,
                FULL_ANCHOR_WIDTH,
                TARGET_CENTRE);

        assertEquals(SCREEN_WIDTH, popupWidth);
    }
    
    /**
     * If the target arrow would otherwise be positioned to the left of the
     * popup, then ensure that the popup is expanded to the appropriate size
     */
    @Test
    public void testGetPopupWidth_targetLeftOfContent() {
        final int minWidth = 15;
        final int desiredWidth = 100;
        
        final int popupWidth = CoachMarkUtils.getPopupWidth(
                minWidth,
                MAX_POPUP_WIDTH, 
                desiredWidth,
                FULL_ANCHOR_WIDTH, 
                TARGET_LEFT);

        assertEquals(555, popupWidth);
    }

    /**
     * If the target arrow would otherwise be positioned to the right of the
     * popup, then ensure that the popup is expanded to the appropriate size
     */
    @Test
    public void testGetPopupWidth_targetRightOfContent() {
        final int minWidth = 15;
        final int desiredWidth = 100;
        
        final int popupWidth = CoachMarkUtils.getPopupWidth(
                minWidth,
                MAX_POPUP_WIDTH, 
                desiredWidth,
                FULL_ANCHOR_WIDTH, 
                TARGET_RIGHT);

        assertEquals(555, popupWidth);
    }
    
    /**
     * If the target arrow fits within the popup, then ensure that the popup
     * keeps its desired width
     */
    @Test
    public void testGetPopupWidth_targetWithinContent() {
        final int minWidth = 15;
        final int desiredWidth = 100;
        
        final int popupWidth = CoachMarkUtils.getPopupWidth(
                minWidth,
                MAX_POPUP_WIDTH, 
                desiredWidth,
                FULL_ANCHOR_WIDTH, 
                TARGET_CENTRE);
        
        assertEquals(100, popupWidth);
    }
    
    /**
     * Test that correct coords are returned if the popup is shorter than the anchor
     */
    @Test
    public void testGetPopupPosition_popupSmallerThanAnchor() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                0, 
                100,
                FULL_ANCHOR_WIDTH, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                NO_PADDING,
                SHOW_ABOVE);
        
        assertEquals(new Point(490, 90), popupPos);
    }

    /**
     * Test that correct coords are returned if the popup is longer than the anchor
     */
    @Test
    public void testGetPopupPosition_popupLargerThanAnchor() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                515, 
                100,
                50, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                NO_PADDING,
                SHOW_ABOVE);
        
        assertEquals(new Point(490, 90), popupPos);
    }
    
    /**
     * If the popup is wider than the anchor and there is not enough room to the
     * left of the anchor for the popup to be centred over the anchor, then
     * ensure that that popup is still positioned on-screen
     */
    @Test
    public void testGetPopupPosition_popupLargerThanAnchorNoRoomLeft() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                0, 
                100,
                50, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                NO_PADDING,
                SHOW_ABOVE);
        
        assertEquals(new Point(0, 90), popupPos);
    }

    /**
     * If the popup is wider than the anchor and there is not enough room to the
     * right of the anchor for the popup to be centred over the anchor, then
     * ensure that that popup is still positioned on-screen 
     */
    @Test
    public void testGetPopupPosition_popupLargerThanAnchorNoRoomRight() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                SCREEN_WIDTH-50, 
                100,
                50, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                NO_PADDING,
                SHOW_ABOVE);
        
        assertEquals(new Point(980, 90), popupPos);
    }
    
    /**
     * Test that the popup is positioned below the anchor if there is not enough
     * room above it
     */
    @Test
    public void testGetPopupPosition_popupLargerThanAnchorNoRoomTop() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                0,
                0,
                SCREEN_WIDTH, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                NO_PADDING,
                SHOW_ABOVE);
        
        assertEquals(new Point(490, 10), popupPos);
    }
    
    /**
     * Test that the popup is positioned above the anchor if there is not enough
     * room below it
     */
    @Test
    public void testGetPopupPosition_popupLargerThanAnchorNoRoomBelow() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                0,
                SCREEN_HEIGHT-10,
                SCREEN_WIDTH, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                NO_PADDING,
                SHOW_BELOW);
        
        assertEquals(new Point(490, 1900), popupPos);
    }
    
    /**
     * If the popup is wider than the anchor and there is not enough room to the
     * left of the anchor for the popup to be centred over the anchor, then
     * ensure that that popup is still positioned with the correct padding 
     */
    @Test
    public void testGetPopupPosition_noRoomOnLeftWithPadding() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                0, 
                100,
                50, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                10,
                SHOW_ABOVE);
        
        assertEquals(new Point(10, 90), popupPos);
    }
    
    /**
     * If the popup is wider than the anchor and there is not enough room to the
     * right of the anchor for the popup to be centred over the anchor, then
     * ensure that that popup is still positioned with the correct padding
     */
    @Test
    public void testGetPopupPosition_noRoomOnRightWithPadding() {
        final int popupWidth = 100;
        final CoachMarkDimens<Integer> anchorDimens = new CoachMarkDimens<>(
                SCREEN_WIDTH-50, 
                100,
                50, 
                ANCHOR_HEIGHT);
        
        final Point popupPos = CoachMarkUtils.getPopupPosition(
                anchorDimens, 
                popupWidth, 
                POPUP_HEIGHT,
                SCREEN_WIDTH,
                SCREEN_HEIGHT,
                10,
                SHOW_ABOVE);
        
        assertEquals(new Point(970, 90), popupPos);
    }
    
    /**
     * Test that correct margin value is returned when anchor is wider than the
     * popup and the target is positioned to the left of centre
     */
    @Test
    public void testArrowLeftMargin_anchorWiderThanPopupTargetLeft() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                TARGET_LEFT,
                FULL_ANCHOR_WIDTH,
                ARROW_WIDTH,
                0,
                100,
                MIN_ARROW_MARGIN,
                MAX_ARROW_MARGIN);
        
        assertEquals(165, margin);
    }

    /**
     * Test that correct margin value is returned when anchor is wider than the
     * popup and the target is positioned to the right of centre
     */
    @Test
    public void testArrowLeftMargin_anchorWiderThanPopupTargetRight() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                TARGET_RIGHT,
                FULL_ANCHOR_WIDTH,
                ARROW_WIDTH,
                0,
                100,
                MIN_ARROW_MARGIN,
                MAX_ARROW_MARGIN);
        
        assertEquals(705, margin);
    }
    
    /**
     * Test that correct margin value is returned when anchor is wider than the
     * popup and the target is positioned directly in the centre
     */
    @Test
    public void testArrowLeftMargin_anchorWiderThanPopupTargetCentre() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                TARGET_CENTRE,
                FULL_ANCHOR_WIDTH,
                ARROW_WIDTH,
                0,
                100,
                MIN_ARROW_MARGIN,
                MAX_ARROW_MARGIN);
        
        assertEquals(435, margin);
    }
    
    /**
     * Test that correct margin value is returned when popup is wider than the
     * anchor and the target is positioned to the left of centre
     */
    @Test
    public void testArrowLeftMargin_popupWiderThanAnchorTargetLeft() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                TARGET_LEFT,
                FULL_ANCHOR_WIDTH-400,
                ARROW_WIDTH,
                200,
                100,
                MIN_ARROW_MARGIN,
                MAX_ARROW_MARGIN);
        
        assertEquals(265, margin);
    }
    
    /**
     * Test that correct margin value is returned when popup is wider than the
     * anchor and the target is positioned to the right of centre
     */
    @Test
    public void testArrowLeftMargin_popupWiderThanAnchorTargetRight() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                TARGET_RIGHT,
                FULL_ANCHOR_WIDTH-400,
                ARROW_WIDTH,
                200,
                100,
                MIN_ARROW_MARGIN,
                MAX_ARROW_MARGIN);
        
        assertEquals(605, margin);
    }
    
    /**
     * Test that correct margin value is returned when popup is wider than the
     * anchor and the target is positioned directly in the centre
     */
    @Test
    public void testArrowLeftMargin_popupWiderThanAnchorTargetCentre() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                TARGET_CENTRE,
                FULL_ANCHOR_WIDTH-400,
                ARROW_WIDTH,
                200,
                100,
                MIN_ARROW_MARGIN,
                MAX_ARROW_MARGIN);
        
        assertEquals(435, margin);
    }
    
    /**
     * Test that the left arrow margin is not less than the minimum
     */
    @Test
    public void testArrowLeftMargin_targetBeforeMinArrowMargin() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                0.01f,
                FULL_ANCHOR_WIDTH,
                ARROW_WIDTH,
                0,
                0,
                15,
                MAX_ARROW_MARGIN);
        
        assertEquals(15, margin);        
    }
    
    /**
     * Test that the left arrow margin is not greater than the maximum
     */
    @Test
    public void testArrowLeftMargin_targetAfterMaxArrowMargin() {
        int margin = CoachMarkUtils.getArrowLeftMargin(
                0.99f,
                FULL_ANCHOR_WIDTH,
                ARROW_WIDTH,
                0,
                0,
                15,
                1080 - ARROW_WIDTH - 15);
        
        assertEquals(1080 - ARROW_WIDTH - 15, margin);
    }
}
