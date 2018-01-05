package me.inrush.mediaplayer.media;

import android.graphics.Bitmap;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUIRadiusImageView;

import java.util.List;

import butterknife.BindView;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.common.BaseRecyclerAdapter;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.utils.MediaUtil;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class MediaRecyclerAdapter extends BaseRecyclerAdapter<Media> {
    public MediaRecyclerAdapter(List<Media> dataList) {
        super(dataList);
    }

    @Override
    protected int getItemViewType(int position, Media data) {
        return R.layout.item_music;
    }

    @Override
    protected BaseViewHolder<Media> onCreateViewHolder(View root, int viewType) {
        return new ViewHolder(root);
    }

    class ViewHolder extends BaseViewHolder<Media> {
        @BindView(R.id.tv_name)
        TextView mName;
        @BindView(R.id.tv_size)
        TextView mSize;
        @BindView(R.id.tv_date)
        TextView mDate;
        @BindView(R.id.iv_album)
        QMUIRadiusImageView mThumb;

        ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(Media data) {
            mName.setText(data.getName());
            mSize.setText(data.getSize());
            mDate.setText(data.getDate());
            Bitmap thumb = data.getThumb();
            if (thumb != null) {
                mThumb.setImageBitmap(thumb);
            } else {
                mThumb.setImageBitmap(MediaUtil.getDefaultMusicThumb());
            }
        }
    }
}
