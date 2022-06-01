package com.mankirat.approck

import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.mankirat.approck.lib.MyConstants
import com.mankirat.approck.lib.Utils
import com.mankirat.approck.lib.admob.AdMobUtil
import com.mankirat.approck.lib.iap.InAppManager
import com.mankirat.approck.lib.util.ConnectionReceiver

open class BaseActivity : AppCompatActivity() {

//    protected val firebaseRemoteConfig = ApplicationGlobal.instance.firebaseRemoteConfig
//    private val firebaseAnalytics = ApplicationGlobal.instance.firebaseAnalytics

    companion object {
        var inAppManager: InAppManager? = null
        var inAppSubs: InAppManager? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(ConnectionReceiver(), intentFilter)
        ConnectionReceiver.internetListener = object : ConnectionReceiver.ReceiverListener {
            override fun onNetworkChange(isConnected: Boolean) {
                if (isConnected) {
                    Utils.alertDialog?.dismiss()
                    Utils.alertDialog = null
                    if (inAppManager == null) appRockSubs()
                }
            }
        }

        Utils.buySubscription = {
            inAppSubs?.purchase(this, it)
        }
    }

    override fun onResume() {
        super.onResume()

        updateUI(isPremium())
        Utils.purchaseCallback = {
            if (it) updateUI(it)
            else {
                if (inAppSubs == null) {
                    // list of subscription that used within your app, You can find these at Google play console
                    inAppSubs = InAppManager("", arrayListOf("week_subs", "month_subs", "year_subs"), MyConstants.BillingConstant.IN_APP_SUBS)
                    inAppSubs?.restartConnection()
                } else updateUI(it)
            }
        }
    }

    protected fun isPremium(): Boolean = inAppManager?.isPurchased() == true || inAppSubs?.isPurchased() == true

    open fun updateUI(it: Boolean) {
        Log.e("updateUI: ", "data  $it")
    }

    protected fun appRockSubs() {
        if (inAppManager != null) return

        // Base64 key of your app
        // list of product that previously used for inAppPurchase, You can find these at Google play console
        inAppManager = InAppManager("", arrayListOf(""), MyConstants.BillingConstant.IN_APP_PURCHASE)
        inAppManager?.setUpBillingClient(this)

        Utils.firebaseEventCallback = { name, bundle ->
//            firebaseAnalytics.logEvent(name, bundle)
        }

        appRockAds()
    }

    protected fun appRockAds() {
        AdMobUtil.adMobIds.apply {
            appOpenId = ""
            interstitialId = ""
            interstitialIdSplash = ""
            bannerId = ""
            nativeId = ""
            rewardId = ""
        }

        // Click counts must be handled using Firebase remote config
        var buttonClickCount = 0 // firebaseRemoteConfig.getLong("").toInt()
        var screenOpenCount = 0 // firebaseRemoteConfig.getLong("").toInt()
        if (buttonClickCount == 0) buttonClickCount = 5
        if (screenOpenCount == 0) screenOpenCount = 1

        // Native Color used for native ads, and must be your app theme color
        AdMobUtil.setUp(this, targetClick = buttonClickCount, targetScreenCount = screenOpenCount, nativeColor = ContextCompat.getColor(this, R.color.black))
    }
}