package com.bt67;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

public class bt extends ReactContextBaseJavaModule {
    ReactApplicationContext reactContext;
    BluetoothAdapter bluetoothAdapter;
    ArrayList<BluetoothDevice> devices;
    SendReceive sendReceive;
    ServerClass server;
    ClientClass client;

    bt(ReactApplicationContext context){
        super(context);
        reactContext = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

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
                    devices.add(device);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress();
                    WritableMap params = Arguments.createMap();
                    params.putString("dvcName", deviceName);
                    params.putString("dvcAddr", deviceHardwareAddress);
                    sendEvent(reactContext, "dvcFound", params);
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

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }
    @ReactMethod public void addListener(String eventName) {}
    @ReactMethod public void removeListeners(Integer count) {}

    @ReactMethod
    public void enable(Callback callback){
        if(!bluetoothAdapter.isEnabled()){
            bluetoothAdapter.enable();
            callback.invoke("Enabled Bluetooth");
        }
    }

    @ReactMethod
    public void discover(@NonNull Callback callback) {
            if(!bluetoothAdapter.isEnabled()){
                bluetoothAdapter.enable();
            }
            devices = new ArrayList<>();
            cancelDiscovery();
            if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                bluetoothAdapter.startDiscovery();
                if (bluetoothAdapter.isDiscovering()) {
                    callback.invoke("Discovery [STARTED]");
                } else {
                    callback.invoke("Discovery [FAILED]");
                }
            }
            IntentFilter intentActionFound = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            reactContext.registerReceiver(discoveryReceiver, intentActionFound);
    }

    @ReactMethod
    public void discoverable(int duration, @NonNull Callback callback) {
        ServerClass server = new ServerClass();
        server.start();
        Intent intentDiscoverable = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intentDiscoverable.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION,duration);
        Objects.requireNonNull(getCurrentActivity()).startActivity(intentDiscoverable);
        callback.invoke("Device is discoverable for "+duration+" seconds");
    }

    @ReactMethod
    public void connect(String deviceHardwareAddress) {
        if (devices.size() > 0) {
            for (BluetoothDevice device : devices) {
                if (Objects.equals(deviceHardwareAddress, device.getAddress())) {
                    bt.ClientClass client = new bt.ClientClass(device);
                    client.start();
                    WritableMap params = Arguments.createMap();
                    params.putString("status","Connecting to "+device.getName());
                    sendEvent(reactContext, "connecting", params);
                    break;
                }
            }
        }
    }

    @ReactMethod
    public void accept() {
        server = new ServerClass();
        server.start();
    }

    @ReactMethod
    public void sendMsg(String string) {
        if(sendReceive!=null){
            sendReceive.write(string.getBytes());
        }
    }

    @ReactMethod
    public void disconnect() {
        if (sendReceive != null) {
            sendReceive.cancel();
        }
        if(server != null) {
            server.cancel();
        }
        if(client != null){
            client.cancel();
        }
    }

    public void sendEventHelper(String msg_name, String msg, String eventName) {
        WritableMap params = Arguments.createMap();
        params.putString(msg_name, msg);
        sendEvent(reactContext, eventName, params);
    }

    Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case Constants.STATE_LISTENING:
                sendEventHelper("status", "Status Listening", "conn");
                break;
            case Constants.STATE_CONNECTING:
                sendEventHelper("status", "Status Connecting", "conn");
                break;
            case Constants.STATE_CONNECTED:
                sendEventHelper("status", "Status Connected", "conn");
                break;
            case Constants.STATE_CONNECTION_FAILED:
                sendEventHelper("status", "Status Connection Failed", "conn");
                break;
            case Constants.STATE_MESSAGE_RECEIVED:
                byte[] readBuff = (byte[]) msg.obj;
                String rcvdMsg = new String(readBuff,0, msg.arg1);
                sendEventHelper("msg",rcvdMsg, "rcvr");
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

            while (true){
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
                    break;
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

    @Override
    public void onCatalystInstanceDestroy() {
        super.onCatalystInstanceDestroy();
        Log.d(Constants.TAG, "Destroyed");
        if (sendReceive != null) {
            sendReceive.cancel();
        }
        if (server != null) {
            server.cancel();
        }
        if (client != null) {
            client.cancel();
        }
    }
}
