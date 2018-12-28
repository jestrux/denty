package com.akilsw.waky.denti.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import com.akilsw.waky.denti.Constants;
import com.akilsw.waky.denti.MoiUtils;
import com.akilsw.waky.denti.data.DentyContract;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by WAKY on 4/g3/2017.
 */
public class Resource {
    private int id;
    private String title;
    private int subject_id;
    private long created_at;
    private int type; //definition, notes, image, book, yt_video
    private String description = null; //optional in case of definition
    private ArrayList<String> files = new ArrayList<>();
    private String link_title = null;

    public Resource() {
    }

    public Resource(int id, String title, int subject_id, long created_at, int type, String description) {
        this.id = id;
        this.title = title;
        this.subject_id = subject_id;
        this.created_at = created_at;
        this.type = type;
        this.description = description;
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

    public int getSubjectId() {
        return subject_id;
    }

    public void setSubjectId(int subject_id) {
        this.subject_id = subject_id;
    }

    public long getCreatedAt() {
        return created_at;
    }

    public String[] getMeta() {
        if(this.type != Constants.RESOURCE_TYPE_LINK || this.files.size() < 1 || this.link_title == null)
            return null;

        return new String[]{this.link_title, this.files.get(0)};
    }

    public String getCreatedAtStr() {
        try {
            Date date = new Date(Long.valueOf(created_at));
            SimpleDateFormat diffFormat = new SimpleDateFormat(
                    "MMM dd, HH:mm", Locale.getDefault());
            return diffFormat.format(date);
        } catch (Exception e) {
            Log.e("WOURA", "Parsing datetime failed", e);
            return "" + created_at;
        }
    }

    public void setCreatedAt(long created_at) {
        this.created_at = created_at;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public String getYoutubeId() {
        return MoiUtils.extractYTId(description);
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void addFile(String path) {
        this.files.add(path);
    }

    public void addMeta(String title, String path) {
        this.link_title = title;
        this.files.add(path);
    }

    public ArrayList<String> getFiles() {
        return files;
    }

    public static Resource fromCursor(Cursor cursor) {
        return new Resource(cursor.getInt(0), //id
                cursor.getString(1), //title
                cursor.getInt(2), //subject_id
                cursor.getLong(3), //created_at
                cursor.getInt(4), //type
                cursor.getString(5) //description
        );
    }

    public static Resource fromBundle(Bundle b) {
        Resource resource = new Resource();
        resource.setId(b.getInt("id"));
        resource.setTitle(b.getString("title"));
        resource.setSubjectId(b.getInt("subject_id"));
        resource.setCreatedAt(b.getLong("created_at"));
        resource.setType(b.getInt("type"));
        resource.setDescription(b.getString("description"));

        return resource;
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putInt("id", this.id);
        b.putString("title", this.title);
        b.putInt("subject_id", this.subject_id);
        b.putLong("created_at", this.created_at);
        b.putInt("type", this.type);
        b.putString("description", this.description);
        return b;
    }

    public ContentValues toContentValues(){
        ContentValues subValues = new ContentValues();
        subValues.put(DentyContract.ReferencesEntry.COLUMN_TITLE, this.title);
        subValues.put(DentyContract.ReferencesEntry.COLUMN_SUBJECT_ID, this.subject_id);
        subValues.put(DentyContract.ReferencesEntry.COLUMN_CREATED_AT, this.created_at);
        subValues.put(DentyContract.ReferencesEntry.COLUMN_TYPE, this.type);
        subValues.put(DentyContract.ReferencesEntry.COLUMN_DESCRIPTION, this.description);

        return subValues;
    }
}
