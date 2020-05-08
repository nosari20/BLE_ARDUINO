package com.nosari20.bleconnect.ui.console;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.nosari20.bleconnect.bluetooth.BLEService;
import com.nosari20.bleconnect.bluetooth.BLEService.BLEServiceHandler;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ConsoleViewModel extends ViewModel {

    private static final String TAG = ConsoleViewModel.class.toString();

    private MutableLiveData<String> mConsoleOutput;
    private BLEService mBLEService;
    private BLEServiceHandler mBLEHandler;

    @SuppressLint("HandlerLeak")
    public ConsoleViewModel() {
        mConsoleOutput = new MutableLiveData<>();
        mConsoleOutput.setValue("");


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
                mConsoleOutput.postValue(mConsoleOutput.getValue() + "Disconnected");
            }

            @Override
            public void onServicesDiscovered(List<BluetoothGattService> services, BluetoothGatt gatt) {
                Log.v(TAG, "Services discovered");
                String output = "\n Services : ";
                for (BluetoothGattService service: services) {
                    output+="\n\t" + service.getUuid().toString();
                    for (BluetoothGattCharacteristic c: service.getCharacteristics()) {
                        output+="\n\t\t\t" + c.getUuid().toString();
                    }
                }
                Log.v(TAG, output);
                mConsoleOutput.postValue(mConsoleOutput.getValue() + "Connected");

                boolean read = mBLEService.listen("0000dfb0-0000-1000-8000-00805f9b34fb", "0000dfb1-0000-1000-8000-00805f9b34fb");
                Log.v(TAG, "read: " + read);

            }

            @Override
            public void onChanged(String value, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
                Log.v(TAG, "Read success: "+value);
                mConsoleOutput.postValue(mConsoleOutput.getValue() + "\nRead value: " + value);
            }

            @Override
            public void onWrite(boolean success, String value, BluetoothGattCharacteristic characteristic, BluetoothGatt gatt) {
                Log.v(TAG, "Write success: "+success);
                if(success) {
                    mConsoleOutput.postValue(mConsoleOutput.getValue() + "\nWrite value: " + value);
                }else{
                    mConsoleOutput.postValue(mConsoleOutput.getValue() + "\nWrite failed");
                }
            }
        };

        mBLEService.addHandler(mBLEHandler);
    }

    public LiveData<String> getConsoleOutput() {
        return mConsoleOutput;
    }

    public void send(String value) {
        Log.v(TAG, "Sending '" + value +"'");
        mBLEService.write("0000dfb0-0000-1000-8000-00805f9b34fb", "0000dfb1-0000-1000-8000-00805f9b34fb", value+'\n');
        //mBLEService.write("0000dfb0-0000-1000-8000-00805f9b34fb", "0000dfb2-0000-1000-8000-00805f9b34fb", value);


        boolean read = mBLEService.listen("0000dfb0-0000-1000-8000-00805f9b34fb", "0000dfb1-0000-1000-8000-00805f9b34fb");
        Log.v(TAG, "read: " + read);

    }
}