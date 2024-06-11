package com.mankirat.approck.lib.iap

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetailsResponseListener
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.mankirat.approck.lib.MyConstants
import com.mankirat.approck.lib.Utils
import com.mankirat.approck.lib.model.PurchaseModel
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

class InAppManager(private val base64Key: String, private val productIds: ArrayList<String>, private val type: String) {

    private var sharedPreferences: SharedPreferences? = null

    val params = QueryPurchasesParams.newBuilder().setProductType(type).build()
    private fun log(msg: String, e: Throwable? = null) = Log.e("InAppManager", msg, e)
    private fun toast(context: Context, msg: String) = Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()

    private val productList = ArrayList<QueryProductDetailsParams.Product>()

    init {
        productIds.forEach {
            productList.add(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(it)
                    .setProductType(type)
                    .build()
            )
        }
    }

    private val skuDetailParams = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

    private var billingClient: BillingClient? = null

    fun setUpBillingClient(context: Context) {
        log("setUpBillingClient : billingClient = $billingClient")
        sharedPreferences = context.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE)
        if (billingClient == null) {
            billingClient =
                BillingClient.newBuilder(context).setListener(productPurchaseCallback(context)).enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                ).build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    log("setUpBillingClient : onBillingSetupFinished : billingResult = $billingResult")
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) billingClient?.queryProductDetailsAsync(
                        skuDetailParams,
                        productsDetailCallback
                    )

                    billingClient?.queryPurchasesAsync(params, historyCallback(context))
                }

                override fun onBillingServiceDisconnected() {
                    log("setUpBillingClient : onBillingServiceDisconnected")
                    billingClient = null
                }
            })
        }
    }

    private val productsDetailCallback = ProductDetailsResponseListener { billingResult, productList ->
        log("getProductDetail : onSkuDetailsResponse : billingResult = $billingResult : productList = $productList")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productList != null) {

            val data = PurchaseModel()
            data.success = "1"
            data.productDetails = ArrayList()
            productList.forEach {
                val purchaseModel = PurchaseModel.PurchaseDetailModel()
                purchaseModel.description = it.description

                    val pricingPhases = it.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList
                    pricingPhases?.forEach { pricingPhase ->
                        if (pricingPhase.priceAmountMicros == 0L) {
                            purchaseModel.freeTrialPeriod = pricingPhase.billingPeriod
//                            it.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.get(0)?.billingPeriod
                        }
                        purchaseModel.subscriptionPeriod = pricingPhase.billingPeriod
                    }
                    purchaseModel.introductoryPrice =
                        it.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice
                    purchaseModel.originalPrice =
                        it.subscriptionOfferDetails?.get(0)?.pricingPhases?.pricingPhaseList?.lastOrNull()?.formattedPrice

//                purchaseModel.subscriptionPeriod = it.subscriptionPeriod
//                purchaseModel.introductoryPrice = it.subscriptionPeriod
//                purchaseModel.originalPrice = it.originalPrice
//                purchaseModel.freeTrialPeriod = it.freeTrialPeriod
                purchaseModel.price = it.oneTimePurchaseOfferDetails?.formattedPrice
                purchaseModel.priceCurrencyCode = it.oneTimePurchaseOfferDetails?.priceCurrencyCode
                purchaseModel.productId = it.productId
                purchaseModel.title = it.title
                purchaseModel.type = it.productType
                data.productDetails?.add(purchaseModel)
            }
            if (type == BillingClient.ProductType.INAPP) Utils.putObject(
                sharedPreferences,
                MyConstants.IN_APP_PRODUCTS,
                data
            )
            if (type == BillingClient.ProductType.SUBS) Utils.putObject(
                sharedPreferences,
                MyConstants.IN_APP_SUBS,
                data
            )
        }
    }

    fun historyCallback(context: Context) = PurchasesResponseListener { billingResult, purchaseHistoryRecordList ->
        log("getHistory : onPurchaseHistoryResponse : billingResult = $billingResult : purchaseHistoryRecordList = $purchaseHistoryRecordList")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

            val status = (purchaseHistoryRecordList.size ?: 0) > 0
            setProductStatus(status)

            Utils.purchaseCallback?.invoke(status)
            if (Utils.purchaseCallback != null) toast(context, "Purchase Restored")
        }
    }

    /*________________________ Shared Pref _______________________*/
    private fun setProductStatus(status: Boolean) =
        sharedPreferences?.edit()?.putBoolean(MyConstants.IS_PREMIUM, status)?.apply()

    /*________________________ History and products detail _______________________*/

    /*________________________________ Restore ________________________________*/
    fun disconnectConnection() {
        log("disconnectConnection")
        billingClient?.endConnection()
        billingClient = null
    }

    fun purchase(activity: Activity, productId: String) {
        log("purchase : productId = $productId")

        val productsDetailCallback = ProductDetailsResponseListener { billingResult, productList ->
            log("purchase : onSkuDetailsResponse : billingResult = $billingResult : productList = $productList")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productList != null && productList.isNotEmpty()) {

                productList.forEach {
                    if (it.productId == productId) {
//                        val productDetailsParams =
//                            BillingFlowParams.ProductDetailsParams.newBuilder().setProductDetails(it).build()

                        val productDetailsParamsList = listOf(
                            it.subscriptionOfferDetails?.get(0)?.let { it1 ->
                                it1.offerToken.let { it2 ->
                                    BillingFlowParams.ProductDetailsParams.newBuilder()
                                        .setProductDetails(it)
                                        .setOfferToken(it2)
                                        .build()
                                }
                            }
                        )
                        val flowParams =
                            BillingFlowParams.newBuilder().setProductDetailsParamsList(productDetailsParamsList)
                                .build()
                        val responseCode = billingClient?.launchBillingFlow(activity, flowParams)?.responseCode
                        log("purchase : productId = $productId : responseCode = $responseCode")
                    }
                }
            }
        }

        billingClient?.queryProductDetailsAsync(skuDetailParams, productsDetailCallback)
    }

    //after completing work on google/play store activity
    private fun productPurchaseCallback(context: Context) = PurchasesUpdatedListener { billingResult, purchaseList ->
        //This method starts when user buy a product
        log("purchasesCallback : onPurchasesUpdated : billingResult = $billingResult : purchaseList = $purchaseList")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList != null && purchaseList.isNotEmpty()) {
            purchaseList.forEach {
                handlePurchase(context, it)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            setProductStatus(true)
            Utils.purchaseCallback?.invoke(true)
            Utils.subsCallback?.invoke()
            if (Utils.purchaseCallback != null) toast(context, "Item already owned")
        } else if (billingResult.debugMessage != "") toast(context, billingResult.debugMessage)
    }

    private fun handlePurchase(context: Context, purchase: Purchase) {
        log("handlePurchase : purchase : $purchase")
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (isSignatureValid(purchase)) {
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams =
                            AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                            log("handlePurchase : onAcknowledgePurchaseResponse : billingResult = $billingResult")

                            setProductStatus(true)
                            Utils.purchaseCallback?.invoke(true)
                            Utils.subsCallback?.invoke()
                            toast(context, "Item purchased")
                        }
                    } else {
                        setProductStatus(true)
                        Utils.purchaseCallback?.invoke(true)
                        Utils.subsCallback?.invoke()
                        toast(context, "Item purchased")
                    }
                }
            }

            Purchase.PurchaseState.PENDING -> toast(context, "Purchase PENDING")
            Purchase.PurchaseState.UNSPECIFIED_STATE -> toast(context, "Purchase UNSPECIFIED_STATE")
        }
    }

    /*________________________ check base 64 key _____________________________*/

    private fun isSignatureValid(purchase: Purchase): Boolean {
        log("isSignatureValid : purchase = $purchase")
        val encodedPublicKey = base64Key
        val signature = purchase.signature
        val signedData = purchase.originalJson
        val signatureAlgorithm = "SHA1withRSA"
        val keyFactoryAlgorithm = "RSA"

        if (signature.trim().isEmpty() || signedData.trim().isEmpty()) {
            return false
        } else {
            val publicKey = generatePublicKey(encodedPublicKey, keyFactoryAlgorithm)

            try {
                val signatureBytes = Base64.decode(signature, Base64.DEFAULT)

                try {
                    val signAlgorithm = Signature.getInstance(signatureAlgorithm)
                    signAlgorithm.initVerify(publicKey)
                    signAlgorithm.update(signedData.toByteArray())
                    return signAlgorithm.verify(signatureBytes)
                } catch (e: NoSuchAlgorithmException) {
                    // "RSA" is guaranteed to be available
                    log("NoSuchAlgorithmException", e)
                    throw RuntimeException(e)
                } catch (e: InvalidKeyException) {
                    //Invalid key specification
                    log("InvalidKeyException", e)
                    return false
                } catch (e: SignatureException) {
                    //Signature exception
                    log("SignatureException", e)
                    return false
                }

            } catch (e: IllegalArgumentException) {
                //Base64 decoding failed
                log("IllegalArgumentException", e)
                return false
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun generatePublicKey(encodedPublicKey: String, keyFactoryAlgorithm: String): PublicKey {
        log("generatePublicKey : encodedPublicKey = $encodedPublicKey : keyFactoryAlgorithm = $keyFactoryAlgorithm")
        return try {
            val decodedKey: ByteArray = Base64.decode(encodedPublicKey, Base64.DEFAULT)
            val keyFactory = KeyFactory.getInstance(keyFactoryAlgorithm)
            keyFactory.generatePublic(X509EncodedKeySpec(decodedKey))
        } catch (e: NoSuchAlgorithmException) {
            // "RSA" is guaranteed to be available.
            log("NoSuchAlgorithmException", e)
            throw java.lang.RuntimeException(e)
        } catch (e: InvalidKeySpecException) {
            log("InvalidKeySpecException", e)
            val msg = "Invalid key specification: $e"
            throw IOException(msg)
        }
    }

    // call
    fun isPurchased(): Boolean = sharedPreferences?.getBoolean(MyConstants.IS_PREMIUM, MyConstants.IAP_DEFAULT_STATUS)
        ?: MyConstants.IAP_DEFAULT_STATUS

    fun getAllProductList(): PurchaseModel? {
        return when (type) {
            BillingClient.ProductType.INAPP -> {
                if (sharedPreferences?.contains(MyConstants.IN_APP_PRODUCTS) == true) Utils.getObject(
                    sharedPreferences,
                    MyConstants.IN_APP_PRODUCTS,
                    PurchaseModel::class.java
                ) else null
            }

            else -> {
                if (sharedPreferences?.contains(MyConstants.IN_APP_SUBS) == true) Utils.getObject(
                    sharedPreferences,
                    MyConstants.IN_APP_SUBS,
                    PurchaseModel::class.java
                ) else null
            }
        }
    }
}

/* Use
* purchase() for buy product
* restore()
* isProductPurchased() two function call from on Resume
* */


/*Pending Tasks in this class
* Strings Localisation
* Firebase Events on
* Optimize Base64Key
* create getProductDetail(productIds:Array): Array<CustomModel>  fun and CustomModel Pojo class
* */