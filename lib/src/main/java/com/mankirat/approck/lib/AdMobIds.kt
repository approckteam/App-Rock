package com.mankirat.approck.lib

class AdMobIds {

    var debugIds = true

    var appId = ""

    var appOpenId = ""
        get() = if (debugIds) "ca-app-pub-3940256099942544/3419835294" else field

    /*var interstitialId: String = ""
        get() = if (debugIds) "ca-app-pub-3940256099942544/1033173712" else field
        set(id) {
            field = id
            if (interstitialIdSplash.isEmpty()) interstitialIdSplash = id
        }*/

    var interstitialId: String = ""
        get() = if (debugIds) "ca-app-pub-3940256099942544/1033173712" else field

    var interstitialIdSplash: String = ""
        get() = if (debugIds) "ca-app-pub-3940256099942544/1033173712" else field

    var bannerId = ""
        get() = if (debugIds) "ca-app-pub-3940256099942544/6300978111" else field

    var nativeId = ""
        get() = if (debugIds) "ca-app-pub-3940256099942544/2247696110" else field

    var rewardId = ""
        get() = if (debugIds) "ca-app-pub-3940256099942544/5224354917" else field

}