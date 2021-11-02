package ru.ircoder.bookreader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ChapterViewModel: ViewModel() {
    val textSize: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>(3)
    }
    val positionRatio: MutableLiveData<Float> by lazy {
        MutableLiveData<Float>(0f)
    }
}