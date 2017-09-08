package com.raokui.ijk.weiget;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.MultiAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;

/**
 * Created by raokui on 9/5/17.
 */

public class IjkVideoView extends FrameLayout {

    private String TAG = this.getClass().getSimpleName();
    /**
     * 播放地址
     */
    private Uri mUri;
    /**
     * 播放器基本配置
     */
    private Map<String, String> mHeaders;

    private int mCurrentState = PlayStateParams.STATE_IDLE;
    private int mTargetState = PlayStateParams.STATE_IDLE;

    private IRenderView.ISurfaceHolder mSurfaceHolder = null;
    /**
     * 视频宽度
     */
    private int video_width;
    /**
     * 视频高度
     */
    private int video_height;
    /**
     * 窗口宽度
     */
    private int surface_width;
    /**
     * 窗口高度
     */
    private int surface_height;
    /**
     * 视频旋转角度
     */
    private int video_rotation_degree;

    /**
     * 媒体播放器
     */
    private IMediaPlayer mediaPlayer;
    /**
     * 媒体控制器
     */
    private IMediaController mMediaController;

    /**
     * 播放完成监听
     */
    private IMediaPlayer.OnCompletionListener mOnCompletionListener;

    /**
     * 播放准备监听
     */
    private IMediaPlayer.OnPreparedListener mOnPreparedListener;

    /**
     * 当前播放缓冲进度
     */
    private int current_buffer_percentage;

    /**
     * 播放错误监听
     */
    private IMediaPlayer.OnErrorListener mOnErrorListener;

    /**
     * 其他信息监听
     */
    private IMediaPlayer.OnInfoListener mOnInfoListener;
    /**
     * 在视频准备阶段记录跳转的位置
     */
    private long seek_when_prepared;

    /**
     * 是否可以暂停
     */
    private boolean can_pause = true;

    private Context mAppContext;

    private IRenderView mRenderView;

    private int video_sar_num;

    private int video_sar_den;

    /**
     * 使用Android播放器
     */
    private boolean using_android_player = false;

    /**
     * 使用编解码器:硬编码还是软编码，true 硬编码 false 为软编码
     */
    private boolean using_media_codec = false;

    /**
     * 使用编解码是否自转
     */
    private boolean using_media_codec_auto_rotate = false;

    private boolean using_open_SLES = false;
    /**
     * Auto Select=,RGB 565=fcc-rv16,RGB 888X=fcc-rv32,YV12=fcc-yv12,默认为RGB 888X
     */
    private String pixel_format = "";

    private boolean enable_background_play;

    private List<Integer> mAllRenders = new ArrayList<>();

    private int current_render_index = 0;

    public static final int RENDER_SURFACE_VIEW = 1;

    public static final int RENDER_TEXTURE_VIEW = 2;

    private int current_render = RENDER_SURFACE_VIEW;

    public IjkVideoView(@NonNull Context context) {
        super(context);
        initVideoView(context);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initVideoView(context);
    }

    public IjkVideoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoView(context);
    }

    /**
     * 初始化视频
     *
     * @param context
     */
    private void initVideoView(Context context) {
        mAppContext = context.getApplicationContext();

        initBackground();

        initRenders();


    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        if (enable_background_play) {
//            MediaPlayerService.intentToStart(getContext());
//            mMediaPlayer = MediaPlayerService.getMediaPlayer();
        }
    }

    /**
     * 初始化渲染器
     */
    private void initRenders() {
        mAllRenders.clear();
        // 添加surface渲染
        mAllRenders.add(RENDER_SURFACE_VIEW);
        // android 版本大于等与android 4.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // 添加texture渲染
            mAllRenders.add(RENDER_TEXTURE_VIEW);
            current_render_index = 1;
        } else {
            current_render_index = 0;
        }

        current_render = mAllRenders.get(current_render_index);
        setRender(current_render);
    }

    /**
     * 旋转渲染器
     *
     * @param current_render
     */
    private void setRender(int current_render) {
        switch (current_render) {
            case RENDER_TEXTURE_VIEW:

                break;
            case RENDER_SURFACE_VIEW:
                break;
            default:
                Log.e(TAG, "setRender:");
                break;
        }
    }


}
