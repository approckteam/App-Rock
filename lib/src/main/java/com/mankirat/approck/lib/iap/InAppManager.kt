package com.mankirat.approck.lib.iap

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import com.google.gson.Gson
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

class InAppManager(private val mContext: Context, private val base64Key: String, private val productIds: ArrayList<String>, val type: String) {

    private val sharedPreferences by lazy { mContext.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE) }
    var purchaseCallback: ((status: Boolean) -> Unit)? = null

    private fun log(msg: String, e: Throwable? = null) = Log.e("InAppPurchase", msg, e)
    private fun toast(msg: String) = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()

    private val skuDetailParams by lazy { SkuDetailsParams.newBuilder().setSkusList(productIds).setType(type).build() }

    companion object {
        private var billingClient: BillingClient? = null
    }

    private fun setUpBillingClient() {
        log("setUpBillingClient : billingClient = $billingClient")
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(mContext).setListener(productPurchaseCallback).enablePendingPurchases().build()

            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    log("setUpBillingClient : onBillingSetupFinished : billingResult = $billingResult")
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) billingClient?.querySkuDetailsAsync(skuDetailParams, productsDetailCallback)

                    billingClient?.queryPurchaseHistoryAsync(type, historyCallback)
                }

                override fun onBillingServiceDisconnected() {
                    log("setUpBillingClient : onBillingServiceDisconnected")
                    billingClient = null
                }
            })
        }
    }

    private val productsDetailCallback = SkuDetailsResponseListener { billingResult, productList ->
        log("getProductDetail : onSkuDetailsResponse : billingResult = $billingResult : productList = $productList")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productList != null) {

            val data = PurchaseModel()
            data.success = "1"
            data.productDetails = ArrayList()
            productList.forEach {
                val purchaseModel = PurchaseModel.PurchaseDetailModel()
                purchaseModel.description = it.description
                purchaseModel.subscriptionPeriod = it.subscriptionPeriod
                purchaseModel.introductoryPrice = it.subscriptionPeriod
                purchaseModel.originalPrice = it.originalPrice
                purchaseModel.freeTrialPeriod = it.freeTrialPeriod
                purchaseModel.price = it.price
                purchaseModel.priceCurrencyCode = it.priceCurrencyCode
                purchaseModel.productId = it.sku
                purchaseModel.title = it.title
                purchaseModel.type = it.type
                data.productDetails?.add(purchaseModel)
            }
            if (type == BillingClient.SkuType.INAPP) Utils.putObject(sharedPreferences, MyConstants.IN_APP_PRODUCTS, data)
            if (type == BillingClient.SkuType.SUBS) Utils.putObject(sharedPreferences, MyConstants.IN_APP_SUBS, data)
        }
    }

    val historyCallback = PurchaseHistoryResponseListener { billingResult, purchaseHistoryRecordList ->
        log("getHistory : onPurchaseHistoryResponse : billingResult = $billingResult : purchaseHistoryRecordList = $purchaseHistoryRecordList")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

            val status = purchaseHistoryRecordList?.size ?: 0 > 0
            setProductStatus(status)

            purchaseCallback?.invoke(status)
            if (purchaseCallback != null) toast("Purchase Restored")
        }
    }

    /*________________________ Shared Pref _______________________*/
    private fun setProductStatus(status: Boolean) = sharedPreferences.edit().putBoolean(MyConstants.IS_PREMIUM, status).apply()

    /*________________________ History and products detail _______________________*/

    /*________________________________ Restore ________________________________*/
    fun restorePurchase(callback: (() -> Unit)? = null) {
        log("restorePurchase")
        billingClient = null
        setUpBillingClient()
    }

    fun purchase(activity: Activity, productId: String, callback: ((status: Boolean) -> Unit)? = null) {
        log("purchase : productId = $productId")
        purchaseCallback = callback

        val productsDetailCallback = SkuDetailsResponseListener { billingResult, productList ->
            log("purchase : onSkuDetailsResponse : billingResult = $billingResult : productList = $productList")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productList != null && productList.isNotEmpty()) {

                productList.forEach {
                    if (it.sku == productId) {
                        val flowParams = BillingFlowParams.newBuilder().setSkuDetails(it).build()
                        val responseCode = billingClient?.launchBillingFlow(activity, flowParams)?.responseCode
                        log("purchase : productId = $productId : responseCode = $responseCode")
                    }
                }
            }
        }

        billingClient?.querySkuDetailsAsync(skuDetailParams, productsDetailCallback)
    }

    //after completing work on google/play store activity
    private val productPurchaseCallback = PurchasesUpdatedListener { billingResult, purchaseList ->
        //This method starts when user buy a product
        log("purchasesCallback : onPurchasesUpdated : billingResult = $billingResult : purchaseList = $purchaseList")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList != null && purchaseList.isNotEmpty()) {
            purchaseList.forEach {
                handlePurchase(it)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            setProductStatus(true)
            purchaseCallback?.invoke(true)
            if (purchaseCallback != null) toast("Item already owned")
        } else toast(billingResult.debugMessage)
    }

    private fun handlePurchase(purchase: Purchase) {
        log("handlePurchase : purchase : $purchase")
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (isSignatureValid(purchase)) {
                    if (!purchase.isAcknowledged) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                            log("handlePurchase : onAcknowledgePurchaseResponse : billingResult = $billingResult")
                        }
                    }
                    setProductStatus(true)
                    purchaseCallback?.invoke(true)
                    toast("Item purchased")
                }
            }
            Purchase.PurchaseState.PENDING -> toast("Purchase PENDING")
            Purchase.PurchaseState.UNSPECIFIED_STATE -> toast("Purchase UNSPECIFIED_STATE")
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
    fun isPurchased() = sharedPreferences.getBoolean(MyConstants.IS_PREMIUM, MyConstants.IAP_DEFAULT_STATUS)

    private fun <T> getObject(key: String, classOfT: Class<T>): T {
        val json: String = sharedPreferences.getString(key, "") ?: ""
        return Gson().fromJson(json, classOfT) ?: throw NullPointerException()
    }

    fun getAllProductList(): PurchaseModel {
        return if (type == BillingClient.SkuType.INAPP) getObject(MyConstants.IN_APP_PRODUCTS, PurchaseModel::class.java)
        else getObject(MyConstants.IN_APP_SUBS, PurchaseModel::class.java)
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