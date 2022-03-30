package com.mankirat.approck.lib.iap

import android.app.Activity
import android.content.Context
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryResponseListener
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.android.billingclient.api.SkuDetailsParams
import com.android.billingclient.api.SkuDetailsResponseListener
import java.io.IOException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.Signature
import java.security.SignatureException
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec

@Suppress("SpellCheckingInspection")
const val BASE_64_KEY =
    "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAiL3v2EPMPfhTQ6LrqdGvfo2riCeomIzyq4yZ8QBcsF1cRzaAU4G6f8/pGBXHjHJTud1+CGqdplyDxw/fzQqn5MDOCwrdaAbt4WeU/q5+9NoQFIdp08ZVjEuSl2vSozbM7U0F4AABZa/P6VkltE7aMlNsHuOcWp6oMveCHggZ9mucw3t2/AFsTwSVbRmYdDM5zhrtmWdTB6GyInoMQeR3HXRT9ti2IrF4R2qs88pPBWPfmnTWoGP0YWCF8p59pPzZITh1nr+RqiuEZcl6e7fItYB42MsNdluk+Ja99Kxu4JdC1kzqaWSI7Dyvfw/aQXBSwREKRWL/3O3sb1hV86LekQIDAQAB"

class InAppPurchase(
    private val mContext: Context, private val premiumId: String, products: ArrayList<String>? = null,
    private val defaultStatus: Boolean = true,
    private val mAcknowledge: Boolean = true, private val base64Key: String = BASE_64_KEY
) {

    private fun log(msg: String, e: Throwable? = null) {
        Log.e("InAppPurchase", msg, e)
    }

    private fun toast(msg: String) {
        //runOnUiThread {
        Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show()
        //}
    }

    private val mProductList = ArrayList<String>()
    private var billingClient: BillingClient? = null
    private val skuDetailParams by lazy { SkuDetailsParams.newBuilder().setSkusList(mProductList).setType(BillingClient.SkuType.INAPP).build() }

    init {
        mProductList.clear()
        if (products == null) mProductList.add(premiumId)
        else mProductList.addAll(products)

        setUpBillingClient()
    }

    private fun setUpBillingClient(restoreCallback: (() -> Unit)? = null) {
        log("setUpBillingClient : billingClient = $billingClient")
        if (billingClient == null) {
            billingClient = BillingClient.newBuilder(mContext)
                .setListener(productPurchaseCallback)
                .enablePendingPurchases()
                .build()
        }

        getHistoryAndProducts(restoreCallback)
    }

    /*________________________ Shared Pref _______________________*/

    private val sharedPreferences by lazy { mContext.getSharedPreferences("iap_app_rock", Context.MODE_PRIVATE) }

    private fun setProductStatus(productId: String, status: Boolean) {
        sharedPreferences.edit().putBoolean(productId + "_enabled", status).apply()
    }

    @Suppress("unused")
    private fun getProductStatus(productId: String, default: Boolean = defaultStatus): Boolean {
        return sharedPreferences.getBoolean(productId + "_enabled", default)
    }

    private fun isProductStatus(productId: String): Boolean {
        return sharedPreferences.contains(productId + "_enabled")
    }

    private fun setProductDetail(product: SkuDetails) {
        val productId = product.sku

        sharedPreferences.edit().putString(productId + "_price", product.price).apply()
    }

    /*________________________ History and products detail _______________________*/

    private fun getHistoryAndProducts(restoreCallback: (() -> Unit)? = null) {
        val historyCallback = PurchaseHistoryResponseListener { billingResult, purchaseHistoryRecordList ->
            log("getHistory : onPurchaseHistoryResponse : billingResult = $billingResult : purchaseHistoryRecordList = $purchaseHistoryRecordList")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                mProductList.forEach {
                    setProductStatus(it, false)
                }

                purchaseHistoryRecordList?.forEach { purchase ->
                    purchase.skus.forEach { productId ->
                        setProductStatus(productId, true)
                    }
                }

                updateUI()

                restoreCallback?.invoke()
                if (restoreCallback != null) toast("Purchase Restored")
            }
        }

        if (billingClient?.connectionState == BillingClient.ConnectionState.CONNECTED) {
            //get History
            billingClient?.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, historyCallback)
            //get Product Details
            billingClient?.querySkuDetailsAsync(skuDetailParams, productsDetailCallback)
        } else {
            billingClient?.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    log("setUpBillingClient : onBillingSetupFinished : billingResult = $billingResult")

                    //get History
                    billingClient?.queryPurchaseHistoryAsync(BillingClient.SkuType.INAPP, historyCallback)
                    //get Product Details
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        billingClient?.querySkuDetailsAsync(skuDetailParams, productsDetailCallback)
                    }
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

            productList.forEach {
                setProductDetail(it)
            }
        }
    }

    /*________________________________ Restore ________________________________*/
    @Suppress("unused")
    fun restorePurchase(callback: (() -> Unit)? = null) {
        log("restorePurchase")
        billingClient?.endConnection()
        billingClient = null
        setUpBillingClient(callback)
    }

    /*________________________ start purchasing _____________________________*/
    private var purchaseCallback: ((status: Boolean) -> Unit)? = null
    private fun invokePurchaseCallback(status: Boolean) {
        purchaseCallback?.invoke(status)
        purchaseCallback = null

        if (status) updateUI()
    }

    @Suppress("unused")
    fun purchase(activity: Activity, productId: String, callback: ((status: Boolean) -> Unit)? = null) {
        log("purchase : productId = $productId")
        purchaseCallback = callback

        val responseCallback = SkuDetailsResponseListener { billingResult, productList ->
            log("purchase : onSkuDetailsResponse : billingResult = $billingResult : productList = $productList")
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productList != null && productList.isNotEmpty()) {

                productList.forEach {
                    if (it.sku == productId) {
                        val flowParams = BillingFlowParams.newBuilder().setSkuDetails(it).build()
                        val responseCode = billingClient?.launchBillingFlow(activity, flowParams)?.responseCode
                        log("purchase : productId = $productId : responseCode = $responseCode")
                    }
                }
            } else {
                invokePurchaseCallback(false)
            }
        }

        billingClient?.querySkuDetailsAsync(skuDetailParams, responseCallback)
    }

    private val productPurchaseCallback = PurchasesUpdatedListener { billingResult, purchaseList ->
        //This method starts when user buy a product
        log("purchasesCallback : onPurchasesUpdated : billingResult = $billingResult : purchaseList = $purchaseList")
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchaseList != null && purchaseList.isNotEmpty()) {
            purchaseList.forEach {
                handlePurchase(it)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            //restorePurchase()
            billingClient?.endConnection()
            billingClient = null
            setUpBillingClient()
            invokePurchaseCallback(true)
        } else {
            invokePurchaseCallback(false)
            toast("${billingResult.debugMessage} ResponseCode ${billingResult.responseCode}")
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        log("handlePurchase : purchase : $purchase")
        when (purchase.purchaseState) {
            Purchase.PurchaseState.PURCHASED -> {
                if (isSignatureValid(purchase)) {
                    purchase.skus.forEach {
                        setProductStatus(it, true)
                    }
                    invokePurchaseCallback(true)

                    if (!purchase.isAcknowledged && mAcknowledge) {
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder().setPurchaseToken(purchase.purchaseToken).build()
                        billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                            log("handlePurchase : onAcknowledgePurchaseResponse : billingResult = $billingResult")
                        }
                    }
                }
            }
            Purchase.PurchaseState.PENDING -> {
                toast("Purchase PENDING")
                invokePurchaseCallback(false)
            }
            Purchase.PurchaseState.UNSPECIFIED_STATE -> {
                toast("Purchase UNSPECIFIED_STATE")
                invokePurchaseCallback(false)
            }
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

    /*________________________ update ui _____________________________________*/

    private var fragmentInstance: Fragment? = null
    private var fragmentProducts: ArrayList<String>? = null
    private var fragmentCallback: ((status: Boolean) -> Unit)? = null
    private fun invokePremiumCallbackFragment(status: Boolean) {
        if (fragmentInstance?.isAdded == true) {
            fragmentCallback?.invoke(status)
        }
    }

    private var activityInstance: Activity? = null
    private var activityProducts: ArrayList<String>? = null
    private var activityCallback: ((status: Boolean) -> Unit)? = null
    private fun invokePremiumCallbackActivity(status: Boolean) {
        if (activityInstance?.isDestroyed == false && activityInstance?.isFinishing == false) {
            activityCallback?.invoke(status)
        }
    }

    private fun isProductPurchasedCommon(context: Any, productList: ArrayList<String>, callback: ((status: Boolean) -> Unit)) {
        var isProductStatus = true
        var status = true
        productList.forEach { productId ->
            if (!isProductStatus(productId)) {
                isProductStatus = false
                return@forEach
            }
            if (!getProductStatus(productId)) {
                status = false
                return@forEach
            }
        }

        if (context is Fragment) {
            fragmentInstance = context
            fragmentProducts = productList
            fragmentCallback = callback

            if (isProductStatus) {
                invokePremiumCallbackFragment(status)
            }
        } else if (context is Activity) {
            activityInstance = context
            activityProducts = productList
            activityCallback = callback

            if (isProductStatus) {
                invokePremiumCallbackActivity(status)
            }
        }

        if (billingClient == null) setUpBillingClient()
        else getHistoryAndProducts()
    }

    private fun updateUI() {
        log("updateUI")

        var statusFragment = true
        fragmentProducts?.forEach { productId ->
            if (!getProductStatus(productId)) {
                statusFragment = false
                return@forEach
            }
        }
        if (fragmentProducts == null) statusFragment = false
        invokePremiumCallbackFragment(statusFragment)

        var statusActivity = true
        activityProducts?.forEach { productId ->
            if (!getProductStatus(productId)) {
                statusActivity = false
                return@forEach
            }
        }
        if (activityProducts == null) statusActivity = false
        invokePremiumCallbackActivity(statusActivity)
    }


    @Suppress("unused")
    fun isProductPurchased(context: Fragment, productList: ArrayList<String>? = null, callback: ((status: Boolean) -> Unit)) {
        isProductPurchasedCommon(context, productList ?: arrayListOf(premiumId), callback)
    }

    @Suppress("unused")
    fun isProductPurchased(context: Activity, productList: ArrayList<String>? = null, callback: ((status: Boolean) -> Unit)) {
        isProductPurchasedCommon(context, productList ?: arrayListOf(premiumId), callback)
    }

}

/*
* purchase() for buy product
* restore()
* isProductPurchased() two function call from on Resume
* */


/*Pending Tasks in this class
* Strings Localisation
* Firebase Events on
* Optimize Base64Key
* */

