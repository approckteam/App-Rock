package com.mankirat.approck.lib.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mankirat.approck.lib.R
import com.mankirat.approck.lib.databinding.AdapterPlansBinding
import com.mankirat.approck.lib.model.PurchaseModel

class PlansAdapter : RecyclerView.Adapter<PlansAdapter.ViewHolder>() {

    private var productList = ArrayList<PurchaseModel.PurchaseDetailModel>()

    private var currentPosition = -1
    private var alreadySelectedPosition = -1

    @SuppressLint("NotifyDataSetChanged")
    fun loadData(productList: ArrayList<PurchaseModel.PurchaseDetailModel>) {
        this.productList.clear()
        this.productList.addAll(productList)
        notifyDataSetChanged()
    }

    class ViewHolder(val binding: AdapterPlansBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdapterPlansBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val binding = holder.binding
        val item = productList[position]
        val duration: String
        val sDuration: String
        when {
            productList[position].subscriptionPeriod?.substring(2) == "M" -> {
                duration = "Monthly"
                sDuration = "Month"
            }
            productList[position].subscriptionPeriod?.substring(2) == "W" -> {
                duration = "Weekly"
                sDuration = "Week"
            }
            else -> {
                duration = "Yearly"
                sDuration = "Year"
            }
        }

        binding.txt3Price.text = "${item.price}/$sDuration"
        if (alreadySelectedPosition == -1) {
            if (item.freeTrialPeriod != "") {
                alreadySelectedPosition = holder.adapterPosition
                binding.cl3Month.setBackgroundResource(R.drawable.bg_selected_premium)
                binding.txt3Price.setTextColor(ContextCompat.getColor(binding.txt3Price.context, android.R.color.white))
                binding.tx3Month.setTextColor(ContextCompat.getColor(binding.txt3Price.context, android.R.color.white))
                clickListener?.invoke(position)
            } else {
                binding.txt3Price.setTextColor(ContextCompat.getColor(binding.txt3Price.context, R.color.grey_3))
                binding.cl3Month.setBackgroundResource(R.drawable.bg_unselected_premium)
                binding.tx3Month.setTextColor(ContextCompat.getColor(binding.txt3Price.context, android.R.color.black))
            }
        } else {
            if (currentPosition == position) {
                binding.txt3Price.setTextColor(ContextCompat.getColor(binding.txt3Price.context, android.R.color.white))
                binding.cl3Month.setBackgroundResource(R.drawable.bg_selected_premium)
                binding.tx3Month.setTextColor(ContextCompat.getColor(binding.txt3Price.context, android.R.color.white))
            } else {
                binding.cl3Month.setBackgroundResource(R.drawable.bg_unselected_premium)
                binding.txt3Price.setTextColor(ContextCompat.getColor(binding.txt3Price.context, R.color.grey_3))
                binding.tx3Month.setTextColor(ContextCompat.getColor(binding.txt3Price.context, android.R.color.black))
            }
        }

        binding.tx3Month.text = duration

        binding.cl3Month.setOnClickListener {
            alreadySelectedPosition = -2
            currentPosition = holder.adapterPosition
            notifyDataSetChanged()
            clickListener?.invoke(position)
        }
    }

    override fun getItemCount() = productList.size

    var clickListener: ((position: Int) -> Unit)? = null
}