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

            if (width_spec_mode == View.MeasureSpec.AT_MOST && height_spec_mode == View.MeasureSpec.AT_MOST) {
                float spec_aspect_ratio = (float) width_spec_size / (float) height_spec_size;
                float display_aspect_radio;
                switch (current_aspect_ratio) {
                    case IRenderView.AR_16_9_FIT_PARENT:
                        display_aspect_radio = 16.0f / 9.0f;
                        if (video_rotation_degree == 90 || video_rotation_degree == 270) {
                            display_aspect_radio = 1.0f / display_aspect_radio;
                        }
                        break;
                    case IRenderView.AR_4_3_FIT_PARENT:
                        display_aspect_radio = 4.0f / 3.0f;
                        if (video_rotation_degree == 90 || video_rotation_degree == 270) {
                            display_aspect_radio = 1.0f / display_aspect_radio;
                        }
                        break;
                    case IRenderView.AR_ASPECT_FIT_PARENT:
                    case IRenderView.AR_ASPECT_WRAP_CONTENT:
                    case IRenderView.AR_ASPECT_FILL_PARENT:
                    default:
                        display_aspect_radio = (float) video_width / (float) video_height;
                        if (video_sar_den > 0 && video_sar_num > 0) {
                            display_aspect_radio = display_aspect_radio * video_sar_num / video_sar_den;
                        }
                        break;
                }
                boolean shouldBeWider = display_aspect_radio > spec_aspect_ratio;

                switch (current_aspect_ratio) {
                    case IRenderView.AR_ASPECT_FIT_PARENT:
                    case IRenderView.AR_16_9_FIT_PARENT:
                    case IRenderView.AR_4_3_FIT_PARENT:
                        if (shouldBeWider) {
                            // 太宽需要适配宽度
                            width = width_spec_size;
                            height = (int) (width / display_aspect_radio);
                        } else {
                            // 太高需要适配高度
                            height = height_spec_size;
                            width = (int) (height * display_aspect_radio);
                        }
                        break;
                    case IRenderView.AR_ASPECT_FILL_PARENT:
                        if (shouldBeWider) {
                            height = height_spec_size;
                            width = (int) (height * display_aspect_radio);
                        } else {
                            width = width_spec_size;
                            height = (int) (width / display_aspect_radio);
                        }
                        break;
                    case IRenderView.AR_ASPECT_WRAP_CONTENT:
                    default:
                        if (shouldBeWider) {
                            // too wide, fix width
                            width = Math.min(video_width, width_spec_size);
                            height = (int) (width / display_aspect_radio);
                        } else {
                            // too high, fix height
                            height = Math.min(video_height, height_spec_size);
                            width = (int) (height * display_aspect_radio);
                        }
                        break;
                }
            } else if (width_spec_mode == View.MeasureSpec.EXACTLY && height_spec_mode == View.MeasureSpec.EXACTLY) {
                width = width_spec_size;
                height = height_spec_size;

                // 为了兼容，调整宽高比
                if (video_width * height < width * video_height) {
                    width = height * video_width / video_height;
                } else if (video_width * height > width * video_height) {
                    height = width * video_height / video_width;
                }
            } else if (width_spec_mode == View.MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = width_spec_size;
                height = width * video_height / video_width;
                if (height_spec_mode == View.MeasureSpec.AT_MOST && height > height_spec_size) {
                    // couldn't match aspect ratio within the constraints
                    height = height_spec_size;
                }
            } else if (height_spec_mode == View.MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = height_spec_size;
                width = height * video_width / video_height;
                if (width_spec_mode == View.MeasureSpec.AT_MOST && width > width_spec_size) {
                    // couldn't match aspect ratio within the constraints
                    width = width_spec_size;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = video_width;
                height = video_height;
                if (height_spec_mode == View.MeasureSpec.AT_MOST && height > height_spec_size) {
                    // too tall, decrease both width and height
                    height = height_spec_size;
                    width = height * video_width / video_height;
                }
                if (height_spec_mode == View.MeasureSpec.AT_MOST && width > width_spec_size) {
                    // too wide, decrease both width and height
                    width = width_spec_size;
                    height = width * video_height / video_width;
                }
            }
        } else {

        }

        measured_width = width;
        measured_height = height;
    }

    public int getMeasuredWidth() {
        return measured_width;
    }

    public int getMeasuredHeight() {
        return measured_height;
    }

    public void setAspectRatio(int aspectRatio) {
        current_aspect_ratio = aspectRatio;
    }


}
