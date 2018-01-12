package me.inrush.mediaplayer.common.recyclerviewpage;

import java.util.List;

import me.inrush.mediaplayer.common.BaseRecyclerAdapter;

/**
 * @author inrush
 * @date 2018/1/8.
 */

public abstract class RecyclerViewPageAdapter<T> extends BaseRecyclerAdapter<T> {
    private int originCount = 0;

    public RecyclerViewPageAdapter(List<T> dataList) {
        super(dataList);
        originCount = dataList.size();
    }

    public void setLoopPage() {
        if (mDataList.size() != originCount) {
            return;
        }
        T lastItem = mDataList.get(mDataList.size() - 1);
        T firstItem = mDataList.get(0);
        mDataList.add(firstItem);
        mDataList.add(0, lastItem);
    }
}
