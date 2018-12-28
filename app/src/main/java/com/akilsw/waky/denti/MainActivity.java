package com.akilsw.waky.denti;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Activity;
import com.akilsw.waky.denti.ui.ActivitiesFragment;
import com.akilsw.waky.denti.ui.HomeFragment;
import com.akilsw.waky.denti.ui.MainSectionFragment;
import com.akilsw.waky.denti.ui.ResourcesFragment;
import com.akilsw.waky.denti.ui.ScheduleFragment;
import com.roughike.bottombar.BottomBar;
import com.roughike.bottombar.OnTabSelectListener;

import java.util.ArrayList;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity{

    /*TODO
    * - App wide Search show categorized results
    * - Enance receive share intent
    * - Receive reference from socket
    * - Share ratiba as image
    * - Settings to change time fr to AM / PM
    * - Use voice input
    * - Share references
    * - Login to sync info online
    * - Take pictures from app of summaries
    * - Show preview for books, ppts and videos
    * - Search on google or youtube then return result(for definition) or video link(for yt video) to app
    * - Customize colors for session type
    * - Link with wasongo
    * */

    @BindView(R.id.bottomBar)
    BottomBar bottomBar;

    @BindView(R.id.fragment_placeHolder)
    FrameLayout placeholder;


    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    MainSectionFragment mCurrentFragment;

    ArrayList<Integer> navStack = new ArrayList<Integer>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        TimeZone tz = TimeZone.getDefault();
        Log.d("WOURA", "Your TimeZone Is:  "+tz.getDisplayName(false, TimeZone.SHORT)+" Timezon id :: " +tz.getID());

        new FetchTodosCountTask().execute();

        fragmentManager = getSupportFragmentManager();
        int start_page = getIntent().getIntExtra("page", R.id.tab_home);

        bottomBar.setOnTabSelectListener(new OnTabSelectListener() {
            @Override
            public void onTabSelected(@IdRes int tabId) {
                navStack.add(tabId);
                updateShell(tabId, false);
                setSection();
            }
        });
        bottomBar.selectTabWithId(start_page);
    }

    public void menuClicked(View view) {
        switch (view.getId()){
            case R.id.menu_item_subjects:
                startActivity(new Intent(this, SubjectsActivity.class));
                finish();
                break;
            case R.id.menu_item_sessions:
                updateShell(R.id.tab_schedule);
                setSection();
                break;
            case R.id.menu_item_resources:
                updateShell(R.id.tab_refs);
                setSection();
                break;
            case R.id.menu_item_activities:
                updateShell(R.id.tab_todos);
                setSection();
                break;
        }
    }


    private class FetchTodosCountTask extends AsyncTask<Integer, String, Integer> {
        private int fetchCount = 0;
        ProgressDialog progressDialog;
        private int whichTask;

        @Override
        protected Integer doInBackground(Integer... params) {
            DentyDbHelper dbHelper = new DentyDbHelper(getBaseContext());
            SQLiteDatabase database = dbHelper.getReadableDatabase();
            Cursor mCursor= database.query(DentyContract.TodosEntry.TABLE_NAME, DentyContract.TodosEntry.PROJECTION, null, null, null, null, null);
            mCursor.moveToFirst();

            mCursor.moveToFirst();
            while(!mCursor.isAfterLast()) {
                Activity a = Activity.fromCursor(mCursor);
                if(a.isDueToday())
                    fetchCount+=1;
                mCursor.moveToNext();
            }

            mCursor.close();

            return fetchCount;
        }

        @Override
        protected void onPostExecute(final Integer count) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bottomBar.getTabWithId(R.id.tab_todos).setBadgeCount(count);
                }
            });
        }

        @Override
        protected void onPreExecute() {

        }
    }

    private void updateShell(int id, boolean back) {
        boolean is_first_tab = id == R.id.tab_home;

        switch (id){
            case R.id.tab_home:{
                if(!back)
                    mCurrentFragment = new HomeFragment();
            }
            break;

            case R.id.tab_schedule:{
                if(!back)
                    mCurrentFragment = new ScheduleFragment();
            }
            break;

            case R.id.tab_refs:{
                if(!back)
                    mCurrentFragment = new ResourcesFragment();
            }
            break;

            case R.id.tab_todos:{
                //checkAllBtn.setVisibility(View.VISIBLE);

                if(!back)
                    mCurrentFragment = new ActivitiesFragment();
            }
        }

        if(back)
            bottomBar.selectTabWithId(id);
    }

    private void updateShell(int id) {
        updateShell(id, false);
        bottomBar.selectTabWithId(id);
    }

    private void setSection(){
        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction
                .replace(R.id.fragment_placeHolder, mCurrentFragment)
                .addToBackStack(null)
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (navStack.size() <= 1) {
            finish();
        }
        else {
            int last_index = navStack.size() - 1;
            updateShell(navStack.get(last_index - 1), true);
            navStack.remove(last_index);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("WOURA", "Some result in main!");
        super.onActivityResult(requestCode, resultCode, data);
    }
}

