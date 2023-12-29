package com.mankirat.approck.lib.model

import java.io.Serializable

class PurchaseModel : Serializable {

    var success: String? = null
    var productDetails: ArrayList<PurchaseDetailModel>? = null

    class PurchaseDetailModel : Serializable {

        var price: String? = null
        var freeTrialPeriod: String? = null
        var description: String? = null
        var introductoryPrice: String? = null
        var originalPrice: String? = null
        var subscriptionPeriod: String? = null
        var priceCurrencyCode: String? = null
        var productId: String? = null
        var title: String? = null
        var type: String? = null
    }
}