package com.example.crudapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("");

        Bundle bundle = getIntent().getExtras();
        task.setText(bundle.getString("task"));
        description.setText(bundle.getString("description"));
        date.setText(bundle.getString("date"));
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }
}