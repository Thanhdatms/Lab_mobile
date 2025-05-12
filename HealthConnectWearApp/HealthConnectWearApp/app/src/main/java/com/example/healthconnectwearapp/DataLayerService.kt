package com.example.healthconnectwearapp

import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.WearableListenerService

class DataLayerService : WearableListenerService() {
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        // Handle data changes if needed

    }
}