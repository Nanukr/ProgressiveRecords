package com.rib.progressiverecords

import android.app.Application

class RecordApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RecordRepository.initialize(this)
    }
}