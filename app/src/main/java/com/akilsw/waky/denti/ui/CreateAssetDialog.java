package com.akilsw.waky.denti.ui;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.akilsw.waky.denti.MoiUtils;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.models.Resource;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Waky on 12/2/2017.
 */

public class CreateAssetDialog extends DialogFragment implements View.OnClickListener {
    @BindView(R.id.input_title)
    TextView newResourceTitle;

    @BindView(R.id.cancel_btn)
    TextView cancelBtn;

    @BindView(R.id.submit_btn)
    TextView submitBtn;

    CreateResourceDialogListener listener;
    private long newResourceDeadline;
    private long newResourceDeadlineTime;
    private long newResourceDeadlineDate;

    Resource mCreatedResource;

    private int mYear, mMonth, mDay, mHour, mMinute;
    Calendar mCal = Calendar.getInstance();

    public interface CreateResourceDialogListener {
        void onSubmitDialog(Resource activity);
    }

    public void setListener(CreateResourceDialogListener listener) {
        this.listener = listener;
    }

    public CreateAssetDialog() {
    }

    @Override
    public void onResume() {
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);

        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.create_asset_dialog, container, false);
        ButterKnife.bind(this,view);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getDialog().setTitle("Create Resource");

        newResourceTitle.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        cancelBtn.setOnClickListener(this);
        submitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.cancel_btn)
            closeDialog();
        else if(view.getId() == R.id.submit_btn)
            submitData();
    }

    public void closeDialog(){
        this.dismiss();
    }

    public void submitData(){
        mCreatedResource = new Resource();
        mCreatedResource.setTitle(newResourceTitle.getText().toString());
        mCreatedResource.setCreatedAt(mCal.getTimeInMillis());
        Log.d("WOURA", "Resource Deadline: " + MoiUtils.formatSessionTime(mCal.getTimeInMillis()));
        listener.onSubmitDialog(mCreatedResource);
    }
}
