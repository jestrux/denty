package com.akilsw.waky.denti.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.akilsw.waky.denti.ActivitiesItemTouchHelper;
import com.akilsw.waky.denti.ActivityReceiver;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.adapters.AcivitiesAdapter;
import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Waky on 11/25/2017.
 */

public class ActivitiesFragment extends MainSectionFragment implements
        ActivitiesItemTouchHelper.RecyclerItemTouchHelperListener, CreateActivityDialog.CreateActivityDialogListener {
    private ArrayList<Activity> mActivitiesArrayList = new ArrayList<>();
    private SQLiteDatabase database;
    Cursor mCursor;
    private AcivitiesAdapter activitiesAdapter;
    private final String CREATE_ACTIVITY_DIALOG = "Create Activity";
    private final String ACTIVITY_NAME = "Activity Title";
    private final String ACTIVITY_DESCRIPTION = "Activity Description";
    CreateActivityDialog mDialog;

    @BindView(R.id.action_add)
    ImageView createInstanceBtn;

    @BindView(R.id.activities_list)
    RecyclerView mActivitiesRecyclerView;

    @BindView(R.id.no_activites_tview)
    TextView noActivitiesTextView;

//    @BindView(R.id.activities_fragment_wrapper)
//    FrameLayout parentWrapper;
    CoordinatorLayout parentWrapper;

    /*TODO
    -Implements mechanism for check all
    - Reapeating todos */

    public ActivitiesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        ButterKnife.bind(this,view);
        DentyDbHelper dbhelper = new DentyDbHelper(getContext());
        database = dbhelper.getReadableDatabase();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mActivitiesRecyclerView.setLayoutManager(layoutManager);
        activitiesAdapter = new AcivitiesAdapter(getContext(),mActivitiesArrayList);
        activitiesAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                if(mActivitiesArrayList.size() > 0){
                    mActivitiesRecyclerView.setVisibility(View.VISIBLE);
                    noActivitiesTextView.setVisibility(View.GONE);
                }else{
                    mActivitiesRecyclerView.setVisibility(View.GONE);
                    noActivitiesTextView.setVisibility(View.VISIBLE);
                }
            }
        });
        mActivitiesRecyclerView.setAdapter(activitiesAdapter);

        new FetchActivitiesTask().execute();

        parentWrapper = getActivity().findViewById(R.id.top_coordinator);

        createInstanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInstance();
            }
        });

        return view;
    }

    public void createInstance(){
        android.app.FragmentManager fm = getActivity().getFragmentManager();
        mDialog = new CreateActivityDialog();
        mDialog.setListener(this);
        mDialog.show(fm, "create_activity");
    }

    public void setData(Cursor c){
        mActivitiesArrayList.clear();

        if(c.getCount() > 0) {
            c.moveToFirst();
            while (!c.isAfterLast()) {
                Activity activity = Activity.fromCursor(c);
                mActivitiesArrayList.add(activity);
                c.moveToNext();
            }
        }
        activitiesAdapter.notifyDataSetChanged();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ActivitiesItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mActivitiesRecyclerView);
    }

    private Cursor queryActivities(){
        return database.query(
                DentyContract.TodosEntry.TABLE_NAME,
                DentyContract.TodosEntry.PROJECTION,
                null,
                null, null, null, DentyContract.TodosEntry.COLUMN_DEADLINE + " ASC", null
        );
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof AcivitiesAdapter.ViewHolder) {
//            Toast.makeText(getActivity(), "Swiped", Toast.LENGTH_LONG).show();
            String name = mActivitiesArrayList.get(viewHolder.getAdapterPosition()).getTitle();

            final Activity deletedItem = mActivitiesArrayList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

//            activitiesAdapter.removeItem(viewHolder.getAdapterPosition());
            mActivitiesArrayList.remove(deletedIndex);
            activitiesAdapter.notifyDataSetChanged();

            Snackbar snackbar = Snackbar
                    .make(parentWrapper, name + " marked complete!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivitiesArrayList.add(deletedIndex, deletedItem);
                    activitiesAdapter.notifyDataSetChanged();
                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.addCallback(new Snackbar.Callback(){
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                        if(deleteActivity(deletedItem.getId())){
                            Log.d("WOURA", "Reference completed");
                        }
                    }
                }
            });
            snackbar.show();
        }
    }

    private boolean deleteActivity(int id){
        return database.delete(DentyContract.TodosEntry.TABLE_NAME, DentyContract.ReferencesEntry._ID + "=" + id, null) > 0;
    }

    @Override
    public void onSubmitDialog(Activity activity) {
        Toast.makeText(getContext(), "New Activity: " + activity.getTitle(), Toast.LENGTH_LONG);
        ContentValues cv = activity.toContentValues();
        mDialog.dismiss();

        try {
            long new_activity_id = database.insert(DentyContract.TodosEntry.TABLE_NAME, null, cv);

            if(new_activity_id == -1){
                Toast.makeText(getContext(), "Sorry! Couldn't save activity", Toast.LENGTH_LONG).show();
                return;
            }

            activity.setId((int) new_activity_id);
            mActivitiesArrayList.add(activity);

            Collections.sort(mActivitiesArrayList, new Comparator<Activity>() {
                @Override
                public int compare(Activity s1, Activity s2) {
                    return Long.compare(s1.getDeadline(), s2.getDeadline());
                }
            });

            activitiesAdapter.notifyDataSetChanged();

            if(mActivitiesRecyclerView.getVisibility() != View.VISIBLE){
                mActivitiesRecyclerView.setVisibility(View.VISIBLE);
                noActivitiesTextView.setVisibility(View.GONE);
            }

            Toast.makeText(getContext(), "Activity successfully added!", Toast.LENGTH_LONG).show();

            Calendar calendar = Calendar.getInstance();
            Date d = new Date(activity.getDeadline());
            calendar.setTime(d);

            AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(getContext(), ActivityReceiver.class);
            intent.putExtras(activity.toBundle());
            intent.setAction("com.example.waky.activity_receiver");

            PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            if(am != null){
                am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pi);
                Log.d("WOURA", "Alarm set for " + d.getHours() + " : " + d.getMinutes());
            }else{
                Log.d("WOURA", "Alarm not set, manager is null.");
            }
        } catch (Exception e) {
            Log.d("WOURA", "Error inserting activity: " + e.getMessage());
            Toast.makeText(getContext(), "Sorry! Couldn't save activity", Toast.LENGTH_LONG).show();
        }
    }

    private class FetchActivitiesTask extends AsyncTask<String, String, Cursor> {
        private Cursor fetchCursor;
        ProgressDialog progressDialog;

        @Override
        protected Cursor doInBackground(String... params) {
            Log.d("WOURA", "Data fetch Task Called!");
            fetchCursor = queryActivities();
            return fetchCursor;
        }

        @Override
        protected void onPostExecute(final Cursor result) {
            Log.d("WOURA", "Data was fetched: Found " + result.getCount() + " Activities");
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setData(result);
                }
            });
            noActivitiesTextView.setText(Html.fromHtml("You have no activities, click the <b><font color='black'>+</font></b></u> icon to add."));
        }

        @Override
        protected void onPreExecute() {
            noActivitiesTextView.setText("Fetching Activities...");
        }
    }
}
