package com.android.app.shoppy.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.android.app.shoppy.R
import com.android.app.shoppy.databinding.CustomerOrdersRowsLayoutBinding
import com.android.app.shoppy.databinding.CustomerSubOrdersRowsLayoutBinding
import com.android.app.shoppy.models.OrderDetailsModel
import com.android.app.shoppy.models.OrderModel
import java.util.*

class CustomSubOrderAdapter(
    private var ordersList: MutableList<OrderDetailsModel>
) : RecyclerView.Adapter<CustomSubOrderAdapter.OrderViewHolder>() {


    inner  class OrderViewHolder(var customerSubOrdersRowsLayoutBinding: CustomerSubOrdersRowsLayoutBinding)
        : RecyclerView.ViewHolder(customerSubOrdersRowsLayoutBinding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomSubOrderAdapter.OrderViewHolder {
        return OrderViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.customer_sub_orders_rows_layout, parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orders = ordersList[position]
        holder.customerSubOrdersRowsLayoutBinding.model = orders



    }

    override fun getItemCount(): Int {
        return ordersList.size
    }


}