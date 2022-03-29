package com.mankirat.approck

import android.app.Application
import android.util.Log

class ApplicationGlobal : Application() {

    companion object {
        lateinit var instance: ApplicationGlobal
    }

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("ApplicationGlobal", msg, e)
    }

    override fun onCreate() {
        super.onCreate()
        log("onCreate")

        instance = this


    }

}