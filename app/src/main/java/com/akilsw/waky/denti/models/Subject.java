package com.akilsw.waky.denti.models;

import android.database.Cursor;
import android.os.Bundle;

/**
 * Created by WAKY on 4/g3/2017.
 */
public class Subject {
    private String id;
    private String name;
    private String color = null;

    public Subject() {
    }

    public Subject(String id, String name, String color) {
        this.id = id;
        this.name = name;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public static Subject fromCursor(Cursor cursor) {
        return new Subject(
                String.valueOf(cursor.getInt(0)), //id
                cursor.getString(1), //name
                cursor.getString(2) //color
        );
    }

    public Bundle toBundle() {
        Bundle b = new Bundle();
        b.putString("id", this.id);
        b.putString("name", this.name);
        b.putString("color", this.color);
        return b;
    }
}
