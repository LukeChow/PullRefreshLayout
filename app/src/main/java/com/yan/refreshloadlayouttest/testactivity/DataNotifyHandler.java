package com.yan.refreshloadlayouttest.testactivity;

import com.yan.pullrefreshlayout.PullRefreshLayout;

import java.lang.ref.WeakReference;

/**
 * Created by yan on 2017/10/2 .
 * 在prl没有移动的时候刷新数据,防止卡顿
 */
public class DataNotifyHandler implements Runnable {
    private WeakReference<Object> holder;
    private SimpleAdapter adapter;
    private PullRefreshLayout prl;

    private volatile Runnable dataSet;

    public DataNotifyHandler(Object holder, PullRefreshLayout prl, SimpleAdapter adapter) {
        this.holder = new WeakReference<>(holder);
        this.adapter = adapter;
        this.prl = prl;
    }

    public void notifyDataSetChanged(Runnable dataSet) {
        this.dataSet = dataSet;
        prl.removeCallbacks(this);
        notifyDataChanged();
    }

    private void notifyDataChanged() {
        Object h = holder.get();
        if (h != null && adapter != null && prl != null) {
            if (prl.isLayoutMoving() || (prl.isRefreshing() && prl.getMoveDistance() < 0) || (prl.isLoading() && prl.getMoveDistance() > 0)) {
                prl.postDelayed(this, 500);
            } else {
                if (dataSet != null) {
                    dataSet.run();
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void run() {
        notifyDataChanged();
    }
}
