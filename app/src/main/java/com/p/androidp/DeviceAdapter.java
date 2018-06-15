package com.p.androidp;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by xiejingbao on 2018/6/13.
 */

public class DeviceAdapter {


    /*刘海屏全屏显示FLAG*/
    public static final int FLAG_NOTCH_SUPPORT=0x00010000;
    public static final String TAG="DeviceAdapter";
    public static boolean hasNotchInScreen(Context context) {
        boolean ret = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            ret = (boolean) get.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.e("test", "hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("test", "hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            Log.e("test", "hasNotchInScreen Exception");
        } finally {
            return ret;
        }
    }


    /**
     * 设置应用窗口在华为刘海屏手机使用刘海区
     * @param window 应用页面window对象
     */
    public static void setNotFullScreenWindowLayoutInDisplayCutout (Window window) {
        if (window == null) {
            return;
        }
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        try {
            Class layoutParamsExCls = Class.forName("com.huawei.android.view.LayoutParamsEx");
            Constructor con=layoutParamsExCls.getConstructor(WindowManager.LayoutParams.class);
            Object layoutParamsExObj=con.newInstance(layoutParams);
            Method method=layoutParamsExCls.getMethod("clearHwFlags", int.class);
            method.invoke(layoutParamsExObj, FLAG_NOTCH_SUPPORT);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException |InstantiationException
                | InvocationTargetException e) {
            Log.e("test", "hw clear notch screen flag api error");
        } catch (Exception e) {
            Log.e("test", "other Exception");
        }
    }

    /**
     * 华为刘海屏幕
     * @param context
     * @param window
     */
    public static void huaWei(Context context,Window window){
        if(hasNotchInScreen(context)){
            setNotFullScreenWindowLayoutInDisplayCutout(window);
        }
    }


    public static final int VIVO_NOTCH = 0x00000020;//是否有刘海
    public static final int VIVO_FILLET = 0x00000008;//是否有圆角

    /**
     * vivo 不提供接口获取刘海尺寸，目前vivo的刘海宽为100dp,高为27dp。
     * @param context
     * @return
     */
    public static boolean hasNotchAtVoio(Context context) {
        boolean ret = false;
        try {
            ClassLoader classLoader = context.getClassLoader();
            Class FtFeature = classLoader.loadClass("android.util.FtFeature");
            Method method = FtFeature.getMethod("isFeatureSupport", int.class);
            ret = (boolean) method.invoke(FtFeature, VIVO_NOTCH);
        } catch (ClassNotFoundException e) {
            Log.e("Notch", "hasNotchAtVoio ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("Notch", "hasNotchAtVoio NoSuchMethodException");
        } catch (Exception e) {
            Log.e("Notch", "hasNotchAtVoio Exception");
        } finally {
            return ret;
        }
    }


    /**
     * oppo  刘海区域则都是宽度为324px, 高度为80px
     * @param context
     * @return
     */
    public static boolean hasNotchInScreenAtOPPO(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }


    public static void androidP(Window window){

      View  contentView = window.getDecorView().findViewById(android.R.id.content).getRootView();
        contentView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
            @RequiresApi(api = 28)
            @Override
            public WindowInsets onApplyWindowInsets(View view, WindowInsets windowInsets) {
                DisplayCutout cutout = windowInsets.getDisplayCutout();
                if (cutout == null) {
                    Log.e(TAG, "cutout==null, is not notch screen");//通过cutout是否为null判断是否刘海屏手机
                } else {
                    List<Rect> rects = cutout.getBoundingRects();
                    if (rects == null || rects.size() == 0) {
                        Log.e(TAG, "rects==null || rects.size()==0, is not notch screen");
                    } else {
                        Log.e(TAG, "rect size:" + rects.size());//注意：刘海的数量可以是多个
                        for (Rect rect : rects) {
                            Log.e(TAG, "cutout.getSafeInsetTop():" + cutout.getSafeInsetTop()
                                    + ", cutout.getSafeInsetBottom():" + cutout.getSafeInsetBottom()
                                    + ", cutout.getSafeInsetLeft():" + cutout.getSafeInsetLeft()
                                    + ", cutout.getSafeInsetRight():" + cutout.getSafeInsetRight()
                                    + ", cutout.rects:" + rect
                            );
                        }
                    }
                }
                return windowInsets;
            }
        });
    }

    public static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 设置toolbar高度，此方法讲状态栏设置透明后，既可调用，显示沉浸式状态栏，此时状态栏的颜色跟toolbar的颜色一致
     * @param view
     * @param context
     */
    public static void setHeight(View view,Context context) {
        // 获取actionbar的高度
        TypedArray actionbarSizeTypedArray = context.obtainStyledAttributes(new int[]{
                android.R.attr.actionBarSize
        });
        float height = actionbarSizeTypedArray.getDimension(0, 0);
        // ToolBar的top值
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        double statusBarHeight = getStatusBarHeight(context);
        lp.height = (int) (statusBarHeight + height);
        view.setPadding(0,(int) statusBarHeight,0, 0);
        view.setLayoutParams(lp);
    }



    /**
     * 此方法需要设置根布局  android:fitsSystemWindows="true"
     */
    private void addStatusBarView(Activity context) {
        View view = new View(context);
        view.setBackgroundColor(ContextCompat.getColor(context,R.color.colorPrimary));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                DeviceAdapter.getStatusBarHeight(context));
        ViewGroup decorView = (ViewGroup) context.findViewById(android.R.id.content);
        decorView.addView(view, params);
    }
}
