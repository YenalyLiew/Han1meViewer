package com.yenaly.yenaly_libs.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;
import android.view.DisplayCutout;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.yenaly.yenaly_libs.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@SuppressLint("ObsoleteSdkInt")
@SuppressWarnings("all")
public class SystemStatusUtil {
    /**
     * 设置界面全屏，状态默认是在为透明
     *
     * @param window     window
     * @param showStatus 是否显示状态栏
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fullScreen(@NonNull Window window, boolean showStatus) {
        //SDK版本>=5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            decorView.setSystemUiVisibility(option);
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                // 仅当缺口区域完全包含在状态栏之中时，才允许窗口延伸到刘海区域显示
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                // 永远不允许窗口延伸到刘海区域
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
                // 始终允许窗口延伸到屏幕短边上的刘海区域
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //设置状态栏透明
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                //设置界面是否全屏，显示状态栏
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                //设置界面是否全屏，隐藏状态栏
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            decorView.setSystemUiVisibility(option);
        }
    }

    /**
     * 设置界面全屏，状态默认是在为透明
     *
     * @param window         window
     * @param showStatus     是否显示状态栏
     * @param showNavigation 是否显示导航栏
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fullScreen(@NonNull Window window, boolean showStatus, boolean showNavigation) {
        //SDK版本>=5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            if (!showNavigation) {
                option |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            decorView.setSystemUiVisibility(option);
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                // 仅当缺口区域完全包含在状态栏之中时，才允许窗口延伸到刘海区域显示
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                // 永远不允许窗口延伸到刘海区域
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
                // 始终允许窗口延伸到屏幕短边上的刘海区域
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //设置状态栏透明
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            if (!showNavigation) {
                option |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            decorView.setSystemUiVisibility(option);
        }
    }

    /**
     * 设置界面全屏，状态默认是在为透明
     *
     * @param activity   Activity
     * @param showStatus 是否显示状态栏
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fullScreen(@NonNull Activity activity, boolean showStatus) {
        Window window = activity.getWindow();
        //SDK版本>=5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            decorView.setSystemUiVisibility(option);
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                // 仅当缺口区域完全包含在状态栏之中时，才允许窗口延伸到刘海区域显示
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                // 永远不允许窗口延伸到刘海区域
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
                // 始终允许窗口延伸到屏幕短边上的刘海区域
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //设置状态栏透明
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                //设置界面是否全屏，显示状态栏
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                //设置界面是否全屏，隐藏状态栏
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            decorView.setSystemUiVisibility(option);
        }
    }

    /**
     * 设置界面全屏，状态默认是在为透明
     *
     * @param activity       Activity
     * @param showStatus     是否显示状态栏
     * @param showNavigation 是否显示导航栏
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void fullScreen(@NonNull Activity activity, boolean showStatus, boolean showNavigation) {
        Window window = activity.getWindow();
        //SDK版本>=5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            if (!showNavigation) {
                option |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            decorView.setSystemUiVisibility(option);
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                WindowManager.LayoutParams lp = window.getAttributes();
                // 仅当缺口区域完全包含在状态栏之中时，才允许窗口延伸到刘海区域显示
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
                // 永远不允许窗口延伸到刘海区域
//            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_NEVER;
                // 始终允许窗口延伸到屏幕短边上的刘海区域
                lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
                window.setAttributes(lp);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //设置状态栏透明
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            View decorView = window.getDecorView();
            int option;
            if (showStatus) {
                option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            } else {
                option = View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            }
            if (!showNavigation) {
                option |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }
            decorView.setSystemUiVisibility(option);
        }
    }

    /**
     * 设置状态栏为透明色 4.4以上
     *
     * @param window window
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void statusTransparent(@NonNull Window window) {
        //SDK版本>=5.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //设置状态栏透明
            window.addFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    /**
     * 设置状态栏颜色 5.0以上
     *
     * @param window window
     * @param color  状态栏颜色
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void statusColor(@NonNull Window window, int color) {
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
    }

    /**
     * 设置状态栏文字颜色
     *
     * @param window window
     * @param dark   是否为暗色
     */
    public static void setStatusIconDarkMode(@NonNull Window window, boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            boolean result;
            if (DeviceUtil.deviceIsMIUI()) {
                result = miuiStatusIconLightMode(window, dark);
            } else if (DeviceUtil.deviceIsFlyme()) {
                result = meizuStatusIconDarkIcon(window, dark);
            } else {
                result = systemStatusIconLightMode(window, dark);
            }
            //设置状态栏颜色失败后，修改状态栏颜色
            if (!result) {
                setColor(window, Color.BLACK, 0);
            }
        }

    }


    /**
     * 设置小米手机状态栏文字颜色，需要MIUI V6以上
     *
     * @param window 需要设置的窗口
     * @param dark   是否把状态栏字体及图标颜色设置为深色
     * @return boolean 成功执行返回true
     */
    public static boolean miuiStatusIconLightMode(@NonNull Window window, boolean dark) {
        boolean result = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Class<? extends Window> clazz = window.getClass();
            try {
                int darkModeFlag = 0;
                Class<?> layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                Method extraFlagField = clazz.getMethod("setExtraFlags", int.class, int.class);
                extraFlagField.invoke(window, dark ? darkModeFlag : 0, darkModeFlag);
                result = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            result = systemStatusIconLightMode(window, dark);
        }
        return result;
    }


    /**
     * 修改魅族状态栏字体颜色
     *
     * @param window window
     * @param dark   是否为暗色
     */
    public static boolean meizuStatusIconDarkIcon(@NonNull Window window, boolean dark) {
        boolean result = false;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class.getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                //e.printStackTrace();
            }
        } else {
            result = systemStatusIconLightMode(window, dark);
        }
        return result;
    }

    /**
     * 设置安卓系统6.0及以上的浅色状态栏
     *
     * @param window window
     * @param dark   是否为暗色
     * @return boolean
     */
    public static boolean systemStatusIconLightMode(@NonNull Window window, boolean dark) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int flag;
            if (dark) {
                // 沉浸式
                flag = window.getDecorView().getSystemUiVisibility() |
                        View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                //非沉浸式
                flag = window.getDecorView().getSystemUiVisibility() &
                        ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flag);
            result = true;
        }
        return result;
    }

    /**
     * 设置状态栏颜色
     *
     * @param window         window
     * @param color          状态栏颜色值
     * @param statusBarAlpha 状态栏透明度
     */
    public static void setColor(@NonNull Window window, @ColorInt int color, @IntRange(from = 0, to = 255) int statusBarAlpha) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(calculateStatusColor(color, statusBarAlpha));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            ViewGroup decorView = (ViewGroup) window.getDecorView();
            View fakeStatusBarView = decorView.findViewById(R.id.statusbarutil_fake_status_bar_view);
            if (fakeStatusBarView != null) {
                if (fakeStatusBarView.getVisibility() == View.GONE) {
                    fakeStatusBarView.setVisibility(View.VISIBLE);
                }
                fakeStatusBarView.setBackgroundColor(calculateStatusColor(color, statusBarAlpha));
            } else {
                decorView.addView(createStatusBarView(window.getContext(), color, statusBarAlpha));
            }
        }
    }

    /**
     * 生成一个和状态栏大小相同的半透明矩形条
     *
     * @param context 需要设置的activity
     * @param color   状态栏颜色值
     * @param alpha   透明值
     * @return 状态栏矩形条
     */
    private static View createStatusBarView(@NonNull Context context, @ColorInt int color, int alpha) {
        // 绘制一个和状态栏一样高的矩形
        View statusBarView = new View(context);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, DeviceUtil.getStatusBarHeight(context));
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(calculateStatusColor(color, alpha));
        statusBarView.setId(R.id.statusbarutil_fake_status_bar_view);
        return statusBarView;
    }

    /**
     * 计算状态栏颜色
     *
     * @param color color值
     * @param alpha alpha值
     * @return 最终的状态栏颜色
     */
    private static int calculateStatusColor(@ColorInt int color, int alpha) {
        if (alpha == 0) {
            return color;
        }
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }


    /**
     * 是否有刘海屏
     *
     * @param activity Activity
     * @return boolean
     */
    public static boolean hasNotchInScreen(Activity activity) {
        boolean hasNotchInScreen = false;
        // android  P 以上有标准 API 来判断是否有刘海屏
        hasNotchInScreen = hasNotchSystem(activity);
        if (!hasNotchInScreen) {
            String manufacturer = Build.MANUFACTURER;
            if (TextUtils.isEmpty(manufacturer)) {
                return false;
            } else if (manufacturer.equalsIgnoreCase("HUAWEI")) {
                return hasNotchHw(activity);
            } else if (manufacturer.equalsIgnoreCase("xiaomi")) {
                return hasNotchXiaoMi(activity);
            } else if (manufacturer.equalsIgnoreCase("oppo")) {
                return hasNotchOPPO(activity);
            } else if (manufacturer.equalsIgnoreCase("vivo")) {
                return hasNotchVIVO(activity);
            } else {
                return false;
            }
        }
        return false;
    }


    private static boolean hasNotchSystem(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowInsets rootWindowInsets = activity.getWindow().getDecorView().getRootWindowInsets();
            DisplayCutout displayCutout = null;
            if (rootWindowInsets != null)
                displayCutout = rootWindowInsets.getDisplayCutout();
            // 说明有刘海屏
            return displayCutout != null;
        }
        return false;
    }

    /**
     * 判断vivo是否有刘海屏
     * https://swsdl.vivo.com.cn/appstore/developer/uploadfile/20180328/20180328152252602.pdf
     *
     * @param activity Activity
     * @return boolean
     */
    private static boolean hasNotchVIVO(Activity activity) {
        try {
            Class<?> c = Class.forName("android.util.FtFeature");
            Method get = c.getMethod("isFeatureSupport", int.class);
            return (boolean) (get.invoke(c, 0x20));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断oppo是否有刘海屏
     * https://open.oppomobile.com/wiki/doc#id=10159
     *
     * @param activity Activity
     * @return boolean
     */
    private static boolean hasNotchOPPO(Activity activity) {
        return activity.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    /**
     * 判断xiaomi是否有刘海屏
     * https://dev.mi.com/console/doc/detail?pId=1293
     *
     * @param activity Activity
     * @return boolean
     */
    private static boolean hasNotchXiaoMi(Activity activity) {
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("getInt", String.class, int.class);
            return (int) (get.invoke(c, "ro.miui.notch", 0)) == 1;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断华为是否有刘海屏
     * https://devcenter-test.huawei.com/consumer/cn/devservice/doc/50114
     *
     * @param activity Activity
     * @return boolean
     */
    private static boolean hasNotchHw(Activity activity) {

        try {
            ClassLoader cl = activity.getClassLoader();
            Class<?> HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            return (boolean) get.invoke(HwNotchSizeUtil);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前实际状态栏高度
     *
     * @param window window
     * @return 当前实际状态栏高度
     */
    public static int getCurrentStatusBarHeight(@NonNull Window window) {
        WindowInsetsCompat windowInsetsCompat = ViewCompat.getRootWindowInsets(window.getDecorView());
        if (windowInsetsCompat != null) {
            return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).top;
        }
        return DeviceUtil.getStatusBarHeight(window.getContext());
    }

    /**
     * 获取当前实际导航栏高度
     *
     * @param window window
     * @return 当前实际导航栏高度
     */
    public static int getCurrentNavBarHeight(@NonNull Window window) {
        WindowInsetsCompat windowInsetsCompat = ViewCompat.getRootWindowInsets(window.getDecorView());
        if (windowInsetsCompat != null) {
            return windowInsetsCompat.getInsets(WindowInsetsCompat.Type.statusBars()).bottom;
        }
        return DeviceUtil.getNavigationBarHeight(window.getContext());
    }

    /**
     * 是否显示系统栏
     *
     * @param window    window
     * @param statusBar 显示状态栏
     * @param navBar    显示导航栏
     */
    public static void showSystemBar(@NonNull Window window, boolean statusBar, boolean navBar) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        if (controller != null) {
            if (statusBar && navBar) {
                controller.show(WindowInsetsCompat.Type.systemBars());
                return;
            }
            if (!statusBar && !navBar) {
                controller.hide(WindowInsetsCompat.Type.systemBars());
                return;
            }
            if (statusBar) {
                controller.show(WindowInsetsCompat.Type.statusBars());
            } else {
                controller.hide(WindowInsetsCompat.Type.statusBars());
            }
            if (navBar) {
                controller.show(WindowInsetsCompat.Type.navigationBars());
            } else {
                controller.hide(WindowInsetsCompat.Type.navigationBars());
            }
        } else {
            if (isStatusBarVisible(window) != statusBar || isNavBarVisible(window) != navBar) {
                SystemStatusUtil.fullScreen(window, statusBar, navBar);
            }
        }
    }

    /**
     * 是否将系统栏图标切换成亮色模式
     *
     * @param window    window
     * @param statusBar 设置状态栏图标为亮色模式
     * @param navBar    设置导航栏图标为亮色模式
     */
    public static void setSystemBarIconLightMode(@NonNull Window window, boolean statusBar, boolean navBar) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(statusBar);
            controller.setAppearanceLightNavigationBars(navBar);
            if (controller.isAppearanceLightStatusBars() != statusBar) {
                setStatusIconDarkMode(window, statusBar);
            }
        } else {
            setStatusIconDarkMode(window, statusBar);
        }
    }

    /**
     * 当前状态栏是否可见
     *
     * @param window window
     * @return 是否可见
     */
    public static boolean isStatusBarVisible(@NonNull Window window) {
        WindowInsetsCompat windowInsetsCompat = ViewCompat.getRootWindowInsets(window.getDecorView());
        if (windowInsetsCompat != null) {
            return windowInsetsCompat.isVisible(WindowInsetsCompat.Type.statusBars());
        }
        return true;
    }

    /**
     * 当前导航栏是否可见
     *
     * @param window window
     * @return 是否可见
     */
    public static boolean isNavBarVisible(@NonNull Window window) {
        WindowInsetsCompat windowInsetsCompat = ViewCompat.getRootWindowInsets(window.getDecorView());
        if (windowInsetsCompat != null) {
            return windowInsetsCompat.isVisible(WindowInsetsCompat.Type.navigationBars());
        }
        return true;
    }

    /**
     * 当前软键盘是否可见
     *
     * @param window window
     * @return 是否可见
     */
    public static boolean isImeVisible(@NonNull Window window) {
        WindowInsetsCompat windowInsetsCompat = ViewCompat.getRootWindowInsets(window.getDecorView());
        if (windowInsetsCompat != null) {
            return windowInsetsCompat.isVisible(WindowInsetsCompat.Type.ime());
        }
        return false;
    }

    /**
     * 是否显示软键盘
     *
     * @param window window
     * @param ime    是否显示软键盘
     */
    public static void showIme(@NonNull Window window, boolean ime) {
        WindowInsetsControllerCompat controller = ViewCompat.getWindowInsetsController(window.getDecorView());
        if (controller != null) {
            if (ime) {
                controller.show(WindowInsetsCompat.Type.ime());
            } else {
                controller.hide(WindowInsetsCompat.Type.ime());
            }
        } else {
            InputMethodManager imm = (InputMethodManager) window.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (ime) {
                imm.showSoftInput(window.getDecorView(), InputMethodManager.SHOW_FORCED);
            } else {
                imm.hideSoftInputFromWindow(window.getDecorView().getWindowToken(), 0);
            }
        }
    }

    /**
     * 判断当前是否为夜间模式
     *
     * @param context 上下文
     * @return boolean
     */
    public static boolean isAppDarkMode(@NonNull Context context) {
        return (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }
}
