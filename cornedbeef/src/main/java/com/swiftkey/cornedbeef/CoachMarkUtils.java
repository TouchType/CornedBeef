package com.swiftkey.cornedbeef;

import android.graphics.Point;

/**
 * Utils for calculating the size and position of the coach mark popup and pointy mark
 * 
 * @author lachie
 */
public class CoachMarkUtils {

    /**
     * The popup width is determined by the minimum width excluding content
     * (usually the width of the pointy mark and padding), the maximum width
     * (usually the width of the screen), a desired width (including content),
     * the width of the anchor and the position along the anchor at which the
     * pointy mark should point. Normally the popup takes the desired width or
     * the maximum width, unless the target arrow would otherwise be positioned
     * outside of the popup, in which case the popup is expanded to include the
     * pointy mark.

     * @param minWidth - the minimum width of the popup (not including content)
     * @param maxWidth - the maximum width of the popup
     * @param desiredWidth - the desired with of the popup (including content)
     * @param anchorWidth - the width of the anchor view
     * @param target - the position on the anchor at which the pointy mark should point
     */
    public static int getPopupWidth(int minWidth, int maxWidth,
            int desiredWidth, int anchorWidth, float target) {
        // Minimum width as a percentage of the anchor width
        final double width = 2 * Math.abs(0.5 - target);

        // Minimum width as an absolute value
        int popupWidth = (int) (anchorWidth * width) + minWidth;

        // Make sure not smaller than the minimum width
        popupWidth = popupWidth > desiredWidth ? popupWidth : desiredWidth;

        // Make sure not larger than the max width
        return popupWidth > maxWidth ? maxWidth : popupWidth;
    }

    /**
     * The popup is normally centred above the anchor. If showBelow is false but
     * there is not enough room above the anchor then the popup is positioned
     * below it. If showBelow is true but there is not enough room below the
     * anchor then the popup is positioned above it. If centering the popup would
     * move it off-screen then it is shifted left or right.
     * 
     * @param anchorDimens - the dimensions of the anchor view
     * @param popupWidth - the width of the popup
     * @param popupHeight - the height of the popup
     * @param screenWidth - the current screen width
     * @param padding - minimum space between coach mark and screen edges
     * @param showBelow - true if the popup should appear below the anchor
     */
    public static Point getPopupPosition(final CoachMark.CoachMarkDimens<Integer> anchorDimens,
            final int popupWidth, final int popupHeight, final int screenWidth,
            final int screenHeight, final int padding, final boolean showBelow) {

        // Get popup X and Y Coords
        int popupX = (anchorDimens.width - popupWidth) / 2 + anchorDimens.x;

        int popupAboveY = anchorDimens.y - popupHeight;
        int popupBelowY = anchorDimens.y + anchorDimens.height;
        
        int popupY;
        if(showBelow) {
            // If there is not enough room below move the popup above
            popupY = popupBelowY + popupHeight > screenHeight ? popupAboveY : popupBelowY;
        } else {
            // If there is not enough room above move the popup below
            popupY = popupAboveY < 0 ? popupBelowY : popupAboveY;
        }

        // Constrain popupX to the screen size (minus padding)
        popupX = popupX < padding ? padding : 
            (popupX + popupWidth > (screenWidth-padding) ? 
                    screenWidth - popupWidth - padding : popupX);

        return new Point(popupX, popupY);
    }

    /**
     * Calculate the size of the arrow's left margin
     * 
     * @param target - the position on the anchor at which the arrow should point
     * @param anchorWidth - the width of the anchor view
     * @param arrowWidth - the width of the arrow
     * @param anchorX - the x coordinate of the anchor view
     * @param popupX - the x coordinate of the popup view
     * @param minMargin - minimum accepted value of left margin
     * @param maxMargin - maximum accepted value of left margin
     */
    public static int getArrowLeftMargin(float target, int anchorWidth, 
            int arrowWidth, int anchorX, int popupX, int minMargin, int maxMargin) {
        int margin = (int) (target * anchorWidth) - (arrowWidth / 2) + anchorX - popupX;
        return margin < minMargin ? minMargin : (margin > maxMargin ? maxMargin : margin);
    }
}
