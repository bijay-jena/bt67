package com.bt67;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableNativeMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.Set;

public class bt extends ReactContextBaseJavaModule {
    BluetoothAdapter bluetoothAdapter;
    Set<BluetoothDevice> pairedDevices;
    SendReceive sendReceive;

    bt(){
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    @NonNull
    @Override
    public String getName() {
        return "bt";
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
    public void runServer() {
        ServerClass server = new ServerClass();
        server.start();
    }

    @ReactMethod
    public void runClient(String deviceHardwareAddress) {
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
    public void sendMessage(String string, Callback callback) {
        if(sendReceive!=null){
            sendReceive.write(string.getBytes());
            callback.invoke(string);
        }
    }

    Handler handler = new Handler(Looper.getMainLooper(), msg -> {
        switch (msg.what) {
            case Constants.STATE_LISTENING:
                Log.d(Constants.TAG,"Status Listening");
                break;
            case Constants.STATE_CONNECTING:
                Log.d(Constants.TAG,"Status Connecting");
                break;
            case Constants.STATE_CONNECTED:
                Log.d(Constants.TAG,"Status Connected");
                break;
            case Constants.STATE_CONNECTION_FAILED:
                Log.d(Constants.TAG,"Status Connection Failed");
                break;
            case Constants.STATE_MESSAGE_RECEIVED:
                byte[] readBuff = (byte[]) msg.obj;
                String tempMsg = new String(readBuff,0, msg.arg1);
                Log.d(Constants.TAG,tempMsg); // send / receive
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
    }

    private class ClientClass extends Thread {
        private BluetoothSocket socket;

        public ClientClass (BluetoothDevice device) {
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
    }
}
