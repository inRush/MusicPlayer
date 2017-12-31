package me.inrush.mediaplayer.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * @author inrush
 * @date 2017/7/21.
 * @package me.inrush.common.app
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected Unbinder mRootUnbinder;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        QMUIStatusBarHelper.translucent(this);
        // 在界面为初始化之前调用的初始化窗口
        initWindows();

        if (initArgs(getIntent().getExtras())) {
            setContentView(getContentLayoutId());
            initWidget();
            initData();
        } else {
            finish();
        }
    }

    /**
     * 初始化窗口
     */
    protected void initWindows() {

    }

    /**
     * 初始化相关参数
     *
     * @param bundle 参数bundle
     * @return 如果参数正确返回True, 否则返回False
     */
    protected boolean initArgs(Bundle bundle) {
        return true;
    }

    /**
     * 获取当前layout的Id
     *
     * @return layoutId(资源文件Id)
     */
    protected abstract int getContentLayoutId();

    /**
     * 初始化控件
     */
    protected void initWidget() {
        mRootUnbinder = ButterKnife.bind(this);
    }

    /**
     * 初始化数据
     */
    protected void initData() {

    }

    @Override
    public boolean onSupportNavigateUp() {
        // 当街面导航返回时,Finish当前的界面
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        // 得到当前Activity下所有的Fragment
        @SuppressWarnings("RestrictedApi")
        List<Fragment> fragments = getSupportFragmentManager().getFragments();

        // 判断是否为空
        if (fragments != null && fragments.size() > 0) {
            for (Fragment fragment : fragments) {
                // 判断是否为能够处理的Fragment类型
                if (fragment instanceof BaseFragment) {
                    // 判断是否拦截了返回按钮
                    if (((BaseFragment) fragment).onBackPressed()) {
                        // 有则直接Return
                        return;
                    }
                }
            }
        }
        super.onBackPressed();
        finish();
    }


}
