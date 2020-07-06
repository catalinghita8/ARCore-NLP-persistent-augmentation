package com.example.arvoice

import android.app.Application
import android.content.Context
import com.example.arvoice.utils.Constants
import com.example.arvoice.domain.LogItem
import com.example.arvoice.nlp.NLPClient
import java.lang.Math.round
import java.util.*


class ARVoiceApplication : Application() {

    init {
        instance = this
    }

    companion object {
        var instance: ARVoiceApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }
    }

}