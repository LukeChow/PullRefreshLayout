package com.yan.refreshloadlayouttest;

import android.content.Context;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.refreshloadlayouttest.widget.ClassicLoadView;

/**
 * Created by yan on 2017/7/4.
 */

public class HeaderWithAutoLoading extends HeaderOrFooter {
    PullRefreshLayout refreshLayout;

    public HeaderWithAutoLoading(Context context, PullRefreshLayout refreshLayout) {
        super(context);
        this.refreshLayout = refreshLayout;
    }

    @Override
    public void onPullReset() {
        super.onPullReset();
        ClassicLoadView classicLoadView = refreshLayout.getFooterView();
        classicLoadView.onPullReset();
    }
}