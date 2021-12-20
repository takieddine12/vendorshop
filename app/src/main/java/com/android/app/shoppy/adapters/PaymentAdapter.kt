package com.android.app.shoppy.adapters

import com.android.app.shoppy.R
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.app.shoppy.databinding.PaymentRowsLayoutBinding
import com.android.app.shoppy.listeners.PaymentListener
import com.android.app.shoppy.models.PaymentModel
import com.android.app.shoppy.roomdb.RoomModel

class PaymentAdapter(
    var list : MutableList<RoomModel>,
    var paymentListener: PaymentListener
) : RecyclerView.Adapter<PaymentAdapter.PaymentViewHolder>() {


    inner class PaymentViewHolder(var paymentRowsLayoutBinding: PaymentRowsLayoutBinding)
        : RecyclerView.ViewHolder(paymentRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PaymentViewHolder {
       return  PaymentViewHolder(
               DataBindingUtil.inflate(
                       LayoutInflater.from(parent.context),
                       R.layout.payment_rows_layout,parent,false
               )
       )
    }

    override fun onBindViewHolder(holder: PaymentViewHolder, position: Int) {
        val data = list[position]
        holder.paymentRowsLayoutBinding.model = data
        holder.paymentRowsLayoutBinding.position = position
        holder.paymentRowsLayoutBinding.listener = paymentListener
    }

    override fun getItemCount(): Int {
       return list.size
    }

    fun deleteNewPosition(position : Int){
        list.removeAt(position)
        notifyItemRemoved(position)
        notifyDataSetChanged()
    }
}