package com.example.bluetoothscanner

import android.bluetooth.le.ScanResult
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bt_device_item.view.*

class BTListAdapter(private val context: Context, private val btdata: MutableList<Pair<String, ScanResult>>) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.bt_device_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = btdata[position]
        holder.macAddress.text = item.first
        holder.signalStrength.text = """${item.second.rssi} dBm"""
        holder.deviceName.text = item.second.device.name
        if(item.second.isConnectable == false){
            holder.macAddress.setTextColor(Color.parseColor("#cfcfcf"))
            holder.deviceName.setTextColor(Color.parseColor("#cfcfcf"))
            holder.signalStrength.setTextColor(Color.parseColor("#cfcfcf"))
        } else {
            holder.macAddress.setTextColor(Color.parseColor("#000000"))
            holder.deviceName.setTextColor(Color.parseColor("#000000"))
            holder.signalStrength.setTextColor(Color.parseColor("#000000"))
        }
    }

    override fun getItemCount(): Int {
        Log.d("size in adapter", btdata.size.toString())
        return btdata.size
    }


}

class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val deviceName = view.device_name
    val macAddress: TextView = view.mac_address
    val signalStrength = view.signal_strength
}