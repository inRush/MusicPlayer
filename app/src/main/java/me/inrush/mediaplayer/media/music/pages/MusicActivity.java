package me.inrush.mediaplayer.media.music.pages;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qmuiteam.qmui.widget.QMUITopBar;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.OnClick;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.mediaplayer.media.music.base.MusicPlayMode;
import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListener;
import me.inrush.mediaplayer.media.music.listeners.OnMusicChangeListenerImpl;
import me.inrush.mediaplayer.media.music.PlayListBottomSheetDialog;
import me.inrush.mediaplayer.media.music.base.BaseMusicActivity;
import me.inrush.mediaplayer.media.music.base.MusicProgressChangeProcessor;
import me.inrush.widget.CircleImageView;

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
    @BindView(R.id.iv_thumb)
    CircleImageView mThumb;
    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    @BindView(R.id.iv_mode)
    ImageView mModeBtn;

    private ObjectAnimator mThumbAnimator;
    private MusicProgressChangeProcessor mProgressChangeProcessor;

    DecimalFormat progressDf = new DecimalFormat("00.00");

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, MusicActivity.class);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.grow_from_bottomleft_to_topright, R.anim.fake_anim);
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_music;
    }


    @Override
    protected void initWidget() {
        super.initWidget();
        initThumbAnimator();
        initTopBart();
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
                mMusicPlayer.setCurrentProgress(progress);
                setCurrentProgress(progress);
            }
        });

    }

    private void initThumbAnimator() {
        mThumbAnimator = ObjectAnimator
                .ofFloat(mThumb, "rotation", 0f, 360f)
                .setDuration(30000);
        mThumbAnimator.setRepeatCount(-1);
        mThumbAnimator.setInterpolator(new LinearInterpolator());
    }

    private void initTopBart() {
        mTopBar.addLeftBackImageButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(0, R.anim.shrink_from_topright_from_bottomleft);
            }
        });
    }


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
            Bitmap thumb = mMusicPlayer.getCurrentMusic().getThumb();
            mThumb.setImageBitmap(thumb);
        }
    }


    private void setCurrentProgress(int duration) {
        int progress = duration / 1000;
        mDuration.setText(progressDf.format(progress / 60 + progress % 60 * 1.0 / 100));
        //让进度条动起来
        mProgress.setProgress(duration);
    }

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

    private void setMusicStatus(MediaStatus status) {
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

    class MusicChangeListener extends OnMusicChangeListenerImpl {

        @Override
        public void onMusicPlayModeChange(MusicPlayMode mode) {
            super.onMusicPlayModeChange(mode);
            setPlayMode(mode);
        }

        @Override
        public void onMusicChange(Media media) {
            super.onMusicChange(media);
            if (mMusicPlayer.getStatus() != MediaStatus.STOP) {
                reset();
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
