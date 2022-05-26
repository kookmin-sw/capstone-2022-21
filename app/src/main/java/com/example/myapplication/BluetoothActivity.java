package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.provider.Telephony;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;


import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.io.IOException;

public class BluetoothActivity extends AppCompatActivity {

    BluetoothAdapter mBluetoothAdapter;
    static final int REQUEST_ENABLE_BT = 3;
    private Set<BluetoothDevice> devices;
    private BluetoothDevice conntedDevice;
    BluetoothSocket mBluetoothSocket;
    BluetoothServerSocket mBluetoothServerSocket;
    ArrayAdapter<String> adapter;
    ListView mListView;
    List<String> pairingList;


    Switch btSwitch;
    Button btSetting;
// 재생 텍스트 설정


// 블루투스 설정하기 위한 버튼

    BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {

        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");

            }

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_layout);

        mListView = (ListView) findViewById(R.id.listview);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        pairingList = new ArrayList<>();
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size()>0) {
            for(BluetoothDevice device : pairedDevices) {
                pairingList.add(device.getName() + "\n" + device.getAddress());
            }
        }

        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1,pairingList);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = (String) adapterView.getItemAtPosition(i);
                Toast.makeText(getApplicationContext(),"연결기기: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });



// 블루투스 기능
        btSetting = findViewById(R.id.bluetooth_setting_btn);
        btSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(intent, 0); //startActivityForResult() 는 호출한 Activity로 부터 결과를 받을 경우 사용.
            }
        });
        btSwitch = findViewById(R.id.bluetooth_state);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


//블루투스 설정
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.ACL_CONNECTED"));
        registerReceiver(this.bluetoothReceiver, new IntentFilter("android.bluetooth.device.action.ACL_DISCONNECTED"));
    }

    //블루투스 어댑터
    @Override
    protected void onResume() {
        super.onResume();

        if (!mBluetoothAdapter.isEnabled()) {
            btSwitch.setChecked(false);
        } else {
            btSwitch.setChecked(true);
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
// TODO: Consider calling
// ActivityCompat#requestPermissions
// here to request the missing permissions, and then overriding
// public void onRequestPermissionsResult(int requestCode, String[] permissions,
// int[] grantResults)
// to handle the case where the user grants the permission. See the documentation
// for ActivityCompat#requestPermissions for more details.
            return;
        }
        devices = mBluetoothAdapter.getBondedDevices();
        if (devices.size() > 0) {
// Toast.makeText(getApplicationContext(),"0"+mBluetoothAdapter.getRemoteDevice("18:54:CF:C8:16:EA").getName(), Toast.LENGTH_SHORT).show(); // 저 주소해당되는 페어링된 기기 이름
// Toast.makeText(getApplicationContext(),"1"+mBluetoothAdapter.getRemoteDevice(mBluetoothAdapter.getAddress()).getName(), Toast.LENGTH_SHORT).show(); // 핸드폰의 주소의 네임을 하는듯

// Toast.makeText(getApplicationContext(),"addr list"+"/"+devices.toString(), Toast.LENGTH_SHORT).show(); //페어링된 기기들의 리스트
            Log.d("TAG", devices.toString());

            for (int i = 0; i < devices.size(); i++) {
// Toast.makeText(getApplicationContext(),"i"+i+"/"+devices.toArray()[i], Toast.LENGTH_SHORT).show();
// mBluetoothAdapter.getState()//12나옴 mBluetoothAdapter.getRemoteDevice(devices.toArray()[i]+"").getBondState() 동일하게 12
                Log.d("TAG", "[" + devices.toArray()[i] + "]" + mBluetoothAdapter.getRemoteDevice(devices.toArray()[i] + "").getName() + " bondstate:" + mBluetoothAdapter.getRemoteDevice(devices.toArray()[i] + "").getBondState() + " getstate:" + mBluetoothAdapter.getState());
// mBluetoothAdapter.getRemoteDevice("");
// if (mBluetoothSocket.isConnected())
// {
// Log.d("TAG","YYYYYYYYEEEEEEEEESSSSSS");
// }
            }
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices == null || pairedDevices.size() == 0) {
            Toast.makeText(getApplicationContext(), "No Paired Devices Found", Toast.LENGTH_SHORT).show();
        } else {
            ArrayList<BluetoothDevice> list = new ArrayList<BluetoothDevice>();
            list.addAll(pairedDevices);

// Toast.makeText(getApplicationContext(),"list2 "+pairedDevices.toString(),Toast.LENGTH_SHORT).show(); // devices랑 똑같이 페어링된 기기목록
            Toast.makeText(getApplicationContext(), "2" + list.get(1).getName(), Toast.LENGTH_SHORT).show();
        }

        {

// Toast.makeText(getApplicationContext(),"3"+mBluetoothAdapter.getState()+mBluetoothAdapter.getAddress(),Toast.LENGTH_SHORT).show();
// Toast.makeText(getApplicationContext(),"4"+mBluetoothAdapter.getBluetoothLeScanner().toString(),Toast.LENGTH_SHORT).show(); //이상한거나옴
// Toast.makeText(getApplicationContext(),"5"+mBluetoothAdapter.getRemoteDevice("18:54:CF:C8:16:EA").getName(),Toast.LENGTH_SHORT).show(); // 저 주소해당되는 페어링된 기기 이름
// Toast.makeText(getApplicationContext(),"5"+mBluetoothAdapter.getBluetoothLeScanner(),Toast.LENGTH_SHORT).show(); //이상한주소같은느낌
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (mBluetoothAdapter == null) {
//장치가 블루투스를 지원하지 않는 경우.
            Toast.makeText(getApplicationContext(), "블루투스 기능을 지원하지 않음", Toast.LENGTH_SHORT).show();
            finish();
        } else {
// 장치가 블루투스를 지원하는 경우.
            if (!mBluetoothAdapter.isEnabled()) {
// 블루투스 활성화 요청
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
// TODO: Consider calling
// ActivityCompat#requestPermissions
// here to request the missing permissions, and then overriding
// public void onRequestPermissionsResult(int requestCode, String[] permissions,
// int[] grantResults)
// to handle the case where the user grants the permission. See the documentation
// for ActivityCompat#requestPermissions for more details.
                    return;
                }
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
    }



}