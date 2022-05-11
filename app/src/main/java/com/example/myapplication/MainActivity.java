package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Set;

public class MainActivity extends AppCompatActivity {
    TextView title;
    TextView text1;
    TextView subtext;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = (TextView) findViewById(R.id.text1) ;
        title = (TextView) findViewById(R.id.title) ;
        subtext = (TextView) findViewById(R.id.subtext);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        boolean isPermissionAllowed = isNotiPermissionAllowed();

        if(!isPermissionAllowed) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }
    }

    private boolean isNotiPermissionAllowed() {
        Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);

        if (notiListenerSet.contains("com.kakao.talk")) {
            //카카오톡 권한 받음
            return true;
        } else {
            return false;
        }
    }

    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String kakaosubtext = intent.getStringExtra("subtext");
            String kakaotitle = intent.getStringExtra("title");
            String kakaotext = intent.getStringExtra("text");
            title.setText(kakaotitle);
            text1.setText(kakaotext);
            subtext.setText(kakaosubtext);


        }
    };
}