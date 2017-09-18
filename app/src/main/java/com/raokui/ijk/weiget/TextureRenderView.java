package com.raokui.ijk.weiget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.ISurfaceTextureHolder;

/**
 * android 4.0 以上的播放器显示View
 * Created by 饶魁 on 2017/9/8.
 */

public class TextureRenderView extends TextureView implements IRenderView {

    // TODO: 2017/9/8  MeasureHelper

    private SurfaceCallBack mSurfaceCallback;


    public TextureRenderView(Context context) {
        super(context);
        initView(context);
    }

    public TextureRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    /**
     * anddroid 5.0
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     * @param defStyleRes
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TextureRenderView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context);
    }

    private void initView(Context context) {
        mSurfaceCallback = new SurfaceCallBack(this);

    }

    @Override
    public View getView() {
        return null;
    }

    @Override
    public boolean shouldWaitForResize() {
        return false;
    }

    @Override
    public void setVideoSize(int video_width, int video_height) {

    }

    @Override
    public void setVideoSampleAspectRatio(int video_sar_num, int video_sar_den) {

    }

    @Override
    public void setVideoRotation(int degree) {

    }

    @Override
    public void setAspectRatio(int aspect_ratio) {

    }

    @Override
    public void addRenderCallback(IRenderCallback callback) {

    }

    @Override
    public void removeRenderCallback(IRenderCallback callback) {

    }


    private static final class SurfaceCallBack implements SurfaceTextureListener {

        private SurfaceTexture mSurfaceTexture;

        private boolean is_format_changed;

        private int width;

        private int height;

        private boolean own_surface_texture = true;

        private WeakReference<TextureRenderView> mWeakRenderView;


        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap<>();

        public SurfaceCallBack(TextureRenderView renderView) {
            mWeakRenderView = new WeakReference<TextureRenderView>(renderView);
        }

        public void setOwnSurfaceTexture(boolean own_surface_texture) {
            this.own_surface_texture = own_surface_texture;
        }


        public void addRenderCallback(IRenderCallback callback) {
            mRenderCallbackMap.put(callback, callback);

            ISurfaceHolder surfaceHolder = null;
            if (mSurfaceTexture != null) {
                if (surfaceHolder == null) {
                    surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), mSurfaceTexture);
                }
                // TODO: 2017/9/8

            }

        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    public ISurfaceHolder getSurfaceHolder() {
        return new InternalSurfaceHolder(this, mSurfaceCallback.mSurfaceTexture);
    }

    private static final class InternalSurfaceHolder implements ISurfaceHolder {

        private TextureRenderView mTextureView;

        private SurfaceTexture mSurfaceTexture;

        public InternalSurfaceHolder(TextureRenderView textureRenderView, SurfaceTexture mSurfaceTexture) {
            this.mTextureView = textureRenderView;
            this.mSurfaceTexture = mSurfaceTexture;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void bindToMediaPlayer(IMediaPlayer mp) {
            if (mp == null) {
                return;
            }
            // android 4.1 及以上版本
            if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && (mp instanceof ISurfaceHolder)) {
                ISurfaceTextureHolder textureHolder = (ISurfaceTextureHolder) mp;
                mTextureView.mSurfaceCallback.setOwnSurfaceTexture(false);

                SurfaceTexture surfaceTexture = textureHolder.getSurfaceTexture();
                if (surfaceTexture != null) {
                    mTextureView.setSurfaceTexture(surfaceTexture);
                } else {
                    textureHolder.setSurfaceTexture(mSurfaceTexture);
                }
            } else {
                mp.setSurface(openSurface());
            }
        }

        @Override
        public IRenderView getRenderView() {
            return mTextureView;
        }

        @Override
        public SurfaceHolder getSurfaceHolder() {
            return null;
        }

        @Override
        public Surface openSurface() {
            return null;
        }

        @Override
        public Surface getSurfaceTexture() {
            if (mSurfaceTexture == null) {
                return null;
            } else {
                return new Surface(mSurfaceTexture);
            }
        }
    }

}
