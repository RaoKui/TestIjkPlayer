package com.raokui.ijk.weiget;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by 饶魁 on 2017/9/22.
 */

public class LayoutQuery {

    private Context context;
    private Activity activity;
    private View view;
    private View rootView;

    public LayoutQuery(Context context, View view) {
        this.context = context;
        this.rootView = view;
    }

    public LayoutQuery(Activity activity) {
        this.activity = activity;
        this.context = activity;
    }

    public LayoutQuery id(int id) {
        if (rootView == null) {
            view = activity.findViewById(id);
        } else {
            view = rootView.findViewById(id);
        }
        return this;
    }

    public LayoutQuery image(int res_id) {
        if (view instanceof ImageView) {
            ((ImageView) view).setImageResource(res_id);
        }
        return this;
    }

    public LayoutQuery visible() {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }
        return this;
    }


    public LayoutQuery gone() {
        if (view != null) {
            view.setVisibility(View.GONE);
        }

        return this;
    }

    public LayoutQuery invisible() {
        if (view != null) {
            view.setVisibility(View.INVISIBLE);
        }
        return this;
    }

    public LayoutQuery clicked(View.OnClickListener handler) {
        if (view != null) {
            view.setOnClickListener(handler);
        }
        return this;
    }

    public LayoutQuery text(CharSequence text) {
        if (view != null && view instanceof TextView) {
            ((TextView) view).setText(text);
        }
        return this;
    }

    public LayoutQuery visibility(int visible) {
        if (view != null) {
            view.setVisibility(visible);
        }
        return this;
    }

    private void size(boolean width, int n, boolean dip) {

        if (view != null) {

            ViewGroup.LayoutParams lp = view.getLayoutParams();


            if (n > 0 && dip) {
                n = dip2pixel(context, n);
            }

            if (width) {
                lp.width = n;
            } else {
                lp.height = n;
            }

            view.setLayoutParams(lp);

        }

    }

    public void height(int height, boolean dip) {
        size(false, height, dip);
    }

    public int dip2pixel(Context context, float n) {
        int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
        return value;
    }

    public float pixel2dip(Context context, float n) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = n / (metrics.densityDpi / 160f);
        return dp;

    }


}
