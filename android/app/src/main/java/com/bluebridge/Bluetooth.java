package com.bluebridge;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public class Bluetooth extends ReactContextBaseJavaModule {

    BluetoothAdapter mBluetoothAdapter;
    private static Boolean isOn = false;
    private static Boolean bluetoothSupported = false;
    public static int REQUEST_BLUETOOTH = 1;
   List<String> paired = new ArrayList<String>();
    int[] intArray = new int[]{ 1,2,3,4,5,6,7,8,9,10 };




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
    public void startBluetooth(Promise promise) {

        Activity currentActivity = getCurrentActivity();

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(mBluetoothAdapter == null){
            bluetoothSupported = false;
            System.out.println("no bluetooth capability...");
        }
        if(!mBluetoothAdapter.isEnabled()){
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            currentActivity.startActivityForResult(enableBT, REQUEST_BLUETOOTH);
//            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
//            currentActivity.registerReceiver(receiver,filter);
        }
        if(mBluetoothAdapter.isEnabled()){
            isOn=true;
            bluetoothSupported = true;
            System.out.println("bluetooth enabled");
        }

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//        System.out.println("paired devices....");
//        System.out.println(pairedDevices);

        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                paired.add(device.getName() + "\n" + device.getAddress());
            }
        }
        String[] returnArray = new String[paired.size()];
        returnArray = paired.toArray(returnArray);
        WritableArray promiseArray=Arguments.createArray();
        for(int i=0;i<returnArray.length;i++){
            promiseArray.pushString(returnArray[i]);
        }

        promise.resolve(promiseArray);
//        System.out.println(paired);


    }

    private final BroadcastReceiver preceiver = new BroadcastReceiver() {
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
            }else{
                System.out.println("not discovering");
            }
        }
    };


    public boolean startDiscovery() {
        mBluetoothAdapter.startDiscovery();
        if(!mBluetoothAdapter.startDiscovery()){
            System.out.println("not discovering");
           return mBluetoothAdapter.startDiscovery();
        }
        return true;
    }

    @ReactMethod
    public void scan(){
        Activity currentActivity = getCurrentActivity();
        System.out.println("discovering");

        startDiscovery();
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        currentActivity.registerReceiver(preceiver, filter);
    }



//    protected void onDestroy() {
//        super.onDestroy();
//        // Don't forget to unregister the ACTION_FOUND receiver.
//        unregisterReceiver(receiver);
//    }




    @Override
    public String getName() {
        return "Bluetooth";
    }
}
