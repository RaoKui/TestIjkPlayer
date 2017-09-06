package com.raokui.ijk.weiget;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import java.util.Map;

/**
 * Created by raokui on 9/5/17.
 */

public class IjkVIdeoView extends FrameLayout {

    private String TAG = this.getClass().getSimpleName();
    /**
     * 播放地址
     */
    private Uri mUri;
    /**
     * 播放器基本配置
     */
    private Map<String,String> mHeaders;

    private int mCurrentState = PlayStateParams.STATE_IDLE;
    private int mTargetState = PlayStateParams.STATE_IDLE;

//    private IRenderView


    public IjkVIdeoView(@NonNull Context context) {
        super(context);
    }

    public IjkVIdeoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public IjkVIdeoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


}
