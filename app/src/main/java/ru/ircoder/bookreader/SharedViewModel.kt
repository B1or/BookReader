package ru.ircoder.bookreader

import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.ircoder.bookreader.MainActivity.Companion.TAG

class SharedViewModel : ViewModel() {

    val textSize: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(3)
    }

    val positionRatio: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>(0f)
    }

    val nightMode: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    fun setNightMode(nightMode: Boolean) {
        if (nightMode != this.nightMode.value) this.nightMode.value = nightMode
    }
}