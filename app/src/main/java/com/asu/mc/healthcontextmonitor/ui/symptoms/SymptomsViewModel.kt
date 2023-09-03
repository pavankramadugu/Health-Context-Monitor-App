package com.asu.mc.healthcontextmonitor.ui.symptoms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SymptomsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is symptoms Fragment"
    }
    val text: LiveData<String> = _text
}