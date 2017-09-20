package com.raokui.ijk.weiget;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.ISurfaceTextureHolder;

/**
 * Created by 饶魁 on 2017/9/20.
 */

public class SurfaceRenderView extends SurfaceView implements IRenderView {

    private MeasureHelper mMeasureHelper;

    private SurfaceCallback mSurfaceCallback;

    public SurfaceRenderView(Context context) {
        super(context);
        initView(context);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SurfaceRenderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mMeasureHelper = new MeasureHelper(this);
        mSurfaceCallback = new SurfaceCallback(this);
        getHolder().addCallback(mSurfaceCallback);
        // noinspection deprecation
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_NORMAL);

    }

    @Override
    public View getView() {
        return this;
    }

    @Override
    public boolean shouldWaitForResize() {
        return true;
    }

    @Override
    public void setVideoSize(int video_width, int video_height) {
        if (video_width > 0 && video_height > 0) {
            mMeasureHelper.setVideoSize(video_width, video_height);
            getHolder().setFixedSize(video_width, video_height);
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
        Log.e("", "SurfaceView doesn't support rotation (" + degree + ")!\n");
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

    private static final class SurfaceCallback implements SurfaceHolder.Callback {

        private SurfaceHolder mSurfaceHolder;

        private boolean is_format_changed;

        private int format;

        private int width;

        private int height;

        private WeakReference<SurfaceRenderView> mWeakReferenceView;

        private Map<IRenderCallback, Object> mRenderCallbackMap = new ConcurrentHashMap<IRenderCallback, Object>();

        public SurfaceCallback(SurfaceRenderView surfaceRenderView) {
            mWeakReferenceView = new WeakReference<SurfaceRenderView>(surfaceRenderView);
        }

        public void addRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.put(callback, callback);

            ISurfaceHolder surfaceHolder = null;
            if (mSurfaceHolder != null) {
                if (surfaceHolder == null) {
                    surfaceHolder = new InternalSurfaceHolder(mWeakReferenceView.get(), mSurfaceHolder);
                }
                callback.onSurfaceCreated(surfaceHolder, width, height);
            }
        }

        public void removeRenderCallback(@NonNull IRenderCallback callback) {
            mRenderCallbackMap.remove(callback);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            is_format_changed = false;
            format = 0;
            width = 0;
            height = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakReferenceView.get(), mSurfaceHolder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceCreated(surfaceHolder, 0, 0);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mSurfaceHolder = holder;
            is_format_changed = true;
            this.format = format;
            this.width = width;
            this.height = height;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakReferenceView.get(), mSurfaceHolder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceChanged(surfaceHolder, format, width, height);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mSurfaceHolder = holder;
            is_format_changed = false;
            format = 0;
            width = 0;
            height = 0;

            ISurfaceHolder surfaceHolder = new InternalSurfaceHolder(mWeakReferenceView.get(), mSurfaceHolder);
            for (IRenderCallback renderCallback : mRenderCallbackMap.keySet()) {
                renderCallback.onSurfaceDestroyed(surfaceHolder);
            }
        }
    }

    private static final class InternalSurfaceHolder implements ISurfaceHolder {

        private SurfaceRenderView mSurfaceView;

        private SurfaceHolder mSurfaceHolder;

        public InternalSurfaceHolder(@NonNull SurfaceRenderView surfaceView, @Nullable SurfaceHolder surfaceHolder) {
            mSurfaceView = surfaceView;
            mSurfaceHolder = surfaceHolder;
        }

        @Override
        public void bindToMediaPlayer(IMediaPlayer mp) {
            if (mp != null) {
                if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) && (mp instanceof ISurfaceTextureHolder)) {
                    ISurfaceTextureHolder textureHolder = (ISurfaceTextureHolder) mp;
                    textureHolder.setSurfaceTexture(null);
                }
                mp.setDisplay(mSurfaceHolder);
            }
        }

        @Override
        public IRenderView getRenderView() {
            return mSurfaceView;
        }

        @Override
        public SurfaceHolder getSurfaceHolder() {
            return mSurfaceHolder;
        }

        @Override
        public Surface openSurface() {
            if (mSurfaceHolder == null) {
                return null;
            }
            return mSurfaceHolder.getSurface();
        }

        @Override
        public Surface getSurfaceTexture() {
            return null;
        }
    }

}
