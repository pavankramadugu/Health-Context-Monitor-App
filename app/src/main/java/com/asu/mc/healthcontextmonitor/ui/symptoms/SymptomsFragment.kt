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
import androidx.navigation.fragment.navArgs
import com.asu.mc.healthcontextmonitor.R
import com.asu.mc.healthcontextmonitor.model.HealthContextEntity
import com.asu.mc.healthcontextmonitor.ui.database.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SymptomsFragment : Fragment() {

    private lateinit var symptomsSpinner: Spinner
    private lateinit var symptomRatingBar: RatingBar
    private lateinit var uploadButton: Button
    private lateinit var uploadProgressBar: ProgressBar
    private var currentTimestamp: Long? = null
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
            setupViews()
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

    private fun loadRatings() {
        lifecycleScope.launch(Dispatchers.IO) {
            val latestRecord = database.healthContextDao().getLatestRecord()

            withContext(Dispatchers.Main) {
                if (latestRecord != null) {
                    symptomRatings["Nausea"] = latestRecord.nausea
                    symptomRatings["Headache"] = latestRecord.headache
                    symptomRatings["Diarrhea"] = latestRecord.diarrhea
                    symptomRatings["Soar Throat"] = latestRecord.soarThroat
                    symptomRatings["Fever"] = latestRecord.fever
                    symptomRatings["Muscle Ache"] = latestRecord.muscleAche
                    symptomRatings["Loss of Smell or Taste"] = latestRecord.lossOfSmellOrTaste
                    symptomRatings["Cough"] = latestRecord.cough
                    symptomRatings["Shortness of Breath"] = latestRecord.shortnessOfBreath
                    symptomRatings["Feeling tired"] = latestRecord.feelingTired
                } else {
                    val allSymptoms = resources.getStringArray(R.array.symptoms_array)
                    for (symptom in allSymptoms) {
                        symptomRatings[symptom] = 0f
                    }
                }
            }
        }
    }

    private val database by lazy { AppDatabase.getInstance(requireContext()) }

    private fun uploadRatings() {
        val args: SymptomsFragmentArgs by navArgs()
        val heartRate = args.heartRate
        val respRate = args.respRate

        lifecycleScope.launch(Dispatchers.IO) {
            val time = currentTimestamp ?: System.currentTimeMillis()
            val existingRecord = database.healthContextDao().getRecordByTimestamp(time)

            if (existingRecord != null) {
                existingRecord.heartRate = heartRate
                existingRecord.respRate = respRate
                existingRecord.nausea = symptomRatings.getOrDefault("Nausea", 0f)
                existingRecord.headache = symptomRatings.getOrDefault("Headache", 0f)
                existingRecord.diarrhea = symptomRatings.getOrDefault("Diarrhea", 0f)
                existingRecord.soarThroat = symptomRatings.getOrDefault("Soar Throat", 0f)
                existingRecord.fever = symptomRatings.getOrDefault("Fever", 0f)
                existingRecord.muscleAche = symptomRatings.getOrDefault("Muscle Ache", 0f)
                existingRecord.lossOfSmellOrTaste = symptomRatings.getOrDefault("Loss of Smell or Taste", 0f)
                existingRecord.cough = symptomRatings.getOrDefault("Cough", 0f)
                existingRecord.shortnessOfBreath = symptomRatings.getOrDefault("Shortness of Breath", 0f)
                existingRecord.feelingTired = symptomRatings.getOrDefault("Feeling tired", 0f)

                database.healthContextDao().update(existingRecord)
            } else {
                val newRecord = HealthContextEntity(
                    timestamp = time,
                    heartRate = heartRate,
                    respRate = respRate,
                    nausea = symptomRatings.getOrDefault("Nausea", 0f),
                    headache = symptomRatings.getOrDefault("Headache", 0f),
                    diarrhea = symptomRatings.getOrDefault("Diarrhea", 0f),
                    soarThroat = symptomRatings.getOrDefault("Soar Throat", 0f),
                    fever = symptomRatings.getOrDefault("Fever", 0f),
                    muscleAche = symptomRatings.getOrDefault("Muscle Ache", 0f),
                    lossOfSmellOrTaste = symptomRatings.getOrDefault("Loss of Smell or Taste", 0f),
                    cough = symptomRatings.getOrDefault("Cough", 0f),
                    shortnessOfBreath = symptomRatings.getOrDefault("Shortness of Breath", 0f),
                    feelingTired = symptomRatings.getOrDefault("Feeling tired", 0f)
                )
                database.healthContextDao().insert(newRecord)
            }

            withContext(Dispatchers.Main) {
                currentTimestamp = null
                uploadProgressBar.visibility = View.GONE
            }
        }
    }
}
