package me.inrush.mediaplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.inrush.mediaplayer.fragments.VideoFragment;
import me.inrush.mediaplayer.media.music.pages.MusicFragment;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author inrush
 */
public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    private static final int RC = 0x8888;

    @BindView(R.id.tv_music)
    TextView mMusicPageBtn;
    @BindView(R.id.tv_video)
    TextView mVideoPageBtn;
    @BindView(R.id.vp_content)
    ViewPager mContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_main);
        // 申请权限
        if (!havePerm(this)) {
            requestPerm();
        }
        ButterKnife.bind(this);
        init();
    }

    /**
     * 选择标题栏的音乐分类按钮
     */
    private void selectMusicPage() {
        mMusicPageBtn.setSelected(true);
        mMusicPageBtn.setTextColor(Color.parseColor("#000000"));
        mVideoPageBtn.setSelected(false);
        mVideoPageBtn.setTextColor(Color.parseColor("#ffffff"));
    }

    private void selectVideoPage() {
        mVideoPageBtn.setSelected(true);
        mVideoPageBtn.setTextColor(Color.parseColor("#000000"));
        mMusicPageBtn.setSelected(false);
        mMusicPageBtn.setTextColor(Color.parseColor("#ffffff"));
    }

    private void init() {
        selectMusicPage();
        mContent.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new MusicFragment();
                    case 1:
                        return new VideoFragment();
                    default:
                        break;
                }
                return null;
            }

            @Override
            public int getCount() {
                return 2;
            }
        });
        mContent.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        selectMusicPage();
                        break;
                    case 1:
                        selectVideoPage();
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mMusicPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContent.setCurrentItem(0, true);
            }
        });
        mVideoPageBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mContent.setCurrentItem(1, true);
            }
        });
    }

    /**
     * 获取是否有读权限
     *
     * @param context 上下文
     * @return true就是有
     */
    private static boolean havePerm(Context context) {
        // 准备需要检查的录音权限
        String[] perms = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        return EasyPermissions.hasPermissions(context, perms);
    }


    /**
     * 申请权限
     */
    @AfterPermissionGranted(RC)
    private void requestPerm() {
        String[] perms = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        if (EasyPermissions.hasPermissions(this, perms)) {
            App.showToast("获取权限成功");
        } else {
            EasyPermissions.requestPermissions(this, "授予权限", RC, perms);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {

    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        // 如果有没有申请成功的权限存在,则弹出弹出框,用户点击去到设置界面自己打开权限
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog
                    .Builder(this)
                    .build().show();
        }
    }

    @Override
    public void onBackPressed() {
        goHome();
    }

    private void goHome() {
        Intent home = new Intent(Intent.ACTION_MAIN);
        home.addCategory(Intent.CATEGORY_HOME);
        startActivity(home);
    }
}
