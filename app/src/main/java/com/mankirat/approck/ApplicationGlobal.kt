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

    /* val inAppPurchase by lazy {
         InAppPurchase(
             this,
             base64Key = Constants.IAP.BASE_64_KEY,
             mainProductId = Constants.IAP.PREMIUM_ID,
             allProducts = arrayListOf(Constants.IAP.PREMIUM_ID, Constants.IAP.DONATE_2_ID, Constants.IAP.DONATE_5_ID)
         )
     }*/


    override fun onCreate() {
        super.onCreate()
        log("onCreate")

        instance = this

        /* val inAppPurchase = InAppPurchase(
             this,
             base64Key = Constants.IAP.BASE_64_KEY,
             mainProductId = Constants.IAP.PREMIUM_ID,
             allProducts = arrayListOf(Constants.IAP.PREMIUM_ID, Constants.IAP.DONATE_2_ID, Constants.IAP.DONATE_5_ID)
         )*/


    }

}