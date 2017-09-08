package com.yan.refreshloadlayouttest.testactivity;

import android.os.Bundle;
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
import com.yan.refreshloadlayouttest.HeaderWithAutoLoading;
import com.yan.refreshloadlayouttest.R;
import com.yan.refreshloadlayouttest.widget.ClassicLoadView;

import java.lang.reflect.Field;

public class CommonActivity2 extends CommonActivity1 {
    private boolean isInterceptedDispatch = false;

    protected int getViewId() {
        return R.layout.common_activity2;
    }

    ScrollView scrollView;

    LinearLayout linearLayout;

    private float lastMoveY;
    private boolean intercept = true;
    HorizontalScrollView horizontalScrollView;

    protected void initRefreshLayout() {
        refreshLayout = (PullRefreshLayout) findViewById(R.id.refreshLayout);
        scrollView = (ScrollView) findViewById(R.id.sv);
        linearLayout = (LinearLayout) findViewById(R.id.ll);
        setImages();
        refreshLayout.setTwinkEnable(true);
        refreshLayout.setAutoLoadingEnable(true);
        refreshLayout.setLoadMoreEnable(true);
        refreshLayout.setHeaderView(new HeaderWithAutoLoading(getBaseContext(), refreshLayout));
        refreshLayout.setFooterView(new ClassicLoadView(getApplicationContext(), refreshLayout));
        refreshLayout.setLoadTriggerDistance((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()));
        refreshLayout.setTargetView(scrollView);

        horizontalScrollView = (HorizontalScrollView) findViewById(R.id.hsv);

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

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastMoveY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (isViewDrag(horizontalScrollView) && Math.abs(ev.getY() - lastMoveY) < ViewConfiguration.get(getBaseContext()).getScaledTouchSlop()) {
                    intercept = false;
                }

                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                intercept = true;
                break;
        }

        if (isInterceptedDispatch) {
            return false;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isViewDrag(View view) {
        try {
            Field field = view.getClass().getDeclaredField("mIsBeingDragged");
            field.setAccessible(true);
            return (boolean) field.get(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
