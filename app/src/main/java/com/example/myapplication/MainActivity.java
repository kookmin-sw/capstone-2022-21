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
    Button bluetooth_TTS_btn, mode_btn, announce;
    Switch onoff;
    boolean isOnApp;
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

//        BackgroundThread thread = new BackgroundThread();
//        thread.start();

        onoff = (Switch) findViewById(R.id.onoff);
        if(onoff!=null) {
            onoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                /**
                 * Called when the checked state of a compound button has changed.
                 *
                 * @param buttonView The compound button view whose state has changed.
                 * @param isChecked  The new checked state of buttonView.
                 */
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        onoff.setText("톡닥톡닥 켜짐");
                        isOnApp = true;

                    } else {
                        onoff.setText("톡닥톡닥 꺼짐");
                        isOnApp = false;
                    }
                }
            });
        }



        mode_btn = (Button) findViewById(R.id.mode_btn);

        mode_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        announce = (Button) findViewById(R.id.anounce);

        announce.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show();
            }
        });

        ImageButton kakao_login_button = (ImageButton)findViewById(R.id.kakao_login_button);

        kakao_login_button.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                // 카카오톡으로 로그인
                if(UserApiClient.getInstance().isKakaoTalkLoginAvailable(MainActivity.this)){
                    login();
                }
                else{
                    accountLogin();
                }
            }
        });

        boolean isPermissionAllowed = isNotiPermissionAllowed();

        if(!isPermissionAllowed) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        LocalBroadcastManager.getInstance(this).registerReceiver(onNotice, new IntentFilter("Msg"));



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
            if(isOnApp) {
                String kakaosubtext = intent.getStringExtra("subtext");
                String kakaotitle = intent.getStringExtra("title");
                String kakaotext = intent.getStringExtra("text");

                sentence = kakaotitle + "님께서 " + kakaosubtext + "톡방에 " + kakaotext + "라고 메세지를 보냈습니다";

                if (kakaosubtext == null) {
                    sentence = kakaotitle + "님께서" + kakaotext + "라고 메세지를 보냈습니다";
                }
                tts.speakMessage(sentence);
            }
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
                    Log.w("getKeyHash", "Unable to get MessageDigest. signature="+signature, e);
                }
            }
        }catch(PackageManager.NameNotFoundException e){
            Log.w("getPackageInfo", "Unable to getPackageInfo");
        }
        return null;
    }

    public void show(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("앱 소개");
        builder.setMessage("본 앱은 카카오톡 메시지 음성안내및 답장 기능을 위해 제작된 어플리케이션입니다.");
        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();
    }
}