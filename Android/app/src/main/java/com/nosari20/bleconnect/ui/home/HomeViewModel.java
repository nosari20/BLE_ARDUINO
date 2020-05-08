package com.nosari20.bleconnect.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.util.Log;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.Navigation;

import com.nosari20.bleconnect.R;
import com.nosari20.bleconnect.bluetooth.BLEService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HomeViewModel extends ViewModel {

    private static final String TAG = HomeViewModel.class.toString();


    private Activity mActivity;
    private Fragment mFragment;
    private MutableLiveData<List<BluetoothDevice>> mDevices;


    // Bluetooth service
    private BLEService mBLEService;
    private Map<String, BluetoothDevice> mSearchResults;
    private BLEService.BLEServiceHandler mBLEHandler;




    @SuppressLint("HandlerLeak")
    public HomeViewModel(Fragment fragment, Activity activity) {

        mActivity = activity;
        mFragment = fragment;

        // Discovered devices list
        mDevices = new MutableLiveData<>();
        mDevices.setValue(new ArrayList<BluetoothDevice>());


        // Init BLE service
        mBLEService = BLEService.instance();
        mBLEService.disconnect();

        mBLEHandler = new BLEService.BLEServiceHandler(){

            @Override
            public void onScanResult(BluetoothDevice device) {
                Log.v(TAG, "New device detected: " + device.getAddress());
                if(!mSearchResults.containsKey(device.getAddress())) {
                    mSearchResults.put(device.getAddress(),device);
                    mDevices.getValue().clear();
                    mDevices.getValue().addAll(mSearchResults.values());
                    mDevices.setValue(mDevices.getValue());
                }

            }

            @Override
            public void onScanFinished() {

            }

            @Override
            public void onConnect() {
                Log.v(TAG, "Device connected, redirecting");
                mActivity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if(mFragment.getView() != null)
                            if(Navigation.findNavController(mFragment.getView()).getCurrentDestination().getId() == R.id.nav_home)
                                Navigation.findNavController(mFragment.getView()).navigate(R.id.action_HomeFragment_to_ConsoleFragment);


                    }
                });
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
        mBLEService.addHandler(mBLEHandler);

    }

    public void disable() {
        mBLEService.disableDevice();
        Log.v(TAG, "Bluetooth enabled: " + mBLEService.isDeviceEnabled());
    }

    public Boolean getBTStatus() {
        return mBLEService.isDeviceEnabled();
    }

    public LiveData<List<BluetoothDevice>> getDevices() {
        return mDevices;
    }

    public void startScan(){
        Log.v(TAG, "Start scan requested");
        mSearchResults = new HashMap<>();
        mBLEService.startLeScan();
    }

    public void connect(BluetoothDevice device){
        Log.v(TAG, "Connect to" + device.toString());
        mBLEService.connect(mActivity,device);
    }





}