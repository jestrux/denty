package com.akilsw.waky.denti.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.akilsw.waky.denti.ActivityReceiver;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.adapters.SessionAdapter;
import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Session;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.akilsw.waky.denti.ui.ScheduleFragment.SCHEDULE_TYPE_FULL;
import static com.akilsw.waky.denti.ui.ScheduleFragment.SCHEDULE_TYPE_SINGLE;

/**
 * Created by Waky on 11/27/2017.
 */

public class SessionsFragment extends Fragment implements ScheduleFragment.ScheduleListener {
    /*TODO
    * -INTRODUCE DAY SEPARATORS INCASE OF FULL SCHEDULE
    * -Filter Sessions By both day and time
    **/

    public static String ARG_CUR_DAY_IDX = "cur_day_idx";
    public static String ARG_CUR_TYPE_IDX = "cur_type_idx";

    private SQLiteQueryBuilder _QB;
    private ArrayList<Session> mFullScheduleArrayList = new ArrayList<>();
    private SQLiteDatabase database;
    ArrayList<Session> mScheduleArrayList = new ArrayList<>();
    Cursor mCursor;
    private SessionAdapter sessionAdapter;

    private int curTypeIdx = SCHEDULE_TYPE_SINGLE;
    private int curDayIdx = 0;

    @BindView(R.id.sessions_rview)
    RecyclerView mSessionsRecyclerView;

    @BindView(R.id.no_sessions_tview)
    TextView noSessionsTextView;

    public SessionsFragment() {
    }

    public static SessionsFragment newInstance(int type, int day) {
        SessionsFragment f = new SessionsFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CUR_DAY_IDX, day);
        args.putInt(ARG_CUR_TYPE_IDX, type);
        f.setArguments(args);
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_sessions, container, false);
        ButterKnife.bind(this,view);

        curDayIdx = getArguments().getInt(ARG_CUR_DAY_IDX);
        curTypeIdx = getArguments().getInt(ARG_CUR_TYPE_IDX);

        new FetchSessionsTask().execute();

        return view;
    }

    private Cursor querySchedule(){
        return database.query(
                DentyContract.SessionEntry.TABLE_NAME,
                DentyContract.SessionEntry.PROJECTION,
                null,
                null, null, null, null, null
        );
    }

    public void setData(Cursor c){
        mFullScheduleArrayList.clear();

        if(c.getCount() > 0){
            c.moveToFirst();
            while(!c.isAfterLast()) {
                Session session = Session.fromCursor(c);
                mFullScheduleArrayList.add(session);
                c.moveToNext();
            }
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mSessionsRecyclerView.setLayoutManager(layoutManager);
        sessionAdapter = new SessionAdapter(getContext(),mFullScheduleArrayList, curTypeIdx);
        mSessionsRecyclerView.setAdapter(sessionAdapter);

        setSchedule();
    }

    private void sortSessions(){
//        Collections.sort(mScheduleArrayList, new Comparator<Session>() {
//            @Override
//            public int compare(Session s1, Session s2) {
//                int dayCmp = Integer.compare(s1.getDay(), s2.getDay());
//                if (dayCmp != 0) {
//                    return dayCmp;
//                }
//
//                return Long.compare(s1.getStartTime(), s2.getStartTime());
//            }
//        });
    }

    private void setSchedule(){
        if(curTypeIdx == SCHEDULE_TYPE_FULL){
            Log.d("WOURA", "Setting full schedule");
            mScheduleArrayList = mFullScheduleArrayList;
            sortSessions();

            sessionAdapter.updateItems(mScheduleArrayList);

            if(mScheduleArrayList.isEmpty()){
                noSessionsTextView.setText("No sessions found.");
                mSessionsRecyclerView.setVisibility(View.GONE);
                noSessionsTextView.setVisibility(View.VISIBLE);
            }else{
                mSessionsRecyclerView.setVisibility(View.VISIBLE);
                noSessionsTextView.setVisibility(View.GONE);
            }
        }else
            setDaySchedule();
    }

    public void setDaySchedule() {
        Log.d("WOURA", "Setting single day schedule!");
        ArrayList<Session> day_sessions = new ArrayList<>();

        for (Session tune : mFullScheduleArrayList) {
            if (tune.getDay() == curDayIdx) {
                day_sessions.add(tune);
            }
        }

        if(day_sessions.isEmpty()){
            String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
            String day = days[curDayIdx];
            noSessionsTextView.setText(Html.fromHtml("No sessions found on " + "<b>"+day+"</b>."));

            noSessionsTextView.setVisibility(View.VISIBLE);
            mSessionsRecyclerView.setVisibility(View.GONE);
        }else{
            noSessionsTextView.setVisibility(View.GONE);
            mSessionsRecyclerView.setVisibility(View.VISIBLE);

            sortSessions();
        }

        sessionAdapter.updateItems(day_sessions);
    }

    @Override
    public void onDayChanged(int i) {
        curDayIdx = i;
        setSchedule();
    }

    @Override
    public void onScheduleTypeChanged(int i) {
        curTypeIdx = i;
        sessionAdapter = new SessionAdapter(getContext(),mFullScheduleArrayList, curTypeIdx);
        mSessionsRecyclerView.setAdapter(sessionAdapter);
        setSchedule();
    }

    @Override
    public void onNewSession(Session session) {
        Toast.makeText(getActivity(), "New Session Item: " + session.getSubjectName(), Toast.LENGTH_LONG).show();
        mFullScheduleArrayList.add(session);
        setSchedule();
        Toast.makeText(getContext(), "Session Added.", Toast.LENGTH_SHORT).show();

        Calendar calendar = Calendar.getInstance();
        Date d = new Date(session.getStartTime());
        calendar.setTime(d);

        AlarmManager am = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getContext(), ActivityReceiver.class);
        intent.putExtras(session.toBundle());
        intent.setAction("com.akilsw.waky.session_receiver");

        PendingIntent pi = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if(am != null){
            am.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), 604800000L,  pi);
            Log.d("WOURA", "Alarm set for " + d.getHours() + " : " + d.getMinutes());
        }else{
            Log.d("WOURA", "Alarm not set, manager is null.");
        }
    }


    private class FetchSessionsTask extends AsyncTask<String, String, Cursor> {
        private Cursor fetchCursor;
        ProgressDialog progressDialog;

        @Override
        protected Cursor doInBackground(String... params) {
            Log.d("WOURA", "Data fetch Task Called!");
            String tables_str = DentyContract.SessionEntry.TABLE_NAME +
                    " LEFT OUTER JOIN " + DentyContract.SubjecstEntry.TABLE_NAME +
                    " ON " + DentyContract.SessionEntry.TABLE_NAME + "." + DentyContract.SessionEntry.COLUMN_SUBJECT_ID +
                    " = " + DentyContract.SubjecstEntry.TABLE_NAME + "." + DentyContract.SubjecstEntry._ID;
//            try {
                _QB = new SQLiteQueryBuilder();
                _QB.setTables(tables_str);

                _QB.setProjectionMap(DentyContract.SessionEntry.PROJECTION_MAP);
                String _OrderBy = DentyContract.SessionEntry.COLUMN_DAY + ", " + DentyContract.SessionEntry.COLUMN_START_TIME + " ASC";
                DentyDbHelper dbhelper = new DentyDbHelper(getContext());
                database = dbhelper.getReadableDatabase();

//                fetchCursor = _QB.query(database, DentyContract.SessionEntry.PROJECTION, null, null, null, null, _OrderBy);
                fetchCursor = _QB.query(database, DentyContract.SessionEntry.PROJECTION, null, null, null, null, _OrderBy);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setData(fetchCursor);
                    }
                });

                Log.d("WOURA", "Data was fetched: Found " + fetchCursor.getCount() + " Sessions");
//            } catch (Exception e) {
//                Log.d("WOURA", "Error fetching data. \n\n " + e.getMessage());
//            }

            return fetchCursor;
        }

        @Override
        protected void onPostExecute(Cursor result) {
//            progressDialog.dismiss();
            noSessionsTextView.setText(Html.fromHtml("You have no sessions, click the <b><font color='black'>+</font></b></u> icon to add."));
        }

        @Override
        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(getContext(),
//                    "Fetching Sessions",
//                    "Please Wait for sessions to get loaded.");
            noSessionsTextView.setText("Fetching Sessions...");
        }
    }
}
