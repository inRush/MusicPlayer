package me.inrush.mediaplayer.media.music;

import android.app.Dialog;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.inrush.mediaplayer.App;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.common.BaseRecyclerAdapter;
import me.inrush.mediaplayer.common.TransStatusBottomSheetDialog;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.music.base.MusicBroadcastReceiver;
import me.inrush.mediaplayer.media.music.base.MusicPlayMode;
import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListenerImpl;
import me.inrush.mediaplayer.media.music.listeners.OnServiceBindCompleteListener;
import me.inrush.mediaplayer.media.music.services.MusicAction;
import me.inrush.mediaplayer.media.music.services.MusicService;

/**
 * @author inrush
 * @date 2017/12/28.
 */

public class PlayListBottomSheetDialog extends BottomSheetDialogFragment {
    private View mRootView;
    private List<Media> mMusicList;
    private PlayListAdapter mAdapter;
    protected MusicService mMusicPlayer;
    private MusicBroadcastReceiver mReceiver;
    private MusicPlayerInitializer mInitializer;


    @BindView(R.id.iv_play_mode_icon)
    ImageView mPlayModeIcon;
    @BindView(R.id.tv_play_mode_text)
    TextView mPlayModeText;
    @BindView(R.id.rv_list)
    RecyclerView mPlayList;
    @BindView(R.id.tv_play_list_count)
    TextView mMusicCount;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.dialog_play_list, container, false);
            ButterKnife.bind(this, mRootView);
            initMusicPlayer();
        } else {
            if (mRootView.getParent() != null) {
                ((ViewGroup) (mRootView.getParent())).removeView(mRootView);
            }
        }
        return mRootView;
    }

    private void initMusicPlayList() {
        mMusicList = mMusicPlayer.getMusicList();
        mAdapter = new PlayListAdapter(mMusicList, mMusicPlayer);
        mAdapter.setListener(new BaseRecyclerAdapter.AdapterListenerImpl<Media>() {
            @Override
            public void onItemClick(BaseRecyclerAdapter.BaseViewHolder holder, Media data) {
                mMusicPlayer.play(data);
                mAdapter.notifyDataSetChanged();
            }
        });
        mPlayList.setAdapter(mAdapter);
        mPlayList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void initMusicPlayer() {
        mInitializer = new MusicPlayerInitializer(getContext(), new OnServiceBindCompleteListener() {
            @Override
            public void onBindComplete(MusicService player) {
                mMusicPlayer = player;
                initReceiver();
                setPlayMode(mMusicPlayer.getPlayMode());
                setMusicCount(mMusicPlayer.getMusicList().size());
                initMusicPlayList();
                resetPlayListCurrentPosition(false);
            }
        });
        mInitializer.bindToService();
    }

    private void setPlayMode(MusicPlayMode mode) {
        Bitmap bitmap = null;
        String text = "";
        switch (mode) {
            case LIST_LOOP:
                bitmap = BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.loop);
                text = "列表循环";
                break;
            case ONE_LOOP:
                bitmap = BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.one_loop);
                text = "单曲循环";
                break;
            case RANDOM:
                bitmap = BitmapFactory.decodeResource(App.getInstance().getResources(), R.drawable.random);
                text = "随机播放";
                break;
            default:
                break;
        }
        mPlayModeIcon.setImageBitmap(bitmap);
        mPlayModeText.setText(text);
    }

    private void setMusicCount(int count) {
        mMusicCount.setText(String.format(Locale.CHINA, "(%d)", count));
    }

    private void resetPlayListCurrentPosition(final boolean isAnim) {
        mPlayList.postDelayed(new Runnable() {
            @Override
            public void run() {
                int index = mMusicPlayer.getMusicIndex(mMusicPlayer.getCurrentMusic().getId(), false);
                if (index == -1) {
                    return;
                }
                if (isAnim) {
                    mPlayList.smoothScrollToPosition(index);
                } else {
                    mPlayList.scrollToPosition(index);
                }
            }
        }, 100);

    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new TransStatusBottomSheetDialog(getContext());
    }


    @OnClick(R.id.iv_clean)
    void onCleanBtnClick() {
        new QMUIDialog.MessageDialogBuilder(getActivity())
                .setTitle("提示")
                .setMessage("确定清空列表？")
                .addAction("取消", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                    }
                })
                .addAction("确定", new QMUIDialogAction.ActionListener() {
                    @Override
                    public void onClick(QMUIDialog dialog, int index) {
                        dialog.dismiss();
                        mMusicPlayer.cleanPlayList();
                    }
                })
                .show();
    }

    @OnClick(R.id.ll_play_mode_btn)
    void onPlayModeBtnClick() {
        mMusicPlayer.changePlayMode();
    }


    class MusicChangeListener extends OnMusicChangeListenerImpl {
        @Override
        public void onMusicChange(Media media) {
            super.onMusicChange(media);
            if (mMusicPlayer.getStatus() != MediaStatus.STOP) {
                mAdapter.notifyDataSetChanged();
                resetPlayListCurrentPosition(true);
            }
        }

        @Override
        public void onMusicPlayModeChange(MusicPlayMode mode) {
            super.onMusicPlayModeChange(mode);
            setPlayMode(mode);
        }

        @Override
        public void onMusicPlayListCountChange(int count) {
            if (count == 0) {
                dismiss();
                return;
            }
            setMusicCount(count);
            resetPlayListCurrentPosition(true);
        }
    }


    private void initReceiver() {
        mReceiver = new MusicBroadcastReceiver(mMusicPlayer);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicAction.MUSIC_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_LIST_COUNT_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_MODE_CHANGE);
        intentFilter.addAction(MusicAction.MUSIC_PLAY_STATUS_CHANGE);
        getContext().registerReceiver(mReceiver, intentFilter);
        mReceiver.setMusicChangeListener(new MusicChangeListener());
    }

    @Override
    public void onPause() {
        super.onPause();
        getContext().unregisterReceiver(mReceiver);
        mInitializer.onDestroy();
    }
}
