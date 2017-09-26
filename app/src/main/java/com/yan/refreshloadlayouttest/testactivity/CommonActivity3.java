package com.yan.refreshloadlayouttest.testactivity;


import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.refreshloadlayouttest.R;
import com.yan.refreshloadlayouttest.widget.DropboxHeader;
import com.yan.refreshloadlayouttest.widget.WithoutTwinkFooter;

import java.util.ArrayList;
import java.util.List;

public class CommonActivity3 extends BaseActivity {
    private static final String TAG = "CommonActivity3";

    protected PullRefreshLayout refreshLayout;
    private List<SimpleItem> datas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.common_activity3);
        initData();
        initListView();
        initRefreshLayout();
        refreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                refreshLayout.autoRefresh();
            }
        }, 150);
    }

    private void initListView() {
        ListView listView = (ListView) findViewById(R.id.lv_data);
        listView.setAdapter(new SimpleListAdapter(this, datas));
    }

    protected void initData() {
        datas = new ArrayList<>();
        datas.add(new SimpleItem(R.drawable.img1, "夏目友人帐"));
        datas.add(new SimpleItem(R.drawable.img2, "夏目友人帐"));
        datas.add(new SimpleItem(R.drawable.img3, "夏目友人帐"));
        datas.add(new SimpleItem(R.drawable.img4, "夏目友人帐"));
        datas.add(new SimpleItem(R.drawable.img5, "夏目友人帐"));
        datas.add(new SimpleItem(R.drawable.img6, "夏目友人帐"));
    }

    private void initRefreshLayout() {
        refreshLayout = (PullRefreshLayout) findViewById(R.id.refreshLayout);
        refreshLayout.setHeaderView(new DropboxHeader(getBaseContext(), refreshLayout));
        refreshLayout.setFooterView(new WithoutTwinkFooter(getBaseContext(), refreshLayout));
        refreshLayout.setLoadMoreEnable(false);
        refreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListenerAdapter() {
            @Override
            public void onRefresh() {
                Log.e(TAG, "refreshLayout onRefresh: ");
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.refreshComplete();
                    }
                }, 3000);
            }
        });
    }
}
