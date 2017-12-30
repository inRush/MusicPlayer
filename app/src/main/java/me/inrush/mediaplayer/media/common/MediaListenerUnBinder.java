package me.inrush.mediaplayer.media.common;

/**
 * @author inrush
 * @date 2017/12/27.
 */

public class MediaListenerUnBinder {
    /**
     * 监听器索引
     */
    private int index;
    private OnUnBindListener mListener;

    /**
     * 解除绑定监听器
     */
    public interface OnUnBindListener {
        /**
         * 解除绑定
         *
         * @param index 监听器索引
         */
        void onUnBind(int index);
    }

    public MediaListenerUnBinder(int index, OnUnBindListener listener) {
        this.index = index;
        this.mListener = listener;
    }

    public void unBind() {
        mListener.onUnBind(index);
    }
}
