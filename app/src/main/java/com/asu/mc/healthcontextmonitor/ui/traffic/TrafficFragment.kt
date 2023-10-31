package com.asu.mc.healthcontextmonitor.ui.traffic

import android.app.AlertDialog
import android.app.ProgressDialog
import android.graphics.Color
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.asu.mc.healthcontextmonitor.R
import com.google.android.gms.common.api.Status
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale

class TrafficFragment : Fragment() {

    var startAddress: String? = null
    var endAddress: String? = null
    private var progressDialog: ProgressDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.activity_traffic, container, false)

        // Initialize the SDK
        Places.initialize(requireContext(), "AIzaSyDhrz7YAoJTMjuF9-YCMZSNXIe5F3d2pNo")

        // Initialize AutocompleteSupportFragment for start and end addresses
        setupAutocompleteFragment(R.id.start_address, true)
        setupAutocompleteFragment(R.id.end_address, false)

        rootView.findViewById<Button>(R.id.btn_analyze_traffic).setOnClickListener {
            if (startAddress != null && endAddress != null) {
                showProgressDialog()
                fetchDirections(startAddress!!, endAddress!!)
            }
        }

        return rootView
    }

    private fun setupAutocompleteFragment(fragmentId: Int, isStartAddress: Boolean) {
        val autocompleteFragment =
            childFragmentManager.findFragmentById(fragmentId) as AutocompleteSupportFragment

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.ADDRESS
            )
        )

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                if (isStartAddress) {
                    startAddress = place.address
                    Log.d("StartAddress", "Selected start address: ${place.address}")
                } else {
                    endAddress = place.address
                    Log.d("EndAddress", "Selected end address: ${place.address}")
                }
            }

            override fun onError(status: Status) {
                // TODO: Handle the error.
            }
        })
    }

    private fun fetchDirections(startAddress: String, endAddress: String) {
        val times = arrayOf("8:00", "13:00", "17:00", "22:00", "15:00")
        val results = mutableListOf<String>()
        val distances = mutableListOf<String>()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(TrafficModels.GoogleMapsApiService::class.java)

        times.forEach { time ->
            // Convert time to Unix timestamp (departureTime)
            val departureTime = timeToUnixTimestamp(time)

            println(departureTime)

            val distanceMatrixCall = service.getDistanceMatrix(
                startAddress,
                endAddress,
                "imperial",
                "driving",
                departureTime,
                "best_guess",
                "AIzaSyDhrz7YAoJTMjuF9-YCMZSNXIe5F3d2pNo"
            )

            distanceMatrixCall.enqueue(object : Callback<TrafficModels.DistanceMatrixResponse> {
                override fun onResponse(
                    call: Call<TrafficModels.DistanceMatrixResponse>,
                    response: Response<TrafficModels.DistanceMatrixResponse>
                ) {
                    val result = response.body()
                    result?.rows?.forEach { row ->
                        row.elements.forEach { element ->
                            val ratio =
                                element.duration_in_traffic.value.toDouble() / element.duration.value
                            val category = if (ratio <= 1.2) "LCW" else "HCW"
                            val distance =
                                element.distance.text
                            results.add("$time: $category")
                            distances.add(distance)
                        }
                    }
                    if (results.size == times.size) {
                        displayResults(results, distances)
                    }
                }

                override fun onFailure(
                    call: Call<TrafficModels.DistanceMatrixResponse>,
                    t: Throwable
                ) {
                    // Handle failure
                }
            })
        }
    }

    private fun displayResults(results: List<String>, distances: List<String>) {
        val finalWorkload: String
        val lcwCount = results.count { it.contains("LCW") }
        val hcwCount = results.count { it.contains("HCW") }

        finalWorkload = if (lcwCount >= hcwCount) {
            "LCW"
        } else {
            "HCW"
        }

        val finalWorkloadColor: Int = if (finalWorkload == "LCW") {
            Color.GREEN
        } else {
            Color.RED
        }

        val message = StringBuilder()
        message.append("Origin: $startAddress\n")
        message.append("Destination: $endAddress\n\n")

        results.zip(distances).forEach { (result, distance) ->
            message.append("$result, Distance: $distance\n")
        }

        val alertDialog = AlertDialog.Builder(context).apply {
            setTitle("Traffic Congestion Analysis")
            setMessage(message)
            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            setCancelable(false)
        }.create()
        hideProgressDialog()
        alertDialog.show()

        alertDialog.findViewById<TextView>(android.R.id.message)?.apply {
            val finalMessage = "$text\nFinal Workload: $finalWorkload"
            val spannableMessage = SpannableString(finalMessage)
            val startIndex = finalMessage.lastIndexOf(finalWorkload)
            spannableMessage.setSpan(
                ForegroundColorSpan(finalWorkloadColor),
                startIndex,
                startIndex + finalWorkload.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            text = spannableMessage
        }
    }

    private fun timeToUnixTimestamp(time: String): Long {
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getDefault() // Set your timezone if necessary

        // Set date to today
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = dateFormat.format(calendar.time)

        // Combine date with time
        val dateTime = "$date $time"

        // Convert to Unix timestamp
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val dateObj = dateTimeFormat.parse(dateTime)

        return dateObj?.time ?: 0L
    }

    private fun showProgressDialog() {
        progressDialog = ProgressDialog(context)
        progressDialog?.setMessage("Analyzing traffic, please wait...")
        progressDialog?.setCancelable(false)
        progressDialog?.show()
    }

    private fun hideProgressDialog() {
        progressDialog?.dismiss()
    }
}
