package com.mankirat.approck

object Constants {

    @Suppress("unused")
    object AdMob {
        const val APP_OPEN = "ca-app-pub-3940256099942544/3419835294"
        const val INTERSTITIAL_SPLASH = "ca-app-pub-6941619747897449/8698471301"
        const val INTERSTITIAL = "ca-app-pub-6941619747897449/8698471301"
        const val BANNER = "ca-app-pub-6941619747897449/8681664598"
        const val NATIVE = "ca-app-pub-6941619747897449/9304798351"
        const val REWARD = ""
    }

    object IAP {
        const val PREMIUM_ID = "imaganize_remove_ads"
        const val DONATE_2_ID = "donate2"
        const val DONATE_5_ID = "donate5"

        @Suppress("SpellCheckingInspection")
        const val BASE_64_KEY =
            "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA5dLtKLnCfMPdGLg8fZjFiXv9PqU0w314/nF+Fl/l9IZdZyxOJRGeBDTn4+lD7fSpJ/oCH+WrrX72Jy+xMOsckT2m9NpW+tgbZwEPIGxFccN7MG1JuduEH7Dm8VmB24q0yekdzvuiuU7zyUVumVHSWswcr4H2/yQWF2s/irskZuDSA12B/Wta66dVdVDYj4kiHhvQqV//uqGi1RSVRdl+3VInahz6XWjv5E5M2H/OjK2pvsQWN/hgSU+Ba/kyntBYtQUINzc36PLKBFYiwpwmtAxiTlUSMaRpOudfgNRJk9H7R0G1IvHVFAwP8qK8two0MmauPhIChxly885evrxJhwIDAQAB"


        const val DEFAULT_STATUS = true
        val PRODUCTS_LIST = listOf(PREMIUM_ID)

    }

}