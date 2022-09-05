package com.bt67;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RequiresApi(api = Build.VERSION_CODES.M)
public class BluetoothModule extends ReactContextBaseJavaModule {
    public static final String LOG_TAG = "BluetoothModule";
    private static final int DISCOVER_REQUEST = 531;
    public BluetoothManager bluetoothManager;
    public BluetoothAdapter bluetoothAdapter;

    public static ReactApplicationContext reactContext;
    BluetoothModule(ReactApplicationContext context){
        super(context);
        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        reactContext = context;
    }

    @NonNull
    @Override
    public String getName() {
        return "BluetoothModule";
    }

    @ReactMethod
    private void bluetoothSwitch() {
        if(bluetoothAdapter.isEnabled()){
            bluetoothAdapter.disable();
            Log.d(LOG_TAG,"Disabled.");
        } else {
            bluetoothAdapter.enable();
            Log.d(LOG_TAG, "Enabled.");
        }
    }

    @ReactMethod
    public Set<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.d(LOG_TAG,deviceName+" "+deviceHardwareAddress);
            }
        }
        return pairedDevices;
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("PAIRED_DEVICES", String.valueOf(getPairedDevices()));
        return constants;
    }

    @ReactMethod
    public void startBluetoothDeviceDiscovery() {
        IntentFilter filterList = new IntentFilter(
                String.valueOf(new Intent(BluetoothDevice.ACTION_FOUND)));
        filterList.addAction(
                String.valueOf(new Intent(BluetoothAdapter.ACTION_DISCOVERY_STARTED)));
        filterList.addAction(
                String.valueOf(new Intent(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)));

        reactContext.registerReceiver(receiver, filterList);
        bluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                Log.d(LOG_TAG,deviceName+" "+deviceHardwareAddress);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.d(LOG_TAG,"Device Discovery Started");
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(LOG_TAG,"Device Discovery Finished");
            }
        }
    };

    @ReactMethod
    public void enableDeviceDiscoverability() {
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        Bundle bundleOptions = new Bundle();
        reactContext.startActivityForResult(discoverableIntent, DISCOVER_REQUEST, bundleOptions);
    }

    @ReactMethod
    public void cancelDeviceDiscovery() {
        bluetoothAdapter.cancelDiscovery();
    }

    @ReactMethod
    public void AcceptThreadRun() {
        AcceptThread obj = new AcceptThread();
        obj.start();
    }

    @ReactMethod
    public void AcceptThreadCancel() {
        AcceptThread obj = new AcceptThread();
        obj.cancel();
    }

    @ReactMethod
    public void ConnectThreadRun(BluetoothDevice device) {
        ConnectThread obj = new ConnectThread(device);
        obj.start();
    }

    @ReactMethod
    public void ConnectThreadCancel(BluetoothDevice device) {
        ConnectThread obj = new ConnectThread(device);
        obj.cancel();
    }
}
