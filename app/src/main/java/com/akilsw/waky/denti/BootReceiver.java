package com.akilsw.waky.denti;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;

import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Activity;
import com.akilsw.waky.denti.models.Session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

/**
 * Created by Waky on 1/2/2018.
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction() == null)
            return;

        if(intent.getAction().equals("android.intent.action.BOOT_COMPLETED")){
            setDigestNotifiers(context);
            setSessionNotifiers(context);
            setActivitiesNotifiers(context);
        }else if(Objects.equals(intent.getAction(), "com.akilsw.waky.daily_digest_receiver")){
            showDigestNotification(context);
        }
    }

    private void setDigestNotifiers(Context context) {
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);

        Intent intent = new Intent(context, BootReceiver.class);
        intent.setAction("com.akilsw.waky.daily_digest_receiver");

        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if(am != null){
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pi);
            Log.d("WOURA", "Daily digest alarm is set.");
        }else{
            Log.d("WOURA", "Daily digest alarm not set, manager is null.");
        }
    }

    private void setSessionNotifiers(Context context) {
        Log.d("WOURA", "Setting Session Notifiers!");
        SQLiteQueryBuilder _QB;
        ArrayList<Session> sessionsArrayList = new ArrayList<>();
        DentyDbHelper dbhelper = new DentyDbHelper(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor fetchCursor;
        String tables_str = DentyContract.SessionEntry.TABLE_NAME +
                " LEFT OUTER JOIN " + DentyContract.SubjecstEntry.TABLE_NAME +
                " ON " + DentyContract.SessionEntry.TABLE_NAME + "." + DentyContract.SessionEntry.COLUMN_SUBJECT_ID +
                " = " + DentyContract.SubjecstEntry.TABLE_NAME + "." + DentyContract.SubjecstEntry._ID;

//            try {
        _QB = new SQLiteQueryBuilder();
        _QB.setTables(tables_str);

        _QB.setProjectionMap(DentyContract.SessionEntry.PROJECTION_MAP);
        String _OrderBy = DentyContract.SessionEntry.COLUMN_DAY + ", " + DentyContract.SessionEntry.COLUMN_START_TIME + " ASC";

//                fetchCursor = _QB.query(database, DentyContract.SessionEntry.PROJECTION, null, null, null, null, _OrderBy);
        fetchCursor = _QB.query(database, DentyContract.SessionEntry.PROJECTION, null, null, null, null, _OrderBy);

        sessionsArrayList.clear();

        if (fetchCursor == null)
            return;

        Log.d("WOURA", "Sessions fetched in Receiver: Found " + fetchCursor.getCount() + " Sessions");

        if(fetchCursor.getCount() > 0){
            fetchCursor.moveToFirst();
            while(!fetchCursor.isAfterLast()) {
                Session session = Session.fromCursor(fetchCursor);
                sessionsArrayList.add(session);
                fetchCursor.moveToNext();
            }

            Calendar cal = Calendar.getInstance(Locale.getDefault());
            int calendarDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
            int curDayIdx = calendarDayOfWeek - 2;
            if (curDayIdx < 0) {
                curDayIdx += 7;
            }

            for (Session session : sessionsArrayList){
                if(session.dueOn(curDayIdx)){
                    Log.d("WOURA", "Valid Session.");
                    Calendar calendar = Calendar.getInstance();
                    Date d = new Date(session.getStartTime());
                    calendar.setTime(d);

                    AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                    Intent intent = new Intent(context, ActivityReceiver.class);
                    intent.putExtras(session.toBundle());
                    intent.setAction("com.akilsw.waky.session_receiver");

                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                    if(am != null){
                        am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 604800000L,  pi);
                        Log.d("WOURA", "Alarm set for " + d.getHours() + " : " + d.getMinutes());
                    }else{
                        Log.d("WOURA", "Alarm not set, manager is null.");
                    }
                }
            }
        }
//            } catch (Exception e) {
//                Log.d("WOURA", "Error fetching data. \n\n " + e.getMessage());
//            }
    }

    private void setActivitiesNotifiers(Context context) {
        Log.d("WOURA", "Setting Activity Notifiers!");

        ArrayList<Activity> activitiesArrayList = new ArrayList<>();
        DentyDbHelper dbhelper = new DentyDbHelper(context);
        SQLiteDatabase database = dbhelper.getReadableDatabase();

        Cursor c = database.query(
                DentyContract.TodosEntry.TABLE_NAME,
                DentyContract.TodosEntry.PROJECTION,
                null,
                null, null, null, DentyContract.TodosEntry.COLUMN_DEADLINE + " ASC", null
        );

        if(c == null)
            return;

        if(c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Activity activity = Activity.fromCursor(c);
                activitiesArrayList.add(activity);
                c.moveToNext();

                Calendar calendar = Calendar.getInstance();
                Date d = new Date(activity.getDeadline());
                calendar.setTime(d);

                Intent intent = new Intent(context, ActivityReceiver.class);
                intent.putExtras(activity.toBundle());
                intent.setAction("com.akilsw.waky.activity_receiver");

                PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

                if(am != null){
                    am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),  pi);
                    Log.d("WOURA", "Alarm set for " + d.getHours() + " : " + d.getMinutes());
                }else{
                    Log.d("WOURA", "Alarm not set, manager is null.");
                }
            }
        }
    }

    public void showDigestNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.denty_logo_circle, null);
        Bitmap myLogo = ((BitmapDrawable) vectorDrawable).getBitmap();

        Intent i = new Intent(context, MainActivity.class);
//        i.putExtra("page", R.id.tab_todos);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(context);
        builder.setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_check))
                .setSmallIcon(R.drawable.denty_logo_circle)
                .setContentTitle("Denty")
                .setContentText("Your daily digest is ready.")
                .setAutoCancel(true);
        builder.setSound(alarmSound);
        notificationManager.notify(100, builder.build());
    }
}