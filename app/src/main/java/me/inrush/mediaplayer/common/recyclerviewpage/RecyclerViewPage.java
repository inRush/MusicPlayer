package me.inrush.mediaplayer.common.recyclerviewpage;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author inrush
 * @date 2018/1/8.
 */

public class RecyclerViewPage extends RecyclerView {

    private OnPageChangeListener mPageChangeListener;
    private SnapHelper mSnapHelper;
    private int mCurrentPosition;
    private int mLastPosition = -1;
    private View mLastView = null;

    public RecyclerViewPage(Context context) {
        this(context, null);
    }

    public RecyclerViewPage(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewPage(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    @Override
    public void requestLayout() {
        RecyclerViewPageAdapter adapter = (RecyclerViewPageAdapter) getAdapter();
        if (adapter != null) {
            adapter.setLoopPage();
        }
        super.requestLayout();
    }

    private void init() {
        mSnapHelper = new PagerSnapHelper();
        mSnapHelper.attachToRecyclerView(this);
        addOnScrollListener(new OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE) {
                    mCurrentPosition = getChildAdapterPosition(mSnapHelper.findSnapView(getLayoutManager()));
                    if (mCurrentPosition == getAdapter().getItemCount() - 1) {
                        scrollToPosition(1);
                        mCurrentPosition = 1;
                    } else if (mCurrentPosition == 0) {
                        scrollToPosition(getAdapter().getItemCount() - 2);
                        mCurrentPosition = getAdapter().getItemCount() - 2;
                    }
                    postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            View newView = mSnapHelper.findSnapView(getLayoutManager());
                            mCurrentPosition = mCurrentPosition - 1;
                            if (mPageChangeListener != null) {
                                mPageChangeListener.onPageChange(mCurrentPosition, newView, mLastPosition, mLastView);
                            }
                            mLastPosition = mCurrentPosition;
                            mLastView = newView;
                        }
                    }, 100);

                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mPageChangeListener != null) {
                    mPageChangeListener.onPageScroll(dx, dy);
                }
                if (mLastView == null) {
                    // 初始化最开始的ItemView
                    mLastView = mSnapHelper.findSnapView(getLayoutManager());
                }
            }
        });
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        mPageChangeListener = listener;
    }

    public View getCurrentPageView() {
        return mSnapHelper.findSnapView(getLayoutManager());
    }


    /**
     * 滚动到对应的页面
     *
     * @param page 目标页码
     */
    public void scrollToPage(int page) {
        this.scrollToPosition(page + 1);
    }

    public void smoothScrollToPage(int page) {
        this.smoothScrollToPosition(page + 1);
    }

    public interface OnPageChangeListener {
        /**
         * 当前页面发生变化
         *
         * @param newPosition 新的位置
         * @param newView     新的ItemView
         * @param oldPosition 旧的位置
         * @param oldView     旧的ItemView
         */
        void onPageChange(int newPosition, View newView, int oldPosition, View oldView);

        /**
         * 页面在滚动
         *
         * @param dx x位移
         * @param dy y位移
         */
        void onPageScroll(int dx, int dy);
    }


}
