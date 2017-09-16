package com.yan.refreshloadlayouttest.testactivity;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.pullrefreshlayout.ShowGravity;
import com.yan.refreshloadlayouttest.widget.HeaderOrFooter;
import com.yan.refreshloadlayouttest.R;
import com.yan.refreshloadlayouttest.widget.TwoRefreshHeader;

public class CommonActivity1 extends AppCompatActivity {
    private static final String TAG = "CommonActivity1";

    protected PullRefreshLayout refreshLayout;

    protected int getViewId() {
        return R.layout.common_activity1;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getViewId());
        initRefreshLayout();
//        ((TextView)findViewById(R.id.tv_data)) .setMovementMethod(ScrollingMovementMethod.getInstance());
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.autoRefresh();
            }
        }, 150);
        if (findViewById(R.id.iv) != null) {
            Glide.with(this)
                    .load(R.drawable.loading_bg)
                    .into((ImageView) findViewById(R.id.iv));
        }
    }

    protected void initRefreshLayout() {
        findViewById(R.id.iv).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "refreshLayout", Toast.LENGTH_LONG).show();
            }
        });

        refreshLayout = (PullRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setLoadMoreEnable(true);
        refreshLayout.setRefreshShowGravity(ShowGravity.FOLLOW, ShowGravity.FOLLOW);

        refreshLayout.setHeaderView(new TwoRefreshHeader(getBaseContext(), refreshLayout));

        refreshLayout.setFooterView(new HeaderOrFooter(getBaseContext(), "PacmanIndicator", Color.WHITE, false));

        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                TwoRefreshHeader twoRefreshHeader = refreshLayout.getHeaderView();
                if (refreshLayout.getRefreshTriggerDistance() == twoRefreshHeader.getTwoRefreshDistance()) {
                    Toast.makeText(getApplicationContext(), "二级刷新", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getApplicationContext(), "一级刷新", Toast.LENGTH_SHORT).show();
                }
                Log.e(TAG, "refreshLayout onRefresh: ");
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.refreshComplete();
                    }
                }, 3000);
            }

            @Override
            public void onLoading() {
                Log.e(TAG, "refreshLayout onLoading: ");
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.loadMoreComplete();
                    }
                }, 3000);
            }
        });
    }
}
