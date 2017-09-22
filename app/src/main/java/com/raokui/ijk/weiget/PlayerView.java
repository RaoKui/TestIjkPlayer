package com.raokui.ijk.weiget;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by 饶魁 on 2017/9/22.
 */

public class PlayerView {

    private static final String TAG = PlayerView.class.getSimpleName();


    private final Context mContext;

    private final Activity mActivity;

    /**
     * Activity界面中布局的查询器
     */
    private final LayoutQuery query;

    /**
     * 原生播放器
     */
    private final IjkVideoView videoView;

    /**
     * 播放器整个界面
     */
    private final View rlBox;


    /**
     * 播放器顶部控制bar
     */
    private final View llTopBar;

    /**
     * 播放器底部控制bar
     */
    private final View llBottomBar;

    /**
     * 播放器封面
     */
    private final ImageView ivTrumb;



}
