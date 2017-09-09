package com.yan.refreshloadlayouttest.testactivity;

import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.refreshloadlayouttest.widget.HeaderWithAutoLoading;
import com.yan.refreshloadlayouttest.R;
import com.yan.refreshloadlayouttest.widget.ClassicLoadView;

public class CommonActivity2 extends CommonActivity1 {
    private boolean isInterceptedDispatch = false;

    protected int getViewId() {
        return R.layout.common_activity2;
    }

    private ScrollView scrollView;
    private LinearLayout linearLayout;

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


        findViewById(R.id.iv1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.iv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.iv3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.iv4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });
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

}
