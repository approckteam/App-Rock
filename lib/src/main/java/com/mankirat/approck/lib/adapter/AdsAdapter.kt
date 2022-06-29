package com.mankirat.approck.lib.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.mankirat.approck.lib.MyConstants
import com.mankirat.approck.lib.R
import com.mankirat.approck.lib.admob.AdMobUtil
import com.mankirat.approck.lib.databinding.ItemAdsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdsAdapter<T : Any>(@LayoutRes val layoutID: Int, private val context: Context) : RecyclerView.Adapter<AdsAdapter.ViewHolder<T>>() {

    private var nativeAds: NativeAd? = null
    private var count = 0
    private var totalAdLoad = 0

    var dataSet: ArrayList<T>? = null
    var bindingInterface: AdsBindingInterface<T>? = null
    var adsCount = 20
    var itemClickCallback: ((item: T) -> Unit)? = null

    class ViewHolder<T : Any>(private val view: View, dataSet: ArrayList<T>? = null, itemClickCallback: ((item: T) -> Unit)? = null) : RecyclerView.ViewHolder(view) {
        fun <T : Any> bind(item: T, bindingInterface: AdsBindingInterface<T>?) = bindingInterface?.bindData(item, view)

        init {
            itemView.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener

                val item = dataSet!![adapterPosition]
                itemClickCallback?.invoke(item)
            }
        }
    }

    interface AdsBindingInterface<T> {
        fun bindData(item: T, view: View)
    }

    override fun getItemCount() = dataSet?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder<T> {
        return if (getItemViewType(viewType) == 2) {
            ViewHolder(ItemAdsBinding.inflate(LayoutInflater.from(parent.context)).root, dataSet, itemClickCallback)
        } else ViewHolder(LayoutInflater.from(parent.context).inflate(layoutID, parent, false), dataSet, itemClickCallback)
    }

    override fun onBindViewHolder(holder: ViewHolder<T>, position: Int) {
        if (getItemViewType(position) == 2) {
            if (nativeAds == null) loadNativeAd(holder)
            else {
                count -= 5
                if (count <= 0 && totalAdLoad <= 10) loadNativeAd(holder)
                else if (nativeAds == null) loadNativeAd(holder)
                else {
                    val frameLayout = holder.itemView.findViewById<FrameLayout>(R.id.nv_frame)

                    val layoutInflater = frameLayout.context.getSystemService(LayoutInflater::class.java)
                    val adView = layoutInflater.inflate(R.layout.native_ad_mob_1, frameLayout, false) as NativeAdView
                    adView.setNativeAd(nativeAds!!)

                    frameLayout.removeAllViews()
                    frameLayout.addView(adView)
                    frameLayout.visibility = View.VISIBLE
                }
            }
        } else holder.bind(dataSet!![position], bindingInterface)
    }

    override fun getItemViewType(position: Int): Int {
        return if (isPremium(context)) 1 else if (position % adsCount == 0) 2 else 1
    }

    private fun isPremium(context: Context): Boolean =
        context.getSharedPreferences(MyConstants.SHARED_PREF_IAP, Context.MODE_PRIVATE).getBoolean(MyConstants.IS_PREMIUM, MyConstants.IAP_DEFAULT_STATUS)

    private fun loadNativeAd(holder: RecyclerView.ViewHolder) {
        val frameLayout = holder.itemView.findViewById<FrameLayout>(R.id.nv_frame)
        CoroutineScope(Dispatchers.IO).launch {
            AdMobUtil.showNativeAd(frameLayout) {
                nativeAds = it
                count = 50
                totalAdLoad++

                CoroutineScope(Dispatchers.Main).launch {
                    frameLayout.visibility = View.VISIBLE
                }
            }
        }
    }
}