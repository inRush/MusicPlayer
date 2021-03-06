package me.inrush.mediaplayer.media.music.pages.musicplaying;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUITopBar;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.common.recyclerviewpage.RecyclerViewPage;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.music.base.BaseMusicActivity;
import me.inrush.mediaplayer.media.music.base.MusicPlayMode;
import me.inrush.mediaplayer.media.music.base.MusicProgressChangeProcessor;
import me.inrush.mediaplayer.media.music.dialogs.PlayListBottomSheetDialog;
import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListener;
import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListenerImpl;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class MusicActivity extends BaseMusicActivity {
    @BindView(R.id.sb_progress)
    SeekBar mProgress;
    @BindView(R.id.iv_play)
    ImageView mPlayBtn;
    @BindView(R.id.tv_duration)
    TextView mDuration;
    @BindView(R.id.tv_total_duration)
    TextView mTotalDuration;
    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    @BindView(R.id.iv_mode)
    ImageView mModeBtn;
    @BindView(R.id.rv_music_page)
    RecyclerViewPage mMusicPage;

    private ObjectAnimator mThumbAnimator;
    private MusicProgressChangeProcessor mProgressChangeProcessor;
    private MusicPageAdapter mMusicPageAdapter;
    private List<Media> mPlayList = new ArrayList<>();
    private boolean isReset = false;

    DecimalFormat progressDf = new DecimalFormat("00.00");

    public static void start(Activity activity, boolean showAnim) {
        Intent intent = new Intent(activity, MusicActivity.class);
        activity.startActivity(intent);
        if (showAnim) {
            activity.overridePendingTransition(R.anim.grow_from_bottomleft_to_topright, R.anim.fake_anim);
        }
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_music;
    }


    @Override
    protected void initWidget() {
        super.initWidget();
        initTopBart();
        initMusicPage();
        mProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //获取拖动结束之后的位置
                int progress = seekBar.getProgress();
                // 设置播放器的播放进度
                mMusicPlayer.setCurrentProgress(progress);
                // 设置当前显示的进度
                setCurrentProgress(progress);
            }
        });

    }

    private void initMusicPage() {
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mMusicPage.setLayoutManager(linearLayoutManager);
        mMusicPage.setOnPageChangeListener(new RecyclerViewPage.OnPageChangeListener() {
            @Override
            public void onPageChange(int newPosition, View newView, int oldPosition, View oldView) {
                Log.e("tag", "onPageChange: newPosition " + newPosition);
                Log.e("tag", "onPageChange: oldPosition " + oldPosition);
                if (newPosition == oldPosition) {
                    mThumbAnimator.resume();
                } else {
                    mMusicPlayer.play(newPosition);
                    startThumbAnimator(newView);
                    if (oldView != null) {
                        oldView.setRotation(0);
                    }
                }
            }

            @Override
            public void onPageScroll(int dx, int dy) {
                if (mThumbAnimator == null) {
                    startThumbAnimator(mMusicPage.getChildAt(0));
                } else if (mThumbAnimator.isRunning()) {
                    if (!isReset) {
                        mThumbAnimator.pause();
                    } else {
                        isReset = false;
                    }
                }
            }
        });
    }

    private void resetMusicPage() {
        mPlayList.clear();
        mPlayList.addAll(mMusicPlayer.getPlayList());
        if (mMusicPageAdapter == null) {
            mMusicPageAdapter = new MusicPageAdapter(mPlayList);
            mMusicPage.setAdapter(mMusicPageAdapter);
        } else {
            mMusicPageAdapter.notifyDataSetChanged();
        }
        mMusicPage.scrollToPage(mMusicPlayer.getCurrentMusicInPlayListIndex());
        isReset = true;
    }

    /**
     * 初始化专辑封面旋转的动画
     */
    private void startThumbAnimator(final View view) {
        if (view == null) {
            return;
        }
        if (mThumbAnimator == null) {
            mThumbAnimator = ObjectAnimator
                    .ofFloat(view, "rotation", 0f, 360f)
                    .setDuration(30000);
            mThumbAnimator.setRepeatCount(-1);
            mThumbAnimator.setInterpolator(new LinearInterpolator());
        } else {
            mThumbAnimator.cancel();
            mThumbAnimator.setTarget(view);
        }
        mThumbAnimator.start();
    }

    private void replaceThumbAnimatorView(final View view, boolean reset) {
        mThumbAnimator.pause();
        mThumbAnimator.removeAllUpdateListeners();
        if (reset) {
            mThumbAnimator.setFloatValues(0f, 360f);
        }
        mThumbAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float rotateDeg = (float) animation.getAnimatedValue();
                view.setRotation(rotateDeg);
            }
        });
        mThumbAnimator.resume();
    }


    /**
     * 初始化标题栏,设置标题栏的返回按钮
     */
    private void initTopBart() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.shrink_from_topright_from_bottomleft);
            }
        });
    }

    /**
     * 重置界面显示
     * 重置内容:
     * 1.歌曲总时间
     * 2.顶部标题栏
     * 3.专辑封面
     * 4.进度条总长度
     */
    private void reset() {
        if (mIsBindComplete) {
            // 获取音乐的总时长true
            int duration = mMusicPlayer.getDuration();
            if (duration == -1) {
                mProgress.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reset();
                    }
                }, 80);
                return;
            }
            int progress = duration / 1000;
            mTotalDuration.setText(progressDf.format(progress / 60 + progress % 60 * 1.0 / 100));
            // 设置进度条的最大值为音乐的总时长
            mProgress.setMax(duration);
            setCurrentProgress(mMusicPlayer.getCurrentProgress());
            mTopBar.setTitle(mMusicPlayer.getCurrentMusic().getName());
            mTopBar.setSubTitle(mMusicPlayer.getCurrentMusic().getArtist());
        }
    }

    /**
     * 设置当前的进度
     * 设置内容:
     * 1.进度条的变化
     * 1.当前时间显示
     *
     * @param duration 当前的进度
     */
    private void setCurrentProgress(int duration) {
        int progress = duration / 1000;
        mDuration.setText(progressDf.format(progress / 60 + progress % 60 * 1.0 / 100));
        //让进度条动起来
        mProgress.setProgress(duration);
    }

    /**
     * 音乐播放器的播放模式发生变化的时候,设置界面的播放模式
     * 主要用来设置播放模式按钮的变化
     *
     * @param mode 播放模式
     */
    private void setPlayMode(MusicPlayMode mode) {
        Bitmap bitmap = null;
        switch (mode) {
            case LIST_LOOP:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.loop);
                break;
            case ONE_LOOP:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.one_loop);
                break;
            case RANDOM:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.random);
                break;
            default:
                break;
        }
        mModeBtn.setImageBitmap(bitmap);
    }

    /**
     * 设置当前页面的音乐播放状态
     * 状态 {@link MediaStatus}
     *
     * @param status 音乐播放状态
     */
    private void setMusicStatus(MediaStatus status) {
        if (status == MediaStatus.START) {
            mPlayBtn.setSelected(true);
            if (mThumbAnimator != null) {
                if (mThumbAnimator.isPaused()) {
                    mThumbAnimator.resume();
                } else {
                    mThumbAnimator.start();
                }
            }
        } else if (status == MediaStatus.PAUSE) {
            mPlayBtn.setSelected(false);
            if (mThumbAnimator != null) {
                mThumbAnimator.pause();
            }
        }
    }

    @OnClick(R.id.iv_play)
    void onPlayMedia(View v) {
        if (mMusicPlayer.isPlaying()) {
            v.setSelected(false);
        } else {
            v.setSelected(true);
        }
        mMusicPlayer.play();
    }

    @OnClick(R.id.iv_pre)
    void onPreMusic() {
        mMusicPlayer.preMusic();
    }

    @OnClick(R.id.iv_next)
    void onNextMusic() {
        mMusicPlayer.nextMusic();
    }

    @OnClick(R.id.iv_mode)
    void onModeBtnClick() {
        mMusicPlayer.changePlayMode();
    }

    @OnClick(R.id.iv_play_list)
    void onPlayListBtnClick() {
        new PlayListBottomSheetDialog()
                .show(getSupportFragmentManager(), "playlist");
    }

    @Override
    public void onDestroy() {
        // 停止进度条监听进程
        mProgressChangeProcessor.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(0, R.anim.shrink_from_topright_from_bottomleft);
    }

    @Override
    protected void onServiceBindComplete() {
        reset();
        resetMusicPage();
        setPlayMode(mMusicPlayer.getPlayMode());
        mProgressChangeProcessor =
                new MusicProgressChangeProcessor(mMusicPlayer,
                        new MusicProgressChangeProcessor.OnMusicProgressChangeListener() {
                            @Override
                            public void onProgressChange(int progress) {
                                setCurrentProgress(progress);
                            }
                        });
        setMusicStatus(mMusicPlayer.getStatus());
    }

    @Override
    protected OnMusicChangeListener getMusicChangeListener() {
        return new MusicChangeListener();
    }

    /**
     * 音乐变化监听器
     * 继承{@link OnMusicChangeListener}实现类{@link OnMusicChangeListenerImpl}
     * 实现部分事件
     */
    class MusicChangeListener extends OnMusicChangeListenerImpl {

        @Override
        public void onMusicPlayModeChange(MusicPlayMode mode) {
            super.onMusicPlayModeChange(mode);
            setPlayMode(mode);
            resetMusicPage();
        }

        @Override
        public void onMusicChange(Media media) {
            super.onMusicChange(media);
            if (mMusicPlayer.getStatus() != MediaStatus.STOP) {
                reset();
                mMusicPage.smoothScrollToPage(mMusicPlayer.getCurrentMusicInPlayListIndex());
            }
        }

        @Override
        public void onPlayerStatusChange(MediaStatus status) {
            super.onPlayerStatusChange(status);
            setMusicStatus(status);
        }

        @Override
        public void onMusicPlayListCountChange(int count) {
            if (count == 0) {
                finish();
            }
        }
    }


}
