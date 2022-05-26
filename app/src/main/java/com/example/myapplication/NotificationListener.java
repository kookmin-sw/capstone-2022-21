package com.example.myapplication;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationListener extends NotificationListenerService {


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("NotificationListener", "onDestroy()");
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        //Log notification
        Log.i("NotificationListener", "onNotificationPosted() - " + sbn.toString());
        Log.i("NotificationListener", "PackageName:" + sbn.getPackageName());
        Log.i("NotificationListener", "PostTime:" + sbn.getPostTime());

        //선언
        String packagename = sbn.getPackageName();

        //카카오톡일때만 동작
        if (!TextUtils.isEmpty(packagename) && packagename.equals("com.kakao.talk")) {

            Notification notificatin = sbn.getNotification();
            Bundle extras = notificatin.extras;
            String title = extras.getString(Notification.EXTRA_TITLE);
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT);

            if(text == null){
                return;
            }

            Intent msg = new Intent("Msg");
            msg.putExtra("subtext", subText);
            msg.putExtra("title", title);
            msg.putExtra("text", text);

            LocalBroadcastManager.getInstance(this).sendBroadcast(msg);




            Log.i("NotificationListener", "Title(이름):" + title);
            Log.i("NotificationListener", "Text(메세지):" + text);
            Log.i("NotificationListener", "Sub Text(채팅방이름):" + subText);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i("NotificationListener", "onNotificationRemoved() - " + sbn.toString());
    }


}
