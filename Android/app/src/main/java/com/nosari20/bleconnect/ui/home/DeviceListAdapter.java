package com.nosari20.bleconnect.ui.home;

import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nosari20.bleconnect.R;

import java.util.List;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder> {

    private static final String TAG = DeviceListAdapter.class.toString();
    private static ClickListener clickListener;

    private List<BluetoothDevice> mDataset;

    public DeviceListAdapter(List<BluetoothDevice> myDataset) {
        mDataset = myDataset;
    }

    @NonNull
    @Override
    public DeviceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v =  LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_device, parent, false);
        DeviceListViewHolder vh = new DeviceListViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListViewHolder holder, int position) {
        holder.bind(mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public void setOnItemClickListener(ClickListener listener) {
        clickListener = listener;
    }

    public interface ClickListener {
        void onItemClick(int position, View v);
    }

    public static class DeviceListViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener  {

        public TextView textView;
        public DeviceListViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            textView = v.findViewById(R.id.device_name);
        }

        @Override
        public void onClick(View v) {
            clickListener.onItemClick(getBindingAdapterPosition(), v);
        }

        public void bind(BluetoothDevice device) {
            textView.setText(device.getName() + " (" + device.getAddress()+")");
        }

    }



}
