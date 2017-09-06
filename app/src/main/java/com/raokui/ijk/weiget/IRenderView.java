package com.raokui.ijk.weiget;

import android.view.View;

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
}
