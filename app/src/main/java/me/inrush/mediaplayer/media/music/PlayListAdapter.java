package me.inrush.mediaplayer.media.music;

import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.common.BaseRecyclerAdapter;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * @author inrush
 * @date 2017/12/28.
 */

public class PlayListAdapter extends BaseRecyclerAdapter<Media> {

    private WeakReference<MusicService> mMusicPlayer;

    public PlayListAdapter(List<Media> dataList, MusicService player) {
        super(dataList);
        this.mMusicPlayer = new WeakReference<>(player);
    }

    @Override
    protected int getItemViewType(int position, Media data) {
        return R.layout.item_play_list;
    }

    @Override
    protected BaseViewHolder<Media> onCreateViewHolder(View root, int viewType) {
        return new ViewHolder(root);
    }


    class ViewHolder extends BaseViewHolder<Media> {
        @BindView(R.id.tv_name)
        TextView mName;
        @BindView(R.id.tv_singer)
        TextView mSinger;
        @BindView(R.id.tv_placeholder)
        TextView mPlaceHolder;
        @BindView(R.id.iv_delete)
        ImageView mDeleteBtn;

        public ViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(final Media data) {
            mName.setText(data.getName());
            mSinger.setText(data.getArtist());
            mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mMusicPlayer.get().removeMusic(data);
                    notifyDataSetChanged();
                }
            });
            int color = Color.BLACK;
            if (mMusicPlayer.get().getCurrentMusic() == data) {
                color = ContextCompat.getColor(App.getInstance(), R.color.colorPrimary);
            }
            setColor(R.id.tv_name, color);
            setColor(R.id.tv_singer, color);
            setColor(R.id.tv_placeholder, color);
        }
    }


}
