package com.example.myapplication;


import android.app.Application;

import com.kakao.sdk.common.KakaoSdk;

public class GlobalApplication extends Application {
    public static GlobalApplication instance;

    @Override
    public void onCreate(){
        super.onCreate();
        instance = this;

        KakaoSdk.init(this, "31f865ddf3997ba9f6361f5bae0fb4d1");
    }

}
