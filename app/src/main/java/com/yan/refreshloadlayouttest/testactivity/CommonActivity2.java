package com.yan.refreshloadlayouttest.testactivity;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.bumptech.glide.Glide;
import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.refreshloadlayouttest.widget.HeaderWithAutoLoading;
import com.yan.refreshloadlayouttest.R;
import com.yan.refreshloadlayouttest.widget.ClassicLoadView;

public class CommonActivity2 extends CommonActivity1 implements View.OnTouchListener {
    private boolean isInterceptedDispatch = false;

    protected int getViewId() {
        return R.layout.common_activity2;
    }

    private ScrollView scrollView;
    private LinearLayout linearLayout;
    private HorizontalScrollView horizontalScrollView;

    private boolean intercept = true;

    protected void initRefreshLayout() {
        refreshLayout = (PullRefreshLayout) findViewById(R.id.refreshLayout);
        scrollView = (ScrollView) findViewById(R.id.sv);
        linearLayout = (LinearLayout) findViewById(R.id.ll);
        setImages();
        refreshLayout.setTwinkEnable(true);
        refreshLayout.setAutoLoadingEnable(true);
        refreshLayout.setLoadMoreEnable(true);
        refreshLayout.setHeaderView(new HeaderWithAutoLoading(getBaseContext(), "LineSpinFadeLoaderIndicator", refreshLayout));
        refreshLayout.setFooterView(new ClassicLoadView(getApplicationContext(), refreshLayout));
        refreshLayout.setLoadTriggerDistance((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()));
        refreshLayout.setTargetView(scrollView);

        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.hsv);
        horizontalScrollView.setOnTouchListener(this);
        refreshLayout.setOnDragIntercept(new PullRefreshLayout.OnDragInterceptAdapter() {
            @Override
            public boolean onHeaderDownIntercept() {
                return intercept;
            }

            @Override
            public boolean onFooterUpIntercept() {
                return intercept;
            }
        });

        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.refreshComplete();
                    }
                }, 3000);
            }

            @Override
            public void onLoading() {
                if (!refreshLayout.isTwinkEnable()) {
                    refreshLayout.autoLoading();
                }
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (linearLayout.getChildCount() > 12) {
                            ClassicLoadView classicLoadView = refreshLayout.getFooterView();
                            classicLoadView.loadFinish();
                            return;
                        }

                        linearLayout.addView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_item, null));

                        if (refreshLayout.getMoveDistance() < 0) {
                            isInterceptedDispatch = true;
                        }

                        refreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.scrollBy(0, -refreshLayout.getMoveDistance());
                                ClassicLoadView classicLoadView = refreshLayout.getFooterView();
                                classicLoadView.startBackAnimation();
                                isInterceptedDispatch = false;
                            }
                        }, 150);
                    }
                }, 2000);
            }
        });
    }

    private void setImages() {
        Glide.with(this)
                .load(R.drawable.img1)
                .into((ImageView) findViewById(R.id.iv1));
        Glide.with(this)
                .load(R.drawable.img2)
                .into((ImageView) findViewById(R.id.iv2));
        Glide.with(this)
                .load(R.drawable.img3)
                .into((ImageView) findViewById(R.id.iv3));
        Glide.with(this)
                .load(R.drawable.img4)
                .into((ImageView) findViewById(R.id.iv4));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        refreshLayout.setTwinkEnable(false);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return !isInterceptedDispatch && super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onTouch(View v, MotionEvent ev) {
        if (!(!refreshLayout.isTargetAbleScrollDown() && !refreshLayout.isTargetAbleScrollUp())) {
            intercept = ev.getActionMasked() != MotionEvent.ACTION_MOVE;
            return false;
        }
        return intercept= !refreshLayout.isDragHorizontal();
    }
}
