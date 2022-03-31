package com.mankirat.approck

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.mankirat.approck.databinding.ActivityHomeBinding
import com.mankirat.approck.lib.admob.AdMobUtil
import com.mankirat.approck.lib.admob.adMobBanner
import com.mankirat.approck.lib.admob.adMobInter
import com.mankirat.approck.lib.admob.adMobNative
import com.mankirat.approck.lib.iap.InAppPurchase

class HomeActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("HomeActivity", msg, e)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        /*val inAppPurchase = InAppPurchase(
            this,
            base64Key = Constants.IAP.BASE_64_KEY,
            mainProductId = Constants.IAP.PREMIUM_ID,
            allProducts = arrayListOf(Constants.IAP.PREMIUM_ID, Constants.IAP.DONATE_2_ID, Constants.IAP.DONATE_5_ID)
        )*/

        /*AdMobUtil.adMobIds.apply {
            interstitialId = Constants.AdMob.INTERSTITIAL
            interstitialIdSplash = Constants.AdMob.INTERSTITIAL_SPLASH
            bannerId = Constants.AdMob.BANNER
            nativeId = Constants.AdMob.NATIVE
            rewardId = Constants.AdMob.REWARD
            appOpenId = Constants.AdMob.APP_OPEN
        }*/
        //AdMobUtil.setUp(this, 4, Color.RED)

//        binding.btnShowInterstitial.setOnClickListener {
//            adMobInter()
//        }
//        binding.flBannerAd.adMobBanner()
//        binding.flNativeAd.adMobNative()
    }

    override fun onResume() {
        super.onResume()

        ApplicationGlobal.instance.inAppPurchase.isProductPurchased(this) {
            log("aaaaaa:status=$it")
        }
    }

}