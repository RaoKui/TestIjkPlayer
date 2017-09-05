package com.raokui.ijk.weiget;

import android.view.View;
import android.widget.MediaController;

/**
 * Created by raokui on 9/5/17.
 */

public interface IMediaController {
    /**
     *隐藏标题栏
     */
    void hide();

    /**
     * 判断是否显示
     */
    void isShowing();

    /**
     *设置主播界面
     * @param view
     */
    void setAnchorView(View view);

    /**
     * 设置是否可用
     * @param enabled
     */
    void setEnabled(boolean enabled);

    /**
     * 设置媒体播放器
     * @param player
     */
    void setMediaPlayer(MediaController.MediaPlayerControl player);

    /**
     * 显示标题栏带超时时间
     * @param timeout
     */
    void show(int timeout);

    /**
     * 显示标题栏
     */
    void show();

    /**
     *
     * @param view
     */
    void showOnce(View view);
}
