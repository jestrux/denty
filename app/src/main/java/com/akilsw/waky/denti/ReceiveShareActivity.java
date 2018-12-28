package com.akilsw.waky.denti;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.akilsw.waky.denti.ui.ResourcesFragment;
import com.bumptech.glide.Glide;
import com.akilsw.waky.denti.data.DentyContract;
import com.akilsw.waky.denti.data.DentyDbHelper;
import com.akilsw.waky.denti.models.Resource;
import com.leocardz.link.preview.library.LinkPreviewCallback;
import com.leocardz.link.preview.library.SourceContent;
import com.leocardz.link.preview.library.TextCrawler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ReceiveShareActivity extends AppCompatActivity implements View.OnClickListener {
    private final String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
    private static final int REQUEST_PERMISSIONS = 12;

    @BindView(R.id.submit_btn)
    Button submitBtn;

    @BindView(R.id.action_close)
    ImageView closeBtn;

    @BindView(R.id.shared_image)
    ImageView sharedImage;

    @BindView(R.id.resource_title)
    EditText resourceTitle;

    boolean dataFetched = false;

    File dest_file;
    private SQLiteDatabase database;
    private Uri imageUri;

    Resource mResource;
    private String linkTitle;
    private String linkImagePath;
    private String filePath;
    private ArrayList<String> filePaths = new ArrayList<>();
    private ProgressDialog progressDialog;
    private boolean multipleIntent = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receive_share);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        DentyDbHelper dbhelper = new DentyDbHelper(this);
        database = dbhelper.getReadableDatabase();
        submitBtn.setOnClickListener(this);
        closeBtn.setOnClickListener(this);
        mResource = new Resource();

        if (Intent.ACTION_SEND.equals(action) && type != null) {

            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if( !URLUtil.isValidUrl(sharedText)){
                    Toast.makeText(getApplicationContext(), "Not a valid URL.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                if (sharedText == null){
                    finish();
                    Toast.makeText(getApplicationContext(), "No string was shared!", Toast.LENGTH_SHORT).show();
                    return;
                }

                String yt_id = MoiUtils.extractYTId(sharedText);
                mResource.setDescription(sharedText);
                if(yt_id != null){
                    String yt_url = "https://i.ytimg.com/vi/"+ yt_id +"/maxresdefault.jpg";
                    mResource.setType(Constants.RESOURCE_TYPE_VIDEO);
                    Glide.with(getBaseContext())
                            .load(yt_url)
                            .into(sharedImage);

                    dataFetched = true;
                    submitBtn.setAlpha(1f);
                }else{
                    mResource.setType(Constants.RESOURCE_TYPE_LINK);
                    new FetchPreviewTask().execute(sharedText);
                }
            }
            else if ("application/pdf".equals(type)) {
                mResource.setType(Constants.RESOURCE_TYPE_FILE);
                imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);

                new SaveResourceFileTask().execute("single");
            }
            else if (type.startsWith("image/")){
                mResource.setType(Constants.RESOURCE_TYPE_IMAGE);
                imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                new SaveResourceFileTask().execute("single");
            }
        }else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            if ("application/pdf".equals(type)){
                mResource.setType(Constants.RESOURCE_TYPE_FILE);
                new SaveResourceFileTask().execute("multiple", "pdf");
            }
            else{
                mResource.setType(Constants.RESOURCE_TYPE_IMAGE);
                new SaveResourceFileTask().execute("multiple", "image");
            }
        }
    }

    private void storeImages(Resource resource){
        if(filePaths == null || filePaths.size() < 1){
            database.execSQL("DELETE FROM " + DentyContract.ReferencesEntry.TABLE_NAME + " WHERE _id = "+ resource.getId() +";");
            Toast.makeText(this, "Failed to save resource.", Toast.LENGTH_LONG).show();
        }

        for (String path : filePaths){
            if(!saveSingleImage(path, resource.getId())){
                database.execSQL("DELETE FROM " + DentyContract.ReferencesEntry.TABLE_NAME + " WHERE _id = "+ resource.getId() +";");
                Toast.makeText(this, "Failed to save resource.", Toast.LENGTH_LONG).show();
                return;
            }
        }

        saySaved(resource);
    }

    private boolean saveSingleImage(String path, int id){
        ContentValues subValues = new ContentValues();
        subValues.put(DentyContract.FilesEntry.COLUMN_NAME, "");
        subValues.put(DentyContract.FilesEntry.COLUMN_PATH, path);
        subValues.put(DentyContract.FilesEntry.COLUMN_RESOURCE_ID, id);
        subValues.put(DentyContract.FilesEntry.COLUMN_IDX, 0);

        try {
            long new_file = database.insert(DentyContract.FilesEntry.TABLE_NAME, null, subValues);
            if(new_file != -1){
                Log.d("WOURA", "File saved.");
                return true;
            }
        } catch (Exception e) {
            Log.d("WOURA", "Error saving file: " + e.getMessage());
        }

        return true;
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
                return;
            }

            saySaved(resource);
        }else{
            saySaved(resource);
        }
    }

    public String saveFile(Boolean perm_checked) {
        boolean perms_not_allowed = (!perm_checked && !hasFilePermission());
        if  (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && perms_not_allowed){
            Log.d("WOURA", "Imekosa permission!");
            requestPermissions(perms, REQUEST_PERMISSIONS);
        }else{
            Log.d("WOURA", "Imefika hapa!");
            try {
                String mBasePath = Environment.getExternalStorageDirectory().getPath();
                File appFolder = new File(mBasePath, Constants.APP_FOLDER);
                if(!appFolder.exists()){
                    Log.d("WOURA", "App folder doesn't exist, creating one....");
                    if(appFolder.mkdirs()){
                        Log.d("WOURA", "App folder was created....");
                    }
                }

                File dummy_file = new File(appFolder, "filename.asgdsag");
                if (!dummy_file.exists()){
                    dummy_file.createNewFile();
                }

                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                ContentResolver cR = getContentResolver();
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String mimeType = "." + mime.getExtensionFromMimeType(cR.getType(imageUri));

                Date d = new Date();
                dest_file = new File(appFolder, "resource_" + d.getTime() + mimeType);

                Log.d("WOURA", "File name: " + dest_file.getName());

                MoiUtils.copyFile((FileInputStream) inputStream, dest_file);

                filePath = dest_file.getAbsolutePath();
                Log.d("WOURA", "File was successfully copied!!");
                Log.d("WOURA", "File path is: " + dest_file.getAbsolutePath());

                return filePath;
            } catch (Exception e) {
                Log.d("WOURA", "Couldn't copy file!!");
                Log.d("WOURA", "Error: " + e.getMessage());
                return filePath;
            }
        }

        return null;
    }

    private void submitResource(){
        Resource r = mResource;
        r.setTitle(resourceTitle.getText().toString());
        Date d = new Date();
        r.setCreatedAt(d.getTime());

        ContentValues cv = r.toContentValues();
        try {
            long new_ref_id = database.insert(DentyContract.ReferencesEntry.TABLE_NAME, null, cv);

            if(new_ref_id == -1){
                Toast.makeText(this, "Sorry! Couldn't save resource", Toast.LENGTH_LONG).show();
            }else{
                r.setId((int) new_ref_id);

                if(r.getType() == Constants.RESOURCE_TYPE_IMAGE
                        || r.getType() == Constants.RESOURCE_TYPE_FILE){
                    storeImages(r);
                }else if(r.getType() == Constants.RESOURCE_TYPE_LINK){
                    if(linkImagePath !=null && linkTitle != null)
                        storeLinkMeta(r);
                    else
                        saySaved(r);
                }else{
                    saySaved(r);
                }
            }
        } catch (Exception e) {
            Log.d("WOURA", "Error inserting resource: " + e.getMessage());
        }
    }

    private void saySaved(Resource r){
        Toast.makeText(getApplicationContext(), r.getTitle() + " added to resources.", Toast.LENGTH_LONG).show();

        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("page", R.id.tab_refs);
        startActivity(i);
        finish();
    }

    private void showProgressDialog(String title, String message){
        progressDialog = ProgressDialog.show(ReceiveShareActivity.this, title, message);
    }

    private void hideProgressDialog(){
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private class SaveResourceFileTask extends AsyncTask<String, String, Boolean> {
        private boolean res;

        @Override
        protected Boolean doInBackground(String... params) {
            if(params[0].equals("multiple")){
                multipleIntent = true;
                ArrayList<Uri> imageUris = getIntent().getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                if (imageUris != null) {
                    for (Uri uri : imageUris){
                        imageUri = uri;
                        String saved_file = saveFile(false);
                        if(saved_file != null){
                            filePaths.add(saved_file);
                        }else{
                            return false;
                        }
                    }

                    return true;
                }
                return false;
            }else{
                String saved_file = saveFile(false);
                if(saved_file != null){
                    filePaths.add(saved_file);
                    return true;
                }
                return false;
            }
        }

        @Override
        protected void onPostExecute(final Boolean result) {
            dataFetched = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgressDialog();

                    if(result) {
                        submitBtn.setAlpha(1f);
                        if(mResource.getType() == Constants.RESOURCE_TYPE_IMAGE)
                            Glide.with(ReceiveShareActivity.this)
                                    .load(dest_file.getAbsoluteFile())
                                    .into(sharedImage);
                        else if(mResource.getType() == Constants.RESOURCE_TYPE_FILE)
                            Glide.with(ReceiveShareActivity.this)
                                    .load(MoiUtils.pdfPreview(ReceiveShareActivity.this, dest_file.getAbsoluteFile()))
                                    .into(sharedImage);
                    }else{
                        Toast.makeText(getApplicationContext(), "Resource not saved.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressDialog("Saving File", "Please Wait for file to be saved.");
                }
            });
        }
    }

    private class FetchPreviewTask extends AsyncTask<String, String, String> {
        private String filePath;

        @Override
        protected String doInBackground(String... params) {
            TextCrawler textCrawler = new TextCrawler();
            LinkPreviewCallback linkPreviewCallback = new LinkPreviewCallback() {
                @Override
                public void onPre() {
                    // Any work that needs to be done before generating the preview. Usually inflate
                    // your custom preview layout here.
                }

                @Override
                public void onPos(final SourceContent sourceContent, boolean b) {
                    final String title = sourceContent.getTitle();
                    final List<String> images = sourceContent.getImages();

                    Log.d("WOURA", "URL RESULTS: " + title);

                    if((title != null && title.length() > 0) && (images != null && images.size() > 0)){
                        linkTitle = title;
                        linkImagePath = images.get(0);
                    }
                    else if((title == null || title.length() < 1) && (images == null || images.size() < 1)){
                        Toast.makeText(getBaseContext(), "Couldn't find preview for link", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        if(title != null && title.length() > 0){
                            linkTitle = title;
                        }
                        if(images != null && images.size() > 0){
                            linkImagePath = images.get(0);


                        }
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(linkImagePath != null){
                                Glide.with(getBaseContext())
                                        .load(linkImagePath)
                                        .into(sharedImage);
                            }
                        }
                    });

                    dataFetched = true;
                }
            };

            textCrawler.makePreview( linkPreviewCallback, params[0]);

            return filePath;
        }

        @Override
        protected void onPostExecute(final String result) {
            dataFetched = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                hideProgressDialog();
                submitBtn.setAlpha(1f);
                }
            });
        }

        @Override
        protected void onPreExecute() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showProgressDialog("Fetching Preview", "Please wait for the preview to load.");
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.submit_btn){
            if(dataFetched)
                submitResource();
        }else if(view.getId() == R.id.action_close){
            if(dest_file != null){
                Log.d("WOURA", "Deleting file....");
                if(dest_file.delete()){
                    Log.d("WOURA", "File successfully deleted!");
                }else{
                    Log.d("WOURA", "Couldn't delete file.");
                }
            }else{
                Log.d("WOURA", "Nothing to delete, closing....");
            }
            finish();
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
    protected void onDestroy() {
        super.onDestroy();
        hideProgressDialog();
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
                                    saveFile(false);
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .setCancelable(false)
                            .show();

                    return;
                }
            }

            saveFile(true);
        } else {
            new AlertDialog.Builder(activity)
                    .setTitle("You rejected permission!!")
                    .setMessage("This feature won't work without the requested permissions, please reconsider.")
                    .setPositiveButton("Okay",  new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            saveFile(false);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .setCancelable(false)
                    .show();
        }
    }
}
