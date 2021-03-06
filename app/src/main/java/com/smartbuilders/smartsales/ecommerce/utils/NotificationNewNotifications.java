package com.smartbuilders.smartsales.ecommerce.utils;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;

import com.smartbuilders.smartsales.ecommerce.NotificationsListActivity;
import com.smartbuilders.smartsales.ecommerce.R;

/**
 * Created by stein on 31/7/2016.
 */
public class NotificationNewNotifications {

    private static final int NOTIFICATION_ID = 111222;

    public static void createNotification(Context context) {
        NotificationCompat.Builder mBuilder;

        //if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new NotificationCompat.Builder(context)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle(context.getString(R.string.app_name))
                    .setColor(context.getResources().getColor(R.color.colorAccent))
                    .setContentText(context.getString(R.string.new_notifications));
        //} else {
        //    // Lollipop specific setColor method goes here.
        //    mBuilder = new NotificationCompat.Builder(context)
        //            .setSmallIcon(R.drawable.ic_launcher_transparent)
        //            .setContentTitle(contentTitle)
        //            .setColor(context.getResources().getColor(R.color.colorPrimary))
        //            .setContentText(contentText);
        //}

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        //stackBuilder.addParentStack(ResultActivity.class);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(context, NotificationsListActivity.class);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = PendingIntent.getActivity(context, 0, resultIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);

        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setAutoCancel(true);

        /*******************************************************************************************/
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtone = sharedPreferences.getString("notifications_ringtone",
                context.getString(R.string.pref_ringtone_silent));
        boolean useSound = !TextUtils.isEmpty(ringtone) && !ringtone.equals(context.getString(R.string.pref_ringtone_silent));
        boolean vibrate = sharedPreferences.getBoolean("notifications_vibrate", true);
        if(useSound && vibrate) {
            mBuilder.setSound(Uri.parse(ringtone));
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE
                    | Notification.DEFAULT_LIGHTS);
        } else if (useSound) {
            mBuilder.setSound(Uri.parse(ringtone));
            mBuilder.setDefaults(Notification.DEFAULT_LIGHTS);
        } else if (vibrate) {
            mBuilder.setDefaults(Notification.DEFAULT_VIBRATE
                    | Notification.DEFAULT_LIGHTS);
        }
        /*******************************************************************************************/

        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        editor.putBoolean("is_notification_shown", true);
        editor.apply();
    }

    public static void cancelNotification(Context context){
        try {
            ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            editor.putBoolean("is_notification_shown", false);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isNotificationShown(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean("is_notification_shown", false);
    }
}
