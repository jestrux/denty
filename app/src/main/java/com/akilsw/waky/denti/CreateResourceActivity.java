package com.akilsw.waky.denti;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Resource;
import com.akilsw.waky.denti.ui.LinkPreviewFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;

public class CreateResourceActivity extends AppCompatActivity implements LinkPreviewFragment.LinkChangedListener{
    private final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int REQUEST_PERMISSIONS = 12;
    private SQLiteDatabase database;
    ArrayList<String> typeList = new ArrayList<String>();

    @BindView(R.id.name_input)
    EditText nameInput;

    @BindView(R.id.description_text)
    EditText descriptionInput;

    @BindView(R.id.description_label)
    TextView descriptionLabel;

    @BindView(R.id.image_gridview)
    GridView imagesGridView;

    @BindView(R.id.link_preview_placeholder)
    FrameLayout linkPreviewPlaceholder;

    int selectedType = 0;
    private ArrayList<String> filePaths = new ArrayList<>();
    private int mCurType;

    ItemsAdapter adapter;
    private FragmentManager fragmentManager;
    private int mCurLinkType;
    private String selectedDescription = "";

    private String linkTitle = "";
    private String linkImagePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_resource);
        ButterKnife.bind(this);
        DentyDbHelper dbhelper = new DentyDbHelper(this);
        database = dbhelper.getReadableDatabase();

        mCurType = R.id.res_type_ref;
        filePaths.add("");
        adapter = new ItemsAdapter(this, filePaths);
        imagesGridView.setAdapter(adapter);

        descriptionInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                selectedDescription = editable.toString();
            }
        });

        fragmentManager = getSupportFragmentManager();
    }

    public void chooseFile(Boolean perm_checked) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(!perm_checked && !hasFilePermission()){
                requestPermissions(perms, REQUEST_PERMISSIONS);
                return;
            }
        }

        FilePickerBuilder builder = FilePickerBuilder.getInstance();
        builder.setMaxCount(5);

        if(selectedType == Constants.RESOURCE_TYPE_IMAGE){
            builder.pickPhoto(this);
        }else if(selectedType == Constants.RESOURCE_TYPE_FILE){
            builder.pickFile(this);
        }
    }

    public void changeResourceType(View view){
        if (view.getId() == R.id.res_type_image){
            filePaths.clear();
            adapter.notifyDataSetChanged();
        }

        findViewById(R.id.res_type_ref).setAlpha(0.3f);
        findViewById(R.id.res_type_link).setAlpha(0.3f);
        findViewById(R.id.res_type_vid).setAlpha(0.3f);
        findViewById(R.id.res_type_image).setAlpha(0.3f);

        mCurType = view.getId();

        switch (view.getId()){
            case R.id.res_type_ref : {
                selectedType = Constants.RESOURCE_TYPE_REFERENCE;
                findViewById(R.id.res_type_ref).setAlpha(1f);

                descriptionLabel.setVisibility(View.VISIBLE);
                descriptionInput.setVisibility(View.VISIBLE);
                imagesGridView.setVisibility(View.GONE);
                linkPreviewPlaceholder.setVisibility(View.GONE);

                descriptionLabel.setText("Description");
                descriptionInput.setHint("Enter description or hold to paste it.");

                final float scale = getResources().getDisplayMetrics().density;
                int pixels = (int) (200 * scale + 0.5f);

                descriptionInput.setHeight(pixels);
            }
            break;

            case R.id.res_type_link : {
                selectedType = Constants.RESOURCE_TYPE_LINK;
                mCurLinkType = LinkPreviewFragment.PAGE_TYPE_OTHER;
                findViewById(R.id.res_type_link).setAlpha(1f);
                showPreviewer();
            }
            break;

            case R.id.res_type_vid : {
                selectedType = Constants.RESOURCE_TYPE_VIDEO;
                mCurLinkType = LinkPreviewFragment.PAGE_TYPE_YOUTUBE;
                findViewById(R.id.res_type_vid).setAlpha(1f);

                showPreviewer();
            }
            break;

            case R.id.res_type_image : {
                selectedType = Constants.RESOURCE_TYPE_IMAGE;
                findViewById(R.id.res_type_image).setAlpha(1f);

                mCurType = R.id.res_type_image;
                descriptionLabel.setText("Chosen Images");
                descriptionInput.setVisibility(View.GONE);
                linkPreviewPlaceholder.setVisibility(View.GONE);
                imagesGridView.setVisibility(View.VISIBLE);

                chooseFile(false);
            }
            break;

            case R.id.res_type_doc : {
                selectedType = Constants.RESOURCE_TYPE_FILE;
                FilePickerBuilder.getInstance().setMaxCount(3)
                        .pickFile(this);
            }
            break;

            default:
                Toast.makeText(getBaseContext(), "Change type", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPreviewer(){
        descriptionLabel.setVisibility(View.GONE);
        descriptionInput.setVisibility(View.GONE);
        imagesGridView.setVisibility(View.GONE);
        linkPreviewPlaceholder.setVisibility(View.VISIBLE);

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction
                .replace(R.id.link_preview_placeholder, LinkPreviewFragment.newInstance(mCurLinkType))
                .commit();
    }

    public void closeActivity(View view){
        finish();
    }

    public void submitData(View view){
        String title = nameInput.getText().toString();
        if(title.length() < 1){
            Toast.makeText(getBaseContext(), "Please enter resource title", Toast.LENGTH_SHORT).show();
            return;
        }else if((selectedType == Constants.RESOURCE_TYPE_LINK || selectedType == Constants.RESOURCE_TYPE_VIDEO) && selectedDescription.length() < 1){
            String desc_message = "";

            switch (selectedType){
                case Constants.RESOURCE_TYPE_LINK:
                    desc_message = "Please enter website link.";
                    break;
                case Constants.RESOURCE_TYPE_VIDEO:
                    desc_message = "Please enter video URL.";
                    break;
            }
            Toast.makeText(getBaseContext(), desc_message, Toast.LENGTH_SHORT).show();

            return;
        }else if(selectedType == Constants.RESOURCE_TYPE_IMAGE && filePaths.size() < 1){
            Toast.makeText(getBaseContext(), "Please pick some images.", Toast.LENGTH_SHORT).show();
            return;
        }

        Resource resource = new Resource();
        resource.setTitle(title);
        resource.setDescription(selectedDescription);
        resource.setType(selectedType);
        resource.setCreatedAt(Calendar.getInstance().getTimeInMillis());
        ContentValues cv = resource.toContentValues();

        try {
            long new_resource_id = database.insert(DentyContract.ReferencesEntry.TABLE_NAME, null, cv);

            if(new_resource_id == -1){
                Toast.makeText(this, "Sorry! Couldn't save resource", Toast.LENGTH_LONG).show();
            }else{
                resource.setId((int) new_resource_id);

                if(selectedType == Constants.RESOURCE_TYPE_IMAGE){
                    storeImages(resource);
                }
                else if(selectedType == Constants.RESOURCE_TYPE_LINK){
                    storeLinkMeta(resource);
                }else{
                    Toast.makeText(this, "Resource saved.", Toast.LENGTH_LONG).show();
                    returnResults(resource);
                }
            }
        } catch (Exception e) {
            Log.d("WOURA", "Error inserting session: " + e.getMessage());
        }
    }

    private void storeImages(Resource resource){
        for (int i = 0; i < filePaths.size(); i++){
            String path = filePaths.get(i);

            ContentValues subValues = new ContentValues();
            subValues.put(DentyContract.FilesEntry.COLUMN_NAME, "");
            subValues.put(DentyContract.FilesEntry.COLUMN_PATH, path);
            subValues.put(DentyContract.FilesEntry.COLUMN_RESOURCE_ID, resource.getId());
            subValues.put(DentyContract.FilesEntry.COLUMN_IDX, i);

            try {
                long new_file = database.insert(DentyContract.FilesEntry.TABLE_NAME, null, subValues);
                if(new_file != -1){
                    resource.addFile(path);
                    Log.d("WOURA", "File saved.");
                }else{
                    database.execSQL("DELETE FROM " + DentyContract.ReferencesEntry.TABLE_NAME + " WHERE _id = "+ resource.getId() +";");
                    Toast.makeText(this, "Failed to save resource.", Toast.LENGTH_LONG).show();
                    return;
                }
            } catch (Exception e) {
                database.execSQL("DELETE FROM " + DentyContract.ReferencesEntry.TABLE_NAME + " WHERE _id = "+ resource.getId() +";");
                Toast.makeText(this, "Failed to save resource.", Toast.LENGTH_LONG).show();
                Log.d("WOURA", "Error saving file: " + e.getMessage());

                return;
            }
        }

        Toast.makeText(this, "Resource saved.", Toast.LENGTH_LONG).show();
        returnResults(resource);
    }

    private void storeLinkMeta(Resource resource){
        if((linkTitle != null && linkTitle.length() > 0) || (linkImagePath != null && linkImagePath.length() > 0)){
            ContentValues subValues = new ContentValues();
            subValues.put(DentyContract.FilesEntry.COLUMN_NAME, linkTitle);
            subValues.put(DentyContract.FilesEntry.COLUMN_PATH, linkImagePath);
            subValues.put(DentyContract.FilesEntry.COLUMN_RESOURCE_ID, resource.getId());
            subValues.put(DentyContract.FilesEntry.COLUMN_IDX, 1);

            try {
                long new_file = database.insert(DentyContract.FilesEntry.TABLE_NAME, null, subValues);
                if(new_file != -1){
                    resource.addFile(linkImagePath);
                    Log.d("WOURA", "File saved.");
                }
            } catch (Exception e) {
                Log.d("WOURA", "Error saving file: " + e.getMessage());
            }

            Toast.makeText(this, "Resource saved.", Toast.LENGTH_LONG).show();
            returnResults(resource);
        }else{
            Toast.makeText(this, "Resource saved.", Toast.LENGTH_LONG).show();
            returnResults(resource);
        }
    }

    private void copyImages(ArrayList<String> paths){
        String mBasePath = Environment.getExternalStorageDirectory().getPath();
        File appFolder = new File(mBasePath, Constants.APP_FOLDER);

        for (String path : paths){
            File dummy_file = new File(appFolder, path);
            try {
                boolean new_file_created = false;
                String mimeType = URLConnection.guessContentTypeFromName(dummy_file.getName());
                mimeType = mimeType.replace("image/", ".");

                String file_name = Calendar.getInstance().getTimeInMillis() + mimeType;
                File dest_file = new File(appFolder, "resource_" + file_name);

                Uri imageUri = Uri.fromFile(new File(path));
                InputStream inputStream = getContentResolver().openInputStream(imageUri);

                MoiUtils.copyFile((FileInputStream) inputStream, dest_file);

                filePaths.add(dest_file.getAbsolutePath());
                adapter.notifyDataSetChanged();
                Log.d("WOURA", "Image copied.");
            } catch (Exception e) {
                Log.d("WOURA", "Error copying file: " + e.getMessage());
            }
        }
    }

    private void returnResults(Resource resource){
        Intent result = new Intent("com.example.RESULT_ACTION");
        result.putExtras(resource.toBundle());
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onLinkChanged(String link) {
        selectedDescription = link;
    }

    @Override
    public void onMetaChanged(String title, String image) {
        linkTitle = title;
        linkImagePath = image;
    }

    @Override
    public void onSwitchToYoutube() {
        findViewById(R.id.res_type_link).setAlpha(0.3f);
        findViewById(R.id.res_type_vid).setAlpha(1f);

        selectedType = Constants.RESOURCE_TYPE_VIDEO;
        mCurLinkType = LinkPreviewFragment.PAGE_TYPE_YOUTUBE;
    }

    private class ItemsAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<String> items;

        ItemsAdapter(Context context, ArrayList<String> paths) {
            mContext = context;
            items = paths;
        }

        public int getCount() {
            return items.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            SquareImageView iv;

            if (convertView == null) {
                iv = new SquareImageView(mContext);
                iv.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
            }else{
                iv = (SquareImageView) convertView;
            }

            if (items.get(position).length() < 1){
                iv.setBackgroundColor(Color.parseColor("#DDDDDD"));
                iv.setPadding(50, 50, 50, 50);
                Glide.with(getBaseContext())
                        .load(R.drawable.ic_add_item)
                        .into(iv);
            }else{
                Glide.with(getBaseContext())
                        .load(items.get(position))
                        .into(iv);
            }

            iv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (position == 0){
                        FilePickerBuilder.getInstance().setMaxCount(5)
                                .pickPhoto(CreateResourceActivity.this);
                        return;
                    }
                    filePaths.remove(position);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(mContext, "Image removed", Toast.LENGTH_SHORT).show();
                }
            });

            return iv;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== RESULT_OK && data!=null){
            if(requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
                ArrayList<String> file_list = new ArrayList<String>();
                file_list.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

                if(file_list.size() > 0)
                    copyImages(file_list);
                else
                    Toast.makeText(getBaseContext(), "No images chosen", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public boolean hasFilePermission() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String perm : perms){
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                                    chooseFile(false);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show();

                    return;
                }
            }

            chooseFile(true);
        } else {
            new AlertDialog.Builder(activity)
                    .setTitle("You rejected permission!!")
                    .setMessage("This feature won't work without the requested permissions, please reconsider.")
                    .setPositiveButton("Okay",  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            chooseFile(false);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setCancelable(false)
                    .show();
        }
    }
}
