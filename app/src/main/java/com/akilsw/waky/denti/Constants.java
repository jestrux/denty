package com.akilsw.waky.denti;

/**
 * Created by Waky on 11/25/2017.
 */

public class Constants {
    public static String APP_FOLDER = "Denti";

    //REFERENCES TYPES
    final public static int RESOURCE_TYPE_REFERENCE = 0; //with definition
    final public static int RESOURCE_TYPE_IMAGE = 1; //youtube
    final public static int RESOURCE_TYPE_FILE = 2; //preview if possible
    final public static int RESOURCE_TYPE_LINK = 3; //preview if possible(paste link, auto get from clipboard)
    final public static int RESOURCE_TYPE_VIDEO = 4; //youtube


    //TODOS TYPES
    final public static int TODO_TYPE_STUDY = 1;
    final public static int TODO_TYPE_DISCUSSION = 2;
    final public static int TODO_TYPE_ASSIGNMENT = 3;
    final public static int TODO_TYPE_OTHER = 4;


    ////SESSION TYPES
    final public static int SESSION_TYPE_TUTORIAL = 0;
    final public static int SESSION_TYPE_LECTURE = 1;
    final public static int SESSION_TYPE_EXAM = 2;

//    SHARED PREFERENCES KEYS
    final public static String SCHEDULE_TYPE_PREF_KEY = "schedule_type_pref";
}
