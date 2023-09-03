package com.asu.mc.healthcontextmonitor.ui.symptoms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.asu.mc.healthcontextmonitor.R
import com.asu.mc.healthcontextmonitor.model.SymptomRating
import com.asu.mc.healthcontextmonitor.ui.database.AppDatabase
import kotlinx.coroutines.launch

class SymptomsFragment : Fragment() {

    private lateinit var symptomsSpinner: Spinner
    private lateinit var symptomRatingBar: RatingBar
    private lateinit var uploadButton: Button
    private lateinit var uploadProgressBar: ProgressBar
    private val symptomRatings = mutableMapOf<String, Float>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        symptomsSpinner = view.findViewById(R.id.symptoms_spinner)
        symptomRatingBar = view.findViewById(R.id.symptom_rating_bar)
        uploadButton = view.findViewById(R.id.upload_button)
        uploadProgressBar = view.findViewById(R.id.upload_progress)

        lifecycleScope.launch {
            loadRatings()
            setupViews()  // Extracted the rest of your onCreateView logic into a separate function
        }

        return view
    }

    private fun setupViews() {
        val adapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.symptoms_array, android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        symptomsSpinner.adapter = adapter

        symptomsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>,
                selectedItemView: View,
                position: Int,
                id: Long
            ) {
                val symptom = symptomsSpinner.selectedItem.toString()
                symptomRatingBar.rating = symptomRatings[symptom] ?: 0f
            }

            override fun onNothingSelected(parentView: AdapterView<*>) {
                // Do nothing here
            }
        }

        symptomRatingBar.setOnRatingBarChangeListener { _, rating, _ ->
            val symptom = symptomsSpinner.selectedItem.toString()
            symptomRatings[symptom] = rating
        }

        uploadButton.setOnClickListener {
            uploadProgressBar.visibility = View.VISIBLE
            uploadRatings()
        }
    }

    private suspend fun loadRatings() {
        val allSymptoms = resources.getStringArray(R.array.symptoms_array)
        for (symptom in allSymptoms) {
            val rating = database.symptomRatingDao().getRatingBySymptom(symptom)?.rating ?: 0f
            symptomRatings[symptom] = rating
        }
    }

    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    private fun uploadRatings() {
        val selectedSymptom = symptomsSpinner.selectedItem.toString()
        val rating = symptomRatingBar.rating
        symptomRatings[selectedSymptom] = rating

        val allSymptoms = resources.getStringArray(R.array.symptoms_array)

        lifecycleScope.launch {
            for (symptom in allSymptoms) {
                val symptomRating = symptomRatings.getOrDefault(symptom, 0f)
                database.symptomRatingDao().insert(SymptomRating(symptom, symptomRating))
            }
            uploadProgressBar.visibility = View.GONE
        }
    }
}