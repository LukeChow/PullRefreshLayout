package com.yan.refreshloadlayouttest.testactivity;

import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.refreshloadlayouttest.widget.ClassicHoldLoadView;
import com.yan.refreshloadlayouttest.widget.HeaderWithAutoLoading;
import com.yan.refreshloadlayouttest.R;
import com.yan.refreshloadlayouttest.widget.ClassicLoadView;

public class CommonActivity2 extends CommonActivity1 {
    protected int getViewId() {
        return R.layout.common_activity2;
    }

    private NestedScrollView scrollView;
    private LinearLayout linearLayout;

    protected void initRefreshLayout() {
        refreshLayout = (PullRefreshLayout) findViewById(R.id.refreshLayout);
        scrollView = (NestedScrollView) findViewById(R.id.sv);
        linearLayout = (LinearLayout) findViewById(R.id.ll);
        setImages();
        refreshLayout.setTwinkEnable(true);
        refreshLayout.setAutoLoadingEnable(true);
        refreshLayout.setLoadMoreEnable(true);
        refreshLayout.setHeaderView(new HeaderWithAutoLoading(getBaseContext(), "LineSpinFadeLoaderIndicator", refreshLayout));
        refreshLayout.setFooterView(new ClassicHoldLoadView(getApplicationContext(), refreshLayout));
        refreshLayout.setLoadTriggerDistance((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 60, getResources().getDisplayMetrics()));
        refreshLayout.setTargetView(scrollView);

        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("onRefresh", "onRefresh run: ");
                        refreshLayout.refreshComplete();
                        ClassicHoldLoadView classicLoadView = refreshLayout.getFooterView();
                        classicLoadView.holdReset();
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
                        if (linearLayout.getChildCount() > 5) {
                            ClassicHoldLoadView classicLoadView = refreshLayout.getFooterView();
                            classicLoadView.loadFinish();
                            return;
                        }

                        linearLayout.addView(LayoutInflater.from(getApplicationContext()).inflate(R.layout.simple_item, null));
                        if (refreshLayout.getMoveDistance() < 0) {
                            refreshLayout.setDispatchTouchAble(false);
                        }
                        refreshLayout.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                scrollView.scrollBy(0, -refreshLayout.getMoveDistance());
                                ClassicHoldLoadView classicLoadView = refreshLayout.getFooterView();
                                classicLoadView.startBackAnimation();
                            }
                        }, 150);
                    }
                }, 2000);
            }
        });
    }

    private void setImages() {
        ((ImageView) findViewById(R.id.iv1)).setImageDrawable(ContextCompat
                .getDrawable(getApplicationContext(), R.drawable.img1));
        ((ImageView) findViewById(R.id.iv2)).setImageDrawable(ContextCompat
                .getDrawable(getApplicationContext(), R.drawable.img2));
        ((ImageView) findViewById(R.id.iv3)).setImageDrawable(ContextCompat
                .getDrawable(getApplicationContext(), R.drawable.img3));
        ((ImageView) findViewById(R.id.iv4)).setImageDrawable(ContextCompat
                .getDrawable(getApplicationContext(), R.drawable.img4));

        findViewById(R.id.iv1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext().getApplicationContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.iv2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext().getApplicationContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.iv3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext().getApplicationContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.iv4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(v.getContext().getApplicationContext(), v.getId() + "", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
