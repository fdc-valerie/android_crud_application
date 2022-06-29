package com.example.crudapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

public class NotificationMessage extends AppCompatActivity {
    private TextView task, description, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification_message);

        task = findViewById(R.id.tvTask);
        description = findViewById(R.id.tvDescription);
        date = findViewById(R.id.date);

        Bundle bundle = getIntent().getExtras();
        task.setText(bundle.getString("task"));


    }
}