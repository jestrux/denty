package com.akilsw.waky.denti;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.akilsw.waky.denti.adapters.StartSubjectsAdapter;
import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Subject;
import com.blikoon.qrcodescanner.QrCodeActivity;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.form.Input;
import eltos.simpledialogfragment.form.SimpleFormDialog;

public class StartActivity extends AppCompatActivity implements SimpleDialog.OnDialogResultListener, StartSubjectsAdapter.ItemClickCallback {
    private final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int REQUEST_PERMISSIONS = 12;

    private final String CREATE_SUBJECT_DIALOG = "Create Subject";
    private final String SUBJECT_NAME = "Subject Name";
    private static final int REQUEST_CODE_QR_SCAN = 101;

    private SQLiteDatabase database;
    ArrayList<Subject> mSubjectsArrayList = new ArrayList<>();
    Subject mSubjects;
    Cursor mCursor;

    @BindView(R.id.subjectsList)
    RecyclerView mList;

    @BindView(R.id.startBtn)
    TextView startBtn;

    @BindView(R.id.page_title)
    TextView pageTitle;

    StartSubjectsAdapter subjectsAdapter;

    @Override
    protected void onStart() {
        super.onStart();
        int start_page = getIntent().getIntExtra("page", -1);
        if(start_page != -1){
            Intent i = new Intent(this, MainActivity.class);
            i.putExtra("page", start_page);
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DentyDbHelper dbhelper = new DentyDbHelper(this);
        database = dbhelper.getReadableDatabase();
        mCursor = querySubjects();
        setData();

        if(mSubjectsArrayList.size() >= 3){
            goToMain();
            return;
        }
        else{
            setContentView(R.layout.activity_start);
            ButterKnife.bind(this);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mList.setLayoutManager(layoutManager);
        subjectsAdapter = new StartSubjectsAdapter(this,mSubjectsArrayList);
        subjectsAdapter.setItemClickCallback(this);
        mList.setAdapter(subjectsAdapter);

        if(!mSubjectsArrayList.isEmpty())
            mList.setVisibility(View.VISIBLE);

        if(mSubjectsArrayList.size() >= 3) {
            startBtn.setVisibility(View.GONE);
        }

        pageTitle.setOnLongClickListener(new View.OnLongClickListener(){
            @Override
            public boolean onLongClick(View view) {
                Toast.makeText(getApplicationContext(), "Hey waky!", Toast.LENGTH_SHORT).show();
                goToMain();
                return true;
            }
        });

        if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if(hasCameraPermission())
                launchScanner();
            else
                requestPermissions(perms, REQUEST_PERMISSIONS);
        }else{
            launchScanner();
        }
    }

    private void launchScanner() {
        Intent i = new Intent(StartActivity.this,QrCodeActivity.class);
        startActivityForResult( i,REQUEST_CODE_QR_SCAN);
    }

    private Cursor querySubjects(){
        return database.query(
                DentyContract.SubjecstEntry.TABLE_NAME,
                DentyContract.SubjecstEntry.PROJECTION,
                null, null, null, null, DentyContract.SubjecstEntry.COLUMN_NAME + " DESC"
        );
    }

    public void setData(){
        mSubjectsArrayList.clear();

        mCursor.moveToFirst();
        while(!mCursor.isAfterLast()) {
            Subject subject = Subject.fromCursor(mCursor);
            mSubjectsArrayList.add(subject);
            mCursor.moveToNext();
        }
    }

    public void createSubject(View view){
        SimpleFormDialog.build()
                .title("Add Subject")
                .fields(
                        Input.name(SUBJECT_NAME).required().hint("Subject Name")
                )
                .pos("SUBMIT")
                .show(this, CREATE_SUBJECT_DIALOG);
    }

    public void getStarted(View view){
        goToMain();
    }

    private void goToMain(){
        startActivity(new Intent(this, MainActivity.class));
//        startActivity(new Intent(this, SyncActivity.class));
        finish();
    }

    public boolean hasCameraPermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String perm : perms){
                if (ActivityCompat.checkSelfPermission(getBaseContext(), perm) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }

            return true;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        AppCompatActivity activity = this;
        if (requestCode == REQUEST_PERMISSIONS && grantResults.length > 0) {
            for (int res : grantResults){
                if(res != PackageManager.PERMISSION_GRANTED){
                    new AlertDialog.Builder(activity)
                            .setTitle("You rejected permission!!")
                            .setMessage("This feature won't work without the requested permissions, please reconsider.")
                            .setPositiveButton("Okay",  new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show();

                    return;
                }
            }

            launchScanner();
        } else {
            new AlertDialog.Builder(activity)
                    .setTitle("You rejected permission!!")
                    .setMessage("This feature won't work without the requested permissions, please reconsider.")
                    .setPositiveButton("Okay",  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode != RESULT_OK)
        {
            Log.d("WOURA","COULD NOT GET A GOOD RESULT.");

            if(data==null)
                return;
            //Getting the passed result
            String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
            if( result!=null)
            {
                AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this).create();
                alertDialog.setTitle("Scan Error");
                alertDialog.setMessage("QR Code could not be scanned");
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                alertDialog.show();
            }
            return;
        }
        if(requestCode == REQUEST_CODE_QR_SCAN)
        {
            if(data==null)
                return;

            String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
            Log.d("WOURA","Have scan result in your app activity :"+ result);

            AlertDialog alertDialog = new AlertDialog.Builder(StartActivity.this).create();
            alertDialog.setTitle("Scan Result");
            alertDialog.setMessage(result);
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();

            Toast.makeText(this, "Scan complete", Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (CREATE_SUBJECT_DIALOG.equals(dialogTag)) {
            if (which != SimpleDialog.OnDialogResultListener.CANCELED) {
                String name = extras.getString(SUBJECT_NAME);
                for (Subject s: mSubjectsArrayList){
                    if(s.getName().equals(name)){
                        Toast.makeText(this, "You already added " + name, Toast.LENGTH_SHORT).show();
                        return false;
                    }
                }

                long newSubjectId = MoiUtils.persistSubject(this, name);

                if(newSubjectId != -1){
                    Toast.makeText(this, "SUBJECT ADDED", Toast.LENGTH_SHORT).show();
                    Log.i("WOURA", "SUBJECT ADDED");
                    Subject subject = new Subject(String.valueOf(newSubjectId), name, "#000");
                    mSubjectsArrayList.add(subject);
                    subjectsAdapter.notifyDataSetChanged();

                    if(mList.getVisibility() != View.VISIBLE)
                        mList.setVisibility(View.VISIBLE);

                    if(mSubjectsArrayList.size() >= 3) {
                        startBtn.setVisibility(View.VISIBLE);
                    }

                }else{
                    Toast.makeText(this, "SUBJECT ADDING FAILED", Toast.LENGTH_SHORT).show();
                    Log.i("WOURA", "FAILED TO ADDE SUBJECT");
                }
            }else{
                Toast.makeText(this, "Add subject cancelled", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public void onItemRemove(int p) {
        Log.d("WOURA", "Subject at position"+ p +", remove!");
        Subject subject = mSubjectsArrayList.get(p);

        if(removeSubject(subject.getId())){
            mSubjectsArrayList.remove(p);
            subjectsAdapter.notifyDataSetChanged();

            if(mSubjectsArrayList.size() < 3) {
                startBtn.setVisibility(View.VISIBLE);
            }

            Toast.makeText(this, "Subject removed", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean removeSubject(String id){
        return database.delete(DentyContract.SubjecstEntry.TABLE_NAME, DentyContract.SubjecstEntry._ID + "=" + id, null) > 0;
    }
}
