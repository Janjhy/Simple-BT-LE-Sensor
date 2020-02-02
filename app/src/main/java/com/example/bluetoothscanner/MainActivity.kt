package com.example.bluetoothscanner

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Adapter
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*

const val REQUEST_ENABLE_FINE_LOCATION = 1
const val REQUEST_ENABLE_BT = 2
const val SCAN_PERIOD: Long = 3000

class MainActivity : AppCompatActivity(), OnItemClickListener, BleWrapper.BleCallback {

    private var bluetoothAdapter: BluetoothAdapter? = null
    private var scanResults: MutableList<Pair<String, ScanResult>> = mutableListOf()

    private lateinit var listAdapter: BTListAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var wrapper: BleWrapper? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewManager = LinearLayoutManager(this)
        listAdapter = BTListAdapter(this, scanResults, this)


        recyclerView = findViewById<RecyclerView>(R.id.bt_list).apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)

            // use a linear layout manager
            layoutManager = viewManager

            // specify an viewAdapter (see also next example)
            adapter = listAdapter

        }

        scan_btn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                != PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_ENABLE_FINE_LOCATION
                )
            } else {
                deviceScan()
            }
        }

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter


        bluetoothAdapter?.takeIf { !it.isEnabled }?.apply {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
        }

    }

    override fun onItemClicked(res: Pair<String, ScanResult>) {
        wrapper = BleWrapper(this, res.first)
        if(res.second.scanRecord.serviceUuids != null){

            res.second.scanRecord.serviceUuids.forEach {
                Log.d("click", res.first)
                Log.d("uuid", it.uuid.toString())
                Log.d("heart rate uuid", wrapper?.HEART_RATE_SERVICE_UUID.toString())
                if (it.uuid == wrapper?.HEART_RATE_SERVICE_UUID) {
                    Log.d("if uuid match", res.first)
                    wrapper?.addListener(this)
                    wrapper?.connect(false)
                }
            }
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_ENABLE_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PERMISSION_GRANTED)) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    deviceScan()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return
            }

        }
    }


    private fun deviceScan() {
        val handler = Handler()
        val scanCB = MyScanCallback()
        val btScanner = bluetoothAdapter!!.bluetoothLeScanner
        scanResults.clear()
        bt_list.adapter?.notifyDataSetChanged()

        handler.postDelayed({ btScanner.stopScan(scanCB) }, SCAN_PERIOD)
        btScanner.startScan(scanCB)
        Log.d("usr", "scan start")

    }

    private inner class MyScanCallback : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            if (result != null) {
                addScanResult(result)
            }
        }

        override fun onBatchScanResults(results: MutableList<ScanResult>?) {
            if (results != null) {
                for (result in results) {
                    addScanResult(result)
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.d("DBG", "BLE Scan Failed with code $errorCode")
        }

        private fun addScanResult(result: ScanResult) {

            val iterator = scanResults.iterator()


            if (scanResults.size == 0) {
                scanResults.add(Pair(result.device.address, result))
                bt_list.adapter?.notifyDataSetChanged()

            } else {
                scanResults.removeIf { it.first == result.device.address }
                scanResults.add(Pair(result.device.address, result))
                bt_list.adapter?.notifyDataSetChanged()
            }
            Log.d("DBG", "Result $result")
            //Log.d("DBG", result.device.address)
            Log.d("list", scanResults.toString())
            Log.d("size", scanResults.size.toString())
        }
    }

    override fun onDeviceReady(gatt: BluetoothGatt) {
        wrapper?.HEART_RATE_SERVICE_UUID?.let {
            wrapper?.HEART_RATE_MEASUREMENT_CHAR_UUID?.let { it1 ->
                wrapper!!.getNotifications(gatt,
                    it, it1)
            }
        }
    }

    override fun onDeviceDisconnected() {

    }

    override fun onNotify(characteristic: BluetoothGattCharacteristic) {
        Log.d("bpm", characteristic.value[1].toString())
        textview_bmp.text = characteristic.value[1].toString()

    }
}
