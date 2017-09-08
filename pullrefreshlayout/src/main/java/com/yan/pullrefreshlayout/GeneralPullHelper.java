package com.yan.pullrefreshlayout;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;

/**
 * support general view to pull refresh
 * Created by yan on 2017/6/29.
 */
class GeneralPullHelper {
    private final PullRefreshLayout pullRefreshLayout;

    /**
     * default values
     */
    private final int minimumFlingVelocity;
    private final int maximumVelocity;
    private final int touchSlop;

    /**
     * is Being Dragged
     * - use by pullRefreshLayout to know is drag moving
     */
    boolean isDragMoving;
    /**
     * is Drag Horizontal
     * - use by pullRefreshLayout to know is drag moving Horizontal
     */
    boolean isDragHorizontal;

    /**
     * is moving direct down
     * - use by pullRefreshLayout to get moving direction
     */
    boolean isMovingDirectDown;

    /**
     * is touch direct down
     * - use by pullRefreshLayout to get drag state
     */
    int dragState;

    /**
     * first touch point x
     */
    private float actionDownPointX;

    /**
     * first touch point y
     */
    private float actionDownPointY;

    /**
     * motion event child consumed
     */
    private int[] childConsumed = new int[2];
    private int lastChildConsumedY;

    /**
     * active pointer id
     */
    private int activePointerId;

    /**
     * last move y
     */
    private int lastMoveY;

    /**
     * lastMotionY
     */
    private float lastMotionY;

    /**
     * touchEvent velocityTracker
     */
    private VelocityTracker velocityTracker;

    /**
     * velocity y
     */
    private float velocityY;

    /**
     * is ReDispatch TouchEvent
     */
    private boolean isReDispatchTouchEvent;

    GeneralPullHelper(PullRefreshLayout pullRefreshLayout, Context context) {
        this.pullRefreshLayout = pullRefreshLayout;
        ViewConfiguration configuration = ViewConfiguration.get(context);
        minimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
        maximumVelocity = configuration.getScaledMaximumFlingVelocity();
        touchSlop = configuration.getScaledTouchSlop();
    }

    boolean dispatchTouchEvent(MotionEvent ev) {
        initVelocityTrackerIfNotExists();

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                velocityTracker.addMovement(ev);

                dellTouchEvent(ev);

                actionDownPointX = ev.getX();
                actionDownPointY = ev.getY();
                lastMotionY = ev.getY();

                lastMoveY = (int) ev.getY();
                activePointerId = ev.getPointerId(0);

                pullRefreshLayout.dispatchSuperTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                /**
                 * director dell
                 */
                float tempY = ev.getRawY();
                if (tempY - lastMotionY > 0) {
                    dragState = 1;
                    isMovingDirectDown = true;
                } else if (tempY - lastMotionY < 0) {
                    dragState = -1;
                    isMovingDirectDown = false;
                }
                lastMotionY = tempY;

                velocityTracker.addMovement(ev);

                float movingX = ev.getX() - actionDownPointX;
                float movingY = ev.getY() - actionDownPointY;
                if (!isDragMoving && Math.abs((int) movingY) > touchSlop && Math.abs(movingY) > Math.abs(movingX)) {
                    isDragMoving = true;
                    lastMoveY = (int) ev.getY();
                    dispatchCancelEvent(ev, (int) movingY);
                } else if (!isDragMoving && !isDragHorizontal && Math.abs((int) movingX) > touchSlop && Math.abs(movingX) > Math.abs(movingY)) {
                    isDragHorizontal = true;
                }

                if (isDragMoving) {
                    reDispatchEvent(ev, (int) movingY);
                    dellTouchEvent(ev);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragState = 0;// get know the touchState first

                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                velocityY = (isMovingDirectDown ? 1 : -1) * Math.abs(velocityTracker.getYVelocity());
                recycleVelocityTracker();

                if (isReDispatchTouchEvent && pullRefreshLayout.moveDistance != 0) {
                    dispatchCancelEvent(ev);
                }

                dellTouchEvent(ev);

                isReDispatchTouchEvent = false;
                isDragHorizontal = false;
                isDragMoving = false;
                velocityY = 0;
                break;
        }
        return pullRefreshLayout.dispatchSuperTouchEvent(ev);
    }

    private void dellTouchEvent(MotionEvent ev) {
        int actionMasked = MotionEventCompat.getActionMasked(ev);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                pullRefreshLayout.onStartScroll();
                break;
            }
            case MotionEvent.ACTION_MOVE:
                // make sure that can nested to work or the targetView is move with content
                // dell the touch logic
                if (!pullRefreshLayout.isTargetNestedScrollingEnabled() || !pullRefreshLayout.isMoveWithContent) {
                    if (activePointerId != ev.getPointerId(0)) {
                        lastMoveY = (int) ev.getY();
                        activePointerId = ev.getPointerId(0);
                    }

                    // --------------------| move offset |--------------------
                    int tempY = (int) ev.getY();
                    int deltaY = lastMoveY - tempY;
                    lastMoveY = tempY;

                    pullRefreshLayout.onPreScroll(deltaY, childConsumed);
                    pullRefreshLayout.onScroll(deltaY - (childConsumed[1] - lastChildConsumedY));
                    lastChildConsumedY = childConsumed[1];

                    // -------------------| event reset |--------------------
                    if (!pullRefreshLayout.isMoveWithContent) {
                        ev.setLocation(ev.getX(), (int) ev.getY() + childConsumed[1]);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                ev.offsetLocation(0, childConsumed[1]);
                pullRefreshLayout.onStopScroll();
                if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && isDragMoving && (Math.abs(velocityY) > minimumFlingVelocity)) {
                    pullRefreshLayout.onPreFling(-(int) velocityY);
                }
                activePointerId = -1;
                childConsumed[1] = 0;
                lastChildConsumedY = 0;
                break;
        }
    }

    private void initVelocityTrackerIfNotExists() {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void dispatchCancelEvent(MotionEvent event, int movingY) {
        if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && (movingY < 0 && pullRefreshLayout.moveDistance > 0 || movingY > 0 && pullRefreshLayout.moveDistance < 0)) {
            dispatchCancelEvent(event);
        }
    }

    private void dispatchCancelEvent(MotionEvent event) {
        MotionEvent cancelEvent = MotionEvent.obtain(event);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
        pullRefreshLayout.dispatchSuperTouchEvent(cancelEvent);
    }

    private void reDispatchEvent(MotionEvent event, int movingY) {
        if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && !isReDispatchTouchEvent
                && ((movingY < 0 && pullRefreshLayout.moveDistance > 0) || (movingY > 0 && pullRefreshLayout.moveDistance < 0))) {
            isReDispatchTouchEvent = true;
            MotionEvent reEvent = MotionEvent.obtain(event);
            reEvent.setAction(MotionEvent.ACTION_DOWN);
            pullRefreshLayout.dispatchSuperTouchEvent(reEvent);
        }
    }
}
