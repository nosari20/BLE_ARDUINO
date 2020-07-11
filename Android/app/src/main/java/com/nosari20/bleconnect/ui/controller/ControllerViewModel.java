package com.nosari20.bleconnect.ui.controller;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nosari20.bleconnect.bluetooth.BLEService;

import java.util.List;

public class ControllerViewModel extends ViewModel {

    private static final String TAG = ControllerViewModel.class.toString();

    private BLEService mBLEService;
    private BLEService.BLEServiceHandler mBLEHandler;

    public ControllerViewModel() {

        mBLEService = BLEService.instance();
        mBLEHandler = new BLEService.BLEServiceHandler(){
            @Override
            public void onScanResult(BluetoothDevice device) {

            }

            @Override
            public void onScanFinished() {

            }

            @Override
            public void onConnect() {

            }

            @Override
            public void onDisconnect() {

            }

            @Override
            public void onServicesDiscovered(List<BluetoothGattService> services, BluetoothGatt gatt) {

            }

            @Override
            public void onChanged(String value, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {

            }

            @Override
            public void onWrite(boolean success, String value, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {

            }
        };


    }

    public void send(String value) {
        Log.v(TAG, "Sending '" + value +"'");
        mBLEService.write("0000dfb0-0000-1000-8000-00805f9b34fb", "0000dfb1-0000-1000-8000-00805f9b34fb", value+'\n');
    }

}