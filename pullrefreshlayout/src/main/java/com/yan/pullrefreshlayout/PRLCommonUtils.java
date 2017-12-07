package com.yan.pullrefreshlayout;

import android.content.Context;
import android.support.v4.widget.ListViewCompat;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import java.lang.reflect.Constructor;

/**
 * Created by yan on 2017/5/21.
 */
public class PRLCommonUtils {

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll up. Override this if the child view is a custom view.
     */
    public static boolean canChildScrollUp(View targetView) {
        if (targetView instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) targetView, -1);
        }
        return targetView.canScrollVertically(-1);
    }

    /**
     * @return Whether it is possible for the child view of this layout to
     * scroll down. Override this if the child view is a custom view.
     */
    public static boolean canChildScrollDown(View targetView) {
        if (targetView instanceof ListView) {
            return ListViewCompat.canScrollList((ListView) targetView, 1);
        }
        return targetView.canScrollVertically(1);
    }

    /**
     * common utils
     *
     * @param context
     * @return
     */
    public static int getWindowHeight(Context context) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.heightPixels;
    }

    public static int dipToPx(Context context, float value) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, metrics);
    }

    /**
     * parseClassName
     *
     * @param context   context
     * @param className className
     * @return
     */
    static View parseClassName(Context context, String className) {
        if (!TextUtils.isEmpty(className)) {
            try {
                final Class<?>[] CONSTRUCTOR_PARAMS = new Class<?>[]{Context.class};
                final Class<View> clazz = (Class<View>) Class.forName(className, true, context.getClassLoader());
                Constructor<View> constructor = clazz.getConstructor(CONSTRUCTOR_PARAMS);
                constructor.setAccessible(true);
                return constructor.newInstance(context);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}