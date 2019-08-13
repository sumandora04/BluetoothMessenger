package com.notepoint4ugmail.bluetoothmessenger;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    private static final String TAG = "MainActivity";
    Button onButton, offButton, searchBtn;
    ListView deviceList;
    TextView statusTextView;
    BluetoothAdapter bluetoothAdapter;
    public static final int REQUEST_CODE_ENABLE = 1;
    ArrayAdapter<BluetoothDevice> newDeviceAdapter;
    ArrayList<BluetoothDevice> newDevicesList = new ArrayList<>();

    private static final int STATE_LISTENING =1;
    private static final int STATE_CONNECTING =2;
    private static final int STATE_CONNECTED =3;
    private static final int STATE_CONNECTION_FAILED =4;
    private static final int STATE_MESSAGE_RECEIVED =5;

    public static final String APP_NAME = "com.notepoint4ugmail.bluetoothmessenger";
    public static final UUID MY_UUID = UUID.fromString("ae38bb2a-bdc6-11e9-9cb5-2a2ae2dbcce4");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        onButton = findViewById(R.id.bluetooth_on_btn);
        offButton = findViewById(R.id.bluetooth_off_btn);
        searchBtn = findViewById(R.id.search_devices_btn);
        deviceList = findViewById(R.id.device_list);
        statusTextView = findViewById(R.id.status_text_view);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        onButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothOn();
            }
        });

        offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                blueToothOff();
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listNearbyBluetoothDevice();
            }
        });


        deviceList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(MainActivity.this, ""+parent.getItemAtPosition(position), Toast.LENGTH_SHORT).show();
                newDevicesList.get(position).createBond();
            }
        });
    }

    private void bluetoothOn() {

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth does not support on this device", Toast.LENGTH_SHORT).show();
        } else {
            if (!bluetoothAdapter.isEnabled()) {
                Intent bluetoothEnableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(bluetoothEnableIntent, REQUEST_CODE_ENABLE);
            }
        }
    }


    private void blueToothOff() {
        if (bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.disable();
            Toast.makeText(this, "Bluetooth turned off", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_CODE_ENABLE) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                makeDeviceDiscoverable();
                // listPairedBluetoothDevices();

            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Connection canceled", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void listPairedBluetoothDevices() {
        if (bluetoothAdapter != null) {
            Set<BluetoothDevice> bluetoothDeviceSet = bluetoothAdapter.getBondedDevices();
            String[] deviceName = new String[bluetoothDeviceSet.size()];
            int index = 0;
            for (BluetoothDevice device : bluetoothDeviceSet) {
                deviceName[index] = device.getName();
                index++;
            }

            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, deviceName);
            deviceList.setAdapter(arrayAdapter);
        }
    }

    private void listNearbyBluetoothDevice() {
        Log.d(TAG, "discoverDevice: Looking for unpaired devices.");
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "discoverDevice: Cancelling discovery.");
            //Run time permission:
            checkBTPermission();
            bluetoothAdapter.startDiscovery();

        }

        if (!bluetoothAdapter.isDiscovering()) {
            checkBTPermission();
            bluetoothAdapter.startDiscovery();
        }

        IntentFilter discoverDevicesFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothBroadcastReceiver, discoverDevicesFilter);
    }



    BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice devices = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (devices!=null) {
                    newDevicesList.add(devices);
                    Log.d(TAG, "onReceive: " + newDevicesList);
                    newDeviceAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, newDevicesList);
                    deviceList.setAdapter(newDeviceAdapter);
                    newDeviceAdapter.notifyDataSetChanged();
                }
            }
        }
    };


    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what){
                case STATE_LISTENING:
                    statusTextView.setText("Listening");
                    break;

                case STATE_CONNECTING:
                    statusTextView.setText("Connecting");
                    break;

                case STATE_CONNECTED:
                    statusTextView.setText("Connected");
                    break;

                case STATE_CONNECTION_FAILED:
                    statusTextView.setText("Connection failed");
                    break;

                case STATE_MESSAGE_RECEIVED:
                   //TODO
                    break;
            }
            return true;
        }
    });


    private void makeDeviceDiscoverable(){
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,120);
        startActivity(discoverableIntent);
    }

    private void checkBTPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            int permissionCheck = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");

                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");

                if (permissionCheck != 0) {
                    this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001);
                } else {
                    Log.d(TAG, "checkBTPermission: No need of permission.");
                }
            }
        }
    }

}
