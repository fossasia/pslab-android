package io.pslab.others;

import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class SwipeGestureDetector extends GestureDetector.SimpleOnGestureListener {

    private final BottomSheetBehavior<View> bottomSheet;

    public SwipeGestureDetector(BottomSheetBehavior<View> bt) {
        bottomSheet = bt;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        try {
            switch (getDirection(e1.getX(), e1.getY(), e2.getX(), e2.getY())) {
                case TOP:
                    if (bottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                        bottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                    if (bottomSheet.getState() == BottomSheetBehavior.STATE_HIDDEN)
                        bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    return true;
                case LEFT:
                    return true;
                case DOWN:
                    if (bottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED)
                        bottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    if (bottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                        bottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                    return true;
                case RIGHT:
                    return true;
                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private Direction getDirection(float x1, float y1, float x2, float y2) {
        double angle = Math.toDegrees(Math.atan2(y1 - y2, x2 - x1));
        if (angle > 45 && angle <= 135)
            return Direction.TOP;
        if (angle >= 135 && angle < 180 || angle < -135 && angle > -180)
            return Direction.LEFT;
        if (angle < -45 && angle >= -135)
            return Direction.DOWN;
        if (angle > -45 && angle <= 45)
            return Direction.RIGHT;
        return null;
    }

    public enum Direction {TOP, RIGHT, LEFT, DOWN}
}
