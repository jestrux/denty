package com.akilsw.waky.denti;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Waky on 11/28/2017.
 */

public class Utils {

    private Context context;
    private SharedPreferences sharedPref;

    private static final String KEY_SHARED_PREF = "PHOENIX_CHAT_SAMPLE";
    private static final int KEY_MODE_PRIVATE = 0;
    private static final String KEY_TOPIC = "topic";

    public Utils(Context context) {
        this.context = context;
        sharedPref = this.context.getSharedPreferences(KEY_SHARED_PREF, KEY_MODE_PRIVATE);
    }

    public void storeChannelDetails(final String topic) {
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(KEY_TOPIC, topic);
        editor.commit();
    }

    public String getTopic() {
        final String prevTopic = sharedPref.getString(KEY_TOPIC, null);
        return prevTopic != null ? prevTopic : context.getText(R.string.default_topic).toString();
    }

    public String getUrl() {
//        return context.getText(R.string.default_url).toString();
        return "http://192.168.8.100:4000";
    }
}