package com.yenaly.yenaly_libs;

import android.app.Activity;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yenaly.yenaly_libs.utils.ContextUtil;

import java.util.Iterator;
import java.util.LinkedList;

@SuppressWarnings("unused")
public class ActivitiesManager {

    private static final String TAG = "ActivitiesManager";

    protected static LinkedList<Activity> mActivityStack = new LinkedList<>();

    /**
     * 获得当前Activity
     *
     * @return 当前Activity
     */
    @Nullable
    public static Activity getCurrentActivity() {
        if (mActivityStack.isEmpty()) {
            return null;
        } else {
            return mActivityStack.getLast();
        }
    }

    /**
     * Activity入栈
     *
     * @param activity 选择的Activity
     */
    public static void pushActivity(Activity activity) {
        if (mActivityStack.contains(activity)) {
            if (mActivityStack.getLast() != activity) {
                mActivityStack.remove(activity);
                mActivityStack.add(activity);
            }
        } else {
            mActivityStack.add(activity);
        }
        Log.i(TAG, activity.getClass().getName() + " has been added.");
    }

    /**
     * Activity出栈
     *
     * @param activity 选择的Activity
     */
    public static void popActivity(Activity activity) {
        mActivityStack.remove(activity);
        Log.i(TAG, activity.getClass().getName() + " has been popped.");
    }

    /**
     * 结束当前Activity
     */
    public static void finishCurrentActivity() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity != null) {
            currentActivity.finish();
            Log.i(TAG, currentActivity.getClass().getName() + " has finished.");
        }
    }

    /**
     * 结束特定Activity
     *
     * @param activity 选择的Activity
     */
    public static void finishActivity(@NonNull Activity activity) {
        activity.finish();
        Log.i(TAG, activity.getClass().getName() + " has finished.");
    }

    /**
     * 结束指定类名的所有Activity
     *
     * @param clazz class
     */
    public static void finishActivity(Class<?> clazz) {
        if (!mActivityStack.isEmpty()) {
            Iterator<Activity> iterator = mActivityStack.iterator();
            while (iterator.hasNext()) {
                Activity activity = iterator.next();
                if (activity == null) {
                    iterator.remove();
                    continue;
                }
                if (activity.getClass().equals(clazz)) {
                    iterator.remove();
                    activity.finish();
                    Log.i(TAG, activity.getClass().getName() + " has finished.");
                }
            }
        }
    }

    public static void finishAllActivityExceptThis() {
        if (!mActivityStack.isEmpty()) {
            if (mActivityStack.size() > 1) {
                for (int i = 0; i < mActivityStack.size() - 1; i++) {
                    finishActivity(mActivityStack.get(i));
                }
            }
        }
    }

    /**
     * 结束所有Activity
     */
    public static void finishAllActivity() {
        if (!mActivityStack.isEmpty()) {
            for (Activity activity : mActivityStack) {
                activity.finish();
                Log.i(TAG, activity.getClass().getName() + " has finished.");
            }
        }
    }

    /**
     * 重建所有Activity
     */
    public static void recreateAllActivity() {
        if (!mActivityStack.isEmpty()) {
            for (Activity activity : mActivityStack) {
                activity.recreate();
                Log.i(TAG, activity.getClass().getName() + " has recreated.");
            }
        }
    }

    /**
     * 退出APP，并且杀掉进程
     */
    public static void exitAppWithKillingProcess() {
        try {
            finishAllActivity();
            Process.killProcess(Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 退出APP，但不杀进程
     */
    public static void exitAppWithoutKillingProcess() {
        try {
            finishAllActivity();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重启APP，并且杀掉进程
     */
    public static void restartAppWithKillingProcess() {
        try {
            finishAllActivity();
            Intent intent = ContextUtil
                    .getApplicationContext()
                    .getPackageManager()
                    .getLaunchIntentForPackage(ContextUtil.getApplicationContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ContextUtil.getApplicationContext().startActivity(intent);
            Process.killProcess(Process.myPid());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 重启APP，但不杀进程
     */
    public static void restartAppWithoutKillingProcess() {
        try {
            finishAllActivity();
            Intent intent = ContextUtil
                    .getApplicationContext()
                    .getPackageManager()
                    .getLaunchIntentForPackage(ContextUtil.getApplicationContext().getPackageName());
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            ContextUtil.getApplicationContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
