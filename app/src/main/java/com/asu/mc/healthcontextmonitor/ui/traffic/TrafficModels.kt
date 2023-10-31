package com.asu.mc.healthcontextmonitor.ui.traffic

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

class TrafficModels {
    data class DistanceMatrixResponse(
        val destination_addresses: List<String>,
        val origin_addresses: List<String>,
        val rows: List<Row>,
        val status: String
    )

    data class Row(val elements: List<Element>)

    data class Element(
        val distance: Distance,
        val duration: Duration,
        val duration_in_traffic: Duration,
        val status: String
    )

    data class Distance(val text: String, val value: Int)

    data class Duration(val text: String, val value: Int)

    interface GoogleMapsApiService {
        @GET("distancematrix/json")
        fun getDistanceMatrix(
            @Query("origins") origins: String,
            @Query("destinations") destinations: String,
            @Query("units") units: String,
            @Query("mode") mode: String,
            @Query("departure_time") departureTime: String,
            @Query("traffic_model") trafficModel: String,
            @Query("key") apiKey: String
        ): Call<DistanceMatrixResponse>
    }
}