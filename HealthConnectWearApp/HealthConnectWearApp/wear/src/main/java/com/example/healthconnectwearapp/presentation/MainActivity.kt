package com.example.healthconnectwearapp.presentation

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.wear.activity.ConfirmationActivity
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import com.example.healthconnectwearapp.databinding.ActivityMainBinding

class MainActivity : ConfirmationActivity(), DataClient.OnDataChangedListener {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Khởi tạo giao diện với giá trị mặc định
        binding.tvSteps.text = "Steps: 0"
        binding.tvCalories.text = "Calories: 0"
    }

    override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
    }

    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("MainActivity", "onDataChanged called")
        for (event in dataEvents) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val dataItem = event.dataItem
                if (dataItem.uri.path == "/health_data") {
                    val dataMap = DataMapItem.fromDataItem(dataItem).dataMap
                    val steps = dataMap.getLong("steps", 0L)
                    val calories = dataMap.getDouble("calories", 0.0)
                    Log.d("MainActivity", "Received steps: $steps, calories: $calories")
                    binding.tvSteps.text = "Steps: $steps"
                    binding.tvCalories.text = "Calories: $calories"
                }
            }
        }
    }
}