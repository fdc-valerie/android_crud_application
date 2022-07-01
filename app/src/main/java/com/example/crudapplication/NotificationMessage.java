package com.example.crudapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class NotificationMessage extends AppCompatActivity {
    private TextView task, description, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_message);

        task = (TextView) findViewById(R.id.tvTask);
        description = (TextView) findViewById(R.id.tvDescription);
        date = (TextView) findViewById(R.id.tvDate);

        Bundle bundle = getIntent().getExtras();
        task.setText(bundle.getString("task"));
        description.setText(bundle.getString("description"));
        date.setText(bundle.getString("date"));
    }
}