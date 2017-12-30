package me.inrush.mediaplayer.fragments;

import android.animation.ObjectAnimator;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUIRadiusImageView;
import com.qmuiteam.qmui.widget.dialog.QMUIDialog;
import com.qmuiteam.qmui.widget.dialog.QMUIDialogAction;
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog;

import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.common.BaseFragment;
import me.inrush.mediaplayer.common.BaseRecyclerAdapter;
import me.inrush.mediaplayer.media.MediaRecyclerAdapter;
import me.inrush.mediaplayer.media.MediaUtil;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.music.MusicActivity;
import me.inrush.mediaplayer.media.music.MusicPlayer;
import me.inrush.mediaplayer.media.music.OnMusicChangeListenerImpl;
import me.inrush.mediaplayer.media.music.PlayListBottomSheetDialog;

/**
 * @author inrush
 * @date 2017/12/27.
 */

public class MusicFragment extends BaseFragment {

    @BindView(R.id.rc_media_list)
    RecyclerView mMusicList;
    @BindView(R.id.iv_play)
    ImageView mPlayBtn;
    @BindView(R.id.cv_thumb)
    QMUIRadiusImageView mThumb;
    @BindView(R.id.tv_name)
    TextView mMusicName;
    @BindView(R.id.tv_singer)
    TextView mSinger;
    @BindView(R.id.pb_progress)
    ProgressBar mProgress;
    @BindView(R.id.fl_play_panel)
    FrameLayout mPlayPanel;

    private MusicPlayer mMusicPlayer;
    private int mCurrentMusicId = -1;
    private ObjectAnimator mThumbAnimator;

    @Override
    protected int getContentLayoutId() {
        return R.layout.fragment_music;
    }

    @Override
    protected void initWidget(View root) {
        super.initWidget(root);
        initThumbAnimator();
        mMusicName.setSelected(true);
        mSinger.setSelected(true);
    }

    private void initThumbAnimator() {
        mThumbAnimator = ObjectAnimator
                .ofFloat(mThumb, "rotation", 0f, 360f)
                .setDuration(30000);
        mThumbAnimator.setRepeatCount(-1);
        mThumbAnimator.setInterpolator(new LinearInterpolator());

    }

    @Override
    protected void initData() {
        super.initData();
        final List<Media> mediaList = MediaUtil.getAudioMedias(getContext());
        mMusicList.setLayoutManager(new LinearLayoutManager(getContext()));
        MediaRecyclerAdapter adapter = new MediaRecyclerAdapter(mediaList);
        adapter.setListener(new BaseRecyclerAdapter.AdapterListenerImpl<Media>() {
            @Override
            public void onItemClick(BaseRecyclerAdapter.BaseViewHolder holder, Media data) {
                if (mMusicPlayer.getMusicList().size() == 0) {
                    mMusicPlayer.addMusics(mediaList);
                } else if (mMusicPlayer.getMusicInOriginListIndex(data.getId()) == -1) {
                    mMusicPlayer.cleanPlayList();
                    mMusicPlayer.addMusics(mediaList);
                }
                MusicActivity.start(getActivity(), data.getId());
            }

            @Override
            public void onItemLongClick(BaseRecyclerAdapter.BaseViewHolder holder, final Media data) {
                new QMUIDialog.MessageDialogBuilder(getActivity())
                        .setTitle("提示")
                        .setMessage("确定添加到列表中？")
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
                                final QMUITipDialog tipDialog;
                                if (mMusicPlayer.getMusicInOriginListIndex(data.getId()) == -1) {
                                    mMusicPlayer.addMusic(data);
                                    tipDialog = new QMUITipDialog.Builder(getContext())
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_SUCCESS)
                                            .setTipWord("添加成功")
                                            .create();
                                } else {
                                    tipDialog = new QMUITipDialog.Builder(getContext())
                                            .setIconType(QMUITipDialog.Builder.ICON_TYPE_FAIL)
                                            .setTipWord("已存在列表中")
                                            .create();
                                }
                                tipDialog.show();
                                mMusicList.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        tipDialog.dismiss();
                                    }
                                }, 1500);
                            }
                        })
                        .show();
            }
        });
        mMusicList.setAdapter(adapter);
        mMusicPlayer = MusicPlayer.getPlayer();

        mMusicPlayer.bindMusicProgressChangeListener(new OnMusicChangeListenerImpl() {
            @Override
            public void onProgressChange(int progress) {
                mProgress.setProgress(progress);
            }

            @Override
            public void onMusicChange(Media media) {
                setCurrentMusic(media);
            }

            @Override
            public void onPlayerStatusChange(MediaStatus status) {
                if (status == MediaStatus.START) {
                    mPlayBtn.setSelected(true);
                    if (mThumbAnimator.isPaused()) {
                        mThumbAnimator.resume();
                    } else {
                        mThumbAnimator.start();
                    }
                } else if (status == MediaStatus.PAUSE) {
                    mPlayBtn.setSelected(false);
                    mThumbAnimator.pause();
                }
            }

            @Override
            public void onMusicPlayListCountChange(int newCount, int oldCount) {
                if (newCount == 0) {
                    mPlayPanel.setVisibility(View.GONE);
                } else {
                    mPlayPanel.setVisibility(View.VISIBLE);
                }
                setCurrentMusic(mMusicPlayer.getCurrentMusic());
            }
        });
    }

    private void setCurrentMusic(Media media) {
        if (media == null) {
            return;
        }
        Bitmap thumb = media.getThumb();
        if (thumb != null) {
            mThumb.setImageBitmap(thumb);
        } else {
            mThumb.setImageBitmap(
                    BitmapFactory.decodeResource(getContext().getResources(),
                            R.drawable.placeholder_disk_play_program));
        }
        mMusicName.setText(media.getName());
        mSinger.setText(media.getArtist());
        mCurrentMusicId = media.getId();
        if (mProgress.getMax() != mMusicPlayer.getDuration()) {
            mProgress.setMax(mMusicPlayer.getDuration());
            mProgress.setProgress(0);
        }

    }


    @OnClick(R.id.ll_play_area)
    void onPlayAreaClick() {
        if (mCurrentMusicId != -1) {
            MusicActivity.start(getActivity(), mCurrentMusicId);
        }
    }

    @OnClick(R.id.iv_next)
    void onNextBtnClick() {
        mMusicPlayer.nextMusic();
    }

    @OnClick(R.id.iv_play)
    void onPlayBtnClick() {
        mMusicPlayer.switchState();
    }


    @OnClick(R.id.iv_play_list)
    void onPlayListBtnClick() {
        new PlayListBottomSheetDialog()
                .show(getFragmentManager(), "playlist");
    }
}
