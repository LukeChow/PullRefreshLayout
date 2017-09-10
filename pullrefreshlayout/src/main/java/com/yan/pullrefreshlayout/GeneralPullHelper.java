package com.yan.pullrefreshlayout;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

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
     * - use by pullRefreshLayout to know is drag Vertical
     */
    boolean isDragVertical;
    /**
     * is Drag Horizontal
     * - use by pullRefreshLayout to know is drag Horizontal
     */
    boolean isDragHorizontal;

    /**
     * is moving direct down
     * - use by pullRefreshLayout to get moving direction
     */
    boolean isMoveTrendDown;

    /**
     * is ReDispatch TouchEvent
     */
    private boolean isReDispatchMoveEvent;

    /**
     * is Dispatch Touch Cancel
     */
    private boolean isDispatchTouchCancel;

    /**
     * is Refresh Layout has moved
     */
    private boolean isLayoutMoved;

    /**
     * first touch point x
     */
    private float actionDownPointX;

    /**
     * first touch point y
     */
    private float actionDownPointY;

    /**
     * last Original MotionY
     */
    private float lastOriginalMotionY;

    /**
     * velocity y
     */
    private float velocityY;

    /**
     * is touch direct down
     * - use by pullRefreshLayout to get drag state
     */
    int dragState;

    /**
     * last Layout Move Distance
     */
    private int lastMoveDistance = Integer.MAX_VALUE;

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
     * last drag MotionEvent y
     */
    private int lastDragEventY;

    /**
     * touchEvent velocityTracker
     */
    private VelocityTracker velocityTracker;

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
                lastOriginalMotionY = ev.getY();

                activePointerId = ev.getPointerId(0);

                pullRefreshLayout.dispatchSuperTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                // -----------| direct dell |-----------
                float tempY = ev.getRawY();
                float dragOffset = tempY - lastOriginalMotionY;
                if (dragOffset > 0) {
                    dragState = 1;
                    isMoveTrendDown = true;
                } else if (dragOffset < 0) {
                    dragState = -1;
                    isMoveTrendDown = false;
                }
                lastOriginalMotionY = tempY;

                velocityTracker.addMovement(ev);

                float movingX = ev.getX() - actionDownPointX;
                float movingY = ev.getY() - actionDownPointY;
                if (!isDragVertical && Math.abs((int) movingY) > touchSlop && Math.abs(movingY) > Math.abs(movingX)) {
                    isDragVertical = true;
                    lastDragEventY = (int) ev.getY();
                    reDispatchMoveEventDrag(ev, (int) dragOffset);
                } else if (!isDragVertical && !isDragHorizontal && Math.abs((int) movingX) > touchSlop && Math.abs(movingX) > Math.abs(movingY)) {
                    isDragHorizontal = true;
                }

                if (isDragVertical) {
                    reDispatchMoveEventDragging(ev, (int) dragOffset);
                    dellTouchEvent(ev);

                    if (lastMoveDistance == Integer.MAX_VALUE) {
                        lastMoveDistance = pullRefreshLayout.moveDistance;
                    }
                    if (lastMoveDistance != pullRefreshLayout.moveDistance) {
                        isLayoutMoved = true;
                    }
                    lastMoveDistance = pullRefreshLayout.moveDistance;
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragState = 0;// get know the touchState first

                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                velocityY = (isMoveTrendDown ? 1 : -1) * Math.abs(velocityTracker.getYVelocity());
                recycleVelocityTracker();

                reDispatchUpEvent(ev);

                dellTouchEvent(ev);

                isReDispatchMoveEvent = false;
                isDispatchTouchCancel = false;
                isDragHorizontal = false;
                isLayoutMoved = false;
                isDragVertical = false;
                lastMoveDistance = Integer.MAX_VALUE;
                velocityY = 0;
                break;
        }
        return pullRefreshLayout.dispatchSuperTouchEvent(ev);
    }

    private void dellTouchEvent(MotionEvent ev) {
        int actionMasked = MotionEventCompat.getActionMasked(ev);
        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                lastDragEventY = (int) ev.getY();

                pullRefreshLayout.onStartScroll();
                break;
            }
            case MotionEvent.ACTION_MOVE:
                // make sure that can nested to work or the targetView is move with content
                // dell the touch logic
                if (!pullRefreshLayout.isTargetNestedScrollingEnabled() || !pullRefreshLayout.isMoveWithContent) {
                    if (activePointerId != ev.getPointerId(0)) {
                        lastDragEventY = (int) ev.getY();
                        activePointerId = ev.getPointerId(0);
                    }

                    // --------------------| move offset |--------------------
                    int tempY = (int) ev.getY();
                    int deltaY = lastDragEventY - tempY;
                    lastDragEventY = tempY;

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
                if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && isDragVertical && (Math.abs(velocityY) > minimumFlingVelocity)) {
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

    private void reDispatchMoveEventDrag(MotionEvent event, int movingY) {
        if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && (movingY < 0 && pullRefreshLayout.moveDistance > 0 || movingY > 0 && pullRefreshLayout.moveDistance < 0
                || (isDragHorizontal && (pullRefreshLayout.moveDistance != 0 || !pullRefreshLayout.isTargetAbleScrollUp() && movingY > 0 || !pullRefreshLayout.isTargetAbleScrollDown() && movingY < 0)))) {
            isDispatchTouchCancel = true;
            pullRefreshLayout.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
        }
    }

    private void reDispatchMoveEventDragging(MotionEvent event, int movingY) {
        if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && isDispatchTouchCancel && !isReDispatchMoveEvent
                && ((movingY < 0 && pullRefreshLayout.moveDistance > 0) || (movingY > 0 && pullRefreshLayout.moveDistance < 0))) {
            isReDispatchMoveEvent = true;
            pullRefreshLayout.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_DOWN));
        }
    }

    private void reDispatchUpEvent(MotionEvent event) {
        if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && isDragVertical && isLayoutMoved) {
            if (!pullRefreshLayout.isTargetAbleScrollDown() && !pullRefreshLayout.isTargetAbleScrollUp()) {
                pullRefreshLayout.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
            } else if (pullRefreshLayout.targetView instanceof ViewGroup) {
                ViewGroup vp = (ViewGroup) pullRefreshLayout.targetView;
                for (int i = 0; i < vp.getChildCount(); i++) {
                    vp.getChildAt(i).dispatchTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
                }
            }
        }
    }

    private MotionEvent getReEvent(MotionEvent event, int action) {
        MotionEvent reEvent = MotionEvent.obtain(event);
        reEvent.setAction(action);
        return reEvent;
    }
}
