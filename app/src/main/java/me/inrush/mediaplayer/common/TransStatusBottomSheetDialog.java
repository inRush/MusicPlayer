package me.inrush.mediaplayer.common;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.view.ViewGroup;
import android.view.Window;

import com.qmuiteam.qmui.util.QMUIDisplayHelper;
import com.qmuiteam.qmui.util.QMUIStatusBarHelper;

/**
 * 透明状态栏的BottomSheetDialog
 *
 * @author inrush
 * @date 2017/12/4.
 */

public class TransStatusBottomSheetDialog extends BottomSheetDialog {
    public TransStatusBottomSheetDialog(@NonNull Context context) {
        super(context);
    }

    public TransStatusBottomSheetDialog(@NonNull Context context, int theme) {
        super(context, theme);
    }

    protected TransStatusBottomSheetDialog(@NonNull Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Window window = getWindow();
        if (window == null) {
            return;
        }

        // 拿到屏幕的高度
        int screenHeight = QMUIDisplayHelper.getScreenHeight(getContext());
        int statusHeight = QMUIStatusBarHelper.getStatusbarHeight(getContext());

        int dialogHeight = screenHeight - statusHeight;
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                dialogHeight <= 0 ? ViewGroup.LayoutParams.MATCH_PARENT : dialogHeight);
    }


}
