package com.nosari20.bleconnect.ui.home;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.nosari20.bleconnect.R;

import java.util.List;

public class HomeFragment extends Fragment {

    private static final String TAG = HomeFragment.class.toString();

    private HomeViewModel homeViewModel;

    private final int REQUEST_ENABLE_BT = 1;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        homeViewModel = new HomeViewModel(this,getActivity());

        View root = inflater.inflate(R.layout.fragment_home, container, false);


        // Device scan result
        final SwipeRefreshLayout swipeRefreshLayout = root.findViewById(R.id.devices_swipeRefreshLayout);
        final DeviceListAdapter deviceListAdapter = new DeviceListAdapter(homeViewModel.getDevices().getValue());

        final RecyclerView deviceList = root.findViewById(R.id.devices_recycler);
        deviceList.setHasFixedSize(true);
        deviceList.setAdapter(deviceListAdapter);
        deviceList.setLayoutManager(new LinearLayoutManager(getContext()));

        homeViewModel.getDevices().observe(getViewLifecycleOwner(), new Observer<List<BluetoothDevice>>() {
            public void onChanged(@Nullable List<BluetoothDevice> l) {
                deviceListAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        deviceListAdapter.setOnItemClickListener(new DeviceListAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {
                Log.d(TAG, "onItemClick: " + homeViewModel.getDevices().getValue().get(position));
                homeViewModel.connect(homeViewModel.getDevices().getValue().get(position));
            }

        });
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!homeViewModel.getBTStatus()) {
                    Log.v(TAG, "Bluetooth not enabled, requesting activation");
                    requestEnableBT();
                    swipeRefreshLayout.setRefreshing(false);
                } else {
                    Log.v(TAG, "Start scanning (swipeRefresh)");
                    homeViewModel.startScan();
                }
            }
        });


        if(homeViewModel.getBTStatus()) {
            Log.v(TAG, "Bluetooth enabled, start scanning");
            homeViewModel.startScan();
        }




        return root;
    }

    public void requestEnableBT(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    public void onActivityResult(int request , int result, Intent intent) {
        if( request == REQUEST_ENABLE_BT){
            if( result == Activity.RESULT_OK) {
                homeViewModel.startScan();
            } else {
                homeViewModel.disable();
            }
        }
    }




}
