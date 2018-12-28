package com.akilsw.waky.denti.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;

import com.akilsw.waky.denti.MoiUtils;
import com.akilsw.waky.denti.data.DentyContract.SessionEntry;

/**
 * Created by WAKY on 4/g3/2017.
 */
public class Session {
    private int id;
    private String venue;
    private int type; //e.g lecture, seminar, tutorial
    private int day;
    private long start_time;
    private long end_time;
    private int subject_id;
    private String subject_name;

    public Session() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getStartTime() {
        return start_time;
    }

    public String getStartTimeStr() {
        return MoiUtils.formatSessionTime(start_time);
    }

    public void setStartTime(long start_time) {
        this.start_time = start_time;
    }

    public long getEndTime() {
        return end_time;
    }

    public String getEndTimeStr() {
        return MoiUtils.formatSessionTime(end_time);
    }

    public void setEndTime(long end_time) {
        this.end_time = end_time;
    }

    public String getFullTimeStr() {
        return getStartTimeStr() + " - " + getEndTimeStr();
    }

    public int getSubjectId() {
        return subject_id;
    }

    public void setSubjectId(int subject_id) {
        this.subject_id = subject_id;
    }

    public String getSubjectName() {
        return subject_name;
    }

    public void setSubjectName(String subject_name) {
        this.subject_name = subject_name;
    }

    public int getDay() {
        return day;
    }

    public boolean dueOn(int day) {
        return this.day == day;
    }

    public String getDayName() {
        return MoiUtils.dayNameFromId(day, false);
    }

    public String getShortDayName() {
        return MoiUtils.dayNameFromId(day, true);
    }

    public void setDay(int day) {
        this.day = day;
    }

    public Session(int id, int day, String venue, int type, long start_time, long end_time, int subject_id, String subject_name) {
        this.id = id;
        this.day = day;
        this.venue = venue;
        this.type = type;
        this.start_time = start_time;
        this.end_time = end_time;
        this.subject_id = subject_id;
        this.subject_name = subject_name;
    }

    public static Session fromCursor(Cursor cursor) {
        return new Session(cursor.getInt(0), //id
                cursor.getInt(1), //day
                cursor.getString(2), //venue
                cursor.getInt(3), //type
                cursor.getLong(4), //start_time
                cursor.getLong(5), //end_time
                cursor.getInt(6), //subject_id
                cursor.getString(7) //subject_name
        );
    }

    public static Session fromBundle(Bundle b) {
        Session session = new Session();
        session.setId(b.getInt("id"));
        session.setSubjectId(b.getInt("subject_id"));
        session.setSubjectName(b.getString("subject_name"));
        session.setDay(b.getInt("day"));
        session.setType(b.getInt("type"));
        session.setVenue(b.getString("venue"));
        session.setStartTime(b.getLong("start_time"));
        session.setEndTime(b.getLong("end_time"));

        return session;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt("id", this.id);
        b.putInt("day", this.day);
        b.putString("venue", this.venue);
        b.putInt("type", this.type);
        b.putInt("subject_id", this.subject_id);
        b.putString("subject_name", this.subject_name);
        b.putLong("start_time", this.start_time);
        b.putLong("end_time", this.end_time);
        return b;
    }

    public ContentValues toContentValues(){
        ContentValues subValues = new ContentValues();
        subValues.put(SessionEntry.COLUMN_DAY, this.day);
        subValues.put(SessionEntry.COLUMN_VENUE, this.venue);
        subValues.put(SessionEntry.COLUMN_TYPE, this.type);
        subValues.put(SessionEntry.COLUMN_SUBJECT_ID, this.subject_id);
        subValues.put(SessionEntry.COLUMN_START_TIME, this.start_time);
        subValues.put(SessionEntry.COLUMN_END_TIME, this.end_time);

        return subValues;
    }
}
