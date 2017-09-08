package com.raokui.ijk.weiget;

import android.graphics.SurfaceTexture;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by 饶魁 on 2017/9/6.
 */

public interface IRenderView {
    int AR_ASPECT_FIT_PARENT = 0;
    int AR_ASPECT_FILL_PARENT = 1; // may clip
    int AR_ASPECT_WRAP_CONTENT = 2;
    int AR_MATCH_PARENT = 3;
    int AR_16_9_FIT_PARENT = 4;
    int AR_4_3_FIT_PARENT = 5;

    /**
     * 获取外层的界面
     *
     * @return
     */
    View getView();

    /**
     * 是否需要等待重置大小
     *
     * @return
     */
    boolean shouldWaitForResize();

    /**
     * 设置视频界面大小
     *
     * @param video_width
     * @param video_height
     */
    void setVideoSize(int video_width, int video_height);

    /**
     * 设置视频裁剪方式
     *
     * @param video_sar_num
     * @param video_sar_den
     */
    void setVideoSampleAspectRatio(int video_sar_num, int video_sar_den);

    /**
     * 设置视频旋转角度
     *
     * @param degree
     */
    void setVideoRotation(int degree);

    /**
     * 设置裁剪方式
     *
     * @param aspect_ratio
     */
    void setAspectRatio(int aspect_ratio);

    /**
     * 添加视频渲染回调
     */
    void addRenderCallback(IRenderCallback callback);

    /**
     * 移除视频渲染回调
     */
    void removeRenderCallback(IRenderCallback callback);

    interface ISurfaceHolder {
        /**
         * surface界面绑定到mediaplay上
         *
         * @param mp
         */
        void bindToMediaPlayer(IMediaPlayer mp);

        /**
         * 获取渲染的View
         *
         * @return
         */
        IRenderView getRenderView();

        /**
         * 获取渲染使用的具体View surface
         *
         * @return
         */
        SurfaceHolder getSurfaceHolder();

        /**
         * 打开surface界面
         *
         * @return
         */
        Surface openSurface();

        /**
         * 获取渲染使用的具体View texture
         *
         * @return
         */
        SurfaceTexture getSurfaceTexture();
    }

    interface IRenderCallback {
        /**
         * 创建surface界面大小
         *
         * @param holder
         * @param width  could be 0
         * @param height could be 0
         */
        void onSurfaceCreated(@NonNull ISurfaceHolder holder, int width, int height);

        /**
         * surface界面大小改变监听
         *
         * @param holder
         * @param format could be 0
         * @param width
         * @param height
         */
        void onSurfaceChanged(@NonNull ISurfaceHolder holder, int format, int width, int height);

        /**
         * 界面回收
         */
        void onSurfaceDestroyed(@NonNull ISurfaceHolder holder);
    }
}