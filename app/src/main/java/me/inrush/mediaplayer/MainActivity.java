package me.inrush.mediaplayer;

import android.Manifest;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;
import com.qmuiteam.qmui.widget.QMUITopBar;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.inrush.mediaplayer.media.music.pages.MusicFragment;
import me.inrush.mediaplayer.fragments.VideoFragment;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @author inrush
 */
public class MainActivity extends AppCompatActivity
        implements EasyPermissions.PermissionCallbacks {

    private static final int RC = 0x8888;

    @BindView(R.id.topbar)
    QMUITopBar mTopBar;
    @BindView(R.id.vp_content)
    ViewPager mContent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_main);
        // 申请权限
        if (!haveReadExternalPerm(this)) {
            requestPerm();
        }
        ButterKnife.bind(this);
        init();
    }

    private void init() {
        mTopBar.setTitle("音乐列表");
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
                        mTopBar.setTitle("音乐列表");
                        break;
                    case 1:
                        mTopBar.setTitle("视频列表");
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 获取是否有读权限
     *
     * @param context 上下文
     * @return true就是有
     */
    private static boolean haveReadExternalPerm(Context context) {
        // 准备需要检查的录音权限
        String[] perms = new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE
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
}
