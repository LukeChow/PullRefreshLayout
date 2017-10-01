package com.yan.refreshloadlayouttest.widget;

import android.content.Context;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.pullrefreshlayout.ShowGravity;
import com.yan.refreshloadlayouttest.R;

/**
 * Created by Administrator on 2017/10/1 0001.
 */

public class DiDiHeader extends NestedFrameLayout implements PullRefreshLayout.OnPullListener, NestedScrollView.OnScrollChangeListener {
    private PullRefreshLayout prl;
    private ClassicsHeader loadingView;
    private View fixedHeader;

    public DiDiHeader(Context context, PullRefreshLayout prl) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        this.prl = prl;
        prl.setHeaderFront(false);
        prl.setHeaderShowGravity(ShowGravity.PLACEHOLDER);
        scrollInit();
        LayoutInflater.from(context).inflate(R.layout.didi_header, this, true);
        fixedHeader = findViewById(R.id.fixed_top);
        loadingView = (ClassicsHeader) findViewById(R.id.loading);
    }

    private void scrollInit() {
        post(new Runnable() {
            @Override
            public void run() {
                View target = prl.getTargetView();
                prl.setRefreshTriggerDistance(loadingView.getHeight());
                target.setOverScrollMode(OVER_SCROLL_NEVER);
                target.setPadding(target.getPaddingLeft()
                        , fixedHeader.getHeight()
                        , target.getPaddingRight()
                        , target.getPaddingBottom());

                if (target instanceof NestedScrollView) {
                    ((NestedScrollView) target).setOnScrollChangeListener(DiDiHeader.this);
                    ((NestedScrollView) target).setClipToPadding(false);
                }//else if target instanceof RecyclerView ...
            }
        });
    }

    @Override
    public void onPullChange(float percent) {
        loadingView.setTranslationY(prl.getMoveDistance());
    }

    @Override
    public void onPullHoldTrigger() {
        loadingView.onPullHoldTrigger();
    }

    @Override
    public void onPullHoldUnTrigger() {
        loadingView.onPullHoldUnTrigger();
    }

    @Override
    public void onPullHolding() {
        loadingView.onPullHolding();
    }

    @Override
    public void onPullFinish() {
        loadingView.onPullFinish();
    }

    @Override
    public void onPullReset() {
        loadingView.onPullReset();
    }

    @Override
    public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
        setTranslationY(-scrollY);
        if (getTranslationY() >= 0) {
            setTranslationY(0);
        }
    }
}
