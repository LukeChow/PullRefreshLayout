package com.yan.refreshloadlayouttest.widget;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wang.avi.AVLoadingIndicatorView;
import com.yan.pullrefreshlayout.PullRefreshLayout;
import com.yan.pullrefreshlayout.ShowGravity;
import com.yan.pullrefreshlayout.ViscousInterpolator;
import com.yan.refreshloadlayouttest.R;

/**
 * Created by yan on 2017/10/11.
 */

public class ClassicHoldLoadView extends FrameLayout implements PullRefreshLayout.OnPullListener {
    private TextView tv;
    private AVLoadingIndicatorView loadingView;
    private PullRefreshLayout refreshLayout;
    private ObjectAnimator objectAnimator;

    private boolean isLoadFinish;
    private boolean interceptEvent;

    public ClassicHoldLoadView(@NonNull Context context, final PullRefreshLayout refreshLayout) {
        super(context);
        this.refreshLayout = refreshLayout;
        this.refreshLayout.setFooterFront(true);
        this.refreshLayout.setFooterShowGravity(ShowGravity.PLACEHOLDER);
        // 设置 布局 为 match_parent
        setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        setBackgroundColor(Color.WHITE);
        initView();

        post(new Runnable() {
            @Override
            public void run() {
                setTranslationY(getHeight());
            }
        });
    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.refresh_view, this, true);
        tv = (TextView) findViewById(R.id.title);
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext().getApplicationContext(), "you just touched me", Toast.LENGTH_SHORT).show();
            }
        });
        loadingView = (AVLoadingIndicatorView) findViewById(R.id.loading_view);

        loadingView.setIndicator("LineScaleIndicator");
        loadingView.setIndicatorColor(ContextCompat.getColor(getContext(), R.color.colorPrimary));
        refreshLayout.setAnimationMainInterpolator(new ViscousInterpolator());
        refreshLayout.setAnimationOverScrollInterpolator(new LinearInterpolator());
    }

    // 动画初始化
    private void animationInit() {
        if (objectAnimator != null) return;

        objectAnimator = ObjectAnimator.ofFloat(this, "y", 0, 0);
        objectAnimator.setDuration(300);
        objectAnimator.setInterpolator(new ViscousInterpolator(8));

        objectAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                refreshLayout.loadMoreComplete();
                refreshLayout.setMoveWithFooter(true);
                refreshLayout.setDispatchTouchAble(true);
                refreshLayout.cancelTouchEvent();
                loadingView.smoothToHide();
            }
        });
    }

    // 自定义回复动画
    public void startBackAnimation() {
        // 记录refreshLayout移动距离
        int moveDistance = refreshLayout.getMoveDistance();
        if (moveDistance >= 0) {// moveDistance大于等于0时不主动处理
            refreshLayout.loadMoreComplete();
            refreshLayout.setDispatchTouchAble(true);
            loadingView.smoothToHide();
            return;
        }
        // 设置事件为ACTION_CANCEL
        refreshLayout.cancelTouchEvent();
        // 阻止refreshLayout的事件分发
        refreshLayout.setDispatchTouchAble(false);
        // 先设置footer不跟随移动
        refreshLayout.setMoveWithFooter(false);
        // 再设置内容移动到0的位置
        refreshLayout.moveChildren(0);

        // 调用自定义footer动画
        animationInit();
        objectAnimator.setFloatValues(getY(), getY() - moveDistance);
        objectAnimator.start();
    }

    public void loadFinish() {
        if (refreshLayout.isLoadMoreEnable()) {
            isLoadFinish = true;
            refreshLayout.setLoadMoreEnable(false);
            refreshLayout.setAutoLoadingEnable(false);
            tv.setText("no more data");
            loadingView.setVisibility(GONE);

            View target = refreshLayout.getTargetView();
            View paddingView = null;
            if (target instanceof NestedScrollView) {
                final NestedScrollView nsv = ((NestedScrollView) target);
                nsv.setOnScrollChangeListener(onScrollChangeListener);
                paddingView = nsv.getChildAt(0);
                paddingView.setPadding(target.getPaddingLeft(), target.getPaddingTop()
                        , target.getPaddingRight(), target.getPaddingBottom() + tv.getHeight());

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        nsv.scrollBy(0, Math.min(Math.abs(refreshLayout.getMoveDistance()), tv.getHeight()));
                        dellHoldFinish();
                    }
                }, 150);
            }
        }
    }

    public void holdReset() {
        if (!isLoadFinish) {
            return;
        }
        View target = refreshLayout.getTargetView();
        if (target instanceof NestedScrollView) {
            ((NestedScrollView) target).setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
                public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                }
            });
            View paddingView = ((NestedScrollView) target).getChildAt(0);
            paddingView.setPadding(target.getPaddingLeft(), target.getPaddingTop()
                    , target.getPaddingRight(), target.getPaddingBottom() - tv.getHeight());
        }

        postDelayed(new Runnable() {
            @Override
            public void run() {
                isLoadFinish = false;
                refreshLayout.setLoadMoreEnable(true);
                refreshLayout.setAutoLoadingEnable(true);
                setTranslationY(getHeight() + refreshLayout.getMoveDistance());
            }
        }, 150);
    }

    private void dellHoldFinish() {
        if (refreshLayout.getMoveDistance() < 0 && refreshLayout.getMoveDistance() >= -refreshLayout.getLoadTriggerDistance()) {
            refreshLayout.moveChildren(0);
            setHoldTranslationY();
        } else if (refreshLayout.getMoveDistance() < -refreshLayout.getLoadTriggerDistance()) {
            refreshLayout.setDispatchPullTouchAble(false);
            interceptEvent = true;
            refreshLayout.moveChildren(refreshLayout.getMoveDistance() + tv.getHeight());
        }
        refreshLayout.loadMoreComplete();
    }

    private final NestedScrollView.OnScrollChangeListener onScrollChangeListener = new NestedScrollView.OnScrollChangeListener() {
        @Override
        public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
            setHoldTranslationY();
        }
    };

    private void setHoldTranslationY() {
        ViewGroup viewGroup = refreshLayout.getTargetView();
        if (viewGroup instanceof NestedScrollView) {
            setTranslationY(viewGroup.getChildAt(0).getHeight() - viewGroup.getScrollY() + refreshLayout.getMoveDistance() - tv.getHeight());
        }
    }

    private void onHoldScroll() {
        if (!isLoadFinish) {
            setTranslationY(getHeight() + refreshLayout.getMoveDistance());
        } else {
            if (interceptEvent) {
                refreshLayout.setDispatchPullTouchAble(true);
                refreshLayout.cancelTouchEvent();
            }
            setHoldTranslationY();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        loadingView.smoothToHide();
        loadingView.clearAnimation();

        if (objectAnimator != null) {
            objectAnimator.cancel();
            objectAnimator = null;
        }
    }

    @Override
    public void onPullChange(float percent) {
        onPullHolding();

        onHoldScroll();

        // 判断是否处在 拖拽的状态
        if (refreshLayout.isDragDown() || refreshLayout.isDragUp() || !refreshLayout.isLoadMoreEnable()) {
            return;
        }

        if (!refreshLayout.isHoldingTrigger() && (percent < 0)) {
            refreshLayout.autoLoading();
        }
    }

    @Override
    public void onPullHoldTrigger() {
    }

    @Override
    public void onPullHoldUnTrigger() {
    }

    @Override
    public void onPullHolding() {
        if (loadingView.getVisibility() != VISIBLE && refreshLayout.isLoadMoreEnable()) {
            loadingView.smoothToShow();
            tv.setText("loading...");
        }
    }

    @Override
    public void onPullFinish() {
        if (refreshLayout.isLoadMoreEnable()) {
            tv.setText("loading finish");
        }
        loadingView.smoothToHide();
    }

    @Override
    public void onPullReset() {
        interceptEvent = false;
        refreshLayout.setDispatchPullTouchAble(true);
        /*
         * 内容没有铺满时继续执行自动加载
         */
        if (!refreshLayout.isTargetAbleScrollDown() && !refreshLayout.isTargetAbleScrollUp()) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!refreshLayout.isRefreshing()) {
                        refreshLayout.autoLoading();
                    }
                }
            }, 250);
        }
    }
}
