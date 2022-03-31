package com.mankirat.approck

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.mankirat.approck.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private val binding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("HomeActivity", msg, e)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        binding.btnShowInterstitial.setOnClickListener {
//            adMobInter()
//        }
//        binding.flBannerAd.adMobBanner()
//        binding.flNativeAd.adMobNative()
    }

    override fun onResume() {
        super.onResume()

        /* ApplicationGlobal.instance.inAppPurchase.isProductPurchased(this) {
             log("aaaaaa:status=$it")
         }*/
    }

}