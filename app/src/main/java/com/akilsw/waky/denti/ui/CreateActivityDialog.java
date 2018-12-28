package com.akilsw.waky.denti.ui;

import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import com.akilsw.waky.denti.MoiUtils;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Activity;

import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Waky on 12/2/2017.
 */

public class CreateActivityDialog extends DialogFragment implements View.OnClickListener {
    @BindView(R.id.input_title)
    TextView newActivityTitle;

    @BindView(R.id.input_deadline_date)
    TextView newActivityDeadlineDateText;

    @BindView(R.id.input_deadline_time)
    TextView newActivityDeadlineTimeText;

    @BindView(R.id.datepicker_btn)
    ImageView newActivityDatePickerBtn;

    @BindView(R.id.timepicker_btn)
    ImageView newActivityTimePickerBtn;

    @BindView(R.id.cancel_btn)
    TextView cancelBtn;

    @BindView(R.id.submit_btn)
    TextView submitBtn;

    CreateActivityDialogListener listener;
    private long newActivityDeadline;
    private long newActivityDeadlineTime;
    private long newActivityDeadlineDate;

    Activity mCreatedActivity;

    private int mYear, mMonth, mDay, mHour, mMinute;
    Calendar mCal = Calendar.getInstance();

    public interface CreateActivityDialogListener {
        void onSubmitDialog(Activity activity);
    }

    public void setListener(CreateActivityDialogListener listener) {
        this.listener = listener;
    }

    public CreateActivityDialog() {
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_activity_dialog, container, false);
        ButterKnife.bind(this,view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle("Create Activity");

        newActivityTitle.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        newActivityDatePickerBtn.setOnClickListener(this);
        newActivityTimePickerBtn.setOnClickListener(this);
        cancelBtn.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.cancel_btn)
            closeDialog();
        else if(view.getId() == R.id.submit_btn)
            submitData();
        else if(view.getId() == R.id.datepicker_btn){
            final Calendar c = Calendar.getInstance(Locale.getDefault());
            mYear = c.get(Calendar.YEAR);
            mMonth = c.get(Calendar.MONTH);
            mDay = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int year,
                                              int month, int day) {
                            newActivityDeadlineDateText.setText(day + "-" + (month + 1) + "-" + year);
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
                                    0, 0, 0);
                            newActivityDeadlineDate  = calendar.getTimeInMillis();
                            mCal.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());
                            mCal.set(Calendar.MONTH, datePicker.getMonth());
                            Log.d("WOURA", "New Date: " + MoiUtils.formatSessionTime(newActivityDeadlineDate));
                        }
                    }, mYear, mMonth, mDay);
            datePickerDialog.show();
        }
        else if(view.getId() == R.id.timepicker_btn){
            final Calendar c = Calendar.getInstance(Locale.getDefault());
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay,
                                              int minute) {

                            newActivityDeadlineTimeText.setText(hourOfDay + ":" + minute);
                            Calendar calendar = Calendar.getInstance();
                            calendar.set(0, 0, 0,
                                    hourOfDay, minute, 0);
                            newActivityDeadlineDate = calendar.getTimeInMillis();

                            mCal.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            mCal.set(Calendar.MINUTE, minute);
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
        }
    }

    public void closeDialog(){
        this.dismiss();
    }

    public void submitData(){
        mCreatedActivity = new Activity();
        mCreatedActivity.setTitle(newActivityTitle.getText().toString());
        mCreatedActivity.setDeadline(mCal.getTimeInMillis());
        Log.d("WOURA", "Activity Deadline: " + MoiUtils.formatSessionTime(mCal.getTimeInMillis()));
        listener.onSubmitDialog(mCreatedActivity);
    }
}
