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
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class AlarmReceiver extends BroadcastReceiver {
    private DatabaseReference reference;
    private String taskID, userID, mTask, mDate, mDescription;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        userID = bundle.getString("user_id");
        taskID = bundle.getString("task_id");

        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(userID).child(taskID);
        reference.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    ToDoModel model = task.getResult().getValue(ToDoModel.class);
                    mTask = model.getTask();
                    mDescription = model.getDescription();
                    mDate = model.getDate();

                    Uri notifSound = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"+ context.getPackageName() + "/" + R.raw.test);

                    //Click on Notification

                    Intent intent1 = new Intent(context, NotificationMessage.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent1.putExtra("task", mTask);
                    intent1.putExtra("description", mDescription);
                    intent1.putExtra("date", mDate);

                    int pendingFlags;
                    if (Build.VERSION.SDK_INT >= 23) {
                        pendingFlags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE;
                    } else {
                        pendingFlags = PendingIntent.FLAG_ONE_SHOT;
                    }

                    //Notification Builder
                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 1, intent1, pendingFlags);
                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                    RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout);
                    contentView.setImageViewResource(R.id.icon, R.drawable.logo3);

                    int pendingSwitchFlags;
                    if (Build.VERSION.SDK_INT >= 23) {
                        pendingSwitchFlags = PendingIntent.FLAG_IMMUTABLE;
                    } else {
                        pendingSwitchFlags = 0;
                    }

                    PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(context, 0, intent, pendingSwitchFlags);
                    contentView.setOnClickPendingIntent(R.id.flashButton, pendingSwitchIntent);
                    contentView.setTextViewText(R.id.message, mTask);
                    contentView.setTextViewText(R.id.date, mDate);
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
        });

    }
}
