package com.yan.refreshloadlayouttest.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.pullrefreshlayout.ViscousInterpolator;
import com.yan.refreshloadlayouttest.App;

/**
 * Created by yan on 2017/9/16.
 */

public class SlidingDownHeader extends NestedFrameLayout implements PullRefreshLayout.OnPullListener {
    private PullRefreshLayout pullRefreshLayout;
    private View targetView;

    private final int SLIDING_OFFSET = dipToPx(60);
    private final int SLIDING_DURING = 400;

    private boolean isSlidingDown;

    private ValueAnimator translateYAnimation;

    private int dipToPx(float value) {
        DisplayMetrics metrics = App.getAppContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
    }

    public void setTargetView(View target) {
        this.targetView = target;
        addView(targetView);
    }

    public SlidingDownHeader(Context context, PullRefreshLayout pullRefreshLayout) {
        super(context);
        this.pullRefreshLayout = pullRefreshLayout;
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        post(new Runnable() {
            @Override
            public void run() {
                slidingDownInit();
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (!isSlidingDown) {
            return super.dispatchTouchEvent(ev);
        }
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
                if (pullRefreshLayout.isLayoutDragMoved()) {
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    return super.dispatchTouchEvent(ev);
                } else if (pullRefreshLayout.isDragHorizontal()) {
                    pullRefreshLayout.setDispatchPullTouchAble(false);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                pullRefreshLayout.setDispatchPullTouchAble(true);
                if (pullRefreshLayout.isDragHorizontal()) {
                    MotionEvent event = MotionEvent.obtain(ev);
                    event.setAction(MotionEvent.ACTION_CANCEL);
                    pullRefreshLayout.dispatchTouchEvent(event);
                }
                ev.setAction(MotionEvent.ACTION_UP);
        }
        return super.dispatchTouchEvent(ev);
    }

    private void slidingDownInit() {
        pullRefreshLayout.setRefreshTriggerDistance(SLIDING_OFFSET);
        pullRefreshLayout.setPullDownMaxDistance(getHeight() * 2);
        pullRefreshLayout.setRefreshAnimationDuring(SLIDING_DURING);

        translateYAnimation = ValueAnimator.ofFloat(0, 0);
        translateYAnimation.setDuration(SLIDING_DURING);
        translateYAnimation.setInterpolator(new ViscousInterpolator());
        translateYAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float m = (float) animation.getAnimatedValue();
                pullRefreshLayout.moveChildren((int) m);
            }
        });
    }

    @Override
    public void onPullChange(float percent) {
        if (!isSlidingDown && pullRefreshLayout.isHoldingTrigger() && !pullRefreshLayout.isHoldingFinishTrigger() && pullRefreshLayout.getMoveDistance() >= getHeight()) {
            isSlidingDown = true;
            pullRefreshLayout.setDispatchPullTouchAble(true);
        }

        if (!isSlidingDown) {
            return;
        }
        if (percent <= 0) {
            pullRefreshLayout.refreshComplete();
        }
        if (pullRefreshLayout.getMoveDistance() > getHeight() - SLIDING_OFFSET) {
            if (!pullRefreshLayout.isDragDown() && !pullRefreshLayout.isDragUp()) {
                translateYAnimation.setFloatValues(pullRefreshLayout.getMoveDistance(), getHeight());
                translateYAnimation.start();
            } else if (translateYAnimation.isRunning()) {
                translateYAnimation.cancel();
            }
        } else if (!pullRefreshLayout.isDragDown() && !pullRefreshLayout.isDragUp() ) {
            pullRefreshLayout.refreshComplete();
        }
    }

    @Override
    public void onPullHoldTrigger() {

    }

    @Override
    public void onPullHoldUnTrigger() {

    }

    @Override
    public void onPullHolding() {
        if (pullRefreshLayout.getMoveDistance() != getHeight()) {
            pullRefreshLayout.setRefreshTriggerDistance(getHeight());
            pullRefreshLayout.setDispatchPullTouchAble(false);
            pullRefreshLayout.setTwinkEnable(false);
        }
    }

    @Override
    public void onPullFinish() {
        isSlidingDown = false;
        pullRefreshLayout.setTwinkEnable(true);
    }

    @Override
    public void onPullReset() {
        pullRefreshLayout.setDispatchPullTouchAble(true);
        pullRefreshLayout.setRefreshTriggerDistance(SLIDING_OFFSET);
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (translateYAnimation.isRunning()) {
            translateYAnimation.cancel();
        }
    }

}