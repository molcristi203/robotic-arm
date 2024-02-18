package com.mareschema.dmparmproject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.slider.Slider;
import com.mareschema.dmparmproject.R;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_DISCOVER_BT = 2;
    private static final int PERMISSION_REQUEST_CODE = 3;
    private ConnectThread connectThread;
    private static final String PASSWORD = "1234";
    private static final byte[] PASSWORD_BYTES = PASSWORD.getBytes();
    private static final String TAG = "Main Activity";

    private static final UUID MY_UUID = UUID.fromString("0000FFE0-0000-1000-8000-00805F9B34FB");
    private static final UUID MY_UUID_CHARACTERISTIC = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
    //0000FFE1-0000-1000-8000-00805F9B34FB
    //private static final UUID MY_UUID = UUID.nameUUIDFromBytes(new byte[] {0xF,0xF,0xE,0x0});
//74278BDA-B644-4520-8F0C-720EAF059935
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ArrayAdapter<String> discoveredDevicesAdapter;
    ArrayList<BluetoothDevice> discoveredDevices;
    private BluetoothGatt bluetoothGatt;

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
        }

        Button connectButton = findViewById(R.id.connectButton);
        ListView discoveredDevicesListView = findViewById(R.id.devicesList);

        // Initialize BluetoothAdapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        discoveredDevices = new ArrayList<>();
        discoveredDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        discoveredDevicesListView.setAdapter(discoveredDevicesAdapter);


        // Check if device supports Bluetooth
        if (bluetoothAdapter == null) {
            // Device doesn't support Bluetooth
            Toast.makeText(this, "Bluetooth is not supported on this device", Toast.LENGTH_SHORT).show();
        } else {
            // Check Bluetooth permission
            checkBluetoothPermission();

            // Check if Bluetooth is enabled
            if (!bluetoothAdapter.isEnabled()) {
                // Request to enable Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else {
                startDiscovery();
            }
        }

        connectButton.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View v) {
                // Check if Bluetooth is already enabled
                if (bluetoothAdapter.isEnabled()) {
                    // Start Bluetooth discovery
                    startDiscovery();
                } else {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    //Toast.makeText(MainActivity.this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
                }
                //startDiscovery();
            }
        });

        Slider sliderOne = findViewById(R.id.slider_one);
        Slider sliderTwo = findViewById(R.id.slider_two);
        Slider sliderThree = findViewById(R.id.slider_three);
        Slider sliderFour = findViewById(R.id.slider_four);
        Slider sliderFive = findViewById(R.id.slider_five);
        Slider sliderSix = findViewById(R.id.slider_six);

        sliderOne.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                BluetoothGattService bluetoothGattService = bluetoothGatt.getService(MY_UUID);
                if (bluetoothGattService != null && bluetoothGattService.getUuid().equals(MY_UUID)) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(MY_UUID_CHARACTERISTIC);
                    if(bluetoothGattCharacteristic != null && bluetoothGattCharacteristic.getUuid().equals(MY_UUID_CHARACTERISTIC)) {
                        bluetoothGattCharacteristic.setValue(sendSliderValueToArduino(1, value).getBytes());
                        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    } else {
                        Log.e(TAG, "No Gatt Characteristic!");
                    }
                } else {
                    Log.e(TAG, "No Gatt Service");
                }
            }
        });

        sliderTwo.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                BluetoothGattService bluetoothGattService = bluetoothGatt.getService(MY_UUID);
                if (bluetoothGattService != null && bluetoothGattService.getUuid().equals(MY_UUID)) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(MY_UUID_CHARACTERISTIC);
                    if(bluetoothGattCharacteristic != null && bluetoothGattCharacteristic.getUuid().equals(MY_UUID_CHARACTERISTIC)) {
                        bluetoothGattCharacteristic.setValue(sendSliderValueToArduino(2, value).getBytes());
                        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    } else {
                        Log.e(TAG, "No Gatt Characteristic!");
                    }
                } else {
                    Log.e(TAG, "No Gatt Service");
                }
            }
        });

        sliderThree.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                BluetoothGattService bluetoothGattService = bluetoothGatt.getService(MY_UUID);
                if (bluetoothGattService != null && bluetoothGattService.getUuid().equals(MY_UUID)) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(MY_UUID_CHARACTERISTIC);
                    if(bluetoothGattCharacteristic != null && bluetoothGattCharacteristic.getUuid().equals(MY_UUID_CHARACTERISTIC)) {
                        bluetoothGattCharacteristic.setValue(sendSliderValueToArduino(3, value).getBytes());
                        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    } else {
                        Log.e(TAG, "No Gatt Characteristic!");
                    }
                } else {
                    Log.e(TAG, "No Gatt Service");
                }
            }
        });

        sliderFour.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                BluetoothGattService bluetoothGattService = bluetoothGatt.getService(MY_UUID);
                if (bluetoothGattService != null && bluetoothGattService.getUuid().equals(MY_UUID)) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(MY_UUID_CHARACTERISTIC);
                    if(bluetoothGattCharacteristic != null && bluetoothGattCharacteristic.getUuid().equals(MY_UUID_CHARACTERISTIC)) {
                        bluetoothGattCharacteristic.setValue(sendSliderValueToArduino(4, value).getBytes());
                        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    } else {
                        Log.e(TAG, "No Gatt Characteristic!");
                    }
                } else {
                    Log.e(TAG, "No Gatt Service");
                }
            }
        });

        sliderFive.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                BluetoothGattService bluetoothGattService = bluetoothGatt.getService(MY_UUID);
                if (bluetoothGattService != null && bluetoothGattService.getUuid().equals(MY_UUID)) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(MY_UUID_CHARACTERISTIC);
                    if(bluetoothGattCharacteristic != null && bluetoothGattCharacteristic.getUuid().equals(MY_UUID_CHARACTERISTIC)) {
                        bluetoothGattCharacteristic.setValue(sendSliderValueToArduino(5, value).getBytes());
                        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    } else {
                        Log.e(TAG, "No Gatt Characteristic!");
                    }
                } else {
                    Log.e(TAG, "No Gatt Service");
                }
            }
        });

        sliderSix.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
            @Override
            public void onStartTrackingTouch(@NonNull Slider slider) {
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                BluetoothGattService bluetoothGattService = bluetoothGatt.getService(MY_UUID);
                if (bluetoothGattService != null && bluetoothGattService.getUuid().equals(MY_UUID)) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(MY_UUID_CHARACTERISTIC);
                    if(bluetoothGattCharacteristic != null && bluetoothGattCharacteristic.getUuid().equals(MY_UUID_CHARACTERISTIC)) {
                        float value = slider.getValue();
                        bluetoothGattCharacteristic.setValue(sendSliderValueToArduino(6, (int)value).getBytes());
                        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    } else {
                        Log.e(TAG, "No Gatt Characteristic!");
                    }
                } else {
                    Log.e(TAG, "No Gatt Service");
                }
            }

            @Override
            public void onStopTrackingTouch(@NonNull Slider slider) {
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(),
                            new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                BluetoothGattService bluetoothGattService = bluetoothGatt.getService(MY_UUID);
                if (bluetoothGattService != null && bluetoothGattService.getUuid().equals(MY_UUID)) {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(MY_UUID_CHARACTERISTIC);
                    if(bluetoothGattCharacteristic != null && bluetoothGattCharacteristic.getUuid().equals(MY_UUID_CHARACTERISTIC)) {
                        float value = 90.0f;
                        slider.setValue(value);
                        bluetoothGattCharacteristic.setValue(sendSliderValueToArduino(6, (int)value).getBytes());
                        bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    } else {
                        Log.e(TAG, "No Gatt Characteristic!");
                    }
                } else {
                    Log.e(TAG, "No Gatt Service");
                }
            }
        });

        discoveredDevicesListView.setOnItemClickListener((parent, view, position, id) -> {
            BluetoothDevice selectedDevice = discoveredDevices.get(position);
            // Initiate pairing/connection with the selected device
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                connectToDevice(selectedDevice);
            }
        });
    }

    private String sendSliderValueToArduino(int sliderNumber, float value) {
        // Convert the slider value to a string or byte array as per your Arduino protocol
        int value_int = (int) value;
        String message = "m" + sliderNumber + " " + value_int + "\n"; // Customize as per your protocol
        Log.e(TAG, message);
        return message;

    }

    private void checkBluetoothPermission() {
        // Check if the app has Bluetooth permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    //private LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();
    private static final long SCAN_PERIOD = 10000;
    private boolean scanning;
    // Device scan callback.
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    discoveredDevices.add(result.getDevice());
                    if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) ceva(),
                                new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                    }
                    discoveredDevicesAdapter.add(result.getDevice().getName() != null ? result.getDevice().getName() : result.getDevice().getAddress());
                    Log.e(TAG, "List: " + discoveredDevices);
                    //discoveredDevices.notifyDataSetChanged();
                }
            };

    private Context ceva() {
        return this;
    }

    private void scanLeDevice() {
        if (!scanning) {
            // Stops scanning after a predefined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;

                    if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) ceva(),
                                new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_REQUEST_CODE);
                    }
                    bluetoothLeScanner.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            bluetoothLeScanner.startScan(leScanCallback);
        } else {
            scanning = false;
            bluetoothLeScanner.stopScan(leScanCallback);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.S)
    private void startDiscovery() {

        discoveredDevicesAdapter.clear();
        discoveredDevices.clear();

        // Start Bluetooth discovery
        if (bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_SCAN}, PERMISSION_REQUEST_CODE);
            }
            //bluetoothAdapter.startDiscovery();
            scanLeDevice();
            //bluetoothLeScanner.startScan(leScanCallback);
            Toast.makeText(this, "Discovering devices...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Please enable Bluetooth first", Toast.LENGTH_SHORT).show();
        }

//        Intent discoverDevicesIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_ADVERTISE}, PERMISSION_REQUEST_CODE);
//        }
//        startActivityForResult(discoverDevicesIntent, REQUEST_DISCOVER_BT);
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryReceiver, filter);
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.S)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    discoveredDevices.add(device);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);

                    }
                    discoveredDevicesAdapter.add(device.getName() != null ? device.getName() : device.getAddress());
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth is enabled", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    startDiscovery();
                }
            } else {
                Toast.makeText(this, "Bluetooth enabling cancelled", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_DISCOVER_BT) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Discovery cancelled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case ConnectThread.ERROR_READ:
                    String error = (String) msg.obj;
                    Log.e(TAG, "Handler received error: " + error);
                    break;
            }
            return false;
        }
    });

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                // Device connected, perform additional operations if needed
                if (ActivityCompat.checkSelfPermission(ceva(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions((Activity) ceva(), new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);
                }
                bluetoothGatt.discoverServices();

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                // Device disconnected, handle accordingly
            }
        }
        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for(BluetoothGattService bluetoothGattService : bluetoothGatt.getServices()){
                    Log.e(TAG, bluetoothGattService.toString());
                    Log.e(TAG, bluetoothGattService.getUuid().toString());
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
    };
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void connectToDevice(BluetoothDevice device) {
        new Thread(() -> {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, PERMISSION_REQUEST_CODE);

                }
                try (BluetoothSocket socket = device.createRfcommSocketToServiceRecord(MY_UUID)) {
                    System.out.println("inainte de connect:" + socket);
                    bluetoothGatt = device.connectGatt(this, false, gattCallback);
                }
                // Connection successful - handle connected socket
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
                    // Handle further operations with the connected socket here
                });
            } catch (IOException e) {
                e.printStackTrace();
                // Handle connection failure
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "Connection failed", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(discoveryReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


}