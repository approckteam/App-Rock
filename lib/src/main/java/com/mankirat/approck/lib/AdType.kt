package com.mankirat.approck.lib

enum class AdType(val firebaseEvent: String) {
    APP_OPEN("app_open"),
    INTERSTITIAL("interstitial"),
    INTERSTITIAL_SPLASH("interstitial_splash"),
    BANNER("banner"),
    NATIVE("native"),
    REWARD("reward"),
}