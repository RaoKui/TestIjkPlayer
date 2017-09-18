package com.raokui.ijk.weiget;

import android.icu.util.Calendar;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Created by 饶魁 on 2017/9/18.
 */

public final class MeasureHelper {
    private WeakReference<View> mWeakView;

    private int video_width;

    private int video_height;

    private int video_sar_num;

    private int video_sar_den;

    private int video_rotation_degree;

//    private int video_rotation_type;

    private int measured_width;

    private int measured_height;
    /**
     * 屏幕宽高比
     */
    private int current_aspect_ratio = IRenderView.AR_ASPECT_FIT_PARENT;

    public MeasureHelper(View view) {
        mWeakView = new WeakReference<View>(view);
    }

    public View getView() {
        if (mWeakView == null) {
            return null;
        } else {
            return mWeakView.get();
        }
    }

    public void setVideoSize(int video_width, int video_height) {
        this.video_height = video_height;
        this.video_width = video_width;
    }

    public void setVideoSampleAspectRatio(int video_sar_num, int video_sar_den) {
        this.video_sar_den = video_sar_den;
        this.video_sar_num = video_sar_num;
    }

    public void setVideoRotation(int video_rotation_degree) {
        this.video_rotation_degree = video_rotation_degree;
    }

    public void doMeasure(int width_measure_spec, int height_measure_spec) {

        if (video_rotation_degree == 90 || video_rotation_degree == 270) {
            int temp_spec = width_measure_spec;
            width_measure_spec = height_measure_spec;
            height_measure_spec = temp_spec;
        }

        int width = View.getDefaultSize(video_width, video_height);
        int height = View.getDefaultSize(video_height, height_measure_spec);
        if (current_aspect_ratio == IRenderView.AR_MATCH_PARENT) {
            width = width_measure_spec;
            height = height_measure_spec;
        } else if (video_width > 0 && video_height > 0) {
            // mode共有三种情况，取值分别为MeasureSpec.UNSPECIFIED, MeasureSpec.EXACTLY, MeasureSpec.AT_MOST
            //
            int width_spec_mode = View.MeasureSpec.getMode(width_measure_spec);
            int width_spec_size = View.MeasureSpec.getSize(width_measure_spec);
            int height_spec_mode = View.MeasureSpec.getMode(height_measure_spec);
            int height_spec_size = View.MeasureSpec.getSize(height_measure_spec);

            if (width_spec_mode == View.MeasureSpec.AT_MOST && height_spec_mode == View.MeasureSpec.AT_MOST){
                float spec_aspect_ratio = (float)width_spec_size/(float)height_spec_size;
            }
        }

    }

}
