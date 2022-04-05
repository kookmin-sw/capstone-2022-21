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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
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

    private Button bluetooth_setting_btn; // 블루투스 설정 버튼

    private Button bluetooth_paring_btn; // 블루투스 설정 버튼

    private ListView pairingListView; //페어링 기기 목록 제공하는 뷰

    private List<String> pairingList; // 페어링 목록

    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);

        //레이아웃 연결

        //블루투스 onoff 버튼 연결
        onOff_bt = (Switch) findViewById(R.id.bluetooth_state);

        //블루투스 설정 버튼 연결
        bluetooth_setting_btn = (Button) findViewById(R.id.bluetooth_setting_btn);

        //블루투스 paring 버튼 연결
        bluetooth_paring_btn = (Button) findViewById(R.id.bluetooth_paring_btn);

        //블루투스 리스트뷰 연결
        pairingListView = (ListView) findViewById(R.id.lv_paired);



        //블루투스 on off 리스터터
        onOff_bt.setOnCheckedChangeListener(new onOffSwitchListener());

        //블루투스 어댑터
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //블루투스 설정 여부 확인
        setbluetooth();

        //설정 버튼 클릭시 블루투스 설정으로 이동
        bluetooth_setting_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                //블루투스 설정으로 이동
                Intent intent = new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);


            }
        });

        //페어링 버튼 클릭시 연결 기기 목록 제공
        bluetooth_paring_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {

                //블루투스 연결 기기 목록 셋팅
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

            bluetooth_paring_btn.setEnabled(false);

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

        bluetooth_paring_btn.setEnabled(true);

        IntentFilter BTIntent = new IntentFilter(bluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(broadcastReceiver, BTIntent);


    }




    void off(){

        bluetoothAdapter.disable();

        bluetooth_paring_btn.setEnabled(false);

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



    //페어링 된 기기 목록을 가져옵니다.
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

            List<String> list = new ArrayList<>();

            // 모든 디바이스의 이름을 리스트에 추가

            for (BluetoothDevice bluetoothDevice : devices) {

                list.add(bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress());

            }


            adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, pairingList);

            pairingListView.setAdapter(adapter);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너

            pairingListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String selectedItem = (String) parent.getItemAtPosition(position);
                    Toast.makeText(getApplicationContext(), "연결기기 : " + selectedItem, Toast.LENGTH_SHORT).show();
                }
            });

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