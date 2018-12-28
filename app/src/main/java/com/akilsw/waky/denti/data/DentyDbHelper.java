package com.akilsw.waky.denti.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.akilsw.waky.denti.data.DentyContract.*;

/**
 * Created by WAKY on 2/19/2017.
 */
public class DentyDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 19;
    public static final String DATABASE_NAME = "denty_test.db";


    public DentyDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SUBJECTS_TABLE =
                "CREATE TABLE IF NOT EXISTS " + SubjecstEntry.TABLE_NAME + " ( \n" +
                        SubjecstEntry._ID + " INTEGER PRIMARY KEY,\n " +
                        SubjecstEntry.COLUMN_NAME + " varchar(255) NOT NULL, \n " +
                        SubjecstEntry.COLUMN_COLOR + " varchar(50));";
        sqLiteDatabase.execSQL(SQL_CREATE_SUBJECTS_TABLE);

        final String SQL_CREATE_SCHEDULE_TABLE =
                "CREATE TABLE IF NOT EXISTS " + SessionEntry.TABLE_NAME + " ( \n" +
                        SessionEntry._ID + " INTEGER PRIMARY KEY, \n " +
                        SessionEntry.COLUMN_DAY + " INTEGER, \n " +
                        SessionEntry.COLUMN_VENUE + " varchar(50), \n " +
                        SessionEntry.COLUMN_SUBJECT_ID + " INTEGER NOT NULL, \n " +
                        SessionEntry.COLUMN_START_TIME + " INTEGER NOT NULL, \n " +
                        SessionEntry.COLUMN_END_TIME + " INTEGER NOT NULL, \n " +
                        SessionEntry.COLUMN_TYPE + " INTEGER DEFAULT 0, \n " +
                        "FOREIGN KEY (" + SessionEntry.COLUMN_SUBJECT_ID + ") \n " +
                        "REFERENCES " + SubjecstEntry.TABLE_NAME + " (" + SubjecstEntry._ID + ")" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_SCHEDULE_TABLE);

        final String SQL_CREATE_TODOS_TABLE =
                "CREATE TABLE IF NOT EXISTS " + TodosEntry.TABLE_NAME + " ( \n " +
                        TodosEntry._ID + " INTEGER PRIMARY KEY, " +
                        TodosEntry.COLUMN_TITLE + " varchar(255) NOT NULL, \n " +
                        TodosEntry.COLUMN_DESCRIPTION + " TEXT, \n " +
                        TodosEntry.COLUMN_DEADLINE + " DATETIME DEFAULT CURRENT_TIMESTAMP, \n " +
                        TodosEntry.COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                        TodosEntry.COLUMN_TYPE + " INTEGER, \n " +
                        TodosEntry.COLUMN_STATE + " INTEGER DEFAULT 1, \n " +
                        TodosEntry.COLUMN_REPEATING + " INTEGER DEFAULT 0, \n " +
                        SessionEntry.COLUMN_SUBJECT_ID + " INTEGER NOT NULL, \n " +
                        "FOREIGN KEY (" + SessionEntry.COLUMN_SUBJECT_ID + ") \n " +
                        "REFERENCES " + SubjecstEntry.TABLE_NAME + " (" + SubjecstEntry._ID + ")" +
                ");";
        sqLiteDatabase.execSQL(SQL_CREATE_TODOS_TABLE);


        final String SQL_CREATE_REFERENCES_TABLE =
                "CREATE TABLE IF NOT EXISTS " + ReferencesEntry.TABLE_NAME + " ( \n " +
                        ReferencesEntry._ID + " INTEGER PRIMARY KEY, \n " +
                        ReferencesEntry.COLUMN_TITLE + " varchar(255) NOT NULL, \n " +
                        ReferencesEntry.COLUMN_SUBJECT_ID + " INTEGER, \n " +
                        ReferencesEntry.COLUMN_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP, \n" +
                        ReferencesEntry.COLUMN_TYPE + " INTEGER, \n " +
                        ReferencesEntry.COLUMN_DESCRIPTION + " TEXT, \n " +
                        "FOREIGN KEY (" + ReferencesEntry.COLUMN_SUBJECT_ID + ") \n " +
                        "REFERENCES " + SubjecstEntry.TABLE_NAME + " (" + SubjecstEntry._ID + ")" +
                        ");";
        sqLiteDatabase.execSQL(SQL_CREATE_REFERENCES_TABLE);

        final String SQL_CREATE_FILES_TABLE =
                "CREATE TABLE IF NOT EXISTS " + FilesEntry.TABLE_NAME + " ( \n" +
                        FilesEntry._ID + " INTEGER PRIMARY KEY,\n " +
                        FilesEntry.COLUMN_NAME + " varchar(255)," +
                        FilesEntry.COLUMN_PATH + " TEXT  NOT NULL," +
                        FilesEntry.COLUMN_RESOURCE_ID + " INTEGER NOT NULL, \n" +
                        FilesEntry.COLUMN_IDX + " INTEGER," +
                        "FOREIGN KEY (" + FilesEntry.COLUMN_RESOURCE_ID +")\n" +
                        "REFERENCES " + ReferencesEntry.TABLE_NAME + " (" + ReferencesEntry._ID + ")" +
                        ");";
        sqLiteDatabase.execSQL(SQL_CREATE_FILES_TABLE);

        Log.i("WOURA", "Tables created!");
        Log.i("WOURA", "SUBJECTS SQL: " + SQL_CREATE_SUBJECTS_TABLE + "\n\n");
        Log.i("WOURA", "SCHEDULE SQL: " + SQL_CREATE_SCHEDULE_TABLE + "\n\n");
        Log.i("WOURA", "TODOS SQL: " + SQL_CREATE_TODOS_TABLE + "\n\n");
        Log.i("WOURA", "REFERENCES SQL: " + SQL_CREATE_REFERENCES_TABLE + "\n\n");
        Log.i("WOURA", "FILES SQL: " + SQL_CREATE_FILES_TABLE + "\n\n");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ SubjecstEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ SessionEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ TodosEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ ReferencesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ FilesEntry.TABLE_NAME);
        Log.i("WOURA", "Tables dropped!");

        onCreate(sqLiteDatabase);
    }
}