package com.raokui.ijk.weiget;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.MediaController;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.AndroidMediaPlayer;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;
import tv.danmaku.ijk.media.player.TextureMediaPlayer;
import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * Created by raokui on 9/5/17.
 */

public class IjkVideoView extends FrameLayout implements MediaController.MediaPlayerControl {

    private String TAG = this.getClass().getSimpleName();
    /**
     * 播放地址
     */
    private Uri mUri;
    /**
     * 播放器基本配置
     */
    private Map<String, String> mHeaders;

    private int current_state = PlayStateParams.STATE_IDLE;
    private int target_state = PlayStateParams.STATE_IDLE;

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

    private static final int[] s_all_aspect_ratio = {
            IRenderView.AR_ASPECT_FIT_PARENT,
            IRenderView.AR_ASPECT_FILL_PARENT,
            IRenderView.AR_ASPECT_WRAP_CONTENT,
            IRenderView.AR_MATCH_PARENT,
            IRenderView.AR_16_9_FIT_PARENT,
            IRenderView.AR_4_3_FIT_PARENT
    };

    private int current_aspect_ratio_index = 0;

    private int current_aspect_ratio = s_all_aspect_ratio[current_aspect_ratio_index];

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

        video_height = 0;
        video_width = 0;

        // 设置可触摸并且获取焦点
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();

        current_state = PlayStateParams.STATE_IDLE;
        target_state = PlayStateParams.STATE_IDLE;
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
     * 设置渲染器
     *
     * @param renderView
     */
    public void setRenderView(IRenderView renderView) {
        if (mRenderView != null) {
            if (mediaPlayer != null) {
                mediaPlayer.setDisplay(null);
            }

            View renderUIView = mRenderView.getView();
            mRenderView.removeRenderCallback(mSHCallback);
            mRenderView = null;
            removeView(renderUIView);
        }

        if (renderView == null) {
            return;
        }

        mRenderView = renderView;
        renderView.setAspectRatio(current_aspect_ratio);
        if (video_width > 0 && video_height > 0) {
            renderView.setVideoSize(video_width, video_height);
        }
        if (video_sar_num > 0 && video_sar_den > 0) {
            renderView.setVideoSampleAspectRatio(video_sar_num, video_sar_den);
        }

        View renderUIView = mRenderView.getView();
        // 解决宽度不满问题
        ViewGroup.LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        renderUIView.setLayoutParams(lp);
        addView(renderUIView);

        mRenderView.addRenderCallback(mSHCallback);
        mRenderView.setVideoRotation(video_rotation_degree);
    }

    /**
     * 设置旋转角度
     *
     * @param rotation
     */
    public void setPlayerRotation(int rotation) {
        video_rotation_degree = rotation;
        if (mRenderView != null) {
            mRenderView.setVideoRotation(video_rotation_degree);
        }
    }


    /**
     * 旋转渲染器
     *
     * @param current_render
     */
    public void setRender(int current_render) {
        switch (current_render) {
            case RENDER_TEXTURE_VIEW: {
                TextureRenderView renderView = new TextureRenderView(getContext());
                if (mediaPlayer != null) {
                    renderView.getSurfaceHolder().bindToMediaPlayer(mediaPlayer);
                    renderView.setVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
                    renderView.setVideoSampleAspectRatio(mediaPlayer.getVideoSarNum(), mediaPlayer.getVideoSarDen());
                    renderView.setAspectRatio(current_aspect_ratio);
                }
                setRenderView(renderView);
                break;
            }
            case RENDER_SURFACE_VIEW: {
                SurfaceRenderView renderView = new SurfaceRenderView(getContext());
                setRenderView(renderView);
                break;
            }
            default:
                Log.e(TAG, "setRender:");
                break;
        }
    }

    public void setVideoPath(String path) {
        setVideoURI(Uri.parse(path));
    }


    public void setVideoURI(Uri uri) {
        setVideoURI(uri, null);
    }

    private void setVideoURI(Uri uri, Map<String, String> headers) {
        mUri = uri;
        mHeaders = headers;
        seek_when_prepared = 0;
        openVideo();
        requestLayout();
        invalidate();
    }

    /**
     * 打开视屏
     */
    private void openVideo() {
        if (mUri == null || mSurfaceHolder == null) {
            return;
        }

        release(false);

        AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
        am.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        try {
            if (using_android_player) {
                mediaPlayer = new AndroidMediaPlayer();
            } else {
                IjkMediaPlayer ijkMediaPlayer = null;
                if (mUri != null) {
                    ijkMediaPlayer = new IjkMediaPlayer();
                    ijkMediaPlayer.native_setLogLevel(ijkMediaPlayer.IJK_LOG_DEBUG);

                    if (using_media_codec) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 1);
                        if (using_media_codec_auto_rotate) {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 1);
                        } else {
                            ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", 0);
                        }
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", 0);
                    }

                    if (using_open_SLES) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1);
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 0);
                    }

                    if (TextUtils.isEmpty(pixel_format)) {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32);
                    } else {
                        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", pixel_format);
                    }

                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0);

                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "http-detect-range-support", 0);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "timeout", 10000000);
                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "reconnect", 1);

                    ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48);

                }

                mediaPlayer = ijkMediaPlayer;
            }

            if (enable_background_play) {
                mediaPlayer = new TextureMediaPlayer(mediaPlayer);
            }

            final Context context = getContext();
            mediaPlayer.setOnPreparedListener(mPreparedListener);
            mediaPlayer.setOnCompletionListener(mCompletionListener);
            mediaPlayer.setOnErrorListener(mErrorListener);
            mediaPlayer.setOnInfoListener(mInfoListener);
            mediaPlayer.setOnVideoSizeChangedListener(mSizeChangedListener);
            mediaPlayer.setOnBufferingUpdateListener(mBufferingUpdateListener);

            current_buffer_percentage = 0;
            String scheme = mUri.getScheme();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && using_android_player && (TextUtils.isEmpty(scheme) || scheme.equalsIgnoreCase("file"))) {
                IMediaDataSource dataSource = new FileMediaDataSource(new File(mUri.toString()));
                mediaPlayer.setDataSource(dataSource);
            } else if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                mediaPlayer.setDataSource(mAppContext, mUri, mHeaders);
            } else {
                mediaPlayer.setDataSource(mUri.toString());
            }

            bindSurfaceHolder(mediaPlayer, mSurfaceHolder);
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.prepareAsync();

            current_state = PlayStateParams.STATE_PREPARING;
            attachMediaController();

        } catch (IOException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            current_state = PlayStateParams.STATE_ERROR;
            target_state = PlayStateParams.STATE_ERROR;
            mErrorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Unable to open content: " + mUri, ex);
            current_state = PlayStateParams.STATE_ERROR;
            target_state = PlayStateParams.STATE_ERROR;
            mErrorListener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
            return;
        } finally {
            // REMOVED: mPendingSubtitleTracks.clear();
        }
    }

    private void setMediaController(IMediaController controller) {
        if (mMediaController != null) {
            mMediaController.hide();
        }
        mMediaController = controller;
        attachMediaController();
    }

    private void attachMediaController() {
        if (mediaPlayer != null && mMediaController != null) {
            mMediaController.setMediaPlayer(this);
            View anchorView = this.getParent() instanceof View ? (View) this.getParent() : this;
            mMediaController.setAnchorView(anchorView);
            mMediaController.setEnabled(isInPlayBackState());
        }
    }

    public void stopPlayBack() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            current_state = PlayStateParams.STATE_IDLE;
            target_state = PlayStateParams.STATE_IDLE;
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }

    /**
     * 释放media player
     *
     * @param clearTargetState
     */
    private void release(boolean clearTargetState) {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;

            current_state = PlayStateParams.STATE_IDLE;
            if (clearTargetState) {
                target_state = PlayStateParams.STATE_IDLE;
            }
            AudioManager am = (AudioManager) mAppContext.getSystemService(Context.AUDIO_SERVICE);
            am.abandonAudioFocus(null);
        }
    }


    private void bindSurfaceHolder(IMediaPlayer mp, IRenderView.ISurfaceHolder holder) {
        if (mp == null) {
            return;
        }
        if (holder == null) {
            mp.setDisplay(null);
            return;
        }

        holder.bindToMediaPlayer(mp);
    }

    IRenderView.IRenderCallback mSHCallback = new IRenderView.IRenderCallback() {
        @Override
        public void onSurfaceCreated(@NonNull IRenderView.ISurfaceHolder holder, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceCreated: unmatched render callback\n");
                return;
            }

            mSurfaceHolder = holder;
            if (mediaPlayer != null) {
                bindSurfaceHolder(mediaPlayer, holder);
            } else {
                openVideo();
            }
        }

        @Override
        public void onSurfaceChanged(@NonNull IRenderView.ISurfaceHolder holder, int format, int width, int height) {
            if (holder.getRenderView() != mRenderView) {
                return;
            }

            surface_width = width;
            surface_height = height;
            boolean isValidState = (target_state == PlayStateParams.STATE_PLAYING);
            boolean hasValidSize = !mRenderView.shouldWaitForResize() || (video_width == width && video_height == height);

            if (mediaPlayer != null && isValidState && hasValidSize) {
                if (seek_when_prepared != 0) {
                    seekTo((int) seek_when_prepared);
                }

                start();
            }
        }

        @Override
        public void onSurfaceDestroyed(@NonNull IRenderView.ISurfaceHolder holder) {
            if (holder.getRenderView() != mRenderView) {
                Log.e(TAG, "onSurfaceDestroyed: unmatched render callback\n");
                return;
            }

            // after we return from this we can't use the surface any more
            mSurfaceHolder = null;
            // REMOVED: if (mMediaController != null) mMediaController.hide();
            // REMOVED: release(true);
            releaseWithoutStop();
        }
    };

    public void releaseWithoutStop() {
        if (mediaPlayer != null) {
            mediaPlayer.setDisplay(null);
        }
    }

    private IMediaPlayer.OnVideoSizeChangedListener mSizeChangedListener = new IMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(IMediaPlayer mp, int width, int height, int sar_num, int sar_den) {
            video_width = mp.getVideoWidth();
            video_height = mp.getVideoHeight();
            video_sar_num = mp.getVideoSarNum();
            video_sar_den = mp.getVideoSarDen();
            if (video_width != 0 && video_height != 0) {
                if (mRenderView != null) {
                    mRenderView.setVideoSampleAspectRatio(video_width, video_height);
                    mRenderView.setVideoSampleAspectRatio(video_sar_num, video_sar_den);
                }

                requestLayout();
            }
        }
    };

    private IMediaPlayer.OnPreparedListener mPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            current_state = PlayStateParams.STATE_PREPARED;

            if (mOnCompletionListener != null) {
                mOnPreparedListener.onPrepared(mediaPlayer);
            }

            if (mMediaController != null) {
                mMediaController.setEnabled(true);
            }

            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mediaPlayer, current_state, 0);
            }

            video_width = mp.getVideoWidth();
            video_height = mp.getVideoHeight();

            long seek_to_position = seek_when_prepared;
            if (seek_to_position != 0) {
                seekTo((int) seek_to_position);
            }
            if (video_width != 0 && video_height != 0) {
                if (mRenderView != null) {
                    mRenderView.setVideoSize(video_width, video_height);
                    mRenderView.setVideoSampleAspectRatio(video_sar_num, video_sar_den);
                    if (!mRenderView.shouldWaitForResize() || surface_width == video_width && surface_height == video_height) {
                        if (target_state == PlayStateParams.STATE_PLAYING) {
                            start();
                            if (mMediaController != null) {
                                mMediaController.show();
                            }
                        } else if (!isPlaying() && (seek_to_position != 0 || getCurrentPosition() > 0)) {
                            if (mMediaController != null) {
                                mMediaController.show(0);
                            }
                        }
                    }
                }
            } else {
                if (target_state == PlayStateParams.STATE_PLAYING) {
                    start();
                }
            }
        }
    };

    private IMediaPlayer.OnErrorListener mErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer mp, int framework_err, int impl_err) {
            current_state = PlayStateParams.STATE_ERROR;
            target_state = PlayStateParams.STATE_ERROR;

            if (mMediaController != null) {
                mMediaController.hide();
            }

            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mediaPlayer, framework_err, impl_err);
            }

            if (mOnInfoListener != null) {
                if (mOnInfoListener.onInfo(mediaPlayer, framework_err, 0)) {
                    return true;
                }
            }

            if (getWindowToken() != null) {
                Resources r = mAppContext.getResources();
                String message = "Unknown error";

                if (framework_err == mediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK) {
                    message = "Invalid progressive playback";
                }

                new AlertDialog.Builder(getContext())
                        .setMessage(message)
                        .setPositiveButton("error"
                                , new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (mOnCompletionListener != null) {
                                            mOnCompletionListener.onCompletion(mediaPlayer);
                                        }
                                    }
                                })
                        .setCancelable(false)
                        .show();
            }

            return true;
        }
    };

    private IMediaPlayer.OnInfoListener mInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer mp, int what, int extra) {
            if (mOnCompletionListener != null) {
                mOnInfoListener.onInfo(mp, what, extra);
            }

            switch (what) {
                case IMediaPlayer.MEDIA_INFO_VIDEO_TRACK_LAGGING:
                    Log.d(TAG, "MEDIA_INFO_VIDEO_TRACK_LAGGING:");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    Log.d(TAG, "MEDIA_INFO_VIDEO_RENDERING_START:");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                    Log.d(TAG, "MEDIA_INFO_BUFFERING_START:");
                    break;
                case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                    Log.d(TAG, "MEDIA_INFO_BUFFERING_END:");
                    break;
                case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                    Log.d(TAG, "MEDIA_INFO_NETWORK_BANDWIDTH: " + extra);
                    break;
                case IMediaPlayer.MEDIA_INFO_BAD_INTERLEAVING:
                    Log.d(TAG, "MEDIA_INFO_BAD_INTERLEAVING:");
                    break;
                case IMediaPlayer.MEDIA_INFO_NOT_SEEKABLE:
                    Log.d(TAG, "MEDIA_INFO_NOT_SEEKABLE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_METADATA_UPDATE:
                    Log.d(TAG, "MEDIA_INFO_METADATA_UPDATE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_UNSUPPORTED_SUBTITLE:
                    Log.d(TAG, "MEDIA_INFO_UNSUPPORTED_SUBTITLE:");
                    break;
                case IMediaPlayer.MEDIA_INFO_SUBTITLE_TIMED_OUT:
                    Log.d(TAG, "MEDIA_INFO_SUBTITLE_TIMED_OUT:");
                    break;
                case IMediaPlayer.MEDIA_INFO_VIDEO_ROTATION_CHANGED:
                    video_rotation_degree = extra;
                    if (mRenderView != null) {
                        mRenderView.setVideoRotation(extra);
                    }
                    break;

                case IMediaPlayer.MEDIA_INFO_AUDIO_RENDERING_START:
                    Log.d(TAG, "MEDIA_INFO_AUDIO_RENDERING_START:");
                    break;
            }

            return true;
        }
    };

    private IMediaPlayer.OnBufferingUpdateListener mBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer mp, int percent) {
            current_buffer_percentage = percent;
        }
    };

    private IMediaPlayer.OnCompletionListener mCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            current_state = PlayStateParams.STATE_COMPLETED;
            target_state = PlayStateParams.STATE_COMPLETED;
            if (mMediaController != null) {
                mMediaController.hide();
            }

            if (mOnCompletionListener != null) {
                mOnCompletionListener.onCompletion(mediaPlayer);
            }

            if (mOnInfoListener != null) {
                mOnInfoListener.onInfo(mediaPlayer, PlayStateParams.STATE_COMPLETED, 0);
            }
        }
    };

    @Override
    public int getCurrentPosition() {
        if (isInPlayBackState()) {
            return (int) mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return isInPlayBackState() && mediaPlayer.isPlaying();
    }

    @Override
    public void start() {
        if (isInPlayBackState()) {
            mediaPlayer.start();
            current_state = PlayStateParams.STATE_PLAYING;
        }

        target_state = PlayStateParams.STATE_PLAYING;
    }

    @Override
    public void pause() {
        if (isInPlayBackState()) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                current_state = PlayStateParams.STATE_PAUSED;
            }
        }
        target_state = PlayStateParams.STATE_PAUSED;
    }

    @Override
    public int getDuration() {
        if (isInPlayBackState()) {
            return (int) mediaPlayer.getDuration();
        }
        return -1;
    }

    @Override
    public int getBufferPercentage() {
        if (mediaPlayer != null) {
            return current_buffer_percentage;
        }
        return 0;
    }

    @Override
    public boolean canPause() {
        return can_pause;
    }

    @Override
    public boolean canSeekBackward() {
        return false;
    }

    @Override
    public boolean canSeekForward() {
        return false;
    }

    @Override
    public int getAudioSessionId() {
        return 0;
    }

    @Override
    public void seekTo(int msec) {
        if (isInPlayBackState()) {
            mediaPlayer.seekTo(msec);
            seek_when_prepared = 0;
        } else {
            seek_when_prepared = msec;
        }
    }

    public void setOnPreparedListener(IMediaPlayer.OnPreparedListener l) {
        mOnPreparedListener = l;
    }

    public void setOnCompletionListener(IMediaPlayer.OnCompletionListener l) {
        mOnCompletionListener = l;
    }

    public void setOnErrorListener(IMediaPlayer.OnErrorListener l) {
        mOnErrorListener = l;
    }

    public void setOnInfoListener(IMediaPlayer.OnInfoListener l) {
        mOnInfoListener = l;
    }


    private boolean isInPlayBackState() {
        return (mediaPlayer != null
                && current_state != PlayStateParams.STATE_ERROR
                && current_state != PlayStateParams.STATE_IDLE
                && current_state != PlayStateParams.STATE_PREPARING);
    }

    private void toggleMediaControlsVisibility() {
        if (mMediaController.isShowing()) {
            mMediaController.hide();
        } else {
            mMediaController.show();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isInPlayBackState() && mMediaController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }

    public void onPause() {
        release(false);
    }

    public void onResume() {
        release(false);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean isKeyCodeSupported = keyCode != KeyEvent.KEYCODE_BACK &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_MENU &&
                keyCode != KeyEvent.KEYCODE_CALL &&
                keyCode != KeyEvent.KEYCODE_ENDCALL;
        if (isKeyCodeSupported && isKeyCodeSupported && mMediaController != null) {
            if (keyCode == KeyEvent.KEYCODE_HEADSETHOOK || keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                if (mediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                } else {
                    start();
                    mMediaController.hide();
                }

                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY) {
                if (!mediaPlayer.isPlaying()) {
                    start();
                    mMediaController.hide();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP
                    || keyCode == KeyEvent.KEYCODE_MEDIA_PAUSE) {
                if (mediaPlayer.isPlaying()) {
                    pause();
                    mMediaController.show();
                }
                return true;
            } else {
                toggleMediaControlsVisibility();
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setAspectRatio(int aspect_ratio) {
        for (int i = 0; i < s_all_aspect_ratio.length; i++) {
            if (s_all_aspect_ratio[i] == aspect_ratio) {
                current_aspect_ratio_index = i;
                if (mRenderView != null) {
                    mRenderView.setAspectRatio(current_aspect_ratio);
                }
                break;
            }
        }
    }

    @Override
    public boolean onTrackballEvent(MotionEvent event) {
        if (isInPlayBackState() && mMediaController != null) {
            toggleMediaControlsVisibility();
        }
        return false;
    }
}
