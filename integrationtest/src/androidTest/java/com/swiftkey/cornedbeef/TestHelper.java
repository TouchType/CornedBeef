package com.swiftkey.cornedbeef;

import android.app.Activity;
import android.app.Instrumentation;
import android.graphics.Rect;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;

public final class TestHelper {

    private static final int TIMEOUT = 1000;
    private static final int SLEEP = 100;

    public TestHelper() {
    }

    /**
     * Call {@link CoachMark#show()} on the given {@link CoachMark}
     */
    public static void showCoachMark(final Instrumentation instrumentation, final CoachMark coachMark) {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                coachMark.show();
            }
        });
        instrumentation.waitForIdleSync();
    }

    /**
     * Call {@link CoachMark#dismiss()}
     * on the given {@link CoachMark}
     */
    public static void dismissCoachMark(final Instrumentation instrumentation, final CoachMark coachMark) {
        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                coachMark.dismiss();
            }
        });
        instrumentation.waitForIdleSync();
    }

    /**
     * Wait until the status bar is fully hidden
     */
    public static void waitUntilStatusBarHidden(final Activity activity) {
        final Rect rect = new Rect();
        final long startTime = SystemClock.uptimeMillis();
        do {
            try {
                activity.getWindow().getDecorView()
                        .getWindowVisibleDisplayFrame(rect);
                Thread.sleep(SLEEP);
            } catch (InterruptedException e) {
                break;
            }
        } while (SystemClock.uptimeMillis() - startTime < TIMEOUT && rect.top != 0);
    }

    /**
     * Move the anchor view to the specified location
     */
    public static void moveAnchor(final Instrumentation instrumentation, final View anchor,
                            final int x, final int y) {
        final ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) anchor.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                anchor.setLayoutParams(params);
            }
        });
        instrumentation.waitForIdleSync();
    }

    /**
     * Move the anchor view to the specified location
     */
    public static void moveTargetView(final Instrumentation instrumentation, final View mTargetView,
                                      final int x, final int y) {
        final ViewGroup.MarginLayoutParams params =
                (ViewGroup.MarginLayoutParams) mTargetView.getLayoutParams();
        params.leftMargin = x;
        params.topMargin = y;

        instrumentation.runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mTargetView.setLayoutParams(params);
            }
        });
        instrumentation.waitForIdleSync();
    }

}
