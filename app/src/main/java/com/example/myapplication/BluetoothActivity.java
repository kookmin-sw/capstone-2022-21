package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태

    private static final int BT_REQUEST_ENABLE = 1;

    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터

    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋

    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스

    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓

    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림

    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림

    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드

    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼

    private int readBufferPosition; // 버퍼 내 문자 저장 위치


    private Switch onOff_bt; // 블루투스 활성화 스위치

    private Button bluetooth_setting_btn; // 블루투스 설정하기 위한 버튼

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);

        //레이아웃 블루투스 버튼 연결
        onOff_bt = (Switch) findViewById(R.id.bluetooth_state);

        bluetooth_setting_btn = (Button) findViewById(R.id.bluetooth_setting_btn);

        onOff_bt.setOnCheckedChangeListener(new onOffSwitchListener());

        //블루투스 어댑터
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        setbluetooth();

        bluetooth_setting_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                //블루투스 연결 기기 확인

                selectBluetoothDevice();


            }
        });

    }

    //블루투스 브로드 캐스트 리시버 -
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)){
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        on();
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        off();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        on();
                        Toast.makeText(getApplicationContext(), "변경중", Toast.LENGTH_SHORT).show();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        off();
                        Toast.makeText(getApplicationContext(), "변경중", Toast.LENGTH_SHORT).show();
                        break;

                }
            }


        }
    };

    //브로드캐스트 리시버 종료
    protected void onDestroy() {
        Toast.makeText(getApplicationContext(), "onDestroy called", Toast.LENGTH_SHORT).show();
        Log.d("onDestroy", "onDestroy called");
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }



    //블루투스 초기 새팅
    void setbluetooth() {

        if (bluetoothAdapter == null){
            //디바이스 자체에 블루투스 기능이 없을 경우
            Toast.makeText( getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();

            //디바이스 사용 불가
            onOff_bt.setText("Disabled Device");

            onOff_bt.setEnabled(false);

            bluetooth_setting_btn.setEnabled(false);

        }
        else { // 디바이스가 블루투스를 지원 할 경우

            if(bluetoothAdapter.isEnabled()) { // 블루투스 활성화 상태
                on();
            }
            else { // 블루투스 비활성화 상태
                off();
            }

        }
    }

    void on() {

        // 활성화

        bluetoothAdapter.enable();

        onOff_bt.setText("ON");

        // 블루투스 스위치 on

        onOff_bt.setChecked(true);

        bluetooth_setting_btn.setEnabled(true);


        Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);

        IntentFilter BTIntent = new IntentFilter(bluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, BTIntent);


    }




    void off(){

        bluetoothAdapter.disable();

        bluetooth_setting_btn.setEnabled(false);

        onOff_bt.setText("OFF");

        // 블루투스 스위치 oFF

        onOff_bt.setChecked(false);


    }

    //블루투스 onoff 스위치 클릭시 동작
    class onOffSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked) {

                on();

                Toast.makeText(getApplicationContext(), "블루투스 ON", Toast.LENGTH_SHORT).show();


            }else {

                off();

                Toast.makeText(getApplicationContext(), "블루투스 OFF", Toast.LENGTH_SHORT).show();;
            }
        }
    }

    //검색 가능 상태인지 확인
    public void ensureDiscoverable(){
        //블루투스 디바이스 검색 상태
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE){
            //검색 가능 상태 허용 시스템 액티비티
            Intent discoverableIntent = new Intent(bluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            //extra_discoverable_duration : 검색가능한 상태 300초
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,300);
            startActivity(discoverableIntent);
        }
    }






    // 연결 기기 검색
    //특정 기기와 데이터 통신을 하려면 해당 기기와 페어링(pairing) 되어야 하는데,
    // 그러기위해선 먼저 페어링 된 기기 목록을 가져와야 합니다.
    public void selectBluetoothDevice() {

        // 이전에 페어링 되어있는 블루투스 기기를 찾습니다.

        devices = bluetoothAdapter.getBondedDevices();

        // 페어링 된 디바이스의 크기를 저장

        int pariedDeviceCount = devices.size();

        // 페어링 되어있는 장치가 없는 경우

        if (pariedDeviceCount == 0) {

            Toast.makeText(getApplicationContext(), "페어링된 기기가 없습니다.", Toast.LENGTH_SHORT).show();


        }

        // 페어링 되어있는 장치가 있는 경우

        else {

            // 디바이스를 선택하기 위한 다이얼로그 생성

            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("페어링 되어있는 디바이스 목록");

            // 페어링 된 각각의 디바이스의 이름과 주소를 저장

            List<String> list = new ArrayList<>();

            // 모든 디바이스의 이름을 리스트에 추가

            for (BluetoothDevice bluetoothDevice : devices) {

                list.add(bluetoothDevice.getName());

            }

            list.add("취소");


            // List를 CharSequence 배열로 변경

            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);

            list.toArray(new CharSequence[list.size()]);


            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너

            builder.setItems(charSequences, new DialogInterface.OnClickListener() {

                @Override

                public void onClick(DialogInterface dialog, int which) {

                    // 해당 디바이스와 연결하는 함수 호출

                    connectDevice(charSequences[which].toString());

                }

            });


            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정

            builder.setCancelable(false);

            // 다이얼로그 생성

            AlertDialog alertDialog = builder.create();

            alertDialog.show();

        }
    }



    // 해당 기기와 연결
    public void connectDevice(String deviceName) {

        // 페어링 된 디바이스들을 모두 탐색하여 해당 기기 찾음

        for(BluetoothDevice tempDevice : devices) {

            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료

            if(deviceName.equals(tempDevice.getName())) {

                bluetoothDevice = tempDevice;

                break;

            }

        }

        // UUID 생성

        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성

        try {

            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

            bluetoothSocket.connect();

            // 데이터 송,수신

            outputStream = bluetoothSocket.getOutputStream();

            inputStream = bluetoothSocket.getInputStream();

            // 데이터 송신 함수 호출

            //sendData(); //미구현

        } catch (IOException e) {

            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();

        }

    }



}