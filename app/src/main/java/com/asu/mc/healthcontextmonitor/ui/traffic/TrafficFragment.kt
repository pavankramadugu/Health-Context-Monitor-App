package com.asu.mc.healthcontextmonitor.ui.traffic

import android.app.AlertDialog
import android.graphics.Color
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

class TrafficFragment : Fragment() {

    var startAddress: String? = null
    var endAddress: String? = null

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
        val retrofit = Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/maps/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(TrafficModels.GoogleMapsApiService::class.java)

        val distanceMatrixCall = service.getDistanceMatrix(
            startAddress,
            endAddress,
            "imperial",
            "driving",
            "now",
            "best_guess",
            "AIzaSyDhrz7YAoJTMjuF9-YCMZSNXIe5F3d2pNo"
        )

        distanceMatrixCall.enqueue(object : Callback<TrafficModels.DistanceMatrixResponse> {
            override fun onResponse(call: Call<TrafficModels.DistanceMatrixResponse>, response: Response<TrafficModels.DistanceMatrixResponse>) {
                val result = response.body()
                println(result)
                result?.rows?.forEach { row ->
                    row.elements.forEach { element ->
                        Log.d("DistanceMatrix", "Distance: ${element.distance.text}, Duration: ${element.duration.text}")

                        val origin = result.origin_addresses[0] // Adjust index based on your needs
                        val destination = result.destination_addresses[0] // Adjust index based on your needs

                        val averageDuration = "${element.duration_in_traffic.text} (${element.duration_in_traffic.value} seconds)"
                        val duration = "${element.duration.text} (${element.duration.value} seconds)"

                        val category = if (element.duration_in_traffic.value >= element.duration.value) "LCW" else "HCW"
                        val categoryColor = if (category == "LCW") Color.GREEN else Color.RED

                        val message = """
                    Origin: $origin
                    Destination: $destination
                    Average Duration: $averageDuration
                    Duration: $duration
                    Category: $category
                """.trimIndent()

                        val alertDialog = AlertDialog.Builder(context).apply {
                            setTitle("Traffic Congestion Analysis")
                            setMessage(message)
                            setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                            setCancelable(false)
                        }.create()

                        alertDialog.show()

                        // Change the color of Category
                        alertDialog.findViewById<TextView>(android.R.id.message)?.apply {
                            val spannable = SpannableString(text)
                            val startIndex = text.indexOf(category)
                            spannable.setSpan(ForegroundColorSpan(categoryColor), startIndex, startIndex + category.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            text = spannable
                        }
                    }
                }
            }

            override fun onFailure(call: Call<TrafficModels.DistanceMatrixResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }
}
