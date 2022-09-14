package com.bt67;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;

import java.util.ArrayList;
import java.util.Objects;

public class discover extends ReactContextBaseJavaModule {
    ReactApplicationContext reactContext;
    ArrayList<BluetoothDevice> discoveredDevices;
    WritableNativeMap reactDiscoveredDevices;
    BluetoothAdapter bluetoothAdapter;

    private final boolean isGreaterThan12 = Build.VERSION.SDK_INT >= 31;

    @NonNull
    @Override
    public String getName() {
        return "discover";
    }

    discover(ReactApplicationContext reactApplicationContext){
        super(reactApplicationContext);
        reactContext = reactApplicationContext;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        discoveredDevices = new ArrayList<>();
        reactDiscoveredDevices = new WritableNativeMap();

//        reactContext.registerReceiver(discoveryReceiver,new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
//        reactContext.registerReceiver(discoveryReceiver,new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        IntentFilter scanIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        reactContext.registerReceiver(scanModeReceiver,scanIntentFilter);
    }

    private final BroadcastReceiver discoveryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName()!=null) {
                    discoveredDevices.add(device);
                    Log.d(Constants.TAG, "Found: " + String.valueOf(device.getName()));
                    reactDiscoveredDevices.putString(String.valueOf(device.getAddress()), String.valueOf(device.getName()));
                }
            }
        }
    };

    BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            String action = intent.getAction();
            if(action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                int modeValue = intent.getIntExtra(
                        BluetoothAdapter.EXTRA_SCAN_MODE,BluetoothAdapter.ERROR);
                if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                    Log.d(Constants.TAG,"Device can receive connection");
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                    Log.d(Constants.TAG,"Device is Discoverable and can receive connection");
                } else if (modeValue == BluetoothAdapter.SCAN_MODE_NONE) {
                    Log.d(Constants.TAG,
                            "Device is NOT Discoverable and can't receive connection");
                } else {
                    Log.d(Constants.TAG,"ERROR");
                }
            }
        }
    };

    @ReactMethod
    public void doDiscovery(@NonNull Callback callback) {
        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.startDiscovery();
            if(bluetoothAdapter.isDiscovering()) {
                callback.invoke("Started Discovery");
            } else {
                callback.invoke("Discovery Failed !!!");
            }
        }

        IntentFilter intentActionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        reactContext.registerReceiver(discoveryReceiver,intentActionFound);
    }

    @ReactMethod
    public void getDiscoveredDevices(@NonNull Callback callback) {
        callback.invoke(reactDiscoveredDevices);
    }

    @ReactMethod
    public void makeDeviceDiscoverable(int duration, @NonNull Callback callback) {
        Intent intentDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intentDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,duration);
        Objects.requireNonNull(getCurrentActivity()).startActivity(intentDiscoverable);
        callback.invoke("Device is discoverable for "+duration+" seconds");
    }
}
