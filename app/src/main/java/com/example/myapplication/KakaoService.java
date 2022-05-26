package com.example.myapplication;

import android.content.Intent;
import android.content.IntentFilter;
import android.se.omapi.Session;
import android.util.Log;
import android.widget.Adapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.kakao.sdk.auth.TokenManageable;
import com.kakao.sdk.auth.model.OAuthToken;
import com.kakao.sdk.talk.TalkApiClient;
import com.kakao.sdk.talk.model.Friend;
import com.kakao.sdk.talk.model.Friends;
import com.kakao.sdk.talk.model.MessageSendResult;
import com.kakao.sdk.template.model.DefaultTemplate;
import com.kakao.sdk.template.model.Link;
import com.kakao.sdk.template.model.TextTemplate;
import com.kakao.sdk.user.UserApiClient;
import com.kakao.sdk.auth.AuthApi;
import com.kakao.sdk.auth.AuthApiClient;
import com.kakao.sdk.auth.AuthApiManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import kotlin.Unit;
import kotlin.jvm.functions.Function2;

public class KakaoService {
    Adapter adapter;

    //send to me 나에게 카톡보내기
    public void sendmessage2(){
        DefaultTemplate defaulttxt = new TextTemplate("abc",new Link());
        TalkApiClient.getInstance().sendDefaultMemo(defaulttxt,(error) -> {
            return Unit.INSTANCE;
        });

    }

    //send to friend 친구에게 카톡 보내기
    public void sendmessage(String person){
        DefaultTemplate defaulttxt = new TextTemplate("톡닥톡닥",new Link());
        TalkApiClient.getInstance().friends(new Function2<Friends<Friend>, Throwable, Unit>() {
            @Override
            public Unit invoke(Friends<Friend> friendFriends, Throwable throwable) {
                if (friendFriends != null) {
                    int num = friendFriends.getTotalCount();

                    Log.i("kakaoservice","친구 : " + friendFriends);

                    for (int i = 0; i < num; i++) {
                        if (person.equals(friendFriends.getElements().get(i).getProfileNickname())) {
                            num = i;
                            Log.i("kakaoservice", person + "의 getuuid : " + friendFriends.getElements().get(i).getUuid());
                            break;
                        }
                    }

                    try {
                        List<String> uuid = Collections.singletonList(friendFriends.getElements().get(num).getUuid());
                        TalkApiClient.getInstance().sendDefaultMessage(uuid, defaulttxt,(result, error) ->{
                            //{"object_type": "text",  "text": "톡닥",  "link": {    "mobile_web_url": " "  }   }
                            //uuid = ["uuid"]
                            return Unit.INSTANCE;
                        });
                    } catch (Exception e) {
                        Log.e("kakaoservice", "친구에게 메세지 보내기 실패");
                    }
                } else {
                    Log.e("kakaoservice", "친구 없거나 권한 허락 안함");
                }
                if(throwable != null){
                    Log.e("kakaoservice",String.valueOf(throwable));
                }
//
//                friendFriends.getElements().stream().map((x)->{
//                    if(x.getProfileNickname() == person){
//                        TalkApiClient.getInstance().sendDefaultMessage(Arrays.asList(x.getUuid()),
//                                defaulttxt, (result, error)->{
//                                    return Unit.INSTANCE;
//                                });
//                        Log.i("kakaoservice", person + " : " + defaulttxt);
//                    }
//                    return null;
//                });

                return null;
            }
        });

    }




}