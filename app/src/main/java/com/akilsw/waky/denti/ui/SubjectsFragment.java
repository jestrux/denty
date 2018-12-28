package com.akilsw.waky.denti.ui;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.akilsw.waky.denti.MoiUtils;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.RecyclerItemTouchHelper;
import com.akilsw.waky.denti.SubjectActivity;
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

/**
 * Created by Waky on 11/25/2017.
 */

public class SubjectsFragment extends MainSectionFragment implements SubjectsAdapter.ItemClickCallback,
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

    public SubjectsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_subjects, container, false);
        ButterKnife.bind(this,view);

        DentyDbHelper dbhelper = new DentyDbHelper(getContext());
        database = dbhelper.getReadableDatabase();
        mCursor = querySubjects();
        Log.d("WOURA", "Found " + mCursor.getCount() + " subjects from db");
        setData();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mList.setLayoutManager(layoutManager);
        subjectsAdapter = new SubjectsAdapter(getContext(),mSubjectsArrayList);
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

        return view;
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

        Intent intent = new Intent(getContext(), SubjectActivity.class);
        Bundle b = subject.toBundle();
        intent.putExtras(b);
        startActivity(intent);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (CREATE_SUBJECT_DIALOG.equals(dialogTag)) {
            if (which != SimpleDialog.OnDialogResultListener.CANCELED) {
                String name = extras.getString(SUBJECT_NAME);
                long newSubjectId = MoiUtils.persistSubject(getActivity(), name);

                if(newSubjectId != -1){
                    Toast.makeText(getActivity(), "SUBJECT ADDED", Toast.LENGTH_SHORT).show();
                    Log.i("WOURA", "SUBJECT ADDED");
                    Subject subject = new Subject(String.valueOf(newSubjectId), name, "#000");
                    mSubjectsArrayList.add(subject);
                    sortSubjects();
                    subjectsAdapter.notifyDataSetChanged();
                }else{
                    Toast.makeText(getActivity(), "SUBJECT ADDING FAILED", Toast.LENGTH_SHORT).show();
                    Log.i("WOURA", "FAILED TO ADDE SUBJECT");
                }
            }else{
                Toast.makeText(getActivity(), "Add subject cancelled", Toast.LENGTH_SHORT).show();
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
}
