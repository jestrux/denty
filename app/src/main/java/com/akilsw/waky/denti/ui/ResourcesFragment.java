package com.akilsw.waky.denti.ui;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.akilsw.waky.denti.Constants;
import com.akilsw.waky.denti.CreateResourceActivity;
import com.akilsw.waky.denti.R;
import com.akilsw.waky.denti.adapters.ResourcesAdapter;
import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Resource;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import eltos.simpledialogfragment.SimpleDialog;
import eltos.simpledialogfragment.list.SimpleListDialog;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Waky on 11/25/2017.
 */

public class ResourcesFragment extends MainSectionFragment implements CreateAssetDialog.CreateResourceDialogListener, ResourcesAdapter.ItemClickCallback, SimpleDialog.OnDialogResultListener {
    private static final String MANAGE_RESOURCE_DIALOG = "manage_resource_dialog";
    private static final String MANAGE_MULTIPLE_RESOURCE_DIALOG = "manage_multiple_resource_dialog";
    private final int NEW_RESOURCE = 17;
    private final String CHOICE_DIALOG = "choice_dialog";

    ArrayList<Resource> mScheduleArrayList = new ArrayList<>();
    Cursor mCursor;
    private ResourcesAdapter resourcesAdapter;
    private SQLiteDatabase database;
    private int chosen_id;

    @BindView(R.id.action_add)
    ImageView createInstanceBtn;

    @BindView(R.id.resources_fragment_wrapper)
    FrameLayout parentWrapper;

    @BindView(R.id.tab_layout)
    TabLayout tabLayout;

    @BindView(R.id.no_resources_tview)
    TextView noResourcesTextView;

    @BindView(R.id.resources_rview)
    RecyclerView mResourcesRecyclerView;
    private ArrayList<String> filePaths;
    private String ref_type;
    private CreateAssetDialog mDialog;
    private int mManageResourcePosition;
    ArrayList<Integer> firstTypeIds = new ArrayList<Integer>();
    private ArrayAdapter<String> filterListAdapter;

    public ResourcesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_references, container, false);
        ButterKnife.bind(this,view);

        DentyDbHelper dbhelper = new DentyDbHelper(getContext());
        database = dbhelper.getReadableDatabase();

//        database.execSQL("DELETE FROM " + DentyContract.FilesEntry.TABLE_NAME + " WHERE 1;");
//        database.execSQL("DELETE FROM " + DentyContract.ReferencesEntry.TABLE_NAME + " WHERE 1;");

        new FetchResourcesTask().execute();
        createInstanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createInstance();
            }
        });
        return view;
    }

    public void createInstance(){
        startActivityForResult(new Intent(getActivity(), CreateResourceActivity.class), NEW_RESOURCE);
    }

    private Cursor queryResources(){
        Cursor fc = null;

        String tables_str = DentyContract.ReferencesEntry.TABLE_NAME +
                " LEFT OUTER JOIN " + DentyContract.FilesEntry.TABLE_NAME +
                " ON " + DentyContract.ReferencesEntry.TABLE_NAME + "." + DentyContract.ReferencesEntry._ID +
                " = " + DentyContract.FilesEntry.TABLE_NAME + "." + DentyContract.FilesEntry.COLUMN_RESOURCE_ID;
        try {
            SQLiteQueryBuilder _QB = new SQLiteQueryBuilder();
            _QB.setTables(tables_str);

            _QB.setProjectionMap(DentyContract.ReferencesEntry.PROJECTION_MAP);
            String _OrderBy = DentyContract.ReferencesEntry.COLUMN_TYPE + " ASC, " + DentyContract.ReferencesEntry.COLUMN_CREATED_AT + " DESC";
            fc = _QB.query(database, DentyContract.ReferencesEntry.PROJECTION, null, null, null, null, _OrderBy);
        } catch (Exception e) {
            Log.d("WOURA", "Error fetching resources. \n\n " + e.getMessage());
        }

        return fc;
    }

    public void setData(Cursor c){
        mScheduleArrayList.clear();

        if(c.getCount() > 0){
            c.moveToFirst();
            int count = 0;
            while(!c.isAfterLast()) {
                int cid = c.getInt(0);
                String cname = c.getString(1);
                int ctype = c.getInt(4);
                String cpath = c.getString(6);
                String clinkName = c.getString(7);

                if(!c.isFirst()){
                    Resource lr = mScheduleArrayList.get(mScheduleArrayList.size() - 1);

                    if(cid == lr.getId()){
                        mScheduleArrayList.get(mScheduleArrayList.size() - 1).addFile(cpath);
                    }else{
                        Resource resource = Resource.fromCursor(c);
                        if (clinkName != null && cpath != null)
                            resource.addMeta(clinkName, cpath);
                        else if (clinkName == null && cpath != null)
                            resource.addFile(cpath);

                        mScheduleArrayList.add(resource);

                        if(ctype != lr.getType()){
                            setFirstOfType(ctype, mScheduleArrayList.size());
                            count++;
                        }
                    }
                }else{
                    Resource resource = Resource.fromCursor(c);
                    if (clinkName != null && cpath != null)
                        resource.addMeta(clinkName, cpath);
                    else if (clinkName == null && cpath != null)
                        resource.addFile(cpath);

                    setFirstOfType(ctype, mScheduleArrayList.size());
                    count++;
                    mScheduleArrayList.add(resource);
                }

                Resource ri = mScheduleArrayList.get(mScheduleArrayList.size() - 1);
                c.moveToNext();
            }

            mResourcesRecyclerView.setVisibility(View.VISIBLE);
            noResourcesTextView.setVisibility(View.GONE);
        }
        else{
            mResourcesRecyclerView.setVisibility(View.GONE);
            noResourcesTextView.setVisibility(View.VISIBLE);
        }

        mResourcesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        resourcesAdapter = new ResourcesAdapter(getContext(),mScheduleArrayList);
        resourcesAdapter.setItemClickCallback((ResourcesAdapter.ItemClickCallback) this);
        mResourcesRecyclerView.setAdapter(resourcesAdapter);

        if(firstTypeIds.size() > 1){
            tabLayout.setVisibility(View.VISIBLE);

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    mResourcesRecyclerView.smoothScrollToPosition(firstTypeIds.get(tab.getPosition()));
                    Log.d("WOURA", "POSITION:" + firstTypeIds.get(tab.getPosition()));
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });
        }
    }

    private void setFirstOfType(int ctype, int position) {
        switch (ctype){
            case Constants.RESOURCE_TYPE_REFERENCE:
                tabLayout.addTab(tabLayout.newTab().setText("References"));
                break;
            case Constants.RESOURCE_TYPE_IMAGE:
                tabLayout.addTab(tabLayout.newTab().setText("Image"));
                break;
            case Constants.RESOURCE_TYPE_FILE:
                tabLayout.addTab(tabLayout.newTab().setText("Pdf Files"));
                break;
            case Constants.RESOURCE_TYPE_LINK:
                tabLayout.addTab(tabLayout.newTab().setText("Links"));
                break;
            case Constants.RESOURCE_TYPE_VIDEO:
                tabLayout.addTab(tabLayout.newTab().setText("Youtube"));
                break;
        }

        firstTypeIds.add(position);
    }

    private boolean deleteResource(int id){
        return database.delete(DentyContract.ReferencesEntry.TABLE_NAME, DentyContract.ReferencesEntry._ID + "=" + id, null) > 0;
    }

    @Override
    public void onSubmitDialog(Resource activity) {
        ContentValues cv = activity.toContentValues();
        mDialog.dismiss();
    }

    @Override
    public void onItemLongClick(int p) {
        mManageResourcePosition = p;
        SimpleListDialog.build()
                .title("")
                .choiceMode(SimpleListDialog.SINGLE_CHOICE_DIRECT)
                .items(getActivity(), R.array.manage_resource_options)
                .show(this, MANAGE_RESOURCE_DIALOG);
    }

    @Override
    public void onItemClick(ArrayList<String> files) {
        if(files.size() == 1){
            openPdfFile(files.get(0));
            return;
        }
        SimpleListDialog.build()
                .title("Choose File")
                .choiceMode(SimpleListDialog.SINGLE_CHOICE_DIRECT)
                .items(files.toArray(new String[0]))
                .show(this, MANAGE_MULTIPLE_RESOURCE_DIALOG);
    }

    private void openPdfFile(String path){
        File file = new File(path);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        getActivity().startActivity(intent);
    }

    @Override
    public boolean onResult(@NonNull String dialogTag, int which, @NonNull Bundle extras) {
        if (which != SimpleDialog.OnDialogResultListener.CANCELED) {
            if (MANAGE_MULTIPLE_RESOURCE_DIALOG.equals(dialogTag)){
                ArrayList<String> labels = extras.getStringArrayList(SimpleListDialog.SELECTED_LABELS);

                if(labels!=null &&!labels.isEmpty()){
                    String path = String.valueOf(labels.get(0));
                    openPdfFile(path);
                }
            }

            if (MANAGE_RESOURCE_DIALOG.equals(dialogTag)){
                ArrayList<String> labels = extras.getStringArrayList(SimpleListDialog.SELECTED_LABELS);

                if(labels!=null &&!labels.isEmpty()){
                    String label = String.valueOf(labels.get(0));

                    switch (label) {
                        case "Edit":
                            Toast.makeText(getActivity(), "Edit resource.", Toast.LENGTH_SHORT).show();
                            break;
                        case "Delete":
                            Resource res = mScheduleArrayList.get(mManageResourcePosition);
                            if(res.getType() == Constants.RESOURCE_TYPE_REFERENCE || res.getType() == Constants.RESOURCE_TYPE_VIDEO){
                                database.delete(DentyContract.ReferencesEntry.TABLE_NAME, DentyContract.ReferencesEntry.COLUMN_ID + " = " + res.getId(), null);
                            }else{
                                ArrayList<String> resFiles = res.getFiles();
                                if(resFiles.size() > 0){
                                    database.delete(DentyContract.FilesEntry.TABLE_NAME, DentyContract.FilesEntry.COLUMN_RESOURCE_ID + " = " + res.getId(), null);

                                    for (String fpath : resFiles){
                                        File file = new File(fpath);
                                        Uri uri = Uri.fromFile(file);
                                        if (uri.getScheme() != null && (uri.getScheme().equals("content") || uri.getScheme().equals("file"))) {
                                            if (file.exists())
                                                file.delete();
                                        }
                                    }
                                }

                                database.delete(DentyContract.ReferencesEntry.TABLE_NAME, DentyContract.ReferencesEntry.COLUMN_ID + " = " + res.getId(), null);
                            }
                            mScheduleArrayList.remove(mManageResourcePosition);
                            resourcesAdapter.notifyDataSetChanged();
                            Toast.makeText(getActivity(), "Resource removed", Toast.LENGTH_SHORT).show();
                            break;
                        default:
                            break;
                    }
                }

                return true;
            }
        }

        return false;
    }

    private class FetchResourcesTask extends AsyncTask<String, String, Cursor> {
        private Cursor fetchCursor;
        ProgressDialog progressDialog;

        @Override
        protected Cursor doInBackground(String... params) {
            fetchCursor = queryResources();

            if (fetchCursor == null)
                return null;

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setData(fetchCursor);
                }
            });

            Log.d("WOURA", "Data was fetched: Found " + fetchCursor.getCount() + " Resources");

            return fetchCursor;
        }

        @Override
        protected void onPostExecute(Cursor result) {
            noResourcesTextView.setText(Html.fromHtml("You have no resources, click the <b><font color='black'>+</font></b></u> icon to add."));
        }

        @Override
        protected void onPreExecute() {
//            progressDialog = ProgressDialog.show(getContext(),
//                    "Fetching Resources",
//                    "Please Wait for sessions to get loaded.");
            noResourcesTextView.setText("Fetching Resources...");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== RESULT_OK && data!=null){
            if(requestCode == NEW_RESOURCE) {
                Log.d("WOURA", "Resource created!");
                //for image resources add images.
                Resource resource = Resource.fromBundle(data.getExtras());
                if(!firstTypeIds.contains(resource.getId())){
                    setFirstOfType(resource.getType(), tabLayout.getTabCount());
                    if(firstTypeIds.size() > 1){
                        tabLayout.setVisibility(View.VISIBLE);
                        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                            @Override
                            public void onTabSelected(TabLayout.Tab tab) {
                                mResourcesRecyclerView.smoothScrollToPosition(firstTypeIds.get(tab.getPosition()));
                            }

                            @Override
                            public void onTabUnselected(TabLayout.Tab tab) {

                            }

                            @Override
                            public void onTabReselected(TabLayout.Tab tab) {

                            }
                        });
                    }
                }

                mScheduleArrayList.add(resource);
                resourcesAdapter.notifyDataSetChanged();
                noResourcesTextView.setVisibility(View.GONE);
                mResourcesRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
}
