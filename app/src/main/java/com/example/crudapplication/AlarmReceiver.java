package com.example.crudapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class AlarmReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String task = bundle.getString("task");
        String date = bundle.getString("date");
        String description = bundle.getString("description");
        Uri notifSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.test);

        //Click on Notification

        Intent intent1 = new Intent(context, NotificationMessage.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("task", task);
        intent1.putExtra("description", description);
        intent1.putExtra("date", date);


        //Notification Builder
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent1, PendingIntent.FLAG_ONE_SHOT);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.icon, R.mipmap.ic_launcher);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        contentView.setOnClickPendingIntent(R.id.flashButton, pendingSwitchIntent);
        contentView.setTextViewText(R.id.message, task);
        contentView.setTextViewText(R.id.date, date);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, "notify_001")
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setSound(notifSound, AudioManager.STREAM_ALARM)
                .setVibrate(new long[0])
                .setAutoCancel(true)
                .setOngoing(true)
                .setContent(contentView)
                .setContentIntent(pendingIntent)
                .setFullScreenIntent(pendingIntent, true);
        Notification mNotif = mBuilder.build();
        mNotif.flags = Notification.FLAG_INSISTENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "channel_id";
            NotificationChannel channel = new NotificationChannel(channelId, "channel name", NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();
            channel.setSound(notifSound, audioAttributes);
            notificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        Notification notification = mBuilder.build();
        notificationManager.notify(1, notification);
    }
}
