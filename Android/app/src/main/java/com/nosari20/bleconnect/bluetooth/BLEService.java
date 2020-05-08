package com.nosari20.bleconnect.bluetooth;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class BLEService {

    private static final String TAG = BLEService.class.toString();

    // Singleton
    private static BLEService mInstance;

    private static final long SCAN_PERIOD = 5000;

    private List<BLEServiceHandler> mHandlers;

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLeScan;
    private ScanSettings mScanSettings;
    private List<ScanFilter> mScanFilters;
    private boolean mConnected = false;
    private BluetoothGatt mGatt;
    private List<BluetoothGattService> mServices;
    private BluetoothGatt mConnectedGatt;

    private BLEService() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mScanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .build();
        mScanFilters = new ArrayList<>();

        mHandlers = new ArrayList<>();

    }

    public static BLEService instance(){
        if(mInstance == null)
            mInstance = new BLEService();
        return mInstance;
    }

    public void addHandler(BLEServiceHandler handler){
        mHandlers.add(handler);
    }

    public void removeHandler(BLEServiceHandler handler){
        mHandlers.remove(handler);
    }

    public boolean isDeviceEnabled(){
        return !(mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled());
    }

    public void disableDevice() {
        mBluetoothAdapter.disable();
        stopLeScan();
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.v(TAG, "onScanResult:callbackType " + String.valueOf(callbackType));
            Log.v(TAG, "onScanResult:result " + result.toString());
            for (BLEServiceHandler handler: mHandlers) {
                handler.onScanResult(result.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    public void startLeScan() {
        Log.v(TAG, "Trying to start scan...");
        if(isDeviceEnabled()) {
            Log.v(TAG, "Trying to start scan... device enabled");
            mLeScan = mBluetoothAdapter.getBluetoothLeScanner();
            Log.v(TAG, "Trying to start scan... device enabled LEScanner: " + mLeScan);
            if (mLeScan != null) {
                disconnect();
                Log.v(TAG, "Start scan");
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        stopLeScan();
                    }
                }, SCAN_PERIOD);
                mLeScan.startScan(mScanFilters, mScanSettings, mScanCallback);
            }
        }
    }

    public void stopLeScan() {
        Log.v(TAG, "Stop scan");
        if(mLeScan != null && isDeviceEnabled())
            mLeScan.stopScan(mScanCallback);
        for (BLEServiceHandler handler: mHandlers) {
            handler.onScanFinished();
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.v(TAG, "onConnectionStateChange:STATE_CONNECTED");
                    stopLeScan();
                    mConnected = true;
                    mConnectedGatt = gatt;
                    gatt.discoverServices();
                    for (BLEServiceHandler handler: mHandlers) {
                        handler.onConnect();
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    mConnected = true;
                    Log.v(TAG, "onConnectionStateChange:STATE_DISCONNECTED");
                    for (BLEServiceHandler handler: mHandlers) {
                        handler.onDisconnect();
                    }
                    break;
                default:
                    Log.v(TAG, "onConnectionStateChange:STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);

            Log.v(TAG, "Service '"+gatt+"' discovered");
            List<BluetoothGattService> services = gatt.getServices();
            for (BluetoothGattService service: services) {
                Log.i(TAG,"onServicesDiscovered: "+service.getUuid());
            }
            mServices = services;
            mGatt = gatt;
            for (BLEServiceHandler handler: mHandlers) {
                handler.onServicesDiscovered(services, gatt);
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            String value = new String(characteristic.getValue());
            value = value.substring(0, value.length() - 1);
            Log.i(TAG,"onCharacteristicWrite: "+value);
            for (BLEServiceHandler handler: mHandlers) {
                handler.onWrite(status == BluetoothGatt.GATT_SUCCESS, value, characteristic, gatt);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(TAG,"onCharacteristicWrite: "+new String(characteristic.getValue()));
            for (BLEServiceHandler handler: mHandlers) {
                handler.onChanged(new String(characteristic.getValue()), characteristic, gatt);
            }
        }
    };


    public void connect(Context context, BluetoothDevice device) {
        device.connectGatt(context, false, gattCallback);
    }

    public void disconnect() {
        if(mConnected & mConnectedGatt != null)
            mConnectedGatt.disconnect();
    }

    public boolean listen(String serviceUUID, String characteristicUUID){
        if(!mConnected || mServices == null || mGatt == null) return false;
        for (BluetoothGattService service: mServices  ) {
            if(service.getUuid().toString().equals(serviceUUID)){
                for (BluetoothGattCharacteristic characteristic: service.getCharacteristics() ) {
                    if(characteristic.getUuid().toString().equals(characteristicUUID)){
                        if(((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0)){
                            return mGatt.setCharacteristicNotification(characteristic,true);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return false;
    }

    public boolean write(String serviceUUID, String characteristicUUID, String value){
        if(!mConnected || mServices == null || mGatt == null) return false;
        for (BluetoothGattService service: mServices  ) {
            if(service.getUuid().toString().equals(serviceUUID)){
                for (BluetoothGattCharacteristic characteristic: service.getCharacteristics() ) {
                    if(characteristic.getUuid().toString().equals(characteristicUUID)){
                        if(((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0)){
                            characteristic.setValue(value);
                            return mGatt.writeCharacteristic(characteristic);
                        }
                        break;
                    }
                }
                break;
            }
        }
        return false;
    }


    public abstract static class BLEServiceHandler extends Handler {
        public abstract void onScanResult(BluetoothDevice device);
        public abstract void onScanFinished();
        public abstract void onConnect();
        public abstract void onDisconnect();
        public abstract void onServicesDiscovered(List<BluetoothGattService> services, BluetoothGatt gatt);
        public abstract void onChanged(String value, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt);
        public abstract void onWrite(boolean success, String value, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt);
    }

}
