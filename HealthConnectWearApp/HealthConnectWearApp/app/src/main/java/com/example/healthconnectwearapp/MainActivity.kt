package com.example.healthconnectwearapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Duration
import java.time.Instant

class MainActivity : AppCompatActivity() {

    private var healthConnectClient: HealthConnectClient? = null
    private lateinit var statusText: TextView
    private val providerPackageName = "com.google.android.apps.health data"
    private val TAG = "HealthConnectWearApp"

    private val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    private val requestPermissions = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        Log.d(TAG, "Permissions result: $permissions")
        if (permissions.all { it.value }) {
            readHealthData()
        } else {
            statusText.text = "Permissions not granted"
            Log.w(TAG, "Permissions not granted: $permissions")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: Starting")
        try {
            Log.d(TAG, "onCreate: Setting content view")
            setContentView(R.layout.activity_main)
            Log.d(TAG, "onCreate: Content view set")

            Log.d(TAG, "onCreate: Finding status_text")
            statusText = findViewById(R.id.status_text)
            Log.d(TAG, "onCreate: status_text found")

            Log.d(TAG, "onCreate: Finding check_permissions_button")
            val checkPermissionsButton = findViewById<Button>(R.id.check_permissions_button)
            Log.d(TAG, "onCreate: check_permissions_button found")

            Log.d(TAG, "onCreate: Checking Health Connect availability")
            checkHealthConnectAvailability()

            Log.d(TAG, "onCreate: Setting click listener")
            checkPermissionsButton.setOnClickListener {
                Log.d(TAG, "Check permissions button clicked")
                checkPermissionsAndRun()
            }
            Log.d(TAG, "onCreate: Click listener set")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate error: ${e.message}", e)
            try {
                setContentView(R.layout.activity_main)
                statusText = findViewById(R.id.status_text)
                statusText.text = "Initialization error: ${e.message}"
            } catch (fallbackException: Exception) {
                Log.e(TAG, "Fallback error: ${fallbackException.message}", fallbackException)
            }
        }
    }

    private fun checkHealthConnectAvailability() {
        try {
            Log.d(TAG, "Checking Health Connect availability")
            val sdkStatus = HealthConnectClient.getSdkStatus(this, providerPackageName)
            Log.d(TAG, "Health Connect SDK Status: $sdkStatus")
            when (sdkStatus) {
                HealthConnectClient.SDK_UNAVAILABLE -> {
                    statusText.text = "Health Connect not installed. Please install from Play Store."
                    Log.w(TAG, "Health Connect not installed")
                }
                HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                    statusText.text = "Health Connect needs update"
                    Log.w(TAG, "Health Connect needs update")
                    val uriString = "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
                    startActivity(
                        Intent(Intent.ACTION_VIEW).apply {
                            setPackage("com.android.vending")
                            data = Uri.parse(uriString)
                            putExtra("overlay", true)
                            putExtra("callerId", packageName)
                        }
                    )
                }
                else -> {
                    healthConnectClient = HealthConnectClient.getOrCreate(this)
                    statusText.text = "Health Connect initialized"
                    Log.d(TAG, "Health Connect initialized successfully")
                }
            }
        } catch (e: Exception) {
            statusText.text = "Health Connect error: ${e.message}"
            Log.e(TAG, "Health Connect error: ${e.message}", e)
        }
    }

    private fun checkPermissionsAndRun() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d(TAG, "Checking permissions")
                if (healthConnectClient == null) {
                    statusText.text = "Health Connect not initialized"
                    Log.e(TAG, "Health Connect client is null")
                    return@launch
                }
                val granted = healthConnectClient!!.permissionController.getGrantedPermissions()
                if (granted.containsAll(PERMISSIONS)) {
                    Log.d(TAG, "All permissions granted, reading health data")
                    readHealthData()
                } else {
                    Log.d(TAG, "Requesting permissions")
                    requestPermissions.launch(PERMISSIONS.map { it.toString() }.toTypedArray())
                }
            } catch (e: Exception) {
                statusText.text = "Permission check error: ${e.message}"
                Log.e(TAG, "Permission check error: ${e.message}", e)
            }
        }
    }

    private fun readHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Reading health data")
                if (healthConnectClient == null) {
                    runOnUiThread {
                        statusText.text = "Health Connect not initialized"
                    }
                    Log.e(TAG, "Health Connect client is null")
                    return@launch
                }
                val endTime = Instant.now()
                val startTime = endTime.minus(Duration.ofDays(1))

                // Read Steps
                val stepsResponse = healthConnectClient!!.readRecords(
                    ReadRecordsRequest(
                        StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                var totalSteps = 0L
                for (record in stepsResponse.records) {
                    totalSteps += record.count
                }

                // Read Active Calories Burned
                val caloriesResponse = healthConnectClient!!.readRecords(
                    ReadRecordsRequest(
                        TotalCaloriesBurnedRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                var totalCalories = 0.0
                for (record in caloriesResponse.records) {
                    totalCalories += record.energy.inKilocalories
                }

                runOnUiThread {
                    statusText.text = "Total Steps: $totalSteps\nTotal Calories: ${String.format("%.2f", totalCalories)} kcal"
                }
                Log.d(TAG, "Health data read: Steps=$totalSteps, Calories=$totalCalories")
                sendHealthDataToWear(totalSteps, totalCalories)
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "Error reading health data: ${e.message}"
                }
                Log.e(TAG, "Error reading health data: ${e.message}", e)
            }
        }
    }

    private fun sendHealthDataToWear(steps: Long, calories: Double) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Sending health data to Wear: Steps=$steps, Calories=$calories")
                val dataClient = Wearable.getDataClient(this@MainActivity)
                val putDataMapReq = com.google.android.gms.wearable.PutDataMapRequest.create("/health_data")
                putDataMapReq.dataMap.putLong("steps", steps)
                putDataMapReq.dataMap.putDouble("calories", calories)
                putDataMapReq.dataMap.putLong("timestamp", System.currentTimeMillis())
                val putDataReq = putDataMapReq.asPutDataRequest()
                putDataReq.setUrgent()
                dataClient.putDataItem(putDataReq).await()
                Log.d(TAG, "Health data sent to Wear successfully")
            } catch (e: Exception) {
                runOnUiThread {
                    statusText.text = "Error sending data to Wear: ${e.message}"
                }
                Log.e(TAG, "Error sending data to Wear: ${e.message}", e)
            }
        }
    }

}