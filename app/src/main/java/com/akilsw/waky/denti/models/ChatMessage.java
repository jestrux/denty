package com.akilsw.waky.denti.models;

import android.graphics.Bitmap;

import java.util.Date;

/**
 * Created by Waky on 11/30/2017.
 */

public class ChatMessage {
    private String sender;
    private String body = null;
    private Bitmap img = null;
    private boolean from_me;
    private Date inserted_date;

    public ChatMessage() {
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public boolean isFromMe() {
        return from_me;
    }

    public void setFromMe(boolean from_me) {
        this.from_me = from_me;
    }

    public Date getInsertedDate() {
        return inserted_date;
    }

    public void setInsertedDate(Date inserted_date) {
        this.inserted_date = inserted_date;
    }

    public Bitmap getImg() {
        return img;
    }

    public void setImg(Bitmap img) {
        this.img = img;
    }
}
