package com.yan.refreshloadlayouttest.widget;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.refreshloadlayouttest.R;

/**
 * Created by yan on 2017/9/16.
 */

public class TwoRefreshHeader extends HeaderOrFooter {
    private static int REFRESH_FIRST_DURING = 180;
    private int twoRefreshDuring = 400;

    private int twoRefreshDistance;
    private int firstRefreshTriggerDistance;

    private String twoRefreshText = "二级刷新";

    private PullRefreshLayout pullRefreshLayout;


    public int getTwoRefreshDistance() {
        return twoRefreshDistance;
    }

    public TwoRefreshHeader(Context context, PullRefreshLayout pullRefreshLayout) {
        super(context);
        this.pullRefreshLayout = pullRefreshLayout;
        twoRefreshInit();
    }

    private void twoRefreshInit() {
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));

        firstRefreshTriggerDistance = TwoRefreshHeader.this.pullRefreshLayout.getRefreshTriggerDistance();
        twoRefreshDistance = firstRefreshTriggerDistance * 2;

        FrameLayout.LayoutParams layoutParams = (LayoutParams) rlContainer.getLayoutParams();
        layoutParams.gravity = Gravity.BOTTOM;
        rlContainer.setLayoutParams(layoutParams);

        ImageView ivTwoRefresh = new ImageView(getContext());
        ivTwoRefresh.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.img3));
        ivTwoRefresh.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams ivLp = new LayoutParams(-1, -1);
        ivLp.gravity = Gravity.CENTER;
        addView(ivTwoRefresh, ivLp);

        ivTwoRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "new world", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPullChange(float percent) {
        super.onPullChange(percent);
        if (!pullRefreshLayout.isHoldingTrigger()) {
            if (pullRefreshLayout.getMoveDistance() >= twoRefreshDistance) {
                if (!tv.getText().toString().equals(twoRefreshText)) {
                    tv.setText(twoRefreshText);
                }
            } else if (tv.getText().toString().equals(twoRefreshText)) {
                tv.setText("release loading");
            }
        } else if (pullRefreshLayout.getMoveDistance() >= getHeight()) {
            pullRefreshLayout.setDispatchPullTouchAble(true);
        }
    }

    @Override
    public void onPullHolding() {
        Log.e("onPullHolding", "onPullHolding: "+pullRefreshLayout.getMoveDistance()+"   "+twoRefreshDistance+"   "+getHeight());
        if (pullRefreshLayout.getMoveDistance() >= twoRefreshDistance) {
            pullRefreshLayout.setPullDownMaxDistance(getHeight() * 2);
            pullRefreshLayout.setRefreshTriggerDistance(getHeight());
            pullRefreshLayout.setRefreshAnimationDuring(twoRefreshDuring);
            pullRefreshLayout.setDispatchPullTouchAble(false);
        }
        super.onPullHolding();
    }

    @Override
    public void onPullReset() {
        super.onPullReset();
        pullRefreshLayout.setPullDownMaxDistance(getHeight());
        pullRefreshLayout.setRefreshTriggerDistance(firstRefreshTriggerDistance);
        pullRefreshLayout.setRefreshAnimationDuring(REFRESH_FIRST_DURING);
        pullRefreshLayout.setDispatchPullTouchAble(true);
    }
}