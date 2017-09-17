package com.yan.refreshloadlayouttest.widget;

import android.content.Context;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.yan.pullrefreshlayout.PullRefreshLayout;

/**
 * Created by yan on 2017/9/16.
 */

public class TwoRefreshHeader extends HeaderOrFooter {
    private PullRefreshLayout pullRefreshLayout;
    private int twoRefreshDistance;
    private int firstRefreshTriggerDistance;

    private String twoRefreshText = "二级刷新";

    public int getTwoRefreshDistance() {
        return twoRefreshDistance;
    }

    private int dipToPx(float value) {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
    }

    public TwoRefreshHeader(Context context, PullRefreshLayout pullRefreshLayout) {
        super(context);
        this.pullRefreshLayout = pullRefreshLayout;
        twoRefreshDistance = dipToPx(200);
        pullRefreshLayout.post(new Runnable() {
            public void run() {
                firstRefreshTriggerDistance = TwoRefreshHeader.this.pullRefreshLayout.getRefreshTriggerDistance();
            }
        });
    }

    @Override
    public void onPullChange(float percent) {
        super.onPullChange(percent);
        if (!pullRefreshLayout.isHoldingTrigger()) {
            if (pullRefreshLayout.getMoveDistance() > twoRefreshDistance) {
                if(!tv.getText().toString().equals(twoRefreshText)) {
                    tv.setText(twoRefreshText);
                }
            } else if (tv.getText().toString().equals(twoRefreshText)) {
                tv.setText("release loading");
            }
        }
    }

    @Override
    public void onPullHolding() {
        super.onPullHolding();
        if (pullRefreshLayout.getMoveDistance() > twoRefreshDistance) {
            pullRefreshLayout.setRefreshTriggerDistance(twoRefreshDistance);
        }
    }

    @Override
    public void onPullReset() {
        pullRefreshLayout.setRefreshTriggerDistance(firstRefreshTriggerDistance);
        super.onPullReset();
    }
}