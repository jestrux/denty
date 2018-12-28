package com.akilsw.waky.denti.data;

import android.provider.BaseColumns;

import java.util.HashMap;

/**
 * Created by WAKY on 2/19/2017.
 */
public class DentyContract {
    public static final class SubjecstEntry implements BaseColumns {

        public static final String TABLE_NAME = "denty_subjects";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_COLOR = "color";

        public static final String[] PROJECTION = {
                "_ID AS _id",
                COLUMN_NAME,
                COLUMN_COLOR
        };
    }

    public static final class SessionEntry implements BaseColumns {

        public static final String TABLE_NAME = "denty_schedule";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_DAY ="day";
        public static final String COLUMN_VENUE ="venue";
        public static final String COLUMN_TYPE ="type"; //e.g lecture, seminar, tutorial
        public static final String COLUMN_START_TIME ="start_time";
        public static final String COLUMN_END_TIME ="end_time";
        public static final String COLUMN_SUBJECT_ID ="subject_id";
        public static final String COLUMN_SUBJECT_NAME ="subject_name";

        public static final String[] PROJECTION_NEW = {
                "_ID AS _id",
                SessionEntry.TABLE_NAME + "." + COLUMN_DAY + " AS " + COLUMN_DAY,
                SessionEntry.TABLE_NAME + "." + COLUMN_VENUE + " AS " + COLUMN_VENUE,
                SessionEntry.TABLE_NAME + "." + COLUMN_TYPE + " AS " + COLUMN_TYPE,
                SessionEntry.TABLE_NAME + "." + COLUMN_START_TIME + " AS " + COLUMN_START_TIME,
                SessionEntry.TABLE_NAME + "." + COLUMN_END_TIME + " AS " + COLUMN_END_TIME,
                SessionEntry.TABLE_NAME + "." + COLUMN_SUBJECT_ID + " AS " + COLUMN_SUBJECT_ID,
                SubjecstEntry.TABLE_NAME + "." + SubjecstEntry.COLUMN_NAME + " AS " + COLUMN_SUBJECT_NAME
        };

        public static final String[] PROJECTION = {
                "_id",
                COLUMN_DAY,
                COLUMN_VENUE,
                COLUMN_TYPE,
                COLUMN_START_TIME,
                COLUMN_END_TIME,
                COLUMN_SUBJECT_ID,
                COLUMN_SUBJECT_NAME
        };

        public static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put("_id", SessionEntry.TABLE_NAME + "._id");
            PROJECTION_MAP.put(COLUMN_DAY, SessionEntry.TABLE_NAME + "." + COLUMN_DAY);
            PROJECTION_MAP.put(COLUMN_VENUE, SessionEntry.TABLE_NAME + "." + COLUMN_VENUE);
            PROJECTION_MAP.put(COLUMN_TYPE, SessionEntry.TABLE_NAME + "." + COLUMN_TYPE);
            PROJECTION_MAP.put(COLUMN_START_TIME, SessionEntry.TABLE_NAME + "." + COLUMN_START_TIME);
            PROJECTION_MAP.put(COLUMN_END_TIME, SessionEntry.TABLE_NAME + "." + COLUMN_END_TIME);
            PROJECTION_MAP.put(COLUMN_SUBJECT_ID, SessionEntry.TABLE_NAME + "." + COLUMN_SUBJECT_ID);
            PROJECTION_MAP.put(COLUMN_SUBJECT_NAME,
                    SubjecstEntry.TABLE_NAME + "." + SubjecstEntry.COLUMN_NAME + " AS " + COLUMN_SUBJECT_NAME);
        }
    }

    public static final class TodosEntry implements BaseColumns {

        public static final String TABLE_NAME = "denty_todos";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DEADLINE ="deadline"; //optional
        public static final String COLUMN_TYPE ="type"; // optional e.g study, discussion, assignment, other
        public static final String COLUMN_SUBJECT_ID ="subject_id"; //optional
        public static final String COLUMN_SUBJECT_NAME ="subject_name";
        public static final String COLUMN_CREATED_AT ="created_at";
        public static final String COLUMN_REPEATING = "repeating";
        public static final String COLUMN_STATE ="state";

        public static final String[] PROJECTION = {
                "_ID AS _id",
                COLUMN_TITLE,
                COLUMN_DEADLINE,
                COLUMN_CREATED_AT,
                COLUMN_TYPE,
                COLUMN_STATE,
                COLUMN_SUBJECT_ID,
//                COLUMN_REPEATING,
        };

        public static final String[] MINI_PROJECTION = {
                "_ID AS _id",
                COLUMN_TITLE,
                COLUMN_DEADLINE
        };
    }

    public static final class ReferencesEntry implements BaseColumns {

        public static final String TABLE_NAME = "denty_references";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_SUBJECT_ID ="subject_id";
        public static final String COLUMN_CREATED_AT ="created_at";
        public static final String COLUMN_TYPE = "type"; //definition, notes, image, book, yt_video
        public static final String COLUMN_DESCRIPTION ="description"; //definition, file path, youtube url
        public static final String COLUMN_PATH ="path";
        public static final String COLUMN_LINK_NAME ="link_name";

        public static final String[] PROJECTION = {
                TABLE_NAME + "._ID AS _id",
                COLUMN_TITLE,
                COLUMN_SUBJECT_ID,
                COLUMN_CREATED_AT,
                COLUMN_TYPE,
                COLUMN_DESCRIPTION,
                COLUMN_PATH,
                COLUMN_LINK_NAME
        };

        public static final HashMap<String, String> PROJECTION_MAP;

        static {
            PROJECTION_MAP = new HashMap<String, String>();
            PROJECTION_MAP.put("_id", ReferencesEntry.TABLE_NAME + "._id");
            PROJECTION_MAP.put(COLUMN_TITLE, ReferencesEntry.TABLE_NAME + "." + COLUMN_TITLE);
            PROJECTION_MAP.put(COLUMN_SUBJECT_ID, ReferencesEntry.TABLE_NAME + "." + COLUMN_SUBJECT_ID);
            PROJECTION_MAP.put(COLUMN_CREATED_AT, ReferencesEntry.TABLE_NAME + "." + COLUMN_CREATED_AT);
            PROJECTION_MAP.put(COLUMN_TYPE, ReferencesEntry.TABLE_NAME + "." + COLUMN_TYPE);
            PROJECTION_MAP.put(COLUMN_DESCRIPTION, ReferencesEntry.TABLE_NAME + "." + COLUMN_DESCRIPTION);
            PROJECTION_MAP.put(COLUMN_PATH,
                    FilesEntry.TABLE_NAME + "." + FilesEntry.COLUMN_PATH + " AS " + COLUMN_PATH);
            PROJECTION_MAP.put(COLUMN_LINK_NAME,
                    FilesEntry.TABLE_NAME + "." + FilesEntry.COLUMN_NAME + " AS " + COLUMN_LINK_NAME);
        }
    }

    public static final class FilesEntry implements BaseColumns {

        public static final String TABLE_NAME = "denty_reference_files";

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_NAME = "file_name";
        public static final String COLUMN_PATH = "path";
        public static final String COLUMN_RESOURCE_ID = "resource_id";
        public static final String COLUMN_IDX = "idx";

        public static final String[] PROJECTION = {
                "_ID AS _id",
                COLUMN_PATH,
                COLUMN_RESOURCE_ID,
                COLUMN_IDX
        };
    }
}
