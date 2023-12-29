package com.mankirat.approck.lib

import com.android.billingclient.api.BillingClient

object MyConstants {

    const val SHARED_PREF_IAP = "iap_app_rock"
    const val IS_PREMIUM = "is_premium"
    const val IAP_DEFAULT_STATUS = false
    const val IN_APP_PRODUCTS = "in_app_products"
    const val IN_APP_SUBS = "in_app_subs"

    const val SUBSCRIPTION_STATUS_POSTFIX = "_sub_enabled"
    const val SUBSCRIPTION_PRICE_POSTFIX = "_sub_price"

    @Suppress("SpellCheckingInspection")
    const val BASE_64_KEY =
        "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiL3v2EPMPfhTQ6LrqdGvfo2riCeomIzyq4yZ8QBcsF1cRzaAU4G6f8/pGBXHjHJTud1+CGqdplyDxw/fzQqn5MDOCwrdaAbt4WeU/q5+9NoQFIdp08ZVjEuSl2vSozbM7U0F4AABZa/P6VkltE7aMlNsHuOcWp6oMveCHggZ9mucw3t2/AFsTwSVbRmYdDM5zhrtmWdTB6GyInoMQeR3HXRT9ti2IrF4R2qs88pPBWPfmnTWoGP0YWCF8p59pPzZITh1nr+RqiuEZcl6e7fItYB42MsNdluk+Ja99Kxu4JdC1kzqaWSI7Dyvfw/aQXBSwREKRWL/3O3sb1hV86LekQIDAQAB"


    object FirebaseEvent {
        const val LOAD_INTERSTITIAL_SUCCESS = "load_inter_success"
        const val LOAD_INTERSTITIAL_ERROR = "load_inter_fail"

        const val SHOW_INTERSTITIAL_SUCCESS = "show_inter_success"
        const val SHOW_INTERSTITIAL_ERROR = "show_inter_fail"

        const val LOAD_INTERSTITIAL_SPLASH_SUCCESS = "load_inter_splash_success"
        const val LOAD_INTERSTITIAL_SPLASH_ERROR = "load_inter_splash_fail"

        const val SHOW_INTERSTITIAL_SPLASH_SUCCESS = "show_inter_splash_success"
        const val SHOW_INTERSTITIAL_SPLASH_ERROR = "show_inter_splash_fail"

        const val LOAD_APP_OPEN_SUCCESS = "load_app_open_success"
        const val LOAD_APP_OPEN_ERROR = "load_app_open_fail"

        const val SHOW_APP_OPEN_SUCCESS = "show_app_open_success"
        const val SHOW_APP_OPEN_ERROR = "show_app_open_fail"

        const val SHOW_BANNER_SUCCESS = "show_banner_success"
        const val SHOW_BANNER_ERROR = "show_banner_fail"

        const val LOAD_NATIVE_SUCCESS = "load_native_success"
        const val LOAD_NATIVE_ERROR = "load_native_fail"

        const val LOAD_REWARD_SUCCESS = "load_reward_success"
        const val LOAD_REWARD_ERROR = "load_reward_fail"
    }

    object BillingConstant {
        const val IN_APP_PURCHASE = BillingClient.SkuType.INAPP
        const val IN_APP_SUBS = BillingClient.SkuType.SUBS
    }
}