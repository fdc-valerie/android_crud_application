package com.example.crudapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String ADMIN_CHANNEL_ID = "notify_001";
    private String mTask, mDescription, mDate;
    private Uri notifSound;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        mTask = remoteMessage.getData().get("title");
        mDescription = remoteMessage.getData().get("message");
        mDate = remoteMessage.getData().get("date");

        final Intent intent = new Intent(this, NotificationMessage.class);

        NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        int notificationID = new Random().nextInt(3000);
        notifSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ this.getPackageName() + "/" + R.raw.test);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            setupChannels(notificationManager);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int pendingFlags;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingFlags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            pendingFlags = PendingIntent.FLAG_ONE_SHOT;
        }

        Intent intent1 = new Intent(this, NotificationMessage.class);
        intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent1.putExtra("task", mTask);
        intent1.putExtra("description", mDescription);
        intent1.putExtra("date", mDate);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent1, pendingFlags);

        RemoteViews contentView = new RemoteViews(this.getPackageName(), R.layout.notification_layout);
        contentView.setImageViewResource(R.id.icon, R.drawable.logo3);

        int pendingSwitchFlags;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingSwitchFlags = PendingIntent.FLAG_IMMUTABLE;
        } else {
            pendingSwitchFlags = 0;
        }

        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0, intent, pendingSwitchFlags);
        contentView.setOnClickPendingIntent(R.id.flashButton, pendingSwitchIntent);
        contentView.setTextViewText(R.id.message, mTask);
        contentView.setTextViewText(R.id.date, mDate);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, ADMIN_CHANNEL_ID)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setSmallIcon(R.drawable.ic_baseline_alarm_24)
                .setContent(contentView)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setSound(notifSound, AudioManager.STREAM_ALARM)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationID, notificationBuilder.build());
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "New notification";
        String adminChannelDescription = "Device notification ";

        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID,
                adminChannelName,
                NotificationManager.IMPORTANCE_HIGH);
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_ALARM)
                .build();

        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        adminChannel.setSound(notifSound, audioAttributes);


        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }
}