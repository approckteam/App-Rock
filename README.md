# app-rock common lib for all apps of Mariano (IN MARCH 2022)
# Developers : MANKIRAT SINGH, Bhumika Sharma, ANKIT, Ashish David

# In project level gradle

allprojects {<br />
    repositories {
    
        //app-rock
        maven { url 'https://jitpack.io' }
        
   }
}


# In app level gradle

dependencies {

    //app-rock
    implementation 'com.github.approckteam:app-rock:1.1.3'
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
