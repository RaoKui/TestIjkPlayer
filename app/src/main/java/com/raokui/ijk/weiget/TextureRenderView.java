package com.raokui.ijk.weiget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

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

    private MeasureHelper mMeasureHelper;

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
        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallBack(this);
        setSurfaceTextureListener(mSurfaceCallback);


    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean shouldWaitForResize() {
        return false;
    }

    @Override
    public void setVideoSize(int video_width, int video_height) {
        if (video_width > 0 && video_height > 0) {
            mMeasureHelper.setVideoSize(video_width, video_height);
            requestLayout();
        }
    }

    @Override
    public void setVideoSampleAspectRatio(int video_sar_num, int video_sar_den) {
        if (video_sar_num > 0 && video_sar_den > 0) {
            mMeasureHelper.setVideoSampleAspectRatio(video_sar_num, video_sar_den);
            requestLayout();
        }
    }

    @Override
    public void setVideoRotation(int degree) {
        mMeasureHelper.setVideoRotation(degree);
        setRotation(degree);
    }

    @Override
    public void setAspectRatio(int aspect_ratio) {
        mMeasureHelper.setAspectRatio(aspect_ratio);
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mMeasureHelper.doMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(mMeasureHelper.getMeasuredWidth(), mMeasureHelper.getMeasuredHeight());
    }

    @Override
    public void addRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.addRenderCallback(callback);
    }

    @Override
    public void removeRenderCallback(IRenderCallback callback) {
        mSurfaceCallback.removeRenderCallback(callback);
    }


    private static final class SurfaceCallBack implements SurfaceTextureListener {

        private SurfaceTexture mSurfaceTexture;

        private boolean isFormatChanged;

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
                callback.onSurfaceCreated(surfaceHolder, width, height);
            }

            if (isFormatChanged) {
                if (surfaceHolder == null) {
                    surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), mSurfaceTexture);
                }
                callback.onSurfaceChanged(surfaceHolder, 0, width, height);
            }

        }

        public void removeRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            isFormatChanged = false;
            width = 0;
            height = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), surface);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            mSurfaceTexture = surface;
            isFormatChanged = true;
            width = 0;
            height = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), surface);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            mSurfaceTexture = surface;
            isFormatChanged = false;
            width = 0;
            height = 0;
            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakRenderView.get(), surface);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(surfaceHolder);
            }
            return own_surface_texture;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        event.setClassName(TextureRenderView.class.getName());

    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        info.setClassName(TextureRenderView.class.getName());
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
