# multiple-text

# In project level gradle

allprojects {<br />
    repositories {
    
        //multiple-text
        maven { url 'https://jitpack.io' }
        
   }
}


# In app level gradle

dependencies {

    //multiple-text
    implementation 'com.github.approckteam:app-rock:1.1.0'
}


# Usage



AdMobUtil.adMobIds.apply {<br />
    interstitialId = Constants.AdMob.INTERSTITIAL<br />
    interstitialIdSplash = Constants.AdMob.INTERSTITIAL_SPLASH<br />
    bannerId = Constants.AdMob.BANNER<br />
    nativeId = Constants.AdMob.NATIVE<br />
    rewardId = Constants.AdMob.REWARD<br />
    appOpenId = Constants.AdMob.APP_OPEN<br />
}<br />

AdMobUtil.setUp(this, 4, Color.RED)<br />

binding.btnShowInterstitial.setOnClickListener {<br />
    adMobInter()<br />
}<br />

binding.flBannerAd.adMobBanner()<br />

binding.flNativeAd.adMobNative()<br />