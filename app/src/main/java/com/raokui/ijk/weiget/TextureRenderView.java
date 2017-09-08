package com.raokui.ijk.weiget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.os.Build;
import android.util.AttributeSet;
import android.view.TextureView;
import android.view.View;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * android 4.0 以上的播放器显示View
 * Created by 饶魁 on 2017/9/8.
 */

public class TextureRenderView extends TextureView implements IRenderView {

    // TODO: 2017/9/8  MeasureHelper


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

        public SurfaceCallBack(TextureRenderView renderView){
            mWeakRenderView = new WeakReference<TextureRenderView>(renderView);
        }

        public void setOwnSurfaceTexture(boolean own_surface_texture) {
            this.own_surface_texture = own_surface_texture;
        }

        public void addRenderCallback(IRenderCallback callback){
            mRenderCallbackMap.put(callback,callback);

            ISurfaceHolder surfaceHolder = null;
            if (mSurfaceTexture!=null){
//             
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

}
