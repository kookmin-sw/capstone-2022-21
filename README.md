[![Review Assignment Due Date](https://classroom.github.com/assets/deadline-readme-button-22041afd0340ce965d47ae6ef1cefeee28c7c493a6346c4f15d667ab976d596c.svg)](https://classroom.github.com/a/E--3axVr)
[![Open in Visual Studio Code](https://classroom.github.com/assets/open-in-vscode-2e0aaae1b6195c2367325f4f02e2d04e9abb55f0b24a779b69b11b9e10269abc.svg)](https://classroom.github.com/online_ide?assignment_repo_id=7041693&assignment_repo_type=AssignmentRepo)
# 톡닥톡닥
# 프로젝트 소개
**1. 프로젝트 목표**

TTS를 활용한 카카오메세지 음성 안내 어플리케이션 제작

# 유사 앱 비교
**1. 바이보이스**
![image](https://user-images.githubusercontent.com/68101236/171630726-15acd173-537e-4e12-a738-868f9a06efb5.png)
- 상태바에 알림을 표시하는 앱이라면 그 알림을 음성으로 읽어 줍니다.
- 폰의 칼렌더의 특정 일정이 진행 중일 때에는 알림이 울리지 않도록 설정
- 원하지 않는 단어가 포함된 알림은 읽지 않도록 설정
- 잦은 알림을 발생시키는 앱의 알림은 몇 초의 간격을 두고 알림을 읽도록 설정

⇒ 타 어플리케이션과 구별점을 두기 위해 카카오톡 답장 기능을 구현하고자 계획했었다.

# 기능 구현
## 1. 블루투스 기능

- **Bluetooth**
    
    연결 요청, 연결 수락 및 데이터 전송과 같은 블루투스 통신을 수행
    
- **Acess_Fine_Location**
    
    블루투스 스캔을 사용하여 사용자 위치에 대한 정보를 수집
    
- **Bluetooth_Admin**
    
    기기 검색을 시작하거나 블루투스 설정을 조작
    
→ 참고자료:

[d](https://developer.android.com/guide/topics/connectivity/bluetooth?hl=ko#QueryPairedDevices)eveloper android

[안드로이드 블루투스 애플리케이션 만들기](https://bugwhale.tistory.com/11)

[[깡샘의 안드로이드 프로그래밍] 정리 29 - 블루투스](https://kkangsnote.tistory.com/47)

위에 브로드캐스트 리시버 활용 → 헤드셋 연결시에만 동작 기능 추가

[](https://developer.android.com/guide/topics/connectivity/bluetooth?hl=ko#QueryPairedDevices)

⇒ 블루투스 기능은 껏다켰다 정도만 AVD로 확인할 수 있고 그 외 활동은 **실물 휴대폰에서만 시연 가능하다.** 본래 구현하던 팀원은 아이폰 사용자로 실제 테스트할 기기를 구하지 못해 중간에 교체되었다.

## 2.카카오톡 오픈 API
: 카카오 메시지 답장하는 기능 제작

**1) API 신청**

- 참조
    1. API 신청방법
        
        [카카오 OpenAPI 사용하기 (준비 사항 : 인증키 발급)](https://ai-creator.tistory.com/20)
        
    2. 릴리즈 키 발급
        
        반드시 팀원 모두의 툴에 키를 만들어 등록해야한다.
        
        [[Android Studio] 릴리즈 키 해시(Key Hash) 등록 및 release APK 빌드](https://songjang.tistory.com/53)
        
    3. 카카오 디벨로퍼 싸이트
        
        [](https://bora-dev.tistory.com/11)
        
        해당 싸이트에 관련 정보를 쉽게 찾을 수 있다.
        

에러:  매우 까다롭다.. 새롭게 영상을 만들고자 이전 영상을 지운뒤 키해시 문제로 어플을 실행하지 못해 **발표날 영상을 제출할 수 없었다..**

**2) 로그인**

카카오 로그인 기능이 구현되어야 카카오 관련 API 들을 활용할 수 있다.  사용자가 앱에 로그인 할 때 필요한 권한을 요청한다. 

카카오 앱이 휴대폰에 설치되어있지 않을 경우 웹으로 우회하여 로그인 할 수 있도록 구현하였다.

```java
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
```

**3) 메시지**

1. 소셜(친구목록)
    1. 친구 검색 0건 **(팀원 등록 후에도 친구가 검색되지 않은 경우)**
        
        카카오에서 제공하는 REST API로 확인했을때 카카오 친구 검색 권한이 허용이었고 팀원을 등록했음에도 검색되지 않았다. 
        
    
    ![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/e8ac6df1-aeda-4844-8bb0-dddefa33cba6/Untitled.png)
    
    [카카오톡 소셜에 관하여 자주 하는 질문](https://kakao-tam.tistory.com/3)
    
    **⇒ 팀원 또한 앱에 로그인하고 권한을 수락해야 친구목록에 검색이 가능하다….!**
    
    즉, 답장을 보내는 사람과 받는 사람 모두 해당 어플에 가입되어있어야만 메시지를 전송할 수 있다. 앱의 가입과는 상관없이 누구에게나 답장할 수 있는, **우리가 원하는 형태와 조금 벗어난 기능이었다..** 
    
2. 메시지 전송 함수
    1. 나(본인)에게 전송하기
        
        ```java
        //send to me 나에게 카톡보내기
        public void sendmessage2(){
            DefaultTemplate defaulttxt = new TextTemplate("abc",new Link());
            TalkApiClient.getInstance().sendDefaultMemo(defaulttxt,(error) -> {
                return Unit.INSTANCE;
            });
        
        }
        ```
        
    2. 친구에게 전송하기
        
        ```java
        //send to friend 친구에게 카톡 보내기
            public void sendmessage(String person ,String msg){
        
                //기본 txt 메시지 템플릿
                DefaultTemplate defaulttxt = new TextTemplate(msg,new Link());
        
                //카카오톡 친구 찾기(검색)
                //uuid = ["uuid"]
                //***찾으려는 친구 역시 앱에 로그인해 권한 승인을 해주어야 검색할 수 있다
                TalkApiClient.getInstance().friends(new Function2<Friends<Friend>, Throwable, Unit>() {
                    @Override
                    public Unit invoke(Friends<Friend> friendFriends, Throwable throwable) {
                        if (friendFriends != null) {
        
                            int num = friendFriends.getTotalCount();
                            Log.i(Tag,"친구 : " + friendFriends);
        
                            for (int i = 0; i < num; i++) {
                                if (person.equals(friendFriends.getElements().get(i).getProfileNickname())) {
                                    num = i;
                                    Log.i(Tag, person + "의 getuuid : " + friendFriends.getElements().get(i).getUuid());
                                    break;
                                }
                            }
        
                            try {
                                List<String> uuid = Collections.singletonList(friendFriends.getElements().get(num).getUuid());
                                TalkApiClient.getInstance().sendDefaultMessage(uuid, defaulttxt,(result, error) ->{
        
                                    return Unit.INSTANCE;
                                });
                            } catch (Exception e) {
                                Log.e(Tag, "친구에게 메세지 보내기 실패");
                            }
                        } else {
                            Log.e("kakaoservice", "친구 없거나 권한 허락 안함");
                        }
                        if(throwable != null){
                            Log.e("kakaoservice",String.valueOf(throwable));
                        }
                        return null;
        
                    }
                });
        
            }
        ```
        
    
**3. 메시지 템플릿**
    1. REST API - 테스트용 템플릿(JSON)
        
        ```java
        //기본 txt 메시지 템플릿 - REST API 테스트용
        {"object_type": "text",  "text": "톡닥",  "link": {    "mobile_web_url": " "  }   }
        ```
        
        [Kakao Developers](https://developers.kakao.com/docs/latest/ko/message/rest-api#default-template-msg)
        
    2. 실제 앱에서 활용한 템플릿 - 텍스트 기본 템플릿
        
        ```java
        //기본 txt 메시지 템플릿 - REST API 테스트용
        DefaultTemplate defaulttxt = new TextTemplate(msg,new Link());
        //msg = "text", Link = 링크 역시 별도의 템플릿 존재
        ```
        
    
**4. REST API - 테스트**
    
    코드 오류인지  API관련 권한 문제인지 확인하는데 유용하다.
    
## 3. Notification service

: 카카오 메시지 수신시 해당 메시지 내용에 접근하는 방법

**1) 권한 설정**

간혹 앱에서 다른 앱의 알림(Notification)에 접근해야 하는 경우가 생긴다. 이 때 사용하는 클래스가 NotificationListenerService 이며, 해당 클래스를 구현해서 사용한다. 해당 클래스는 Service이기 때문에 AndroidManifest에 등록 후 사용해야 한다. permission으로는 android.permission.BIND_NOTIFICATION_LISTENER_SERVICE을 부여해줘야 하는데 해당 권한은 'signature'로 시스템에서 부여하는 권한으로 앱에서 임의로 권한을 획득할 수 없고, 사용자가 설정에서 부여해줘야 한다.

```java
<service
    android:name=".NotificationListener"
    android:label="Notification"
    android:enabled="true"
    android:exported="false"
    android:permission="android.permission.BIND_NOTIFICATION_LISTENER_SERVICE">
    <intent-filter>
        <action android:name="android.service.notification.NotificationListenerService" />
    </intent-filter>
</service>
```

인텐트필터를 설정하는 이유는 저 action으로 NotificationManager가 단말에 등록된 서비스를 찾는데 사용됨. 만약 인텐트필터를 등록하지 않으면 노티를 알림받지 못합니다.

```java
android:enabled="true"
android:exported="false"
```

왜인지 모르겠지만 위를 선언한 후에야 오류 제거됨

**2) import**

```
import android.app.Notification;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

```

**3) notification**

Notification내용을 받을 MainActivity에 BroadcastReciever를 만들고 IntentFilter(”msg”)선언

NotificationListenerService 클래스에서 LocalBroadcastManager를 통해 메시지의 칼럼+값 엑스트라를 넘기면 메인액티비티에서 BroadcastReceiver가 출력하여 올바르게 수신했는지 확인함

method

public void onNotificationPosted(StatusBarNotification sbn) : 카카오톡 미리알림 생성시 동작

                                                              Notification(카톡 내용)전달 함수 호출

***참조**

[https://kong-99.tistory.com/entry/Android-Studio-%EC%B9%B4%EC%B9%B4%EC%98%A4%EB%A1%9C%EA%B7%B8%EC%9D%B8API-%EC%97%B0%EB%8F%99%ED%95%98%EA%B8%B0-%EC%98%88%EC%A0%9C](https://kong-99.tistory.com/entry/Android-Studio-%EC%B9%B4%EC%B9%B4%EC%98%A4%EB%A1%9C%EA%B7%B8%EC%9D%B8API-%EC%97%B0%EB%8F%99%ED%95%98%EA%B8%B0-%EC%98%88%EC%A0%9C)

[https://hoyi327.tistory.com/64](https://hoyi327.tistory.com/64)

 :  카카오톡
 
# 개인 소감
회의에서 차별성을 자주 논의했었다.

흔한 기술이고 모두가 할 수 있는 일인만큼 차별성이 간절했기에 팀원과 함께 다양한 아이디어를 나열했다. 모두 구현하고 싶었지만 인적, 시간적 자원이 다른 팀보다 더 한정적이라 한가지만 추가하기로 했다. 2가지 기능-지정된 사람의 알림만 받는 기능과 stt로 답장하는 기능-사이 고민할 때 마침 교수님께서 방문하셔서 의견을 구할 수 있었다.

교수님께서는 운전 중 모든 수신 내용이 tts로 변환될 경우의 위험성을 언급하시며 앞의 기능을 추천해 주셨다. 운전을 하지 않아 한 번도 생각지 못한 가정이었고 그런 우리의 생각이 닿지 않는 곳을 짚어 주셔서 새삼스레 감탄했다. 그와 동시에 다양한 견해를 들을 수 없다는 작은 팀의 한계를 깨닫고 다양한 사례와 유사 앱들을 분석하여 그 점을 보완해가겠다고 다짐했다.

# 고려사항

1. 단톡방 무음설정 읽지 않는 기능
2. 시현-집중모드와 기본 모드의 차이 질문; 기본적 기능 또한 집중모드와 동일하게 사전에 설정된 사람만 읽어줌 
    1.  주현 : 집중모드시 이름만 안내
    2. 시현: 그 기능을 설정하는 버튼을 집중모드에 추가 하는 방식 제안
3. 앱 작동 버튼을 켜도 동작하지않을 시 안내메세지(~한 점들을 확인해주세요) 띄움
    
    ![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/b0d66059-5cab-4726-b88f-9f31f53f3fc8/Untitled.png)
    
    ![Untitled](https://s3-us-west-2.amazonaws.com/secure.notion-static.com/329dda0d-7578-4596-8e53-ad54a2bec736/Untitled.png)
    
4. 이모티콘 무시하는 기능
5. 주현 - 유사 앱과 우리 프로젝트를 구분지을 차별점 필요
    1. 주현, 시현 : 기존의 제시했던 stt 답장 기능 
6. 주현 - 카카오톡 대화 내용 가져올 방식 찾음
    1. notification(수신 내용을 미리보기로 제공) :어플리케이션과 별도로 관리되는 메세지NotificationListenerService를 통해 카톡 수신 메세지에 우회하여 접근하는 방식 
    2. 카톡 API - 카톡 친구 목록을 가져옴(카톡상 이름이 달라 엇갈리는 경우를 대비) TTS를 이용하며 답장을 하는 메시지 전송방식 등 추가할 기능들에 활용할 생각입니다.

##참조
1. 카카오톡 음성 stt API - 파이썬

[[python] 카카오 음성API STT (feat. postman)](https://park-duck.tistory.com/entry/python-%EC%B9%B4%EC%B9%B4%EC%98%A4-%EC%9D%8C%EC%84%B1API-STT-feat-postman)

2. 안드로이드 tts  java구현

[[Java][Android] 안드로이드 TTS](https://stickode.tistory.com/118)

3. 매너모드 - 장치 연결 확인

[[android] Bluetooth 장치가 연결되어 있는지 프로그래밍 방식으로 확인하는 방법은 무엇입니까?](http://daplus.net/android-bluetooth-%EC%9E%A5%EC%B9%98%EA%B0%80-%EC%97%B0%EA%B2%B0%EB%90%98%EC%96%B4-%EC%9E%88%EB%8A%94%EC%A7%80-%ED%94%84%EB%A1%9C%EA%B7%B8%EB%9E%98%EB%B0%8D-%EB%B0%A9%EC%8B%9D%EC%9C%BC%EB%A1%9C/)

4. 문전자 모드 자동 실행

Activity Recognition Transition API

[[Android] 사용자의 활동 상태(걷기, 자전거, 자동차 등)를 알려주는 Activity Recognition Transition API](https://readystory.tistory.com/198)

[](https://developer.android.com/guide/topics/location/transitions?hl=ko#setup)

5. tts

[[Java][Android] 안드로이드 TTS](https://stickode.tistory.com/118)

[[안드로이드] TextToSpeech 텍스트(Text)를 음성으로 전환시켜주는 방법](https://aries574.tistory.com/119)

6. NOTIFICATION

[[Android] NotificationListenerService(카카오톡 대화 반응하기)](https://blog.naver.com/PostView.nhn?blogId=vps32&logNo=221787113736)

[카톡이 오면 (Notification) 내용을 가져오려고 합니다](https://devtalk.kakao.com/t/notification/27017)

[안드로이드 - NotificationListener를 이용하여 노티 정보 받기](https://webcache.googleusercontent.com/search?q=cache:OdbO-Q6YfaMJ:https://codechacha.com/ko/notification-listener/+&cd=10&hl=ko&ct=clnk&gl=kr)

# 시연 영상
(https://youtube.com/shorts/1-8jTy7tvTc?feature=share)
**톡닥톡닥 시연 영상(TTS 음성안내 및 STT 카카오톡 답장 기능)**
(https://youtube.com/shorts/R_CfbEV7qKk?feature=share)
**톡닥톡닥 시연 영상(STT 답장 받는 시연 영상)**
# 팀 소개
팀원정보 및 담당이나 사진 및 sns.
# 사용법
소스코드제출시 설치법이나 사용법을 작성하세요.
# 추가적인 내용
추가적인 내용을 자유롭게 작성하세요
