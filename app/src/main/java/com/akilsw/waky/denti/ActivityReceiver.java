package com.akilsw.waky.denti;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.widget.Toast;

import com.akilsw.waky.denti.models.Activity;

/**
 * Created by Waky on 1/1/2018.
 */

public class ActivityReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("WOURA", "Alarm intent received.");

        if(intent.getAction() == null)
            return;

        if(intent.getAction().equals("com.akilsw.waky.activity_receiver")){
            Bundle b = intent.getExtras();
            Activity activity = Activity.fromBundle(b);

            String message = "Deadline for " + activity.getTitle() + " reached.";

            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            showNotification(context, message);
        }
    }

    public void showNotification(Context context, String message) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Drawable vectorDrawable = ResourcesCompat.getDrawable(context.getResources(), R.drawable.denty_logo_circle, null);
        Bitmap myLogo = ((BitmapDrawable) vectorDrawable).getBitmap();

        Intent i = new Intent(context, MainActivity.class);
        i.putExtra("page", R.id.tab_todos);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 100, i, PendingIntent.FLAG_UPDATE_CURRENT);
        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(context);
        builder.setContentIntent(pendingIntent)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_resources))
                .setSmallIcon(R.drawable.denty_logo_circle)
                .setContentTitle("Denty")
                .setContentText(message)
                .setAutoCancel(true);
        builder.setSound(alarmSound);
        notificationManager.notify(100, builder.build());
    }
}
