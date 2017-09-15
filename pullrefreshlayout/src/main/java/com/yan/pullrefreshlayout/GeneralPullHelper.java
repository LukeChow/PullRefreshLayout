package com.yan.pullrefreshlayout;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.VelocityTrackerCompat;
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
    private final int[] childConsumed = new int[2];
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
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                activePointerId = ev.getPointerId(0);
                actionDownPointX = ev.getX();
                actionDownPointY = ev.getY();
                lastDragEventY = (int) actionDownPointY;

                pullRefreshLayout.onStartScroll();
                pullRefreshLayout.dispatchSuperTouchEvent(ev);
                return true;
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = ev.findPointerIndex(activePointerId);
                if (ev.findPointerIndex(activePointerId) == -1) {
                    break;
                }
                int tempY = (int) ev.getY(activePointerIndex);
                int deltaY = lastDragEventY - tempY;
                lastDragEventY = tempY;

                if (deltaY < 0) {
                    dragState = 1;
                    isMoveTrendDown = true;
                } else if (deltaY > 0) {
                    dragState = -1;
                    isMoveTrendDown = false;
                }

                float movingX = ev.getX() - actionDownPointX;
                float movingY = ev.getY() - actionDownPointY;
                if (!isDragVertical && Math.abs((int) movingY) > touchSlop && Math.abs(movingY) > Math.abs(movingX)) {
                    isDragVertical = true;
                    lastDragEventY = (int) ev.getY();
                    reDispatchMoveEventDrag(ev, deltaY);
                } else if (!isDragVertical && !isDragHorizontal && Math.abs((int) movingX) > touchSlop && Math.abs(movingX) > Math.abs(movingY)) {
                    isDragHorizontal = true;
                }

                if (isDragVertical) {
                    reDispatchMoveEventDragging(ev, deltaY);

                    // make sure that can nested to work or the targetView is move with content
                    // dell the touch logic
                    if (!pullRefreshLayout.isTargetNestedScrollingEnabled() || !pullRefreshLayout.isMoveWithContent) {
                        pullRefreshLayout.onPreScroll(deltaY, childConsumed);
                        pullRefreshLayout.onScroll(deltaY - (childConsumed[1] - lastChildConsumedY));
                        lastChildConsumedY = childConsumed[1];

                        // -------------------| event reset |--------------------
                        if (!pullRefreshLayout.isMoveWithContent) {
                            ev.setLocation(ev.getX(), (int) ev.getY() + childConsumed[1]);
                        }
                    }
                    if (lastMoveDistance == Integer.MAX_VALUE) {
                        lastMoveDistance = pullRefreshLayout.moveDistance;
                    }
                    if (lastMoveDistance != pullRefreshLayout.moveDistance) {
                        isLayoutMoved = true;
                    }
                    lastMoveDistance = pullRefreshLayout.moveDistance;
                }
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                lastDragEventY = (int) ev.getY(index);
                activePointerId = ev.getPointerId(index);
                reDispatchPointEvent(ev);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                lastDragEventY = (int) ev.getY(ev.findPointerIndex(activePointerId));
                reDispatchPointEvent(ev);
                break;

            case MotionEvent.ACTION_UP:
                dragState = 0;// get know the touchState first

                velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
                float velocityY = (isMoveTrendDown ? 1 : -1) * Math.abs(VelocityTrackerCompat.getYVelocity(velocityTracker, activePointerId));
                if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && isDragVertical && (Math.abs(velocityY) > minimumFlingVelocity)) {
                    pullRefreshLayout.onPreFling(-(int) velocityY);
                }
                recycleVelocityTracker();

                reDispatchUpEvent(ev);
            case MotionEvent.ACTION_CANCEL:
                pullRefreshLayout.onStopScroll();

                isReDispatchMoveEvent = false;
                isDispatchTouchCancel = false;
                isDragHorizontal = false;
                isLayoutMoved = false;
                isDragVertical = false;

                lastMoveDistance = Integer.MAX_VALUE;
                lastChildConsumedY = 0;
                childConsumed[1] = 0;
                activePointerId = -1;
                dragState = 0;
                break;

        }
        if (velocityTracker != null) {
            velocityTracker.addMovement(ev);
        }

        return pullRefreshLayout.dispatchSuperTouchEvent(ev);
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

    private void reDispatchPointEvent(MotionEvent event) {
        if (!pullRefreshLayout.isMoveWithContent && pullRefreshLayout.moveDistance == 0 && isLayoutMoved) {
            pullRefreshLayout.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
        }
    }

    private void reDispatchMoveEventDrag(MotionEvent event, int movingY) {
        if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && (movingY > 0 && pullRefreshLayout.moveDistance > 0 || movingY < 0 && pullRefreshLayout.moveDistance < 0
                || (isDragHorizontal && (pullRefreshLayout.moveDistance != 0 || !pullRefreshLayout.isTargetAbleScrollUp() && movingY < 0 || !pullRefreshLayout.isTargetAbleScrollDown() && movingY > 0)))) {
            isDispatchTouchCancel = true;
            pullRefreshLayout.dispatchSuperTouchEvent(getReEvent(event, MotionEvent.ACTION_CANCEL));
        }
    }

    private void reDispatchMoveEventDragging(MotionEvent event, int movingY) {
        if (!pullRefreshLayout.isTargetNestedScrollingEnabled() && isDispatchTouchCancel && !isReDispatchMoveEvent
                && ((movingY > 0 && pullRefreshLayout.moveDistance > 0) || (movingY < 0 && pullRefreshLayout.moveDistance < 0))) {
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

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK)
                >> MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == activePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            lastDragEventY = (int) ev.getY(newPointerIndex);
            activePointerId = ev.getPointerId(newPointerIndex);
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

}
