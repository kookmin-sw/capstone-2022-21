package com.example.myapplication;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    TextView title;
    TextView text1;
    TextView subtext;
    String sentence;
    private TextToSpeech tts;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = (TextView) findViewById(R.id.text1) ;
        title = (TextView) findViewById(R.id.title) ;
        subtext = (TextView) findViewById(R.id.subtext);

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        // TTS를 생성하고 OnInitListener로 초기화 한다.
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR) {
                    // 언어를 선택한다.
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });



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


            //test
            title.setText(kakaotitle);
            text1.setText(kakaotext);
            subtext.setText(kakaosubtext);

            sentence = kakaotitle + "님께서 " + kakaosubtext + "톡방에 " + kakaotext + "라고 메세지를 보냈습니다";

            tts.speak(sentence,TextToSpeech.QUEUE_FLUSH, null);




        }
    };
}