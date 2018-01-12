package me.inrush.mediaplayer.media.music.pages.musicplaying;

import android.view.View;

import java.util.List;

import butterknife.BindView;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.common.recyclerviewpage.RecyclerViewPageAdapter;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.widget.CircleImageView;

/**
 * @author inrush
 * @date 2018/1/8.
 */

public class MusicPageAdapter extends RecyclerViewPageAdapter<Media> {

    public MusicPageAdapter(List<Media> dataList) {
        super(dataList);
    }

    @Override
    protected int getItemViewType(int position, Media data) {
        return R.layout.item_music_page;
    }

    @Override
    protected BaseViewHolder<Media> onCreateViewHolder(View root, int viewType) {
        return new ViewHolder(root);
    }


    class ViewHolder extends BaseViewHolder<Media> {
        @BindView(R.id.iv_thumb)
        CircleImageView mThumb;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(Media data, int position) {
            mThumb.setImageBitmap(data.getThumb());
        }
    }
}
