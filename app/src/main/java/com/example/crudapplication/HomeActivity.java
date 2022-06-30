package com.example.crudapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.crudapplication.databinding.ActivityMainBinding;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;
    private Button cancelBtn, saveBtn, selectTime;

    private DatabaseReference reference;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String userID;

    private ProgressDialog loader;
    private EditText etTask, etDescription;

    private String key = "";
    private String task;
    private String description;
    private String previousTimeToNotify;

    private ActivityMainBinding binding;
    private MaterialTimePicker picker;
    private Calendar calendar;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private String timeTonotify;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        createNotificationChannel();

        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        loader = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        userID = mUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(userID);

        floatingActionButton = (FloatingActionButton) findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addTask();
            }
        });


    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "todoReminderChannel";
            String description = "Channel for Alarm Manager";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("todo", name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void addTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);

        View myView = inflater.inflate(R.layout.input_file, null);
        myDialog.setView(myView);

        final AlertDialog dialog = myDialog.create();
        dialog.setCancelable(false);

        cancelBtn = (Button)myView.findViewById(R.id.cancelBtn);
        saveBtn = (Button)myView.findViewById(R.id.saveBtn);
        selectTime = (Button)myView.findViewById(R.id.selectTime);

        etTask = (EditText)myView.findViewById(R.id.task);
        etDescription =  (EditText)myView.findViewById(R.id.description);

        selectTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTimePicker();
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mTask = etTask.getText().toString();
                String mDescription = etDescription.getText().toString();
                String id = reference.push().getKey();
                String date = DateFormat.getDateInstance().format(new Date());

                if (TextUtils.isEmpty(mTask)) {
                    etTask.setError("Task required");
                    return;
                }

                if (TextUtils.isEmpty(mDescription)) {
                    etDescription.setError("Description required");
                    return;
                } else {
                    loader.setMessage("Adding your data");
                    loader.setCanceledOnTouchOutside(false);
                    loader.show();
                    //setAlarm();

                    String dateAndTime = date + " " + timeTonotify;

                    ToDoModel model = new ToDoModel(id, mTask, mDescription, dateAndTime);
                    reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "Task successfully added", Toast.LENGTH_SHORT).show();
                            } else {
                                String error = task.getException().toString();
                                Toast.makeText(HomeActivity.this, "Fail to add Task", Toast.LENGTH_SHORT).show();
                            }

                            loader.dismiss();
                        }
                    });
                }
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void setAlarm(String dateTime, String task, String description) {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(HomeActivity.this, AlarmReceiver.class);
        intent.putExtra("task", task);
        intent.putExtra("description", description);
        intent.putExtra("date", dateTime);

        int pendingFlags;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingFlags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            pendingFlags = PendingIntent.FLAG_ONE_SHOT;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, pendingFlags);

        DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm");
        try {
            Date date1 = formatter.parse(dateTime);
            am.set(AlarmManager.RTC_WAKEUP, date1.getTime(), pendingIntent);

            Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int i, int i1) {
                timeTonotify = i + ":" + i1;
                selectTime.setText(FormatTime(i, i1));
            }
        }, hour, minute, false);
        timePickerDialog.show();
    }

    public String FormatTime(int hour, int minute) {
        String time;
        time = "";
        String formattedMinute;

        if (minute / 10 == 0) {
            formattedMinute = "0" + minute;
        } else {
            formattedMinute = "" + minute;
        }

        if (hour == 0) {
            time = "12" + ":" + formattedMinute + " AM";
        } else if (hour < 12) {
            time = hour + ":" + formattedMinute + " AM";
        } else if (hour == 12) {
            time = "12" + ":" + formattedMinute + " PM";
        } else {
            int temp = hour - 12;
            time = temp + ":" + formattedMinute + " PM";
        }

        return time;
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<ToDoModel> options = new FirebaseRecyclerOptions.Builder<ToDoModel>()
                .setQuery(reference, ToDoModel.class)
                .build();

        FirebaseRecyclerAdapter<ToDoModel, MyViewHolder> adapter = new FirebaseRecyclerAdapter<ToDoModel, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, int position, @NonNull final ToDoModel model) {
                final int pos = position;
                holder.setDate(model.getDate());
                holder.setTask(model.getTask());
                holder.setDescription(model.getDescription());

                String alarmDate = model.getDate();
                String dataTask = model.getTask();
                String dataDescription = model.getDescription();

                if (!TextUtils.isEmpty(alarmDate)) {
                    setAlarm(alarmDate, dataTask, dataDescription);
                }

                holder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(pos).getKey();
                        task = model.getTask();
                        description = model.getDescription();
                        previousTimeToNotify = model.getDate();

                        updateTask();
                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.retrieve_todo_list, parent, false);
                return new MyViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void updateTask() {
        AlertDialog.Builder myDialog = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.update_data, null);
        myDialog.setView(view);

        AlertDialog dialog = myDialog.create();

        final EditText editTask = view.findViewById(R.id.edittask);
        EditText editDescription = view.findViewById(R.id.editdescription);

        editTask.setText(task);
        editTask.setSelection(task.length());


        editDescription.setText(description);
        editDescription.setSelection(description.length());

        Button deleteBtn = view.findViewById(R.id.deleteBtn);
        Button updateBtn = view.findViewById(R.id.updateBtn);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                       if (task.isSuccessful())  {
                           Toast.makeText(HomeActivity.this, "Task successfully deleted", Toast.LENGTH_SHORT).show();
                            cancelAlarm();
                       } else {
                           String err = task.getException().toString();
                           Toast.makeText(HomeActivity.this, "Failed to delete task "+err, Toast.LENGTH_SHORT).show();
                       }
                    }
                });
                dialog.dismiss();
            }
        });

        updateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                task = editTask.getText().toString().trim();
                description = editDescription.getText().toString().trim();
                String date = DateFormat.getDateInstance().format(new Date());


                if (TextUtils.isEmpty(task)) {
                    editTask.setError("Task required");
                    return;
                }

                if (TextUtils.isEmpty(description)) {
                    editDescription.setError("Description required");
                    return;
                } else {

                    String dateAndTime = date + " " + timeTonotify;

                    if (TextUtils.isEmpty(timeTonotify)) {
                        dateAndTime = previousTimeToNotify;
                    }

                    ToDoModel model = new ToDoModel(task, description, dateAndTime);
                    reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "Task has been updated successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                String err = task.getException().toString();
                                Toast.makeText(HomeActivity.this, "Update failed " + err, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    private void cancelAlarm() {
        Intent intent = new Intent(this, AlarmReceiver.class);


        int pendingFlags;
        if (Build.VERSION.SDK_INT >= 23) {
            pendingFlags = PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE;
        } else {
            pendingFlags = PendingIntent.FLAG_ONE_SHOT;
        }
        pendingIntent = PendingIntent.getBroadcast(this, 0, intent, pendingFlags);

        if (alarmManager == null) {
            alarmManager = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        }

        alarmManager.cancel(pendingIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                mAuth.signOut();
                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        View mView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTask(String task) {
            TextView taskTextView = mView.findViewById(R.id.tvTask);
            taskTextView.setText(task);
        }

        public void setDescription (String desc) {
            TextView descriptionTextView = mView.findViewById(R.id.tvDescription);
            descriptionTextView.setText(desc);
        }

        public void setDate (String date) {
            TextView dateTextView = mView.findViewById(R.id.tvDate);
            dateTextView.setText(date);
        }


    }
}