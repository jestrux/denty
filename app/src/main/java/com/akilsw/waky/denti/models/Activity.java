package com.akilsw.waky.denti.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;

import com.akilsw.waky.denti.data.DentyContract;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by WAKY on 4/g3/2017.
 */
public class Activity {
    private int id;
    private String title;
    private long description;
    private long deadline;
    private long created_at;
    private int type = -1;
    private int state = 0;
    private int repeating = 0;
    private int subject_id = -1;

    public Activity() {
    }

    public Activity(int id, String title, long deadline, long created_at, int type, int state, int subject_id) {
        this.id = id;
        this.title = title;
        this.deadline = deadline;
        this.created_at = created_at;
        this.type = type;
        this.state = state;
        this.subject_id = subject_id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getDeadline() {
        return deadline;
    }

    public String getDeadlineStr() {
        try {
            Date date = new Date(deadline);
            long diffDays = (date.getTime() - Calendar.getInstance().getTimeInMillis()) / (24 * 60 * 60 * 1000);
            SimpleDateFormat diffFormat = new SimpleDateFormat(
                    "HH:mm", Locale.getDefault());
            String str;
            if(diffDays >= 0 && diffDays < 1){
                str = "Due Today at " + diffFormat.format(date) ;

                if(date.getTime() < Calendar.getInstance().getTimeInMillis()){
                    str = "Was Due Today at " + diffFormat.format(date);
                }
            }
            else if(diffDays >= 1 && diffDays <= 2){
                str = "Due Tomorrow at " + diffFormat.format(date) ;
            }
            else if(diffDays > 2){
                str = "Due on " + diffFormat.format(date) ;
            }else{
                diffFormat = new SimpleDateFormat(
                        "MMM dd, HH:mm", Locale.getDefault());

                str = "Due on " + diffFormat.format(date);

                if(diffDays < 0 ){
                    if(diffDays < -280){
                        diffFormat = new SimpleDateFormat(
                                "MMM dd", Locale.getDefault());
                    }

                    str = "Was due last " + diffFormat.format(date);
                }
            }
            return str;

        } catch (Exception e) {
            Log.e("WOURA", "Parsing datetime failed", e);
            return deadline + "";
        }
    }

    public boolean isDueToday() {
        try {
            Date date = new Date(deadline);
            long diffDays = (date.getTime() - Calendar.getInstance().getTimeInMillis()) / (24 * 60 * 60 * 1000);
            SimpleDateFormat diffFormat = new SimpleDateFormat(
                    "HH:mm", Locale.getDefault());

            return (diffDays >= 0 && diffDays < 1) &&
                    (date.getTime() > Calendar.getInstance().getTimeInMillis());
        } catch (Exception e) {
            Log.e("WOURA", "Parsing datetime failed", e);
            return false;
        }
    }

    public void setDeadline(long deadline) {
        this.deadline = deadline;
    }

    public long getCreatedAt() {
        return created_at;
    }

    public String getCreatedAtStr() {
        try {
            Date date = new Date(created_at);
            SimpleDateFormat diffFormat = new SimpleDateFormat(
                    "MMM dd, HH:mm", Locale.getDefault());
            return diffFormat.format(date);
        } catch (Exception e) {
            Log.e("WOURA", "Parsing datetime failed", e);
            return created_at + "";
        }
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setCreatedAt(long created_at) {
        this.created_at = created_at;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getSubjectId() {
        return subject_id;
    }

    public void setSubjectId(int subject_id) {
        this.subject_id = subject_id;
    }

    public int getRepeating() {
        return repeating;
    }

    public void setRepeating(int repeating) {
        this.repeating = repeating;
    }

    public static Activity fromCursor(Cursor cursor) {
        return new Activity(cursor.getInt(0),//ID
                cursor.getString(1), //title
                cursor.getLong(2), //deadline
                cursor.getLong(3), //created_at
                cursor.getInt(4), //type
                cursor.getInt(5), //state
                cursor.getInt(6) //subject_id
        );
    }

    public static Activity fromBundle(Bundle b) {
        Activity todo = new Activity();
        todo.setId(b.getInt("id"));
        todo.setTitle(b.getString("title"));
        todo.setDeadline(b.getLong("deadline"));
        todo.setCreatedAt(b.getLong("created_at"));
        todo.setType(b.getInt("type"));
        todo.setState(b.getInt("state"));
        todo.setSubjectId(b.getInt("subject_id"));

        return todo;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt("id", this.id);
        b.putString("title", this.title);
        b.putLong("deadline", this.deadline);
        b.putLong("created_at", this.created_at);
        b.putInt("type", this.type);
        b.putInt("state", this.state);
        b.putInt("subject_id", this.subject_id);
        return b;
    }

    public ContentValues toContentValues(){
        ContentValues subValues = new ContentValues();
        subValues.put(DentyContract.TodosEntry.COLUMN_TITLE, this.title);
        subValues.put(DentyContract.TodosEntry.COLUMN_DEADLINE, this.deadline);
        subValues.put(DentyContract.TodosEntry.COLUMN_CREATED_AT, this.created_at);
        subValues.put(DentyContract.TodosEntry.COLUMN_TYPE, this.type);
        subValues.put(DentyContract.TodosEntry.COLUMN_STATE, this.state);
        subValues.put(DentyContract.TodosEntry.COLUMN_SUBJECT_ID, this.subject_id);

        return subValues;
    }
}
