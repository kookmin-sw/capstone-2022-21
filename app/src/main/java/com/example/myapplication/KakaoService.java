package com.example.myapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kakao.sdk.talk.TalkApiClient;
import com.kakao.sdk.talk.model.Friend;
import com.kakao.sdk.talk.model.Friends;
import com.kakao.sdk.template.model.DefaultTemplate;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.TextTemplate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class KakaoService {
    Adapter adapter;

    public void sendmessage(String person){

        DefaultTemplate defaulttxt = new TextTemplate("abc",new Link());
        TalkApiClient.getInstance().sendDefaultMemo(defaulttxt,(error) -> {
            return Unit.INSTANCE;
        });

        TalkApiClient.getInstance().friends(new Function2<Friends<Friend>, Throwable, Unit>() {
            @Override
            public Unit invoke(Friends<Friend> friendFriends, Throwable throwable) {

                if (friendFriends.getElements().isEmpty()) {
                    Log.e("kakaoservice", "friendFriends is Empty");
                }else{
                    //상대방의 UUID를 통해 메세지 전송
                    friendFriends.getElements().stream().map((x)->{
                        if(x.getProfileNickname() == person){
                            TalkApiClient.getInstance().sendDefaultMessage(Arrays.asList(x.getUuid()),
                                    defaulttxt, (result, error)->{
                                        return Unit.INSTANCE;
                                    });
                            Log.i("kakaoservice", person + " : " + defaulttxt);
                        }
                        return null;
                    });


                }
                return null;
            }
        });

    }



}