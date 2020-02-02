package com.example.bluetoothscanner

import android.bluetooth.le.ScanResult
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.bt_device_item.view.*

class BTListAdapter(private val context: Context, private val btdata: MutableList<Pair<String, ScanResult>>, private val clickListener: OnItemClickListener) : RecyclerView.Adapter<ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.bt_device_item, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = btdata[position]
        holder.macAddress.text = item.first
        holder.signalStrength.text = """${item.second.rssi} dBm"""
        holder.deviceName.text = item.second.device.name

        holder.bind(btdata.get(position), clickListener)

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

class ViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
    val deviceName = itemView.device_name
    val macAddress: TextView = itemView.mac_address
    val signalStrength = itemView.signal_strength

    fun bind(res: Pair<String, ScanResult>, clickListener: OnItemClickListener) {
        if(res.second.isConnectable == false){
            macAddress.setTextColor(Color.parseColor("#cfcfcf"))
            deviceName.setTextColor(Color.parseColor("#cfcfcf"))
            signalStrength.setTextColor(Color.parseColor("#cfcfcf"))
        } else {
            macAddress.setTextColor(Color.parseColor("#000000"))
            deviceName.setTextColor(Color.parseColor("#000000"))
            signalStrength.setTextColor(Color.parseColor("#000000"))
        }
        itemView.setOnClickListener {
            clickListener.onItemClicked(res)

        }
    }
}

interface OnItemClickListener {
    fun onItemClicked(res: Pair<String, ScanResult>)
}