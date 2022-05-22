package com.example.myapplication;

import static android.speech.tts.TextToSpeech.ERROR;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.user.model.Account;
import com.kakao.sdk.common.util.Utility;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    TextView title;
    TextView text1;
    TextView subtext;
    String sentence;
    private TextToSpeach tts;
    private KakaoService service;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //kakao 서비스 객체 생성
        service = new KakaoService();
        // TTS를 객체 생성.
        tts = new TextToSpeach();


        String keyHash = Utility.INSTANCE.getKeyHash(this.getApplicationContext());

        Log.d("KeyHash", getKeyHash());
        Log.e("KeyHash", keyHash);
        ImageButton kakao_login_button = (ImageButton)findViewById(R.id.kakao_login_button);
        kakao_login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                accountLogin();
                //}
            }
        });

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
        //Notification권한이 있는 경우
        if(notiListenerSet.contains(getPackageName())) {
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

            tts.speakMessage(sentence);
        }
    };

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

    public void accountLogin(){
        String TAG = "accountLogin()";
        UserApiClient.getInstance().loginWithKakaoAccount(this,(oAuthToken, error) -> {
            if (error != null) {
                Log.e(TAG, "로그인 실패", error);
            } else if (oAuthToken != null) {
                Log.i(TAG, "로그인 성공(토큰) : " + oAuthToken.getAccessToken());

                getUserInfo();

                service.sendmessage("안시현");

            }
            return null;
        });
    }

    public void getUserInfo(){
        UserApiClient.getInstance().me((user, meError) -> {
            if (meError != null) {
                Toast.makeText(this, "사용자 정보 접근 거부", Toast.LENGTH_SHORT).show();
            } else {
                System.out.println("로그인 완료");

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
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getPackageInfo", "Unable to getPackageInfo");
        }
        return null;
    }
}