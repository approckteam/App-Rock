package com.mankirat.approck.lib.admob

import com.mankirat.approck.lib.BuildConfig

class AdMobIds {
    var appOpenId = ""
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/3419835294" else field

    var interstitialId: String = ""
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else field

    var interstitialIdSplash: String = ""
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/1033173712" else field

    var bannerId = ""
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/6300978111" else field

    var nativeId = ""
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else field

    var rewardId = ""
        get() = if (BuildConfig.DEBUG) "ca-app-pub-3940256099942544/5224354917" else field
}