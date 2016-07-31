package com.smartbuilders.smartsales.ecommerce.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.List;

/**
 * Created by stein on 31/7/2016.
 * Samsung, Sony and HTC
 */
public class BadgeUtils {

    public static void setBadge(Context context, int count) {
        setBadgeSamsung(context, count);
        setBadgeSony(context, count);

    }

    public static void clearBadge(Context context) {
        setBadgeSamsung(context, 0);
        clearBadgeSony(context);
    }


    private static void setBadgeSamsung(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }
        Intent intent = new Intent("android.intent.action.BADGE_COUNT_UPDATE");
        intent.putExtra("badge_count", count);
        intent.putExtra("badge_count_package_name", context.getPackageName());
        intent.putExtra("badge_count_class_name", launcherClassName);

        context.sendBroadcast(intent);
    }

    private static void setBadgeSony(Context context, int count) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME", launcherClassName);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", true);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(count));
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());

        context.sendBroadcast(intent);
    }


    private static void clearBadgeSony(Context context) {
        String launcherClassName = getLauncherClassName(context);
        if (launcherClassName == null) {
            return;
        }

        Intent intent = new Intent();
        intent.setAction("com.sonyericsson.home.action.UPDATE_BADGE");
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.ACTIVITY_NAME" , launcherClassName);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.SHOW_MESSAGE", false);
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.MESSAGE", String.valueOf(0));
        intent.putExtra("com.sonyericsson.home.intent.extra.badge.PACKAGE_NAME", context.getPackageName());

        context.sendBroadcast(intent);
    }

    private static String getLauncherClassName(Context context) {
        if (context!=null) {
            PackageManager pm = context.getPackageManager();

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);

            if (pm!=null) {
                List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
                if (resolveInfos!=null) {
                    for (ResolveInfo resolveInfo : resolveInfos) {
                        if (resolveInfo!=null && resolveInfo.activityInfo!=null && resolveInfo.activityInfo.applicationInfo!=null
                                && resolveInfo.activityInfo.applicationInfo.packageName!=null
                                && resolveInfo.activityInfo.applicationInfo.packageName.equalsIgnoreCase(context.getPackageName())) {
                            return resolveInfo.activityInfo.name;
                        }
                    }
                }
            }
        }
        return null;
    }
}
