package me.inrush.mediaplayer;

import android.Manifest;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.inrush.mediaplayer.fragments.VideoFragment;
import me.inrush.mediaplayer.helper.DialogHelper;
import me.inrush.mediaplayer.media.music.pages.musiclist.MusicFragment;

/**
 * @author inrush
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_music)
    TextView mMusicPageBtn;
    @BindView(R.id.tv_video)
    TextView mVideoPageBtn;
    @BindView(R.id.vp_content)
    ViewPager mContent;
    private String[] perms = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean isInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        selectMusicPage();
    }

    @Override
    protected void onStart() {
        if(checkPermissions() && !isInit){
            init();
            isInit = true;
        }
        super.onStart();
    }

    private boolean checkPermissions() {
        if (!PermissionUtils.isGranted(perms)) {
            requestPerm();
            return false;
        }
        return true;
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
     * 申请权限
     */
    private void requestPerm() {
        PermissionUtils.permission(perms)
                .rationale(new PermissionUtils.OnRationaleListener() {
                    @Override
                    public void rationale(ShouldRequest shouldRequest) {
                        DialogHelper.showRationaleDialog(shouldRequest);
                    }
                })
                .callback(new PermissionUtils.FullCallback() {
                    @Override
                    public void onGranted(List<String> permissionsGranted) {
                        if(!isInit){
                            isInit = true;
                            init();
                        }
                        ToastUtils.showShort(R.string.get_permissions_successful);
                    }

                    @Override
                    public void onDenied(List<String> permissionsDeniedForever, List<String> permissionsDenied) {
                        DialogHelper.showOpenAppSettingDialog();
                    }
                }).request();
    }

    @Override
    public void onBackPressed() {
        ActivityUtils.startHomeActivity();
    }

}
