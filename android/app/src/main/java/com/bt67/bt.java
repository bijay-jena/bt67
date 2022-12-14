package com.bt67;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public class bt extends ReactContextBaseJavaModule {
    ReactApplicationContext reactContext;
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    String connectionStatus = "";

    // For Discovery
    ArrayList<BluetoothDevice> discoveredDevices;
    WritableMap reactDiscoveredDevices;

    SendReceive sendReceive;
    String receivedMessage = "";

    private final boolean isGreaterThan12 = Build.VERSION.SDK_INT >= 31;

    bt(ReactApplicationContext context){
        super(context);
        reactContext = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }

        IntentFilter scanIntentFilter = new IntentFilter(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        BroadcastReceiver scanModeReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, @NonNull Intent intent) {
                String action = intent.getAction();
                if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                    int modeValue = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);
                    if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE) {
                        Log.d(Constants.TAG, "Device can receive connection");
                    } else if (modeValue == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
                        Log.d(Constants.TAG,
                                "Device is Discoverable and can receive connection");
                    } else if (modeValue == BluetoothAdapter.SCAN_MODE_NONE) {
                        Log.d(Constants.TAG,
                                "Device is NOT Discoverable and can't receive connection");
                    } else {
                        Log.d(Constants.TAG, "ERROR");
                    }
                }
            }
        };
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
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                    Log.d(Constants.TAG, "Found: " + deviceName);
                    reactDiscoveredDevices.putString(deviceHardwareAddress, deviceName);
                }
            }
        }
    };

    @NonNull
    @Override
    public String getName() {
        return "bt";
    }

    public void cancelDiscovery() {
        if (bluetoothAdapter.isDiscovering()) {
            Log.d(Constants.TAG,"Is Discovering");
            bluetoothAdapter.cancelDiscovery();
        } else {
            Log.d(Constants.TAG,"Not Discovering");
        }
    }

    @ReactMethod
    public void enable(){
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
        }
    }

    @ReactMethod
    public void enable(Callback callback){
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            callback.invoke("Enabled Bluetooth");
        }
    }

    @ReactMethod
    public void getPairedDevices(Callback callback) {
        pairedDevices = bluetoothAdapter.getBondedDevices();
        WritableNativeMap reactPairedDevices = new WritableNativeMap();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress();
                Log.d(Constants.TAG,deviceName+" "+deviceHardwareAddress);
                reactPairedDevices.putString(deviceHardwareAddress,deviceName);
            }
        }
        callback.invoke(reactPairedDevices);
    }

    @ReactMethod
    public void doDiscovery(@NonNull Callback callback) {
        // For discover
        discoveredDevices = new ArrayList<>();
        reactDiscoveredDevices = Arguments.createMap();

        // If we're already discovering, stop it
        cancelDiscovery();

        // Request discover from BluetoothAdapter
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.startDiscovery();
            if(bluetoothAdapter.isDiscovering()) {
                callback.invoke("Started Discovery");
            } else {
                callback.invoke("Discovery Failed !!!");
            }
        } else {
            callback.invoke("Enable Bluetooth");
        }

        IntentFilter intentActionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        reactContext.registerReceiver(discoveryReceiver,intentActionFound);
    }

    @ReactMethod
    public void getDiscoveredDevices(@NonNull Callback callback) {
        cancelDiscovery();
        callback.invoke(reactDiscoveredDevices);
    }

    @ReactMethod
    public void makeDeviceDiscoverable(int duration, @NonNull Callback callback) {
        Intent intentDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intentDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,duration);
        Objects.requireNonNull(getCurrentActivity()).startActivity(intentDiscoverable);
        callback.invoke("Device is discoverable for "+duration+" seconds");
    }

    @ReactMethod
    public void initiateDiscoveredConnection(String deviceHardwareAddress) {
        if (discoveredDevices.size() > 0) {
            for (BluetoothDevice device : discoveredDevices) {
                if (Objects.equals(deviceHardwareAddress, device.getAddress())) {
                    bt.ClientClass client = new bt.ClientClass(device);
                    client.start();
                    Log.d(Constants.TAG,"Connecting " + device.getName());
                    break;
                }
            }
        }
    }

    @ReactMethod
    public void acceptConnection() {
        ServerClass server = new ServerClass();
        server.start();
    }

    @ReactMethod
    public void initiateConnection(String deviceHardwareAddress) {
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                if (Objects.equals(deviceHardwareAddress, device.getAddress())) {
                    ClientClass client = new ClientClass(device);
                    client.start();
                    Log.d(Constants.TAG,"Connecting " + device.getName());
                    break;
                }
            }
        }
    }

    @ReactMethod
    public void sendMessage(String string) {
        if(sendReceive!=null){
            sendReceive.write(string.getBytes());
        }
    }

    @ReactMethod
    public void getMessage(@NonNull Callback callback) {
        callback.invoke(receivedMessage);
    }

    @ReactMethod
    public void getConnectionStatus(@NonNull Callback callback) {
        if(bluetoothAdapter.isEnabled()){
            callback.invoke(connectionStatus);
        } else {
            callback.invoke("Bluetooth is off");
            sendReceive.cancel();
        }
    }

    Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case Constants.STATE_LISTENING:
                connectionStatus = "Status Listening";
                Log.d(Constants.TAG,connectionStatus);
                break;
            case Constants.STATE_CONNECTING:
                connectionStatus = "Status Connecting";
                Log.d(Constants.TAG,connectionStatus);
                break;
            case Constants.STATE_CONNECTED:
                connectionStatus = "Status Connected";
                Log.d(Constants.TAG,connectionStatus);
                break;
            case Constants.STATE_CONNECTION_FAILED:
                connectionStatus = "Status Connection Failed";
                Log.d(Constants.TAG,connectionStatus);
                break;
            case Constants.STATE_MESSAGE_RECEIVED:
                byte[] readBuff = (byte[]) msg.obj;
                receivedMessage = new String(readBuff,0, msg.arg1);
                Log.d(Constants.TAG,receivedMessage); // received Message
                break;
        }
        return true;
    });

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass(){
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(Constants.APP_NAME,Constants.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket==null){
                try{
                    Message message = Message.obtain();
                    message.what = Constants.STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e){
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = Constants.STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket!=null){
                    Message message = Message.obtain();
                    message.what = Constants.STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }

        // Closes the connect socket and causes the thread to finish.
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Could not close the connect socket", e);
            }
        }
    }

    private class ClientClass extends Thread {
        private BluetoothSocket socket;

        public ClientClass (@NonNull BluetoothDevice device) {
            try {
                socket = device.createRfcommSocketToServiceRecord(Constants.MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = Constants.STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = Constants.STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Could not close the client socket", e);
            }
        }
    }

    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while(true) {
                try {
                    bytes = inputStream.read(buffer);

                    handler.obtainMessage(
                            Constants.STATE_MESSAGE_RECEIVED,
                            bytes,-1,buffer).sendToTarget();

                    Log.d(Constants.TAG,String.valueOf(bytes)); // received

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Call this method from the main activity to shut down the connection.
        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(Constants.TAG, "Could not close the connect socket", e);
            }
        }
    }
}
