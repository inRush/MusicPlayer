package me.inrush.mediaplayer.media.music;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.text.DecimalFormat;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import me.inrush.mediaplayer.R;
import me.inrush.mediaplayer.media.bean.Media;
import me.inrush.mediaplayer.media.common.MediaListenerUnBinder;
import me.inrush.mediaplayer.media.common.MediaStatus;
import me.inrush.widget.CircleImageView;

/**
 * @author inrush
 * @date 2017/12/18.
 */

public class MusicActivity extends AppCompatActivity {
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

    private MusicPlayer mMusicPlayer;
    private MediaListenerUnBinder mUnBinder;
    private ObjectAnimator mThumbAnimator;

    DecimalFormat progressDf = new DecimalFormat("00.00");


    public static void start(Activity activity, int id) {
        Intent intent = new Intent(activity, MusicActivity.class);
        intent.putExtra("music", id);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_music);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        int id = intent.getIntExtra("music", 0);

        initThumbAnimator();
        initTopBart();
        // 初始化音乐播放器
        initMusicPlayer(id);
        // 初始化事件
        initEvent();
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
                overridePendingTransition(R.anim.slide_still, R.anim.slide_out_right);
            }
        });
    }


    private void initMusicPlayer(int id) {
        mMusicPlayer = MusicPlayer.getPlayer();
        int index = mMusicPlayer.getMusicInPlayListIndex(id);
        // 设置监听
        mUnBinder = mMusicPlayer.bindMusicProgressChangeListener(new OnMusicChangeListenerImpl() {
            @Override
            public void onProgressChange(int progress) {
                if (progress < 0) {
                    return;
                }
                setCurrentProgress(progress);
            }

            @Override
            public void onMusicChange(Media music) {
                reset();
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
            public void onMusicPlayModeChange(MusicPlayMode mode) {
                setPlayMode(mode);
            }

            @Override
            public void onMusicPlayListCountChange(int newCount, int oldCount) {
                if (newCount == 0) {
                    finish();
                }
            }
        });
        // 播放音频文件
        mMusicPlayer.play(index);
        setPlayMode(mMusicPlayer.getPlayMode());
    }

    private void reset() {
        // 获取音乐的总时长true
        int duration = mMusicPlayer.getDuration();
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


    private void initEvent() {
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

    @OnClick(R.id.iv_play)
    void onPlayMedia(View v) {
        if (mMusicPlayer.isPlaying()) {
            mMusicPlayer.pause();
            v.setSelected(false);
        } else {
            mMusicPlayer.play();
            v.setSelected(true);
        }
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
    protected void onDestroy() {
        // Activity 销毁之前,要解除音乐播放器的监听器的绑定
        if (mUnBinder != null) {
            mUnBinder.unBind();
        }
        super.onDestroy();
    }


}
