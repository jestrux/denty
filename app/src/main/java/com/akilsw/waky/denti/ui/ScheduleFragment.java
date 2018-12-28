package com.akilsw.waky.denti.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akilsw.waky.denti.Constants;
import com.akilsw.waky.denti.CreateScheduleActivity;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Session;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Waky on 11/25/2017.
 */

public class ScheduleFragment extends MainSectionFragment {
    /*
    * TODO
    * - Share schedule as image(vie bluetooth) or online as json file
    * - Swipe left to hide day filters and show all sessions and vice
    * - Store selected filter show state in shared preferences
    * - Show session past now only
    * SELECT * FROM table
    * WHERE strftime('%s', your_date_column) < strftime('%s','now')
    *
    * */

    @BindView(R.id.action_add)
    ImageView createInstanceBtn;

    public final static int SCHEDULE_TYPE_SINGLE = 0;
    public final static int SCHEDULE_TYPE_FULL = 1;

    private static final int NEW_SESSION = 12;
    private ArrayAdapter<String> listAdapter;
    SharedPreferences.Editor prefs_editor;

    @BindView(R.id.day_list)
    ListView dayListView;

    @BindView(R.id.type_day_tview)
    TextView typeDayTview;

    @BindView(R.id.type_week_tview)
    TextView typeWeekTview;

    @BindView(R.id.week_days_wrapper)
    LinearLayout weekDaysWrapper;

    private ScheduleListener scheduleListener;

    public interface ScheduleListener {
        void onDayChanged(int p);
        void onScheduleTypeChanged(int p);
        void onNewSession(Session session);
    }

    public void setScheduleParamsListener(ScheduleListener listener){
        scheduleListener = listener;
    }

    private int curDayIdx = 0;
    private int curTypeIdx = SCHEDULE_TYPE_SINGLE;

    SharedPreferences pref;

    public ScheduleFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        pref = getActivity().getApplicationContext().getSharedPreferences("MyPref", 0); // 0 - for private mode

        curTypeIdx = pref.getInt(Constants.SCHEDULE_TYPE_PREF_KEY, SCHEDULE_TYPE_SINGLE);
//        curTypeIdx = SCHEDULE_TYPE_SINGLE;
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);
        ButterKnife.bind(this,view);

        String[] namesOfDays = DateFormatSymbols.getInstance().getShortWeekdays();
        ArrayList<String> dayList = new ArrayList<String>();

        dayList.add( "Mon" );
        dayList.add( "Tue" );
        dayList.add( "Wed" );
        dayList.add( "Thu" );
        dayList.add( "Fri" );
        dayList.add( "Sat" );
        dayList.add( "Sun" );

        listAdapter = new ArrayAdapter<String>(getContext(), R.layout.day_item, dayList);
        dayListView.setAdapter( listAdapter);


        Calendar calendar = Calendar.getInstance();
        int calendarDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        curDayIdx = calendarDayOfWeek - 2;
        if (curDayIdx < 0) {
            curDayIdx += 7;
        }

        dayListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        dayListView.setItemChecked(curDayIdx, true);

        dayListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                onDayChanged(i);
            }
        });

        FragmentTransaction ft = getChildFragmentManager().beginTransaction();
        SessionsFragment sessionsFragment = SessionsFragment.newInstance(curTypeIdx, curDayIdx );

        ft.replace(R.id.sessions_placeholder, sessionsFragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack(null);
        ft.commit();

        setScheduleParamsListener(sessionsFragment);

        createInstanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInstance();
            }
        });

        typeWeekTview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curTypeIdx = SCHEDULE_TYPE_FULL;
                onScheduleTypeChanged(SCHEDULE_TYPE_FULL);
                setTabViews();
            }
        });
        typeDayTview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                curTypeIdx = SCHEDULE_TYPE_SINGLE;
                onScheduleTypeChanged(SCHEDULE_TYPE_SINGLE);
            }
        });

        setTabViews();
        return view;
    }

    private void setTabViews(){
        if(curTypeIdx == SCHEDULE_TYPE_FULL){
            typeWeekTview.setSelected(true);
            typeDayTview.setSelected(false);
            weekDaysWrapper.setVisibility(View.GONE);
//            weekDaysWrapper.animate()
//                    .translationX(-weekDaysWrapper.getWidth())
//                    .setDuration(300)
//                    .setListener(new AnimatorListenerAdapter() {
//                        @Override
//                        public void onAnimationEnd(Animator animation) {
//                            super.onAnimationEnd(animation);
//                            weekDaysWrapper.setVisibility(View.GONE);
//                            weekDaysWrapper.setTranslationX(0);
//                        }
//                    });
        }else{
            typeWeekTview.setSelected(false);
            typeDayTview.setSelected(true);
            weekDaysWrapper.setVisibility(View.VISIBLE);
//            weekDaysWrapper.setTranslationX(-weekDaysWrapper.getWidth());
//            weekDaysWrapper.animate()
//                .translationX(0)
//                .setDuration(300)
//                .setListener(new AnimatorListenerAdapter() {
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        super.onAnimationEnd(animation);
//                        weekDaysWrapper.setTranslationX(0);
//                    }
//                });;
        }
    }

    private void onScheduleTypeChanged(int type){
        prefs_editor = pref.edit();
        curTypeIdx = type;
        scheduleListener.onScheduleTypeChanged(type);
        prefs_editor.putInt(Constants.SCHEDULE_TYPE_PREF_KEY, type);
        prefs_editor.apply();

        setTabViews();
        Log.d("WOURA", "New Type: " + (pref.getInt(Constants.SCHEDULE_TYPE_PREF_KEY, SCHEDULE_TYPE_FULL) ==  SCHEDULE_TYPE_FULL));
    }

    private void onDayChanged(int day){
        curDayIdx = day;
        scheduleListener.onDayChanged(day);
    }

    public void createInstance(){
        Intent intent = new Intent(getActivity(), CreateScheduleActivity.class);
        if(curTypeIdx != SCHEDULE_TYPE_FULL)
            intent.putExtra("day_idx", curDayIdx);

        startActivityForResult(intent, NEW_SESSION);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("WOURA", "Some result in fragment!");

        if(requestCode == NEW_SESSION && data != null && data.getExtras() != null){
            scheduleListener.onNewSession(Session.fromBundle(data.getExtras()));
            Toast.makeText(getContext(), "New Session.", Toast.LENGTH_SHORT).show();
            Log.d("WOURA", "Session Saved");
        }
    }
}
