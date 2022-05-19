package com.example.crudapplication;

public class ToDoModel {
    private String id, task, description, date;


    public ToDoModel() {
    }

    public ToDoModel(String id, String task, String description, String date) {
        this.id = id;
        this.task = task;
        this.description = description;
        this.date = date;
    }

    public ToDoModel(String task, String description, String date) {
        this.task = task;
        this.description = description;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTask() {
        return task;
    }

    public void setTask(String task) {
        this.task = task;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
