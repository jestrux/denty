package com.akilsw.waky.denti;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Session;
import com.akilsw.waky.denti.models.Subject;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CreateScheduleActivity extends AppCompatActivity implements MaterialSpinner.OnItemSelectedListener,
    TimePicker.OnTimeChangedListener{
    @BindView(R.id.day_picker)
    MaterialSpinner dayPicker;

    @BindView(R.id.subject_picker)
    MaterialSpinner subjectPicker;

    @BindView(R.id.type_picker)
    MaterialSpinner typePicker;

    @BindView(R.id.venue_input)
    EditText venueInput;

    @BindView(R.id.start_time)
    TimePicker startTimePicker;

    @BindView(R.id.end_time)
    TimePicker endTimePicker;

    private SQLiteDatabase database;
    ArrayList<String> namesOfSubjects = new ArrayList<String>();
    ArrayList<Integer> idsOfSubjects = new ArrayList<Integer>();

    ArrayList<String> dayList = new ArrayList<String>(),
            typeList = new ArrayList<String>();

    int selectedDay = 0, selectedType = 0, selectedSubject;
    long selectedStartTime, selectedEndTime;

    /*TODO
    * - Store chosen type(with color) to SharedPreferences(if none entered) store
    * - Move subjects query to async task
    * - After Item is saved give option to create another item or go back
    * - Prevent start time from being equal to stop time
    * - Prevent end time from being lower than start time
    * - Prevent non null value for venue
    * - Limit length of letters for venue
    * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_schedule);
        ButterKnife.bind(this);
        DentyDbHelper dbhelper = new DentyDbHelper(this);
        database = dbhelper.getReadableDatabase();
        setSubjects();

        selectedDay = getIntent().getIntExtra("day_idx", 0);

        String[] namesOfDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        String[] types = {"Tutorial", "Lecture"};

        dayList.addAll( Arrays.asList(namesOfDays));
        typeList.addAll( Arrays.asList(types));

        dayPicker.setItems(namesOfDays);
        dayPicker.setSelectedIndex(selectedDay);
        typePicker.setItems(types);

        startTimePicker.setIs24HourView(true);
        endTimePicker.setIs24HourView(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            startTimePicker.setMinute(0);
            endTimePicker.setMinute(0);
        }

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(Calendar.MINUTE, 0);
        selectedStartTime = cal.getTimeInMillis();
        selectedEndTime = cal.getTimeInMillis() + 3600000;

        dayPicker.setOnItemSelectedListener(this);
        subjectPicker.setOnItemSelectedListener(this);
        typePicker.setOnItemSelectedListener(this);

        startTimePicker.setOnTimeChangedListener(this);
        endTimePicker.setOnTimeChangedListener(this);
    }

    private void setSubjects(){
        Cursor subCursor = database.query(
                DentyContract.SubjecstEntry.TABLE_NAME,
                DentyContract.SubjecstEntry.PROJECTION,
                null, null, null, null, DentyContract.SubjecstEntry.COLUMN_NAME + " ASC"
        );

        subCursor.moveToFirst();
        while(!subCursor.isAfterLast()) {
            Subject subject = Subject.fromCursor(subCursor);
            namesOfSubjects.add(subject.getName());
            idsOfSubjects.add(Integer.valueOf(subject.getId()));
            subCursor.moveToNext();
        }

        subjectPicker.setItems(namesOfSubjects);
        selectedSubject = idsOfSubjects.get(0);
        subCursor.close();
    }

    @Override
    public void onItemSelected(MaterialSpinner view, int position, long id, Object o) {
        String item = (String) o;

        if(view.getId() == R.id.day_picker)
            selectedDay = dayList.indexOf(item);
        else if(view.getId() == R.id.subject_picker){
            int idx = namesOfSubjects.indexOf(item);
            selectedSubject = idsOfSubjects.get(idx);
        }
        else if(view.getId() == R.id.type_picker)
            selectedType = typeList.indexOf(item);
    }

    @Override
    public void onTimeChanged(TimePicker timePicker, int hour, int minute) {
        Calendar cal = Calendar.getInstance(Locale.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);

        if(timePicker.getId() == R.id.start_time){
            selectedStartTime = cal.getTimeInMillis();
        }
        else if(timePicker.getId() == R.id.end_time) {
            selectedEndTime = cal.getTimeInMillis();
        }
    }

    public void closeActivity(View view){
        finish();
    }

    public void submitData(View view){
        String venue = venueInput.getText().toString();

        if(venue.length() < 1){
            Toast.makeText(this, "Please enter venue.", Toast.LENGTH_LONG).show();
            return;
        }

        Session session = new Session();
        session.setSubjectId(selectedSubject);
        session.setDay(selectedDay);
        session.setType(selectedType);
        session.setVenue(venue);
        session.setStartTime(selectedStartTime);
        session.setEndTime(selectedEndTime);

        Log.d("WOURA", "Selected Day: " + selectedDay + ", " + session.getDay());

        ContentValues cv = session.toContentValues();

        try {
            long new_session_id = database.insert(DentyContract.SessionEntry.TABLE_NAME, null, cv);

            if(new_session_id == -1){
                Toast.makeText(this, "Sorry! Couldn't save subject", Toast.LENGTH_LONG).show();
                return;
            }

            session.setId((int) new_session_id);
            int cur_subject_idx = idsOfSubjects.indexOf(selectedSubject);
            session.setSubjectName(namesOfSubjects.get(cur_subject_idx));

            Intent result = new Intent("com.example.RESULT_ACTION");
            result.putExtras(session.toBundle());
            setResult(Activity.RESULT_OK, result);
            finish();
        } catch (Exception e) {
            Log.d("WOURA", "Error inserting session: " + e.getMessage());
        }
    }
}
