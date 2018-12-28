package com.akilsw.waky.denti;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.akilsw.waky.denti.adapters.SubjectsAdapter;
import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Subject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.form.Input;
import eltos.simpledialogfragment.form.SimpleFormDialog;

public class SubjectsActivity extends AppCompatActivity implements SubjectsAdapter.ItemClickCallback,
        SimpleDialog.OnDialogResultListener,  RecyclerItemTouchHelper.RecyclerItemTouchHelperListener {
    private final String CREATE_SUBJECT_DIALOG = "Create Subject";
    private final String SUBJECT_NAME = "Subject Name";

    private SQLiteDatabase database;
    ArrayList<Subject> mSubjectsArrayList = new ArrayList<>();
    Subject mSubjects;
    Cursor mCursor;

    @BindView(R.id.subjectsList)
    RecyclerView mList;

    @BindView(R.id.subjects_fragment_wrapper)
    LinearLayout parentWrapper;

    SubjectsAdapter subjectsAdapter;

    @BindView(R.id.action_add)
    ImageView createInstanceBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subjects);
        ButterKnife.bind(this);

        DentyDbHelper dbhelper = new DentyDbHelper(getBaseContext());
        database = dbhelper.getReadableDatabase();
        mCursor = querySubjects();
        Log.d("WOURA", "Found " + mCursor.getCount() + " subjects from db");
        setData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        mList.setLayoutManager(layoutManager);
        subjectsAdapter = new SubjectsAdapter(getBaseContext(),mSubjectsArrayList);
        subjectsAdapter.setItemClickCallback(this);
        mList.setAdapter(subjectsAdapter);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mList);

        createInstanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInstance();
            }
        });
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

        sortSubjects();
    }

    private void sortSubjects(){
        Collections.sort(mSubjectsArrayList, new Comparator<Subject>() {
            @Override
            public int compare(Subject s1, Subject s2) {
                return s1.getName().compareToIgnoreCase(s2.getName());
            }
        });
    }

    public void createInstance(){
        SimpleFormDialog.build()
                .title("Add Subject")
                .fields(
                        Input.name(SUBJECT_NAME).required().hint("Subject Name")
                )
                .pos("SUBMIT")
                .show(this, CREATE_SUBJECT_DIALOG);
    }

    @Override
    public void onItemClick(int p) {
        Log.d("WOURA", "Subject at position"+ p +", clicked!");
        Subject subject = mSubjectsArrayList.get(p);

        Intent intent = new Intent(getBaseContext(), SubjectActivity.class);
        Bundle b = subject.toBundle();
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (CREATE_SUBJECT_DIALOG.equals(dialogTag)) {
            if (which != SimpleDialog.OnDialogResultListener.CANCELED) {
                String name = extras.getString(SUBJECT_NAME);
                long newSubjectId = MoiUtils.persistSubject(getBaseContext(), name);

                if(newSubjectId != -1){
                    Toast.makeText(getBaseContext(), "SUBJECT ADDED", Toast.LENGTH_SHORT).show();
                    Log.i("WOURA", "SUBJECT ADDED");
                    Subject subject = new Subject(String.valueOf(newSubjectId), name, "#000");
                    mSubjectsArrayList.add(subject);
                    sortSubjects();
                    subjectsAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(getBaseContext(), "SUBJECT ADDING FAILED", Toast.LENGTH_SHORT).show();
                    Log.i("WOURA", "FAILED TO ADDE SUBJECT");
                }
            }else{
                Toast.makeText(getBaseContext(), "Add subject cancelled", Toast.LENGTH_SHORT).show();
            }
        }

        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof SubjectsAdapter.ViewHolder) {
            // get the removed item name to display it in snack bar
            String name = mSubjectsArrayList.get(viewHolder.getAdapterPosition()).getName();

            // backup of removed item for undo purpose
            final Subject deletedItem = mSubjectsArrayList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            // remove the item from recycler view
            subjectsAdapter.removeItem(viewHolder.getAdapterPosition());

            final boolean[] undone = {false};
            // showing snack bar with Undo option
            Snackbar snackbar = Snackbar
                    .make(parentWrapper, name + " deleted!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // undo is selected, restore the deleted item
                    subjectsAdapter.restoreItem(deletedItem, deletedIndex);
                    undone[0] = true;
                }
            })
                    .addCallback(new Snackbar.Callback(){
                        @Override
                        public void onDismissed(Snackbar transientBottomBar, int event) {
                            if (event == Snackbar.Callback.DISMISS_EVENT_TIMEOUT) {
                                if(deleteSubject(deletedItem.getId())){
                                    Log.d("WOURA", "Subject deeted");
                                }
                            }
                        }
                    });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();
        }
    }

    private boolean deleteSubject(String id){
        return database.delete(DentyContract.SubjecstEntry.TABLE_NAME, DentyContract.SubjecstEntry._ID + "=" + id, null) > 0;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        goBack();
    }

    private void goBack() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    public void goBack(View view) {
        goBack();
    }
}
