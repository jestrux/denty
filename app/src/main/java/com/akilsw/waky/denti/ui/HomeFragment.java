package com.akilsw.waky.denti.ui;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.data.DentyContract.*;
import com.akilsw.waky.denti.data.DentyDbHelper;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Waky on 11/25/2017.
 */

public class HomeFragment extends MainSectionFragment {
    private static final int TASK_FETCH_SUBJECTS = 33;
    private static final int TASK_FETCH_SESSIONS = 34;
    private static final int TASK_FETCH_RESOURCES = 35;
    private static final int TASK_FETCH_ACTIVITIES = 36;

    private SQLiteDatabase database;

    @BindView(R.id.home_fragment_wrapper)
    LinearLayout wrapper;

    @BindView(R.id.subject_count)
    TextView subjectsCountView;

    @BindView(R.id.menu_subjects)
    TextView subjectsView;

    @BindView(R.id.sessions_count)
    TextView sessionsCountView;

    @BindView(R.id.menu_sessions)
    TextView sessionsView;

    @BindView(R.id.resource_count)
    TextView resourcesCountView;

    @BindView(R.id.menu_resources)
    TextView resourcesView;

    @BindView(R.id.activities_count)
    TextView activitesCountView;

    @BindView(R.id.menu_activities)
    TextView activitesView;

    CoordinatorLayout parentWrapper;

    public HomeFragment() {
    }

    public void createInstance(){
        android.app.FragmentManager fm = getActivity().getFragmentManager();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        ButterKnife.bind(this,view);
        DentyDbHelper dbhelper = new DentyDbHelper(getContext());
        database = dbhelper.getReadableDatabase();

        new DbFetchTask().execute(TASK_FETCH_SUBJECTS);
        new DbFetchTask().execute(TASK_FETCH_SESSIONS);
        new DbFetchTask().execute(TASK_FETCH_RESOURCES);
        new DbFetchTask().execute(TASK_FETCH_ACTIVITIES);

        parentWrapper = getActivity().findViewById(R.id.top_coordinator);

        return view;
    }

    public void setData(int count, int whichTask){
        switch (whichTask){
            case TASK_FETCH_SUBJECTS:
                subjectsCountView.setText(count + "");
                if(count == 1)
                    subjectsView.setText("Subject");
                break;
            case TASK_FETCH_SESSIONS:
                sessionsCountView.setText(count + "");
                if(count == 1)
                    sessionsView.setText("Session");
                break;
            case TASK_FETCH_RESOURCES:
                resourcesCountView.setText(count + "");
                if(count == 1)
                    resourcesView.setText("Resource");
                break;
            case TASK_FETCH_ACTIVITIES:
                activitesCountView.setText(count + "");
                if(count == 1)
                    activitesView.setText("Activity");
                break;
        }
    }

    private int queryCommon(String table){
        Cursor mCount= database.rawQuery("select count(*) from " + table, null);
        mCount.moveToFirst();
        int count= mCount.getInt(0);
        mCount.close();

        return count;
    }

    private class DbFetchTask extends AsyncTask<Integer, String, Integer> {
        private int fetchCount;
        ProgressDialog progressDialog;
        private int whichTask;

        @Override
        protected Integer doInBackground(Integer... params) {
            whichTask = params[0];

            switch (whichTask){
                case TASK_FETCH_SUBJECTS:
                    fetchCount = queryCommon(SubjecstEntry.TABLE_NAME);
                    break;
                case TASK_FETCH_SESSIONS:
                    fetchCount = queryCommon(SessionEntry.TABLE_NAME);
                    break;
                case TASK_FETCH_RESOURCES:
                    fetchCount = queryCommon(ReferencesEntry.TABLE_NAME);
                    break;
                case TASK_FETCH_ACTIVITIES:
                    fetchCount = queryCommon(TodosEntry.TABLE_NAME);
                    break;
            }
            return fetchCount;
        }

        @Override
        protected void onPostExecute(final Integer result) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setData(result, whichTask);
                }
            });
        }

        @Override
        protected void onPreExecute() {

        }
    }
}
