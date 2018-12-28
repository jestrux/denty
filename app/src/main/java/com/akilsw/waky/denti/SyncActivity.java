package com.akilsw.waky.denti;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import com.akilsw.waky.denti.adapters.SyncAdapter;
import com.akilsw.waky.denti.models.ChatMessage;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import droidninja.filepicker.FilePickerBuilder;
import droidninja.filepicker.FilePickerConst;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SyncActivity extends AppCompatActivity {
    Socket socket;
    private ArrayList<String> filePaths;

    {
        try{
            socket = IO.socket("http://192.168.8.100:3000");
        }catch (Exception e){
            Log.d("WOURA", "Socket error: " + e.getMessage());
        }
    }
    private ArrayList<ChatMessage> mArrayList = new ArrayList<>();
    private SyncAdapter syncAdapter;
    @BindView(R.id.sync_rview)
    RecyclerView mSyncRecyclerView;

    @BindView(R.id.button_pick)
    Button pickBtn;

    @BindView(R.id.button_send)
    Button sendBtn;

    @BindView(R.id.new_message_input)
    EditText messageBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sync);
        ButterKnife.bind(this);

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filePaths != null && filePaths.size() > 0)
                    sendImage(filePaths.get(0));
                else
                    sendMessage();
            }
        });
        pickBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });
        syncAdapter = new SyncAdapter(this,mArrayList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mSyncRecyclerView.setLayoutManager(layoutManager);
        mSyncRecyclerView.setAdapter(syncAdapter);

        socket.connect();

        socket.on("message", handleIncomingMessages);
        socket.on("image", handleIncomingImages);
    }

    private Emitter.Listener handleIncomingMessages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String message, image;
                    Log.d("WOURA", "New message: ");
                    try{
                        Log.d("WOURA", "Message content: " + data.getString("message"));
                        message = data.getString("message");
                        addMessage(message);
                    }catch (Exception e){
                        Log.d("WOURA", "Error displaying message" + e.getMessage() );
                    }
                }
            });
        }
    };

    private Emitter.Listener handleIncomingImages = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String image;
                    Log.d("WOURA", "New Image: ");
                    try{
                        Log.d("WOURA", "Image string: " + data.getString("image"));
                        image = data.getString("image");
                        addImage(decodeImage(image));
                    }catch (Exception e){
                        Log.d("WOURA", "Error displaying message" + e.getMessage() );
                    }
                }
            });
        }
    };

    private void chooseImage(){
        FilePickerBuilder.getInstance()
                .enableCameraSupport(true)
                .showFolderView(false)
                .enableImagePicker(true)
                .setMaxCount(1)
                .pickPhoto(this);
    }

    private void sendMessage(){
        String message = messageBox.getText().toString();
        JSONObject sendData = new JSONObject();
        try {
            sendData.put("message", message);
            addMessage(message);
            socket.emit("message", sendData);
            messageBox.setText("");
        }catch (Exception e){
            return;
        }
    }

    private void sendImage(String path){
        JSONObject sendData = new JSONObject();
        try {
            Bitmap bm = decodeImage(encodeImage(path));
            addImage(bm);
            filePaths = new ArrayList<>();

            sendData.put("image", Base64.decode(toBase64(bm).getBytes("ISO-8859-1"), Base64.DEFAULT));
            socket.emit("image", sendData);
        }catch (Exception e){
            return;
        }
    }

    private void addMessage(String message) {
        ChatMessage m = new ChatMessage();
        m.setSender("Sender");
        m.setBody(message);
        mArrayList.add(m);
        syncAdapter.notifyDataSetChanged();
    }

    private void addImage(Bitmap bmp) {
        ChatMessage m = new ChatMessage();
        m.setSender("Sender");
        m.setImg(bmp);
        mArrayList.add(m);
        syncAdapter.notifyDataSetChanged();
    }

    private Bitmap decodeImage(String base64){
        byte[] b = Base64.decode(base64, Base64.DEFAULT);
        Bitmap bmp = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bmp;
    }

    private String encodeImage(String path){
        File imageFile = new File(path);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Bitmap bm = BitmapFactory.decodeStream(fis);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    private String toBase64(Bitmap bm){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        socket.disconnect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== RESULT_OK && data!=null){
            if(requestCode == FilePickerConst.REQUEST_CODE_PHOTO) {
                filePaths = new ArrayList<>();
                filePaths.addAll(data.getStringArrayListExtra(FilePickerConst.KEY_SELECTED_MEDIA));

                if(filePaths.size() > 0){
                    Log.d("WOURA", "Files url: " + filePaths.get(0));
                }else{
                    Toast.makeText(this, "No file chosen", Toast.LENGTH_SHORT).show();
                    Log.d("WOURA", "No file chosen");
                }
            }
        }
    }
}