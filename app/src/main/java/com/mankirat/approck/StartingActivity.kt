package com.mankirat.approck

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mankirat.approck.lib.Utils
import com.mankirat.approck.lib.activity.SubsActivity
import com.mankirat.approck.lib.admob.AdMobUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StartingActivity : BaseActivity() {

    companion object {
        private var job: Job? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val splashScreen = installSplashScreen()
//        setContentView(binding.root)

//        ApplicationGlobal.instance.firebaseRemoteConfig.fetchAndActivate().addOnCompleteListener { task ->
//            log("onCreate : addOnCompleteListener : isSuccessful = ${task.isSuccessful} : all = ${ApplicationGlobal.instance.firebaseRemoteConfig.all}")
//        }.addOnSuccessListener { result ->
//            log("onCreate : addOnSuccessListener : result = $result")
//        }.addOnFailureListener { e ->
//            log("onCreate : addOnFailureListener : message = ${e.message}", e)
//        }.addOnCanceledListener {
//            log("onCreate : addOnCanceledListener")
//        }

        appRockAds()
        checkNetwork()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) splashScreen.setKeepOnScreenCondition { true }
    }

    override fun onPause() {
        super.onPause()
        job?.cancel(null)
        job = null
    }

    override fun onResume() {
        super.onResume()
        job?.cancel(null)
        job = null
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(1000)
                if (isPremium()) {
                    next(true)
                    break
                } else if (inAppSubs != null) {
                    delay(1000)
                    next(isPremium())
                    break
                }
            }
        }
    }

    private fun checkNetwork() {
        Utils.hasInternetConnection(this) {
            if (it) {
                Utils.alertDialog?.dismiss()
                Utils.alertDialog = null
                appRockSubs()
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//                    startActivity(Intent(this@StartingActivity, MainActivity::class.java))
                    finishAffinity()
                } else {
                    Utils.internetDialog(this) {
//                        startActivity(Intent(this@StartingActivity, MainActivity::class.java))
                        finishAffinity()
                    }
                }
            }
        }
    }

    private suspend fun next(isPremium: Boolean) {
        withContext(Dispatchers.Main) {
            if (isPremium) {
//                startActivity(Intent(this@StartingActivity, MainActivity::class.java))
                finishAffinity()
            } else {
                AdMobUtil.loadInterstitialSplash(this@StartingActivity) {
                    SubsActivity.purchaseModel = inAppSubs?.getAllProductList()
//                    SubsActivity.selectedSubs = firebaseRemoteConfig.getString(FirebaseRemote.SELECTED_SUBS)
                    SubsActivity.callback = {
//                        startActivity(Intent(this@StartingActivity, MainActivity::class.java))
                        finishAffinity()
                    }

                    val intent = Intent(this@StartingActivity, SubsActivity::class.java)
                    intent.putExtra(SubsActivity.FROM_SPLASH, true)
                    startActivity(intent)
                    finishAffinity()
                }
            }
        }
    }
}