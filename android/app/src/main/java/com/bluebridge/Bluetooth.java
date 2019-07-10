package com.bluebridge;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class Bluetooth extends ReactContextBaseJavaModule {

    BluetoothAdapter mBluetoothAdapter;
    private static Boolean isOn = false;
    private static Boolean bluetoothSupported = false;
    public static int REQUEST_BLUETOOTH = 1;
    List<String> paired = new ArrayList<String>();
    List<String> unpaired = new ArrayList<String>();
//    List<String> receivedData = new ArrayList<String>();
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothSocket mBTSocket = null;
    int[] intArray = new int[]{ 1,2,3,4,5,6,7,8,9,10 };
    private boolean fail = false;
    public ConnectedThread mConnectedThread;
    String receivedData= "";
    public static Boolean cancel = false;


    public Bluetooth(ReactApplicationContext reactContext) {

        super(reactContext);
    }


    @ReactMethod
    public void checkBluetoothSupport(
            Callback successCallback) {
        successCallback.invoke(null, bluetoothSupported);

    }

    @ReactMethod
    public void getStatus(
            Callback successCallback) {
        successCallback.invoke(null, isOn);

    }
    @ReactMethod
    public void getReceivedData(
            Callback successCallback) {
        successCallback.invoke(receivedData);

    }



//    @ReactMethod
//    public void getReceivedData() {
//        WritableMap infoMap = Arguments.createMap();
//        infoMap.putString("data", receivedData);
//        sendEvent('', infoMap);
//    }

//    @ReactMethod
//    public void onDataReceive() {
//        WritableMap params = Arguments.createMap();
//        params.putString("data", receivedData);
//        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("data-received", params);
//    }

//    private void sendEvent() {
//        // first, grab the current context
//        ReactContext currentContext = getReactApplicationContext();
//        String eventName = "myAwesomeEvent";
//
//        // a WritableMap is the equivalent to a JS Object:
//        // the React native bridge will convert it as is
//        WritableMap params = Arguments.createMap();
//        params.putString("type", receivedData);
//
//        // What is left to do is:
//        // "get the JS object corresponding to the instance
//        // of the RCTDeviceEventEmitter class in the current context"
//        // (i.e. the reference to our DeviceEventEmitter),
//        // then emit the event
//        currentContext
//                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                .emit(eventName, params);
//    }





    @ReactMethod
    public void startBluetooth() {

        Activity currentActivity = getCurrentActivity();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null) {
            bluetoothSupported = false;
            System.out.println("no bluetooth capability...");
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            currentActivity.startActivityForResult(enableBT, REQUEST_BLUETOOTH);
//            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            currentActivity.registerReceiver(receiver,filter);
        }
        if (mBluetoothAdapter.isEnabled()) {
            isOn = true;
            bluetoothSupported = true;
            System.out.println("bluetooth enabled");
        }
    }

    @ReactMethod
    public void startCreateThread(final String id){
        System.out.println("printing id");
        System.out.println(id);
        createThread(id);
    }

    @ReactMethod
    public void disconnectSocket(){
        cancel = true;
    }


    public void createThread(final String id) {
        System.out.println("create thread called");
        new Thread() {
            public void run() {
                boolean fail = false;

                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(id);
                System.out.println("printing device id");
                System.out.println(id);
//        System.out.println(device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID));
                try {
                    mBTSocket = createBluetoothSocket(device);
                    System.out.println(mBTSocket.isConnected());

                } catch (IOException e) {
                    return;
                }

                try {
                    mBTSocket.connect();
                    System.out.println(mBTSocket.isConnected());

                } catch (IOException e) {
                    try {
                        fail = true;
                        mBTSocket.close();
                    } catch (IOException e2) {
                        //insert code to deal with this

                    }
                }
                if (fail == false) {
                    System.out.println("fail is false");

                     mConnectedThread = new ConnectedThread(mBTSocket);
                    mConnectedThread.start();


                }
            }
        }.start();
    }



    @ReactMethod
    public void getPairedDevices(Promise promise){
        System.out.println("get paired device called");
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        System.out.println("paired devices....");
//        System.out.println(pairedDevices);

        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            paired.clear();
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                paired.add(device.getName() + "\n" + device.getAddress());
            }
        }
        String[] returnArray = new String[paired.size()];
        returnArray = paired.toArray(returnArray);
        WritableArray promiseArray = Arguments.createArray();
        for (int i = 0; i < returnArray.length; i++) {
            promiseArray.pushString(returnArray[i]);
        }

        promise.resolve(promiseArray);
        System.out.println("printing paired");
        System.out.println(paired);

    }



    public BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
        //creates secure outgoing connection with BT device using UUID
    }

             class ConnectedThread extends Thread {
                private final BluetoothSocket mmSocket;
                private final InputStream mmInStream;
                private final OutputStream mmOutStream;

                public ConnectedThread(BluetoothSocket socket) {
                    mmSocket = socket;
                    InputStream tmpIn = null;
                    OutputStream tmpOut = null;

                    // Get the input and output streams, using temp objects because
                    // member streams are final
                    try {
                        tmpIn = socket.getInputStream();
                        tmpOut = socket.getOutputStream();
                    } catch (IOException e) { }

                    mmInStream = tmpIn;
                    mmOutStream = tmpOut;
                }

                public void run() {
                    byte[] buffer = new byte[1024];  // buffer store for the stream
                    int bytes; // bytes returned from read()
                    // Keep listening to the InputStream until an exception occurs
                    System.out.println("inside run");
                    while (true) {
                        try {
                            // Read from the InputStream
//                            System.out.println("printing mmInStream");
//                            System.out.println(mmInStream);
                            bytes = mmInStream.available();
//                            System.out.println(bytes);
                            if(bytes != 0) {
                                SystemClock.sleep(100); //pause and wait for rest of data. Adjust this depending on your sending speed.
//                                bytes = mmInStream.available(); // how many bytes are ready to be read?
//                                bytes = mmInStream.read(buffer, 0, bytes); // record how many bytes we actually read
//                                System.out.println("reading buffer----------------------");
//                                System.out.println(bytes);
                                  bytes = mmInStream.read(buffer);
                                  String readMessage = new String(buffer, 0, bytes);
//                                   System.out.println("reading buffer----------------------");
                                   System.out.println(readMessage);
                                receivedData = readMessage;



                            }
                        } catch (IOException e) {
                            e.printStackTrace();

                            break;
                        }
                    }
                }

                /* Call this from the main activity to send data to the remote device */
                public void write(String input) {
                    byte[] bytes = input.getBytes();           //converts entered String into bytes
                    try {
                        mmOutStream.write(bytes);
                    } catch (IOException e) { }
                }

                /* Call this from the main activity to shutdown the connection */
                public void cancel() {
                    try {
                        mmSocket.close();
                    } catch (IOException e) { }
                }
            }


    public final BroadcastReceiver preceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            System.out.println("receiver");

            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                System.out.println("====>discovering");
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                System.out.println(deviceName);
                System.out.println(deviceHardwareAddress);
                unpaired.add(deviceName + "\n" + deviceHardwareAddress);
                System.out.println(unpaired);

            }else{
                System.out.println("not discovering");
            }
        }
    };



    @ReactMethod
    public void scan() {
        //Activity currentActivity = getCurrentActivity();
        System.out.println("discovering");
        Activity currentActivity = getCurrentActivity();

        IntentFilter filter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //System.out.println(preceiver);
        System.out.println(filter1);
       // currentActivity.registerReceiver(preceiver, filter1);
        mBluetoothAdapter.startDiscovery();



//        try {
//            mBTSocket = createBluetoothSocket(device);
//        } catch (IOException e) {
//            fail = true;
//            Toast.makeText(getBaseContext(), "Socket creation failed", Toast.LENGTH_SHORT).show();
//        }

    }



//    @Override
//    protected void onDestroy() {
//        Activity currentActivity = getCurrentActivity();
//
//        // Don't forget to unregister the ACTION_FOUND receiver.
//        currentActivity.unregisterReceiver(preceiver);
//        System.out.println("destroyed");
//
//    }






    @Override
    public String getName() {
        return "Bluetooth";
    }
}
