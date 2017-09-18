package com.yan.refreshloadlayouttest.widget.fungame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.pullrefreshlayout.ShowGravity;
import com.yan.refreshloadlayouttest.widget.NestedFrameLayout;


/**
 * 游戏 header
 * Created by SCWANG on 2017/6/17.
 * from https://github.com/scwang90/SmartRefreshLayout
 */

public class FunGameBase extends NestedFrameLayout implements PullRefreshLayout.OnPullListener {

    //<editor-fold desc="Field">
    protected int mHeaderHeight;
    protected int mScreenHeightPixels;
    protected boolean mManualOperation;
    protected PullRefreshLayout refreshLayout;
    private boolean isGameViewReady;
    //</editor-fold>

    //<editor-fold desc="View">
    public FunGameBase(Context context, PullRefreshLayout refreshLayout) {
        super(context);
        this.refreshLayout = refreshLayout;
        initView(context);
    }

    private void initView(Context context) {
        mScreenHeightPixels = context.getResources().getDisplayMetrics().heightPixels;
        mHeaderHeight = (int) (mScreenHeightPixels * 0.16f);
        refreshLayout.setRefreshTriggerDistance(mHeaderHeight);
        refreshLayout.setHeaderShowGravity(ShowGravity.FOLLOW);
        refreshLayout.setHeaderFront(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.parseColor("#fefefe"));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    protected void onManualOperationMove(float percent) {

    }

    @Override
    public void onPullChange(float percent) {
        if (mManualOperation && !refreshLayout.isDragUp() && !refreshLayout.isDragDown() && refreshLayout.isHoldingFinishTrigger() && percent <= 1) {
            onPullFinish();
            return;
        }

        if (mManualOperation) {
            if (!isGameViewReady && percent == 1) {
                refreshLayout.setDispatchPullTouchAble(true);
                refreshLayout.setMoveWithHeader(false);
                isGameViewReady = true;
            }
            if (isGameViewReady && refreshLayout.getMoveDistance() < refreshLayout.getRefreshTriggerDistance() / 4) {
                refreshLayout.moveChildren(refreshLayout.getRefreshTriggerDistance() / 4);//保证onPullChange() 会持续触发
            }
            onManualOperationMove(1 + (percent - 1) * 0.8F);
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
        mManualOperation = true;
        refreshLayout.setDragDampingRatio(1f);
        refreshLayout.setDispatchPullTouchAble(false);
        refreshLayout.setDispatchChildrenEventAble(false);
    }

    @Override
    public void onPullFinish() {
        if (refreshLayout.isDragDown() || refreshLayout.isDragUp()) {
            refreshLayout.cancelAllAnimation();
        } else {
            mManualOperation = false;
            isGameViewReady = false;
            refreshLayout.setDragDampingRatio(0.6F);
            refreshLayout.setMoveWithHeader(true);
            refreshLayout.setDispatchPullTouchAble(true);
            refreshLayout.setDispatchChildrenEventAble(true);
        }
    }

    @Override
    public void onPullReset() {
    }
}
