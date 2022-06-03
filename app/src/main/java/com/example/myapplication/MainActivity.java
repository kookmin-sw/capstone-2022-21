package com.example.myapplication;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.common.util.Utility;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    String sentence;
    Button bluetooth_TTS, mode, announce;
    ImageButton kakao_login_button;

    private TextToSpeach Tts;
    private KakaoService service;
    private SpeechToText stt;
    private TextToSpeech tts;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //kakao 서비스 객체 생성
        service = new KakaoService();
        // TTS 객체 생성
        Tts = new TextToSpeach();
        // STT 객체 생성
        stt = new SpeechToText();

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

        String keyHash = Utility.INSTANCE.getKeyHash(this.getApplicationContext());
        Log.d("KeyHash", getKeyHash());
        Log.e("KeyHash", keyHash);


        //Notification-카카오톡 메세지 수신 대기
        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));

        //모드 설정 버튼 - 설정 페이지 이동
        mode = (Button) findViewById(R.id.mode_btn);

        mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        //블루투스와 TTS 세부 설정 버튼 - 설정 페이지 전환
        bluetooth_TTS = (Button) findViewById(R.id.bluetooth_TTS_btn);

        bluetooth_TTS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothActivity.class);
                startActivity(intent);
            }
        });

        //앱소개 - 앱 소개 알림창 제시
        announce = (Button) findViewById(R.id.anounce_btn);

        announce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

        //카카오톡 로그인 버튼 - 답장하기위해 필수
        kakao_login_button = (ImageButton)findViewById(R.id.kakao_login_btn);

        kakao_login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)){
                    //카카오톡 앱으로 로그인
                    login();
                }
                else{
                    //카카오톡 웹으로 로그인인
                   accountLogin();
                }
            }
        });
        ///버튼 등록 끝


        //Notification 허락 여부 확인
        boolean isPermissionAllowed = isNotiPermissionAllowed();

        if(!isPermissionAllowed) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

    }

    //Noti 권한 확인
    private boolean isNotiPermissionAllowed() {
        Set<String> notiListenerSet = NotificationManagerCompat.getEnabledListenerPackages(this);
        //Notification권한이 있는 경우
        if(notiListenerSet.contains(getPackageName())) {
            return true;
        } else {
            return false;
        }
    }

    //noti listener 감지시 동작할 브로트캐스트 리시버
    private BroadcastReceiver onNotice= new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i("MainBroadcast","앱 기능 켜짐");

            String kakaosubtext = intent.getStringExtra("subtext");
            String kakaotitle = intent.getStringExtra("title");
            String kakaotext = intent.getStringExtra("text");

            sentence = kakaotitle + "님께서 " + kakaosubtext + "톡방에 " + kakaotext + "라고 메세지를 보냈습니다";

            if (kakaosubtext == null) {
                sentence = kakaotitle + "님께서 " + kakaotext + "라고 메세지를 보냈습니다";
            }

            Log.i("MainTTS", sentence);


            tts.speak(sentence,TextToSpeech.QUEUE_ADD, null);;
//            Tts.speakMessage(sentence);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    tts.speak("답장을 하시겠습니까?",TextToSpeech.QUEUE_ADD, null);
                }
            },5000);

            String response = stt.checkResponse();

            tts.speak(response + " 라고 " + kakaotitle + "님께 보내시겠습니다",TextToSpeech.QUEUE_ADD, null);

            service.sendmessage(kakaotitle,response);

        }

    };

    //카카오톡 앱 로그인 함수
    public void login(){
        String TAG = "login()";
        UserApiClient.getInstance().loginWithKakaoTalk(MainActivity.this,(oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "로그인 실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());
                getUserInfo();
            }
            return null;
        });
    }

    //카카오톡 웹 로그인
    public void accountLogin(){
        String TAG = "accountLogin()";
        UserApiClient.getInstance().loginWithKakaoAccount(this,(oAuthToken, error) -> {

            if (error != null) {
                Log.e(TAG, "로그인 실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공 : " + oAuthToken.getAccessToken());

                getUserInfo();

            }
            return null;
        });
    }

    public void getUserInfo(){
        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Toast.makeText(this, "사용자 정보 접근 거부", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("로그인 완료" + user);

                Account user1 = user.getKakaoAccount();
                System.out.println("사용자 계정" + user1);
            }
            return null;
        });
    }

    public String getKeyHash(){
        try{
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_SIGNATURES);
            if(packageInfo == null) return null;
            for(Signature signature: packageInfo.signatures){
                try{
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    return android.util.Base64.encodeToString(md.digest(), Base64.NO_WRAP);
                }catch (NoSuchAlgorithmException e){
                    Log.w("getkeyhash", " MessageDigest Null, signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getkeyhash", "getPackageInfo NULL");
        }
        return null;
    }

    //앱 소개 알림창
    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("앱 소개");
        builder.setMessage("본 앱은 카카오톡 메시지 음성 안내 및 답장 기능을 위해 제작된 어플리케이션입니다. 음성 안내를 받고 싶다면 알림 권한을 허용해 주시고 답장하길 원하신다면 앱에 로그인 해주세요.\n!받으시는 분 또한 앱에 로그인하셔야 합니다.");
        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }
}