package com.asu.mc.healthcontextmonitor.ui.sensing

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.asu.mc.healthcontextmonitor.R
import com.asu.mc.healthcontextmonitor.model.RespRateEntity
import com.asu.mc.healthcontextmonitor.ui.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

class RespRateFragment : Fragment(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelValuesX = mutableListOf<Float>()
    private var accelValuesY = mutableListOf<Float>()
    private var accelValuesZ = mutableListOf<Float>()
    private lateinit var startCaptureButton: Button
    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_resp_rate, container, false)

        sensorManager = activity?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val progressDialog = ProgressDialog(context)
        startCaptureButton = view.findViewById(R.id.startCaptureButton)

        val args: RespRateFragmentArgs by navArgs()
        val heartRate = args.heartRate

        startCaptureButton.setOnClickListener {
            it.isEnabled = false
            accelValuesX.clear()
            accelValuesY.clear()
            accelValuesZ.clear()

            val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_NORMAL)

            object : CountDownTimer(45000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    startCaptureButton.text = "${millisUntilFinished / 1000} secs left"
                }

                override fun onFinish() {
                    sensorManager.unregisterListener(this@RespRateFragment)

                    progressDialog.show()
                    CoroutineScope(Dispatchers.Main).launch {
                        val rate = captureAndCalculateRate()
                        withContext(Dispatchers.IO) {
                            saveRespRateToDatabase(rate)
                        }
                        progressDialog.dismiss()
                        AlertDialog.Builder(context)
                            .setTitle("Result")
                            .setMessage("Your Resp Rate is: $rate")
                            .setPositiveButton("OK") { _, _ ->
                                val action =
                                    RespRateFragmentDirections.actionRespRateFragmentToSymptomsFragment(
                                        heartRate,
                                        rate
                                    )
                                findNavController().navigate(action)
                            }
                            .show()
                        it.isEnabled = true
                        startCaptureButton.text = "Start Capture"
                    }
                }

            }.start()
        }

        return view
    }

    private fun saveRespRateToDatabase(respRate: Int) {
        val time = System.currentTimeMillis()
        val respRateEntity = RespRateEntity(0, time, respRate)
        database.respRateDao().insert(respRateEntity)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            accelValuesX.add(it.values[0])
            accelValuesY.add(it.values[1])
            accelValuesZ.add(it.values[2])
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun captureAndCalculateRate(): Int {
        var previousValue = 0f
        var k = 0
        previousValue = 10f

        for (i in 11 until minOf(accelValuesX.size, accelValuesY.size, accelValuesZ.size)) {
            val currentValue = sqrt(
                accelValuesX[i].toDouble().pow(2.0) +
                        accelValuesY[i].toDouble().pow(2.0) +
                        accelValuesZ[i].toDouble().pow(2.0)
            ).toFloat()

            if (abs(previousValue - currentValue) > 0.15) {
                k++
            }
            previousValue = currentValue
        }

        val ret = k / 45.00
        return (ret * 30).toInt()
    }
}
