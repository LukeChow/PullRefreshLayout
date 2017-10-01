package com.yan.refreshloadlayouttest.testactivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.refreshloadlayouttest.R;
import com.yan.refreshloadlayouttest.widget.DiDiHeader;

public class DiDiActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_didi);
        final PullRefreshLayout prl = (PullRefreshLayout) findViewById(R.id.prl);
        findViewById(R.id.click).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "item touched", Toast.LENGTH_SHORT).show();
            }
        });
        prl.setHeaderView(new DiDiHeader(getApplicationContext(), prl));
        prl.setOnRefreshListener(new PullRefreshLayout.OnRefreshListenerAdapter() {
            @Override
            public void onRefresh() {
                prl.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        prl.refreshComplete();
                    }
                }, 3000);
            }
        });
    }
}
