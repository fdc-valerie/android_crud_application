package com.example.crudapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AlarmReceiver extends BroadcastReceiver {
    private DatabaseReference reference;
    private String taskID, userID, mTask, mDate, mDescription, topic;

    final private String FCM_API = "https://fcm.googleapis.com/fcm/send";
    final private String serverKey = "key=" + "AAAApzVeTKs:APA91bEsti7GgrE1VBro-JsxcGuypGDi9kp-3Zq-7pedkl6V5DDzRtMw-WXGdXVOququWavsFvjGtD0tBVNdYfWV503h5pgA1Wc5pM9ONElM0TW9589dHGjFOWlpFqLMrcNTU1Kf5M-W";
    final private String contentType = "application/json";
    final String TAG = "NOTIFICATION TAG";

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

                    topic = "/topics/alarmTask" + userID;
                    JSONObject notification = new JSONObject();
                    JSONObject notificationBody = new JSONObject();

                    Log.e(TAG, "topic: " + topic);

                    try {
                        notificationBody.put("title", mTask);
                        notificationBody.put("message", mDescription);
                        notificationBody.put("date", mDate);
                        notification.put("to", topic);
                        notification.put("data", notificationBody);

                        Log.e(TAG, "topic notification" + notification);
                    } catch (JSONException e) {
                        Log.e(TAG, "onCreate: " + e.getMessage());
                    }
                    sendNotification(notification, context);
                }
            }
        });

    }

    private void sendNotification(JSONObject notification, Context context) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(FCM_API, notification,
                new Response.Listener() {
                    @Override
                    public void onResponse(Object response) {
                        Log.i(TAG, "onResponse: " + response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.i(TAG, "onErrorResponse: Didn't work");
                    }
                }){
            @Override
            public Map getHeaders() throws AuthFailureError {
                Map params = new HashMap<>();
                params.put("Authorization", serverKey);
                params.put("Content-Type", contentType);
                return params;
            }
        };
        MySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(jsonObjectRequest);
    }
}
